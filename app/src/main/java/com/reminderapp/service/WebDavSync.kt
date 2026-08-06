package com.reminderapp.service

import android.content.Context
import com.reminderapp.ReminderApp
import com.reminderapp.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * WebDAV 同步：将全部提醒导出为 JSON 上传/下载，以 exportedAt 时间戳判断新旧
 *
 * 同步策略（简单可靠）：
 * - 本地数据版本 = 最后一次实际修改时间（SyncStore.lastLocalChange）
 * - 远程数据版本 = 远程 JSON 内的 exportedAt
 * - 远程新 → 下载并覆盖本地；本地新 → 上传覆盖远程；相同 → 跳过
 */
object WebDavSync {

    private const val REMOTE_FILE = "reminder_backup.json"
    private const val CONTENT_TYPE = "application/json; charset=utf-8"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    sealed class SyncResult {
        object Success : SyncResult()
        data class Error(val message: String) : SyncResult()
    }

    /**
     * 执行同步。result 返回上传/下载/无变更的描述
     */
    suspend fun syncNow(context: Context): SyncResult = withContext(Dispatchers.IO) {
        if (!SyncStore.isConfigured) {
            return@withContext SyncResult.Error("请先配置 WebDAV 服务器")
        }

        val dao = AppDatabase.getInstance(context).reminderDao()
        val localVersion = SyncStore.lastLocalChange

        try {
            val remoteJson = download(remoteUrl())

            if (remoteJson == null) {
                // 远程无文件 → 上传本地
                val uploadJson = buildUploadJson(dao.getAllSync(), localVersion)
                if (uploadJson != null) upload(uploadJson)
                SyncStore.lastSyncAt = System.currentTimeMillis()
                return@withContext SyncResult.Success
            }

            val remoteVersion = try {
                val raw = JSONObject(remoteJson).optLong("exportedAt", 0L)
                // 统一时间戳单位：iOS 导出的是「秒」，Android 内部用「毫秒」。
                // 秒级时间戳远小于 1e12（约 33658 年之前都成立），据此归一化为毫秒。
                if (raw > 0L && raw < 1_000_000_000_000L) raw * 1000 else raw
            } catch (e: Exception) {
                0L
            }

            return@withContext when {
                remoteVersion > localVersion -> {
                    // 远程新 → 覆盖本地
                    val items = BackupService.importFromJson(remoteJson)
                    if (items == null) {
                        SyncResult.Error("远程文件解析失败")
                    } else {
                        replaceLocal(dao, items)
                        SyncStore.lastLocalChange = remoteVersion
                        SyncStore.lastSyncAt = System.currentTimeMillis()
                        SyncResult.Success
                    }
                }
                localVersion > remoteVersion -> {
                    // 本地新 → 上传
                    val uploadJson = buildUploadJson(dao.getAllSync(), localVersion)
                    if (uploadJson != null) upload(uploadJson)
                    SyncStore.lastSyncAt = System.currentTimeMillis()
                    SyncResult.Success
                }
                else -> {
                    SyncStore.lastSyncAt = System.currentTimeMillis()
                    SyncResult.Success // 无变更
                }
            }
        } catch (e: Exception) {
            SyncResult.Error(friendlyMessage(e))
        }
    }

    /** 远程文件完整 URL（WebDAV 目录 + 文件名） */
    private fun remoteUrl(): String {
        var base = SyncStore.url.trim()
        if (base.endsWith("/")) base = base.dropLast(1)
        return "$base/$REMOTE_FILE"
    }

    /** 构建上传 JSON，exportedAt 保证比远程版本新 */
    private suspend fun buildUploadJson(
        reminders: List<com.reminderapp.data.entity.ReminderEntity>,
        localVersion: Long
    ): String? {
        val json = BackupService.exportToJson(reminders)
        return try {
            val obj = JSONObject(json)
            obj.put("exportedAt", maxOf(System.currentTimeMillis(), localVersion + 1))
            obj.toString(2)
        } catch (e: Exception) {
            null
        }
    }

    /** 用远程数据整体替换本地（先软删全部，再插入，最后重新调度全部提醒） */
    private suspend fun replaceLocal(
        dao: com.reminderapp.data.dao.ReminderDao,
        items: List<com.reminderapp.data.entity.ReminderEntity>
    ) {
        dao.getAllSync().forEach { dao.softDelete(it.id) }

        // 重新计算下次触发时间，避免导入的陈旧时间戳导致「立即触发」或「永不触发」，
        // 同时保留已完成状态。
        val toInsert = items.map { entity ->
            val next = ReminderEngine.calculateNextTrigger(entity)
            entity.copy(
                nextTriggerAt = next,
                status = if (entity.status == "confirmed") "confirmed" else "pending",
                retryCount = 0
            )
        }
        toInsert.forEach { dao.insert(it) }

        // 关键：下载覆盖本地后必须重新调度，否则所有提醒不再响，直到重启。
        ReminderScheduler(ReminderApp.instance).rescheduleAll(toInsert)
        com.reminderapp.receiver.ReminderWidgetProvider.refresh(ReminderApp.instance)
    }

    private fun download(url: String): String? {
        val request = Request.Builder()
            .url(url)
            .header("Authorization", Credentials.basic(SyncStore.username, SyncStore.password))
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (response.code == 404) return null
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}")
            }
            return response.body?.string()
        }
    }

    private fun upload(json: String) {
        val body = json.toRequestBody(CONTENT_TYPE.toMediaType())
        val request = Request.Builder()
            .url(remoteUrl())
            .header("Authorization", Credentials.basic(SyncStore.username, SyncStore.password))
            .put(body)
            .build()
        client.newCall(request).execute().use { response ->
            if (response.code == 404) {
                // 坚果云等服务器要求目录已存在：先 MKCOL 创建父目录再重试一次
                mkcolIfNeeded()
                client.newCall(request).execute().use { retry ->
                    if (!retry.isSuccessful) {
                        throw Exception("HTTP ${retry.code}")
                    }
                }
            } else if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}")
            }
        }
    }

    /** 确保 WebDAV 目录存在（MKCOL；已存在时返回 405/301 属正常，忽略） */
    private fun mkcolIfNeeded() {
        val base = SyncStore.url.trim().trimEnd('/')
        val request = Request.Builder()
            .url(base)
            .header("Authorization", Credentials.basic(SyncStore.username, SyncStore.password))
            .method("MKCOL", null)
            .build()
        try {
            client.newCall(request).execute().close()
        } catch (_: Exception) {
        }
    }

    /**
     * 测试连接：PROPFIND 验证连通性与认证（坚果云友好提示）。
     * 添加 WebDAV 时先测试，再同步。
     */
    suspend fun testConnection(): SyncResult = withContext(Dispatchers.IO) {
        val base = SyncStore.url.trim().trimEnd('/')
        if (base.isEmpty() || SyncStore.username.isEmpty() || SyncStore.password.isEmpty()) {
            return@withContext SyncResult.Error("请先填写 WebDAV 地址、用户名和应用密码")
        }
        try {
            val request = Request.Builder()
                .url(base)
                .header("Authorization", Credentials.basic(SyncStore.username, SyncStore.password))
                .header("Depth", "0")
                .method("PROPFIND", null)
                .build()
            client.newCall(request).execute().use { response ->
                val code = response.code
                // 207 Multi-Status 是 PROPFIND 的正常成功响应
                if (code in 200..299 || code == 207) {
                    SyncResult.Success
                } else {
                    SyncResult.Error(friendlyMessage(code))
                }
            }
        } catch (e: Exception) {
            SyncResult.Error(friendlyMessage(e))
        }
    }

    /** 把 HTTP 状态码/异常转成可操作的中文提示（尤其坚果云 401 应用密码） */
    private fun friendlyMessage(code: Int): String = when (code) {
        401 -> "认证失败（HTTP 401）：请确认用户名；密码必须是坚果云「应用密码」——在坚果云网页端「账户信息 → 安全选项 → 添加应用密码」生成，不能用登录密码。"
        403 -> "无权限（HTTP 403）：请检查该 WebDAV 路径是否可写（如 dav.jianguoyun.com/dav/ 根目录）。"
        404 -> "目录不存在（HTTP 404）：请确认 WebDAV 地址指向已存在的目录，坚果云请填 https://dav.jianguoyun.com/dav/（根目录），不要带不存在的子路径。"
        405 -> "服务器不支持该操作（HTTP 405）：请确认填的是 WebDAV 地址（如 …/dav/），不是网盘网页地址。"
        409 -> "资源冲突（HTTP 409）。"
        423 -> "资源被锁定（HTTP 423）。"
        507 -> "存储空间不足（HTTP 507）。"
        else -> "HTTP $code：请检查服务器地址/账号，或稍后重试。"
    }

    private fun friendlyMessage(e: Exception): String {
        val m = e.message ?: return "无法连接服务器：请检查网络和地址。"
        if (m.startsWith("HTTP")) {
            val code = m.removePrefix("HTTP").trim().toIntOrNull()
            if (code != null) return friendlyMessage(code)
        }
        val low = m.lowercase()
        return when {
            low.contains("timeout") -> "连接超时：请检查网络或服务器地址。"
            low.contains("failed to connect") || low.contains("network") ||
                low.contains("unable to resolve") -> "无法连接服务器：请检查网络和地址。"
            else -> m
        }
    }
}
