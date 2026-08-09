package com.reminderapp.i18n

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * 手动语言切换（v2.0.4 新增）
 *
 * 偏好存 SharedPreferences：code = system/zh/en/zh-rTW/ja/ko（system = 跟随系统）。
 * 资源目录对应：默认 values(简中) / values-en / values-ja / values-ko / values-zh-rTW(繁中)。
 *
 * 生效机制：
 *  - Application.onCreate 调 [apply]：Locale.setDefault + 预热文案 Context；
 *  - Activity.attachBaseContext 调 [wrap]：整棵 Activity 资源换语言；
 *  - L.kt 的 zh()/zhf() 经 [wrap] 取文案 Context，保证非 Composable 全局生效。
 */
object LocaleManager {

    private const val PREFS = "locale_prefs"
    private const val KEY_LANGUAGE = "app_language"

    /** 可选项：跟随系统/简体中文/English/繁體中文/日本語/한국어 */
    val options = listOf("system", "zh", "en", "zh-rTW", "ja", "ko")

    fun currentCode(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "system") ?: "system"

    fun isOverride(context: Context): Boolean = currentCode(context) != "system"

    /** 偏好代码 → Locale；简中用 Locale("zh")（匹配默认 values），繁中用 zh-rTW */
    fun toLocale(code: String): Locale = when (code) {
        "zh-rTW" -> Locale("zh", "TW")
        "zh" -> Locale("zh")
        "en" -> Locale("en")
        "ja" -> Locale("ja")
        "ko" -> Locale("ko")
        else -> Locale.getDefault()
    }

    /** 语言选项显示名：语言名用各自语言自称，不参与本地化 */
    fun displayName(code: String): String = when (code) {
        "system" -> zh("跟随系统")
        "zh" -> "简体中文"
        "en" -> "English"
        "zh-rTW" -> "繁體中文"
        "ja" -> "日本語"
        "ko" -> "한국어"
        else -> code
    }

    // 缓存包装后的文案 Context（按 code），避免每次 getString 都重建
    @Volatile
    private var cachedCode: String? = null
    @Volatile
    private var cachedContext: Context? = null

    /** 取带目标 Locale 的 Context；跟随系统时原样返回 */
    fun wrap(base: Context): Context {
        if (!isOverride(base)) return base
        val code = currentCode(base)
        cachedCode?.let { if (it == code) return cachedContext ?: base }
        val locale = toLocale(code)
        val conf = Configuration(base.resources.configuration)
        conf.setLocale(locale)
        val wrapped = base.createConfigurationContext(conf)
        cachedCode = code
        cachedContext = wrapped
        return wrapped
    }

    /** Application.onCreate 调用：默认 Locale + 预热文案 Context */
    fun apply(app: Context) {
        if (!isOverride(app)) return
        val locale = toLocale(currentCode(app))
        Locale.setDefault(locale)
        wrap(app) // 预热缓存
    }

    /** 设置语言：保存偏好并重建当前 Activity（attachBaseContext 会重新 wrap） */
    fun setLanguage(context: Context, code: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANGUAGE, code).apply()
        cachedCode = null
        cachedContext = null
        (context as? Activity)?.recreate()
    }
}
