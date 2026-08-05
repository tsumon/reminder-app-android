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

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var url: String
        get() = prefs(ReminderApp.instance).getString(KEY_URL, "") ?: ""
        set(value) = prefs(ReminderApp.instance).edit().putString(KEY_URL, value.trim()).apply()

    var username: String
        get() = prefs(ReminderApp.instance).getString(KEY_USER, "") ?: ""
        set(value) = prefs(ReminderApp.instance).edit().putString(KEY_USER, value.trim()).apply()

    var password: String
        get() = prefs(ReminderApp.instance).getString(KEY_PASS, "") ?: ""
        set(value) = prefs(ReminderApp.instance).edit().putString(KEY_PASS, value).apply()

    var autoSync: Boolean
        get() = prefs(ReminderApp.instance).getBoolean(KEY_AUTO_SYNC, false)
        set(value) = prefs(ReminderApp.instance).edit().putBoolean(KEY_AUTO_SYNC, value).apply()

    val isConfigured: Boolean
        get() = url.isNotBlank() && username.isNotBlank() && password.isNotBlank()

    /** 本地数据最后一次变更时间戳 */
    var lastLocalChange: Long
        get() = prefs(ReminderApp.instance).getLong(KEY_LAST_LOCAL_CHANGE, 0L)
        set(value) = prefs(ReminderApp.instance).edit().putLong(KEY_LAST_LOCAL_CHANGE, value).apply()

    /** 标记本地数据发生了变更 */
    fun touchLocalChange() {
        lastLocalChange = System.currentTimeMillis()
    }

    var lastSyncAt: Long
        get() = prefs(ReminderApp.instance).getLong(KEY_LAST_SYNC, 0L)
        set(value) = prefs(ReminderApp.instance).edit().putLong(KEY_LAST_SYNC, value).apply()
}
