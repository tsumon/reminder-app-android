package com.reminderapp.ui.screen

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.reflect.TypeToken
import java.util.UUID

/**
 * AI tool execution trace step.
 * Kept in `com.reminderapp.ui.screen` so R8 ProGuard keep rules remain 100% matched.
 */
data class ToolStep(
    val name: String,
    val status: String,
    val summary: String? = null
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: Role,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val options: List<String>? = null,
    @Expose(serialize = false, deserialize = false)
    val toolSteps: List<ToolStep> = emptyList()
) {
    enum class Role { USER, ASSISTANT, SYSTEM }
}

object ChatHistoryStore {
    private const val PREFS = "ai_chat_history"
    private const val KEY = "messages"
    private const val MAX = 200
    private val gson = Gson()

    fun save(context: Context, messages: List<ChatMessage>) {
        val slim = messages.filter { it.content.isNotBlank() }.takeLast(MAX)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, gson.toJson(slim))
            .apply()
    }

    fun load(context: Context): List<ChatMessage> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
            ?: return emptyList()
        return try {
            val list: List<ChatMessage> = gson.fromJson(
                raw,
                object : TypeToken<List<ChatMessage>>() {}.type
            ) ?: return emptyList()
            @Suppress("SENSELESS_COMPARISON", "USELESS_ELVIS")
            list.filter { it.role != null && !it.content.isNullOrBlank() }
                .map {
                    it.copy(
                        id = it.id ?: UUID.randomUUID().toString(),
                        content = it.content ?: "",
                        toolSteps = it.toolSteps ?: emptyList()
                    )
                }
                .distinctBy { it.id }
        } catch (e: Exception) {
            Log.e("ChatHistory", "load failed, raw=${raw.take(120)}", e)
            emptyList()
        }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY).apply()
    }
}
