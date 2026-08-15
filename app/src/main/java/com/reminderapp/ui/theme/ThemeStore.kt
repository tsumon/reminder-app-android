package com.reminderapp.ui.theme

import android.content.Context

/**
 * 手动主题（v2.1.1）：0=跟随系统 1=浅色 2=深色
 * 与 iOS ThemeStore 对齐；自签环境系统外观可能不可控，手动切换是最稳的兜底。
 */
object ThemeStore {
    private const val PREFS = "theme_settings"
    private const val KEY_MODE = "theme_mode"

    fun mode(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_MODE, 0)

    fun setMode(context: Context, mode: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_MODE, mode).apply()
    }

    // v2.4.0: 主题色板索引（0=青碧默认）
    private const val KEY_COLOR = "theme_color"

    fun colorIndex(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_COLOR, 0)

    fun setColorIndex(context: Context, index: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_COLOR, index).apply()
    }
}
