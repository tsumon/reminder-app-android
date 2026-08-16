package com.reminderapp.service

import android.content.Context
import android.content.SharedPreferences

/**
 * 用户自定义 AI API 配置（v2.2.0：多 provider + 备用降级 + 本地模型）。
 * apiKey 以 AES/GCM（AndroidKeyStore，见 CryptoHelper）静态加密后存入 SharedPreferences，
 * 其余字段明文存 SharedPreferences。
 *
 * - 主配置：endpoint/apiKey/model（原三件套）
 * - 备用配置（可选）：fallbackEndpoint/fallbackKey/fallbackModel —— 主配置失败时自动切换
 * - 本地模型：isLocal=true 时 apiKey 可留空（如 Ollama http://localhost:11434/v1）
 */
class AISettings(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ai_settings", Context.MODE_PRIVATE)

    // ── 主配置 ──

    var apiEndpoint: String
        get() = prefs.getString("endpoint", "https://api.openai.com/v1") ?: "https://api.openai.com/v1"
        set(value) = prefs.edit().putString("endpoint", value).apply()

    var apiKey: String
        get() {
            val raw = prefs.getString("api_key", "") ?: ""
            if (raw.isEmpty()) return ""
            // v2.4.9: 不像密文格式（不含冒号分隔符）→ 视为旧版明文，自动加密迁移
            if (!raw.contains(":")) {
                val encrypted = CryptoHelper.encrypt(raw)
                prefs.edit().putString("api_key", encrypted).apply()
                return raw
            }
            val decrypted = CryptoHelper.decrypt(raw)
            if (decrypted.isNotEmpty()) return decrypted
            // 密文格式但解密失败（KeyStore 密钥轮换后旧密文不可恢复）→ 不回写，返回空串让用户重填
            return ""
        }
        set(value) {
            val encrypted = if (value.isEmpty()) "" else CryptoHelper.encrypt(value)
            prefs.edit().putString("api_key", encrypted).apply()
        }

    var model: String
        get() = prefs.getString("model", "gpt-4o-mini") ?: "gpt-4o-mini"
        set(value) = prefs.edit().putString("model", value).apply()

    // ── 本地模型（v2.2.0：如 Ollama，apiKey 可空）──

    var isLocal: Boolean
        get() = prefs.getBoolean("is_local", false)
        set(value) = prefs.edit().putBoolean("is_local", value).apply()

    // ── 备用配置（v2.2.0：主配置失败自动降级）──

    var fallbackEnabled: Boolean
        get() = prefs.getBoolean("fallback_enabled", false)
        set(value) = prefs.edit().putBoolean("fallback_enabled", value).apply()

    var fallbackEndpoint: String
        get() = prefs.getString("fallback_endpoint", "https://api.deepseek.com/v1") ?: "https://api.deepseek.com/v1"
        set(value) = prefs.edit().putString("fallback_endpoint", value).apply()

    var fallbackApiKey: String
        get() {
            val raw = prefs.getString("fallback_api_key", "") ?: ""
            if (raw.isEmpty()) return ""
            if (!raw.contains(":")) {
                val encrypted = CryptoHelper.encrypt(raw)
                prefs.edit().putString("fallback_api_key", encrypted).apply()
                return raw
            }
            val decrypted = CryptoHelper.decrypt(raw)
            if (decrypted.isNotEmpty()) return decrypted
            return ""
        }
        set(value) {
            val encrypted = if (value.isEmpty()) "" else CryptoHelper.encrypt(value)
            prefs.edit().putString("fallback_api_key", encrypted).apply()
        }

    var fallbackModel: String
        get() = prefs.getString("fallback_model", "deepseek-chat") ?: "deepseek-chat"
        set(value) = prefs.edit().putString("fallback_model", value).apply()

    /** 主配置是否可用：本地模型（Ollama）无需 key；API 模式需 key */
    val isConfigured: Boolean
        get() = apiEndpoint.isNotBlank() && (isLocal || apiKey.isNotBlank())

    /** 备用配置是否可用 */
    val hasFallback: Boolean
        get() = fallbackEnabled && fallbackEndpoint.isNotBlank() && fallbackApiKey.isNotBlank()
}
