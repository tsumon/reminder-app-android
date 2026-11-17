package com.reminderapp.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/**
 * 手动主题（v2.1.1）：0=跟随系统 1=浅色 2=深色
 * v2.10.0: 由 Compose Snapshot State 驱动，切换主题色或深浅色后整树即时重组。
 */
object ThemeStore {
    private const val PREFS = "theme_settings"
    private const val KEY_MODE = "theme_mode"
    private const val KEY_COLOR = "theme_color"

    private var loaded = false
    private var modeState by mutableIntStateOf(0)
    private var colorState by mutableIntStateOf(0)

    private fun ensureLoaded(context: Context) {
        if (loaded) return
        val sp = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        modeState = sp.getInt(KEY_MODE, 0)
        colorState = sp.getInt(KEY_COLOR, 0)
        loaded = true
    }

    fun mode(context: Context): Int {
        ensureLoaded(context)
        return modeState
    }

    fun setMode(context: Context, mode: Int) {
        ensureLoaded(context)
        modeState = mode
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_MODE, mode).apply()
    }

    fun colorIndex(context: Context): Int {
        ensureLoaded(context)
        return colorState
    }

    fun setColorIndex(context: Context, index: Int) {
        ensureLoaded(context)
        colorState = index
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_COLOR, index).apply()
    }
}
