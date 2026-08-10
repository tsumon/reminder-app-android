package com.reminderapp.service

import android.content.Context
import android.content.SharedPreferences

/**
 * 用户自定义 AI API 配置。
 * apiKey 以 AES/GCM（AndroidKeyStore，见 CryptoHelper）静态加密后存入 SharedPreferences，
 * 其余字段（endpoint/model）明文存 SharedPreferences。仅 API 模式。
 */
class AISettings(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ai_settings", Context.MODE_PRIVATE)

    var apiEndpoint: String
        get() = prefs.getString("endpoint", "https://api.openai.com/v1") ?: "https://api.openai.com/v1"
        set(value) = prefs.edit().putString("endpoint", value).apply()

    var apiKey: String
        get() {
            val raw = prefs.getString("api_key", "") ?: ""
            if (raw.isEmpty()) return ""
            val decrypted = CryptoHelper.decrypt(raw)
            if (decrypted.isNotEmpty()) return decrypted
            // 旧版本明文兼容：首次读取时自动加密并回写，避免明文落盘
            prefs.edit().putString("api_key", CryptoHelper.encrypt(raw)).apply()
            return raw
        }
        set(value) {
            val encrypted = if (value.isEmpty()) "" else CryptoHelper.encrypt(value)
            prefs.edit().putString("api_key", encrypted).apply()
        }

    var model: String
        get() = prefs.getString("model", "gpt-4o-mini") ?: "gpt-4o-mini"
        set(value) = prefs.edit().putString("model", value).apply()

    val isConfigured: Boolean
        get() = apiEndpoint.isNotBlank() && apiKey.isNotBlank()
}
