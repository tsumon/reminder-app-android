package com.reminderapp.service

import android.content.Context
import com.reminderapp.ReminderApp

/**
 * 同步设置存储（WebDAV）
 */
object SyncStore {
    private const val PREFS = "sync_settings"
    private const val KEY_URL = "webdav_url"
    private const val KEY_USER = "webdav_user"
    private const val KEY_PASS = "webdav_pass"
    private const val KEY_AUTO_SYNC = "auto_sync"
    private const val KEY_LAST_LOCAL_CHANGE = "last_local_change"
    private const val KEY_LAST_SYNC = "last_sync"
    // v2.0.17: 单调版本——墙钟可回拨/双端时钟有偏差，判新改用自增版本，时间戳仅兜底
    private const val KEY_LOCAL_VERSION = "local_version"
    private const val KEY_LAST_SYNC_VERSION = "last_sync_version"
    // v2.0.21 F1: 是否成功同步过至少一次（首次同步版本不可比，需回退时间戳判新）
    private const val KEY_HAS_SYNCED_ONCE = "has_synced_once"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var url: String
        get() = prefs(ReminderApp.instance).getString(KEY_URL, "") ?: ""
        set(value) = prefs(ReminderApp.instance).edit().putString(KEY_URL, value.trim()).apply()

    var username: String
        get() = prefs(ReminderApp.instance).getString(KEY_USER, "") ?: ""
        set(value) = prefs(ReminderApp.instance).edit().putString(KEY_USER, value.trim()).apply()

    var password: String
        get() {
            val raw = prefs(ReminderApp.instance).getString(KEY_PASS, "") ?: ""
            if (raw.isEmpty()) return ""
            // 兼容升级前的明文：首次读取时透明迁移为加密存储
            if (!raw.contains(":")) {
                prefs(ReminderApp.instance).edit().putString(KEY_PASS, CryptoHelper.encrypt(raw)).apply()
                return raw
            }
            val decrypted = CryptoHelper.decrypt(raw)
            // 明文密码本身含冒号（如 abc:def）会被误判为密文 → 解密失败时回退明文，
            // 否则 isConfigured 判 false，同步不可用
            return if (decrypted.isEmpty()) raw else decrypted
        }
        set(value) = prefs(ReminderApp.instance).edit().putString(KEY_PASS, CryptoHelper.encrypt(value)).apply()

    var autoSync: Boolean
        get() = prefs(ReminderApp.instance).getBoolean(KEY_AUTO_SYNC, false)
        set(value) = prefs(ReminderApp.instance).edit().putBoolean(KEY_AUTO_SYNC, value).apply()

    val isConfigured: Boolean
        get() = url.isNotBlank() && username.isNotBlank() && password.isNotBlank()

    /** 本地数据最后一次变更时间戳 */
    var lastLocalChange: Long
        get() = prefs(ReminderApp.instance).getLong(KEY_LAST_LOCAL_CHANGE, 0L)
        set(value) = prefs(ReminderApp.instance).edit().putLong(KEY_LAST_LOCAL_CHANGE, value).apply()

    /** 本地自增单调版本（v2.0.17：判新主依据，防墙钟回拨/时钟偏差；每次本地变更 +1） */
    var localVersion: Long
        get() = prefs(ReminderApp.instance).getLong(KEY_LOCAL_VERSION, 0L)
        set(value) = prefs(ReminderApp.instance).edit().putLong(KEY_LOCAL_VERSION, value).apply()

    /** 上次成功同步时的本地版本（冲突判定：双端自上次同步后都有变化 = 冲突；0 = 从未同步过） */
    var lastSyncVersion: Long
        get() = prefs(ReminderApp.instance).getLong(KEY_LAST_SYNC_VERSION, 0L)
        set(value) = prefs(ReminderApp.instance).edit().putLong(KEY_LAST_SYNC_VERSION, value).apply()

    /**
     * v2.0.21 F1: 是否成功同步过至少一次。
     *
     * 首次同步时远程 dataVersion 是「该账号历史累计值」（可能很大），而本地 localVersion 从 0 起，
     * 两者不同源不可比——直接版本比较会永远判「远程新」，把本机新建的提醒静默覆盖掉。
     * 因此首次同步回退时间戳判新。
     *
     * 兼容升级：老用户 lastSyncVersion 已 > 0，判定时会与本标记「或」处理，不受影响。
     */
    var hasSyncedOnce: Boolean
        get() = prefs(ReminderApp.instance).getBoolean(KEY_HAS_SYNCED_ONCE, false)
        set(value) = prefs(ReminderApp.instance).edit().putBoolean(KEY_HAS_SYNCED_ONCE, value).apply()

    /** 是否为「首次同步」（版本不可比场景）：从未成功同步过且无历史同步版本 */
    val isFirstSync: Boolean
        get() = !hasSyncedOnce && lastSyncVersion <= 0L

    /** 标记本地数据发生了变更（墙钟时间戳 + 单调版本同时推进） */
    fun touchLocalChange() {
        lastLocalChange = System.currentTimeMillis()
        localVersion = localVersion + 1
    }

    var lastSyncAt: Long
        get() = prefs(ReminderApp.instance).getLong(KEY_LAST_SYNC, 0L)
        set(value) = prefs(ReminderApp.instance).edit().putLong(KEY_LAST_SYNC, value).apply()
}
