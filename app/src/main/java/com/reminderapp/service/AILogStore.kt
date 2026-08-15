package com.reminderapp.service

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * v2.2.0: AI 调用日志——记录最近 20 次对话（模型/provider/轮数/token/耗时/成败），
 * 供诊断页查看，面试可讲「AI 可观测性」。内存 + SharedPreferences 双份（重启不丢）。
 */
object AILogStore {

    data class Entry(
        val time: Long = System.currentTimeMillis(),
        val model: String = "",
        val provider: String = "",     // primary / fallback
        val turns: Int = 0,            // Agent 工具轮数
        val promptTokens: Int? = null,
        val completionTokens: Int? = null,
        val durationMs: Long = 0,
        val ok: Boolean = true,
        val error: String? = null
    ) {
        fun timeText(): String =
            SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()).format(Date(time))
    }

    private const val PREFS = "ai_log_store"
    private const val KEY = "entries"
    private const val MAX = 20

    fun add(context: Context, entry: Entry) {
        val list = recent(context).toMutableList()
        list.add(0, entry)
        val trimmed = list.take(MAX)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, Gson().toJson(trimmed)).apply()
    }

    fun recent(context: Context): List<Entry> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
            ?: return emptyList()
        return try {
            Gson().fromJson(raw, object : TypeToken<List<Entry>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY).apply()
    }
}
