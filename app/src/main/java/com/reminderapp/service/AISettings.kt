package com.reminderapp.service

import android.content.Context
import android.content.SharedPreferences

/**
 * 用户自定义 AI API 配置（SharedPreferences 持久化）
 * 仅 API 模式（免 API 网页跳转模式已移除）
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

    val isConfigured: Boolean
        get() = apiEndpoint.isNotBlank() && apiKey.isNotBlank()
}
