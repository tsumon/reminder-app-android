package com.reminderapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.reminderapp.ReminderApp
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.i18n.zh
import com.reminderapp.service.AILogStore
import com.reminderapp.service.AIService
import com.reminderapp.service.AISettings
import com.reminderapp.service.AITools
import com.reminderapp.service.NotificationManager
import com.reminderapp.service.ReminderScheduler
import com.reminderapp.service.VoiceService
import com.reminderapp.ui.screen.ChatHistoryStore
import com.reminderapp.ui.screen.ChatMessage
import com.reminderapp.ui.screen.ToolStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val STREAM_BUBBLE_ID = "stream-current-bubble"
private const val STEP_BUBBLE_ID = "tool-steps-bubble"

class AIChatViewModel(
    private val appContext: Context,
    private val settings: AISettings,
    private val aiService: AIService,
    private val database: AppDatabase,
    private val scheduler: ReminderScheduler,
    private val notificationMgr: NotificationManager
) : ViewModel() {
    private val gson = Gson()
    private val voiceService = VoiceService(ReminderApp.instance)

    private val _messages = MutableStateFlow(ChatHistoryStore.load(appContext))
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    init {
        if (!settings.isConfigured && _messages.value.isEmpty()) {
            val guide = zh("👋 你好！请先在设置中配置 API Key（支持 DeepSeek / 通义千问 / 豆包等，均有免费额度）。\n\n我能帮你：\n• 创建提醒「每天提醒我喝水」\n• 查看列表「有什么提醒」\n• 确认完成「确认喝水」\n• 修改提醒「把交房租改成每月5号」\n• 推迟/删除提醒")
            _messages.value = listOf(ChatMessage(role = ChatMessage.Role.ASSISTANT, content = guide))
        }
    }

    fun dismissError() {
        _errorMsg.value = null
    }

    fun clearHistory() {
        _messages.value = emptyList()
        ChatHistoryStore.clear(appContext)
    }

    fun upsertMessage(m: ChatMessage) {
        val cur = _messages.value
        val idx = cur.indexOfFirst { it.id == m.id }
        val next = if (idx >= 0) cur.toMutableList().also { it[idx] = m } else cur + m
        _messages.value = next
        ChatHistoryStore.save(appContext, next)
    }

    fun appendMessages(newMsgs: List<ChatMessage>) {
        val next = _messages.value + newMsgs
        _messages.value = next
        ChatHistoryStore.save(appContext, next)
    }

    fun sendUserMessage(text: String, onMissingKey: () -> Unit) {
        val clean = text.trim()
        if (clean.isEmpty() || _isLoading.value) return
        appendMessages(listOf(ChatMessage(role = ChatMessage.Role.USER, content = clean)))
        if (!settings.isConfigured) {
            onMissingKey()
            return
        }
        _isLoading.value = true
        _errorMsg.value = null
        viewModelScope.launch {
            runAgentLoop()
        }
    }

    fun selectOption(sourceMessage: ChatMessage, option: String, onMissingKey: () -> Unit) {
        if (_isLoading.value) return
        upsertMessage(sourceMessage.copy(options = null))
        sendUserMessage(option, onMissingKey)
    }

    fun startVoiceInput(onMissingKey: () -> Unit) {
        if (_isListening.value) return
        _isListening.value = true
        viewModelScope.launch {
            voiceService.recognize().fold(
                onSuccess = { text ->
                    if (text.isNotBlank()) {
                        sendUserMessage(text, onMissingKey)
                    }
                },
                onFailure = { e -> _errorMsg.value = e.message }
            )
            _isListening.value = false
        }
    }

    private suspend fun runAgentLoop() {
        val historySnapshot = _messages.value
        var msgs = mutableListOf<ChatMessage>()
        val startedAt = System.currentTimeMillis()
        val conversation = mutableListOf<Map<String, Any?>>(
            mapOf("role" to "system", "content" to AITools.systemPrompt)
        )
        historySnapshot.takeLast(20).forEach { msg ->
            conversation.add(mapOf("role" to msg.role.name.lowercase(), "content" to msg.content))
        }

        var maxTurns = 5
        var usedFallback = false
        var finalUsage: AIService.Usage? = null
        var streamBubble: ChatMessage? = null

        fun pushStep(steps: List<ToolStep>) {
            upsertMessage(
                ChatMessage(
                    id = STEP_BUBBLE_ID,
                    role = ChatMessage.Role.ASSISTANT,
                    content = "",
                    toolSteps = steps
                )
            )
        }

        while (maxTurns > 0) {
            maxTurns--
            try {
                val streamEnabled = maxTurns < 4
                val reply = withTimeout(30_000) {
                    aiService.chatWithFallback(
                        settings,
                        conversation,
                        onStream = if (streamEnabled) { delta ->
                            streamBubble = streamBubble?.copy(content = streamBubble!!.content + delta)
                                ?: ChatMessage(id = STREAM_BUBBLE_ID, role = ChatMessage.Role.ASSISTANT, content = delta)
                            upsertMessage(streamBubble!!)
                        } else null
                    )
                }
                usedFallback = reply.usedFallback
                finalUsage = reply.usage

                if (!reply.toolCalls.isNullOrEmpty()) {
                    val steps = mutableListOf<ToolStep>()
                    for (tc in reply.toolCalls) {
                        if (tc.function.name == "ask_user") {
                            var question = zh("请补充信息")
                            var options: List<String> = emptyList()
                            try {
                                val args: Map<String, Any?> = gson.fromJson(
                                    tc.function.arguments,
                                    object : TypeToken<Map<String, Any?>>() {}.type
                                )
                                (args["question"] as? String)?.takeIf { it.isNotBlank() }?.let { question = it }
                                (args["options"] as? List<*>)?.filterIsInstance<String>()
                                    ?.filter { it.isNotBlank() }?.let { options = it }
                            } catch (_: Exception) {
                            }
                            upsertMessage(
                                ChatMessage(
                                    id = STEP_BUBBLE_ID,
                                    role = ChatMessage.Role.ASSISTANT,
                                    content = question,
                                    options = options.ifEmpty { null }
                                )
                            )
                            _isLoading.value = false
                            AILogStore.add(
                                ReminderApp.instance,
                                AILogStore.Entry(
                                    model = if (usedFallback) settings.fallbackModel else settings.model,
                                    provider = if (usedFallback) "fallback" else "primary",
                                    turns = 5 - maxTurns,
                                    promptTokens = finalUsage?.prompt_tokens,
                                    completionTokens = finalUsage?.completion_tokens,
                                    durationMs = System.currentTimeMillis() - startedAt,
                                    ok = true
                                )
                            )
                            return
                        }
                        steps.add(ToolStep(name = tc.function.name, status = "running"))
                        pushStep(steps.toList())
                        val result = executeAITool(
                            tc.function.name,
                            tc.function.arguments,
                            database,
                            scheduler,
                            notificationMgr,
                            gson
                        )
                        val isError = result == zh("参数解析失败") || result.startsWith(zh("未知工具"))
                        steps[steps.size - 1] = ToolStep(
                            name = tc.function.name,
                            status = if (isError) "error" else "done",
                            summary = result.take(80)
                        )
                        pushStep(steps.toList())
                        conversation.add(
                            mapOf(
                                "role" to "assistant",
                                "tool_calls" to listOf(
                                    mapOf(
                                        "id" to tc.id,
                                        "type" to "function",
                                        "function" to mapOf(
                                            "name" to tc.function.name,
                                            "arguments" to tc.function.arguments
                                        )
                                    )
                                )
                            )
                        )
                        conversation.add(mapOf("role" to "tool", "content" to result, "tool_call_id" to tc.id))
                    }
                    msgs = mutableListOf()
                    continue
                }

                var content = reply.content ?: zh("好的，已处理。")
                if (reply.finishReason == "length") {
                    content += "\n\n（响应长度受限，已截断）"
                }
                if (streamBubble != null) {
                    upsertMessage(ChatMessage(id = STREAM_BUBBLE_ID, role = ChatMessage.Role.ASSISTANT, content = content))
                } else {
                    msgs.add(ChatMessage(role = ChatMessage.Role.ASSISTANT, content = content))
                    appendMessages(msgs)
                }
                _isLoading.value = false
                AILogStore.add(
                    ReminderApp.instance,
                    AILogStore.Entry(
                        model = if (usedFallback) settings.fallbackModel else settings.model,
                        provider = if (usedFallback) "fallback" else "primary",
                        turns = 5 - maxTurns,
                        promptTokens = finalUsage?.prompt_tokens,
                        completionTokens = finalUsage?.completion_tokens,
                        durationMs = System.currentTimeMillis() - startedAt,
                        ok = true
                    )
                )
                return
            } catch (e: Exception) {
                msgs.add(ChatMessage(role = ChatMessage.Role.ASSISTANT, content = "✕ ${e.message}"))
                _isLoading.value = false
                appendMessages(msgs)
                AILogStore.add(
                    ReminderApp.instance,
                    AILogStore.Entry(
                        model = if (usedFallback) settings.fallbackModel else settings.model,
                        provider = if (usedFallback) "fallback" else "primary",
                        turns = 5 - maxTurns,
                        durationMs = System.currentTimeMillis() - startedAt,
                        ok = false,
                        error = e.message
                    )
                )
                return
            }
        }

        msgs.add(ChatMessage(role = ChatMessage.Role.ASSISTANT, content = zh("对话轮次过多，请重新描述你的需求。")))
        _isLoading.value = false
        appendMessages(msgs)
    }
}
