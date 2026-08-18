package com.reminderapp.service

import android.content.Context
import androidx.room.withTransaction
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
        data class Error(val message: String) : SyncResult()
    }

    /**
     * 执行同步。result 返回上传/下载/无变更的描述
     */
    suspend fun syncNow(context: Context): SyncResult = withContext(Dispatchers.IO) {
        if (!SyncStore.isConfigured) {
            return@withContext SyncResult.Error(zh("请先配置 WebDAV 服务器"))
        }

        val db = AppDatabase.getInstance(context)
        val dao = db.reminderDao()
        // v2.0.17: 单调版本（localVer）为判新主依据；墙钟（localChange）仅兜底（旧文件/升级前本地无版本）
        val localChange = SyncStore.lastLocalChange
        val localVer = SyncStore.localVersion
        // v2.0.21 F1: 首次同步 + 上次同步版本必须在分支改写前快照，否则冲突判定会拿到刚写入的新值恒 false
        val firstSync = SyncStore.isFirstSync
        val lastSyncVer = SyncStore.lastSyncVersion
        val localItems = dao.getAllSync()

        try {
            val remoteJson = download(remoteUrl())

            if (remoteJson == null) {
                // 远程无文件 → 上传本地
                // I11: 构建上传 JSON 失败（如序列化异常）必须上报错误，不能静默当 Success 让用户以为已备份
                val uploadJson = buildUploadJson(localItems, localChange)
                    ?: return@withContext SyncResult.Error(zh("备份上传失败：本地数据序列化出错，未上传到云端"))
                upload(uploadJson)
                // v2.0.17: 记录已同步版本，防下轮误判（上传 dataVersion = 当前本地版本）
                SyncStore.lastSyncVersion = SyncStore.localVersion
                SyncStore.lastSyncAt = System.currentTimeMillis()
                SyncStore.hasSyncedOnce = true
                return@withContext SyncResult.Success(conflict = false)
            }

            val remoteVersion = try {
                val obj = JSONObject(remoteJson)
                val raw = obj.optLong("exportedAt", 0L)
                if (raw <= 0L) {
                    // E4: 合法 JSON 但缺 exportedAt → 视为损坏，不能当「版本 0」走本地覆盖分支
                    return@withContext SyncResult.Error(zh("远程文件解析失败：缺少 exportedAt（文件可能已损坏），未覆盖本地数据"))
                }
                // 统一时间戳单位：iOS 导出的是「秒」，Android 内部用「毫秒」。
                // 秒级时间戳远小于 1e12（约 33658 年之前都成立），据此归一化为毫秒。
                if (raw < 1_000_000_000_000L) raw * 1000 else raw
            } catch (e: Exception) {
                // E4: 坏 JSON 不能静默当 0——否则本地会覆盖远程唯一备份（丢数据链）
                return@withContext SyncResult.Error(zh("远程文件解析失败：不是有效的 JSON（文件可能已损坏），未覆盖本地数据"))
            }

            // v2.0.17: 远程单调版本（旧文件无 dataVersion → 0，判新回退时间戳）
            val remoteDataVersion = BackupService.dataVersionOf(remoteJson)
            // v2.0.21 F1: 首次同步的冲突判定依据「两边都有数据」（版本不可比，无法用版本差判断）
            val remoteHasData = BackupService.remindersCountOf(remoteJson) > 0
            val localHasData = localItems.isNotEmpty()

            return@withContext when {
                // v2.0.17 判新：双方都有单调版本 → 版本比较；任一为 0（旧文件/升级前）→ 时间戳兜底
                remoteNewer(remoteDataVersion, remoteVersion, localVer, localChange, firstSync) -> {
                    // 远程新 → 覆盖本地
                    val items = BackupService.importFromJson(remoteJson)
                    if (items == null) {
                        SyncResult.Error(zh("远程文件解析失败"))
                    } else {
                        mergeRemote(db, items)
                        SyncStore.lastLocalChange = remoteVersion
                        // v2.0.17: 下载后本地数据 = 远程数据 → 单调版本对齐远程
                        // （旧文件 dataVersion=0 时保持本地版本，兼容路径下次仍会上传）
                        if (remoteDataVersion > 0L) {
                            SyncStore.localVersion = remoteDataVersion
                        }
                        // v2.0.16/17 冲突提示：上次同步后本地也改过（版本化判定；旧文件回退不提示）
                        val conflict = isConflict(
                            firstSync, localHasData, remoteHasData,
                            localVer, remoteDataVersion, lastSyncVer
                        )
                        if (remoteDataVersion > 0L) SyncStore.lastSyncVersion = remoteDataVersion
                        SyncStore.lastSyncAt = System.currentTimeMillis()
                        SyncStore.hasSyncedOnce = true
                        SyncResult.Success(conflict = conflict)
                    }
                }
                localNewer(remoteDataVersion, remoteVersion, localVer, localChange, firstSync) -> {
                    // 本地新 → 上传
                    // I11: 构建上传 JSON 失败必须上报错误，同「远程无文件」分支
                    val uploadJson = buildUploadJson(localItems, localChange)
                        ?: return@withContext SyncResult.Error(zh("备份上传失败：本地数据序列化出错，未上传到云端"))
                    upload(uploadJson)
                    // E1: 上传后必须推进 lastLocalChange（对齐 iOS setLastLocalChange(max(...))）——
                    // 否则无编辑时下轮误判「远程新」→ 全量 replaceLocal 重建
                    val uploadedAt = runCatching { JSONObject(uploadJson).optLong("exportedAt", 0L) }.getOrDefault(0L)
                    if (uploadedAt > SyncStore.lastLocalChange) SyncStore.lastLocalChange = uploadedAt
                    // v2.0.16/17 冲突提示：上次同步后远程也改过（版本化判定）
                    val conflict = isConflict(
                        firstSync, localHasData, remoteHasData,
                        localVer, remoteDataVersion, lastSyncVer
                    )
                    SyncStore.lastSyncVersion = localVer
                    SyncStore.lastSyncAt = System.currentTimeMillis()
                    SyncStore.hasSyncedOnce = true
                    SyncResult.Success(conflict = conflict)
                }
                else -> {
                    // 版本与时间戳都相等 → 两边确为同一份数据，无变更
                    // （F2：版本相等但时间戳不同的情况已在 remoteNewer/localNewer 里用时间戳决胜，不会落到这里）
                    SyncStore.lastSyncAt = System.currentTimeMillis()
                    SyncStore.hasSyncedOnce = true
                    SyncResult.Success(conflict = false)
                }
            }
        } catch (e: Exception) {
            SyncResult.Error(friendlyMessage(e))
        }
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

    /**
     * 冲突判定（是否需要提示「已按版本覆盖」）。
     *
     * - 首次同步：无 lastSyncVersion 基线可比，只要两边都有数据就意味着一方内容会被整体覆盖 → 提示
     * - 后续同步：自上次同步后双端都推进过版本 → 提示
     */
    private fun isConflict(
        firstSync: Boolean,
        localHasData: Boolean,
        remoteHasData: Boolean,
        localVer: Long,
        remoteDataVersion: Long,
        lastSyncVer: Long
    ): Boolean = if (firstSync) {
        localHasData && remoteHasData
    } else {
        lastSyncVer > 0L && localVer > lastSyncVer && remoteDataVersion > lastSyncVer
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

    /** 用远程数据合并本地（阶段2：按 syncId upsert，不再整库 delete/reinsert） */
    private suspend fun mergeRemote(
        db: AppDatabase,
        items: List<com.reminderapp.data.entity.ReminderEntity>
    ) {
        val dao = db.reminderDao()
        // 阶段2: 覆盖前自动存本地快照（可恢复；保留最近 5 份）
        snapshotLocal(dao.getAllSync())

        db.withTransaction {
            val localItems = dao.getAllSync()
            val localBySync = localItems.mapNotNull { it.syncId?.let { s -> s to it } }.toMap()
            val localByFp = localItems.associateBy { BackupService.fingerprint(it) }
            val upserted = mutableListOf<com.reminderapp.data.entity.ReminderEntity>()

            items.forEach { remote ->
                // 匹配优先级：syncId（协议 v2）→ 指纹（旧协议文件无 syncId 时，识别上轮已导入的条目）
                val match = remote.syncId?.let { localBySync[it] }
                    ?: localByFp[BackupService.fingerprint(remote)]

                if (match != null) {
                    // 已存在：保留本地自增 id（通知/小组件/操作记录引用不失效）和本地 syncId
                    val updated = remote.copy(
                        id = match.id,
                        syncId = match.syncId ?: remote.syncId,
                        nextTriggerAt = ReminderEngine.calculateNextTrigger(remote, com.reminderapp.ReminderApp.instance),
                        // 阶段2: 状态随同步保留（iOS 同样保留 overdue/snoozed/notifying；未知值回落 pending）
                        status = if (remote.status in KEPT_STATUS) remote.status else "pending",
                        retryCount = 0
                    )
                    dao.update(updated)
                    upserted.add(updated)
                } else {
                    // 新条目：id=0 自增，syncId 缺失补 UUID
                    val fresh = BackupService.ensureSyncId(remote).copy(
                        id = 0,
                        nextTriggerAt = ReminderEngine.calculateNextTrigger(remote, com.reminderapp.ReminderApp.instance),
                        status = if (remote.status in KEPT_STATUS) remote.status else "pending",
                        retryCount = 0
                    )
                    val newId = dao.insert(fresh)
                    upserted.add(fresh.copy(id = newId))
                }
            }

            // 本地有而远程没有的条目保留（无 tombstone，无法区分「远端已删」与「远端从未有过」，保守不删）
            // 关键：合并后必须重新调度，否则提醒不再响，直到重启。
            ReminderScheduler(ReminderApp.instance).rescheduleAll(upserted)
        }
        com.reminderapp.receiver.ReminderWidgetProvider.refresh(ReminderApp.instance)
    }

    /** 阶段2: 合并前把当前本地全量导出为 JSON 快照（可恢复；保留最近 5 份） */
    private fun snapshotLocal(localItems: List<com.reminderapp.data.entity.ReminderEntity>) {
        try {
            val dir = java.io.File(ReminderApp.instance.filesDir, "webdav_snapshots").apply { mkdirs() }
            val stamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
            java.io.File(dir, "before_$stamp.json").writeText(BackupService.exportToJson(localItems))
            // 只保留最近 5 份
            dir.listFiles()?.sortedByDescending { it.name }?.drop(5)?.forEach { it.delete() }
        } catch (e: Exception) {
            android.util.Log.w("WebDavSync", "本地快照写入失败（不影响同步）: ${e.message}")
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
