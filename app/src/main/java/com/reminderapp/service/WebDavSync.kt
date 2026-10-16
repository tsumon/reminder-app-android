package com.reminderapp.service

import android.content.Context
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf

/**
 * WebDAV 同步：将全部提醒导出为 JSON 上传/下载，以 exportedAt 时间戳判断新旧
 *
 * 同步策略（简单可靠）：
 * - 本地数据版本 = 最后一次实际修改时间（SyncStore.lastLocalChange）
 * - 远程数据版本 = 远程 JSON 内的 exportedAt
 * - 远程新 → 下载并覆盖本地；本地新 → 上传覆盖远程；相同 → 跳过
 */
object WebDavSync {
    private val syncMutex=Mutex()
    val pendingConflict=kotlinx.coroutines.flow.MutableStateFlow<SyncResult.Conflict?>(null)


    private const val REMOTE_FILE = "reminder_backup.json"
    private const val CONTENT_TYPE = "application/json; charset=utf-8"

    /** 阶段2: 下载合并时随同步保留的状态（未知值回落 pending） */
    private val KEPT_STATUS = setOf("confirmed", "overdue", "snoozed", "notifying")

    /** 批次3 功能4: 日历订阅用的 .ics 文件名（与备份 JSON 同目录） */
    const val REMOTE_ICS_FILE = "reminders.ics"
    private const val ICS_CONTENT_TYPE = "text/calendar; charset=utf-8"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    sealed class SyncResult {
        /** conflict=true：检测到上次同步后双端都有修改，已按版本覆盖（UI 提示用户） */
        data class Success(val conflict: Boolean = false) : SyncResult()
        data class Conflict(val remoteJson: String, val localVersion: Long, val remoteEtag: String?) : SyncResult()
        data class Error(val message: String) : SyncResult()
    }

    /**
     * 执行同步。result 返回上传/下载/无变更的描述
     */
    suspend fun syncNow(context: Context, resolution: Resolution? = null): SyncResult = syncMutex.withLock {
        withContext(Dispatchers.IO) { syncLocked(context,resolution) }
    }
    private suspend fun syncLocked(context: Context,resolution: Resolution?): SyncResult {
        if(!SyncStore.isConfigured) return SyncResult.Error(zh("请先配置 WebDAV 服务器"))
        QuietHoursStore.migrateLegacy(context)
        val coordinator=ReminderSyncCoordinator(context)
        val captured=coordinator.capture()
        val localVer=captured.generation
        val localChange=captured.lastChange
        val localItems=captured.items
        try {
            val remote=downloadSnapshot(remoteUrl())
            val remoteJson=remote?.json
            suspend fun uploadCaptured(): SyncResult {
                val json=coordinator.uploadJson(captured)
                upload(json,remote?.etag)
                coordinator.acknowledgeUpload(captured,JSONObject(json).getLong("exportedAt"))
                pendingConflict.value=null
                return SyncResult.Success()
            }
            if(remoteJson==null) {
                if(resolution!=null) return SyncResult.Error(zh("数据已有新变更，请重新同步后选择"))
                return uploadCaptured()
            }
            val raw=JSONObject(remoteJson).optLong("exportedAt",0)
            if(raw<=0) return SyncResult.Error(zh("远程文件解析失败：缺少 exportedAt（文件可能已损坏），未覆盖本地数据"))
            val remoteTimestamp=if(raw<1_000_000_000_000L) raw*1000 else raw
            val remoteGeneration=BackupService.dataVersionOf(remoteJson)
            val conflict=ReminderSyncCoordinator.conflicts(captured,remoteGeneration,remoteTimestamp,BackupService.remindersCountOf(remoteJson)>0)
            if(resolution!=null && (resolution.conflict.localVersion!=localVer || resolution.conflict.remoteJson!=remoteJson))
                return SyncResult.Error(zh("数据已有新变更，请重新同步后选择"))
            if(SyncStore.localVersion!=localVer) return SyncResult.Error(zh("本地数据已变更，请重试"))
            if(conflict && resolution==null) return SyncResult.Conflict(remoteJson,localVer,remote?.etag).also { pendingConflict.value=it }
            if(resolution!=null) {
                snapshotLocal(localItems,localVer)
                if(resolution.useLocal) return uploadCaptured()
            }
            if(resolution!=null || remoteNewer(remoteGeneration,remoteTimestamp,localVer,localChange,captured.firstSync)) {
                val items=BackupService.importFromJson(remoteJson) ?: return SyncResult.Error(zh("远程文件解析失败"))
                if(resolution==null) snapshotLocal(localItems,localVer)
                coordinator.merge(captured,items,remoteGeneration,remoteTimestamp)
                // Generation acknowledgement is already committed. A later completion stays dirty during restoration.
                coordinator.restoreSchedules()
                pendingConflict.value=null
                return SyncResult.Success()
            }
            if(localNewer(remoteGeneration,remoteTimestamp,localVer,localChange,captured.firstSync) || localVer>captured.lastSyncGeneration) return uploadCaptured()
            coordinator.acknowledgeUpload(captured,localChange)
            return SyncResult.Success()
        } catch(e: Exception) { return SyncResult.Error(friendlyMessage(e)) }
    }

    // MARK: - v2.0.17 单调版本判新（v2.0.21 修正 F1/F2）
    // 双端都有 dataVersion 时用单调版本比较（防墙钟回拨/时钟偏差）；
    // 任一为 0（旧版远程文件 / 升级前本地从未计数）回退 exportedAt 时间戳比较。
    //
    // F1：首次同步（firstSync）时两边版本不同源——远程是该账号历史累计值、本地从 0 起，
    //     版本比较必然判「远程新」→ 本机新建数据被静默覆盖。此时一律回退时间戳。
    // F2：版本相等（双端自同一基线各改相同次数）时，若仍按 rdv > lv / lv > rdv 判定，
    //     两者皆 false → 落入「无变更」分支 → 双方改动都不同步且不提示，数据永久分叉。
    //     故版本相等时用时间戳决胜。

    /** 远程是否更新 */
    private fun remoteNewer(rdv: Long, rts: Long, lv: Long, lts: Long, firstSync: Boolean): Boolean {
        if (firstSync) return rts > lts
        if (rdv > 0L && lv > 0L) {
            if (rdv == lv) return rts > lts
            return rdv > lv
        }
        return rts > lts
    }

    /** 本地是否更新 */
    private fun localNewer(rdv: Long, rts: Long, lv: Long, lts: Long, firstSync: Boolean): Boolean {
        if (firstSync) return lts > rts
        if (rdv > 0L && lv > 0L) {
            if (rdv == lv) return lts > rts
            return lv > rdv
        }
        return lts > rts
    }

    /** 远程文件完整 URL（WebDAV 目录 + 文件名） */
    private fun remoteUrl(): String = remoteUrl(REMOTE_FILE)

    private fun remoteUrl(fileName: String): String {
        var base = SyncStore.url.trim()
        if (base.endsWith("/")) base = base.dropLast(1)
        return "$base/$fileName"
    }

    // MARK: - 批次3 功能4: 日历订阅链接

    /**
     * 把当前全部提醒导出为 .ics 并上传到 WebDAV 目录，返回该文件的 WebDAV URL。
     *
     * 这个 URL 本身**需要账号密码**，不能直接丢给系统日历订阅——坚果云等网盘的正确姿势是：
     * 上传后在网页端对该文件「创建分享链接」，再把分享直链填进日历订阅。
     * 因此 UI 侧会把这个 URL 连同操作指引一起给用户（见 SettingsScreen 的订阅入口）。
     *
     * 每次调用都覆盖同名文件，订阅端下次刷新即可拿到最新日程。
     */
    suspend fun uploadIcs(reminders: List<com.reminderapp.data.entity.ReminderEntity>): IcsUploadResult =
        withContext(Dispatchers.IO) {
            if (!SyncStore.isConfigured) {
                return@withContext IcsUploadResult.Error(zh("请先配置 WebDAV 服务器"))
            }
            val active = reminders.filter { it.isActive }
            if (active.isEmpty()) {
                return@withContext IcsUploadResult.Error(zh("当前没有可导出的提醒"))
            }
            try {
                val ics = IcsExporter.generateIcs(active)
                uploadRaw(ics, REMOTE_ICS_FILE, ICS_CONTENT_TYPE)
                IcsUploadResult.Success(remoteUrl(REMOTE_ICS_FILE), active.size)
            } catch (e: Exception) {
                IcsUploadResult.Error(friendlyMessage(e))
            }
        }

    sealed class IcsUploadResult {
        data class Success(val url: String, val count: Int) : IcsUploadResult()
        data class Error(val message: String) : IcsUploadResult()
    }

    /** 通用 PUT：404 时先 MKCOL 建目录再重试一次（与 upload 同策略） */
    private fun uploadRaw(content: String, fileName: String, contentType: String) {
        val body = content.toRequestBody(contentType.toMediaType())
        val request = Request.Builder()
            .url(remoteUrl(fileName))
            .header("Authorization", Credentials.basic(SyncStore.username, SyncStore.password))
            .put(body)
            .build()
        client.newCall(request).execute().use { response ->
            if (response.code == 404) {
                mkcolIfNeeded()
                client.newCall(request).execute().use { retry ->
                    if (!retry.isSuccessful) throw Exception("HTTP ${retry.code}")
                }
            } else if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}")
            }
        }
    }

    /** 阶段2: 合并前把当前本地全量导出为 JSON 快照（可恢复；保留最近 5 份） */
    private fun snapshotLocal(localItems: List<com.reminderapp.data.entity.ReminderEntity>, generation: Long) {
        val dir=java.io.File(ReminderApp.instance.filesDir,"webdav_snapshots").apply { mkdirs() }
        val file=java.io.File(dir,"before_${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}.json")
        file.writeText(BackupService.exportToJson(localItems,generation))
        check(file.length()>0) { "本地快照写入失败，同步已停止" }
        dir.listFiles()?.sortedByDescending { it.lastModified() }?.drop(5)?.forEach { it.delete() }
    }

    data class Resolution(val conflict: SyncResult.Conflict, val useLocal: Boolean)
    private data class RemoteSnapshot(val json: String,val etag: String?)
    private fun downloadSnapshot(url: String): RemoteSnapshot? {
        val request=Request.Builder().url(url).header("Authorization",Credentials.basic(SyncStore.username,SyncStore.password)).get().build()
        client.newCall(request).execute().use { response ->
            if(response.code==404) return null
            check(response.isSuccessful) { "HTTP ${response.code}" }
            return RemoteSnapshot(response.body?.string() ?: error("远程文件为空"),response.header("ETag"))
        }
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

    private fun upload(json: String, expectedEtag: String? = null) {
        val body = json.toRequestBody(CONTENT_TYPE.toMediaType())
        val request = Request.Builder()
            .url(remoteUrl())
            .header("Authorization", Credentials.basic(SyncStore.username, SyncStore.password))
            .apply { if(expectedEtag != null) header("If-Match",expectedEtag) else header("If-None-Match","*") }
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
     * 测试连接：完整读写测试。
     * 仅 PROPFIND 通过不算成功——很多账号只读不开写（如坚果云第三方登录受限），
     * 必须实际能写才算配置成功。
     */
    suspend fun testConnection(): SyncResult = withContext(Dispatchers.IO) {
        val base = SyncStore.url.trim().trimEnd('/')
        if (base.isEmpty() || SyncStore.username.isEmpty() || SyncStore.password.isEmpty()) {
            return@withContext SyncResult.Error(zh("请先填写 WebDAV 地址、用户名和应用密码"))
        }

        // 1. 验证读权限
        val readCode = runCatching {
            client.newCall(Request.Builder()
                .url(base)
                .header("Authorization", Credentials.basic(SyncStore.username, SyncStore.password))
                .header("Depth", "0")
                .method("PROPFIND", null)
                .build()
            ).execute().use { it.code }
        }.getOrElse { return@withContext SyncResult.Error(friendlyMessage(it)) }
        if (readCode !in 200..299 && readCode != 207) {
            return@withContext SyncResult.Error(friendlyMessage(readCode))
        }

        // 2. 验证写权限：PUT 一个随机文件
        val testName = ".reminder_test_${java.util.UUID.randomUUID().toString().take(8)}"
        val testUrl = "$base/$testName"
        val writeCode = runCatching {
            client.newCall(Request.Builder()
                .url(testUrl)
                .header("Authorization", Credentials.basic(SyncStore.username, SyncStore.password))
                .put("ok".toRequestBody("application/octet-stream".toMediaType()))
                .build()
            ).execute().use { it.code }
        }.getOrElse { return@withContext SyncResult.Error(friendlyMessage(it)) }
        if (writeCode !in 200..299) {
            return@withContext SyncResult.Error(writeFailureHint(writeCode))
        }

        // 3. 清理测试文件（失败不影响结论）
        runCatching {
            client.newCall(Request.Builder()
                .url(testUrl)
                .header("Authorization", Credentials.basic(SyncStore.username, SyncStore.password))
                .delete()
                .build()
            ).execute().close()
        }
        SyncResult.Success(conflict = false)
    }

    /** 「可读但不可写」的精准提示（testConnection 第二步失败时使用） */
    private fun writeFailureHint(code: Int): String = when (code) {
        401 -> zh("账号或密码错误（HTTP 401）：坚果云请用「应用密码」——网页端 → 账户信息 → 安全选项 → 添加应用密码，不能用登录密码。")
        403 -> zh("账号只读不可写（HTTP 403）：可能原因：① 第三方登录注册的坚果云账号（如 Google/微信登录）不支持 WebDAV 写入，请用坚果云独立注册的账号；② 账号未在网页端启用 WebDAV（账户信息 → 安全选项）。")
        404 -> zh("账号只读不可写（HTTP 404）：可能原因：① 第三方登录注册的坚果云账号不支持 WebDAV 写入，请用坚果云独立注册的账号；② 应用密码生成后未刷新权限，删除旧密码重新生成一次。")
        405 -> zh("服务器不允许写入（HTTP 405）：地址可能不是 WebDAV 路径，请确认填 https://dav.jianguoyun.com/dav/（坚果云以 /dav/ 结尾）。")
        else -> zhf("可读但不可写（HTTP %s）：账号可能被禁用 WebDAV，或地址权限不足，请联系服务器管理员。", code)
    }

    /** 把 HTTP 状态码/异常转成可操作的中文提示（尤其坚果云 401 应用密码） */
    private fun friendlyMessage(code: Int): String = when (code) {
        401 -> zh("认证失败（HTTP 401）：请确认用户名；密码必须是坚果云「应用密码」——在坚果云网页端「账户信息 → 安全选项 → 添加应用密码」生成，不能用登录密码。")
        403 -> zh("无权限（HTTP 403）：请检查该 WebDAV 路径是否可写（如 dav.jianguoyun.com/dav/ 根目录）。")
        404 -> zh("目录不存在（HTTP 404）：请确认 WebDAV 地址指向已存在的目录，坚果云请填 https://dav.jianguoyun.com/dav/（根目录），不要带不存在的子路径。")
        405 -> zh("服务器不支持该操作（HTTP 405）：请确认填的是 WebDAV 地址（如 …/dav/），不是网盘网页地址。")
        409 -> zh("资源冲突（HTTP 409）。")
        412 -> zh("远程版本已变更，或服务器不支持安全版本更新。同步已停止，请重新下载检查；未覆盖远程文件。")
        423 -> zh("资源被锁定（HTTP 423）。")
        507 -> zh("存储空间不足（HTTP 507）。")
        else -> zhf("HTTP %s：请检查服务器地址/账号，或稍后重试。", code)
    }

    private fun friendlyMessage(e: Throwable): String {
        if (e is Exception) return friendlyMessage(e as Exception)
        return zhf("未知错误：%s", e.message ?: e.javaClass.simpleName)
    }

    private fun friendlyMessage(e: Exception): String {
        val m = e.message ?: return zh("无法连接服务器：请检查网络和地址。")
        if (m.startsWith("HTTP")) {
            val code = m.removePrefix("HTTP").trim().toIntOrNull()
            if (code != null) return friendlyMessage(code)
        }
        val low = m.lowercase()
        return when {
            low.contains("timeout") -> zh("连接超时：请检查网络或服务器地址。")
            low.contains("failed to connect") || low.contains("network") ||
                low.contains("unable to resolve") -> zh("无法连接服务器：请检查网络和地址。")
            else -> m
        }
    }
}
