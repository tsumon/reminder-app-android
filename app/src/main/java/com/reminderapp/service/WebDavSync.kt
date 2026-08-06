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
            SyncResult.Error(e.message ?: "同步失败")
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
                throw Exception("下载失败 HTTP ${response.code}")
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
            if (!response.isSuccessful) {
                throw Exception("上传失败 HTTP ${response.code}")
            }
        }
    }
}
