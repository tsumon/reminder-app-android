package com.reminderapp.service

import android.content.Context
import android.content.SharedPreferences

/**
 * 用户自定义 AI API 配置（SharedPreferences 持久化）
 * 支持免 API 模式（无需 Key，跳转网页版）
 */
class AISettings(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ai_settings", Context.MODE_PRIVATE)

    var apiEndpoint: String
        get() = prefs.getString("endpoint", "https://api.openai.com/v1") ?: "https://api.openai.com/v1"
        set(value) = prefs.edit().putString("endpoint", value).apply()

    var apiKey: String
        get() = prefs.getString("api_key", "") ?: ""
        set(value) = prefs.edit().putString("api_key", value).apply()

    var model: String
        get() = prefs.getString("model", "gpt-4o-mini") ?: "gpt-4o-mini"
        set(value) = prefs.edit().putString("model", value).apply()

    /// 免 API 模式：不填 Key，直接跳转外部 App 网页版
    var useNoAPIMode: Boolean
        get() = prefs.getBoolean("noapi_mode", false)
        set(value) = prefs.edit().putBoolean("noapi_mode", value).apply()

    /// 免 API 模式下默认跳转的服务商
    var noAPIProvider: String
        get() = prefs.getString("noapi_provider", "deepseek") ?: "deepseek"
        set(value) = prefs.edit().putString("noapi_provider", value).apply()

    val isConfigured: Boolean
        get() = if (useNoAPIMode) true
                else apiEndpoint.isNotBlank() && apiKey.isNotBlank()
}
