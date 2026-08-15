package com.reminderapp.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf

/**
 * OpenAI 兼容 API 调用服务（v2.2.0：流式 + token 统计 + 备用降级）
 * 镜像 iOS AIService
 */
class AIService {

    data class ChatMessage(
        val role: String,
        val content: String? = null,
        val tool_calls: List<ToolCall>? = null,
        val tool_call_id: String? = null
    )

    data class ToolCall(
        val id: String,
        val type: String,
        val function: FunctionCall
    )

    data class FunctionCall(
        val name: String,
        val arguments: String
    )

    data class Choice(
        val message: ChatMessage?,
        val delta: ChatMessage?,
        val finish_reason: String?
    )

    data class Usage(
        val prompt_tokens: Int? = null,
        val completion_tokens: Int? = null,
        val total_tokens: Int? = null
    )

    data class ChatResponse(
        val choices: List<Choice>?,
        val usage: Usage?
    )

    /** 一次完整对话的结果（含流式累积的文本、工具调用、token 用量、实际使用的 provider） */
    data class ChatResult(
        val content: String?,
        val toolCalls: List<ToolCall>?,
        val usage: Usage?,
        val finishReason: String?,
        val usedFallback: Boolean
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    /**
     * 聊天（带工具）：主配置失败且配置了备用时自动降级重试一次。
     * stream 模式下只对纯文本回复做增量输出（工具调用轮次保持非流式，避免 tool_calls 分片累积的复杂性）。
     */
    suspend fun chatWithFallback(
        settings: AISettings,
        messages: List<Map<String, Any?>>,
        onStream: ((String) -> Unit)? = null
    ): ChatResult {
        val primary = runCatching {
            chat(settings.model, messages, settings.apiEndpoint, settings.apiKey, onStream)
        }
        val primaryResult = primary.getOrNull()
        if (primaryResult != null) return primaryResult

        // 主配置失败 → 备用降级（带一次退避，避免紧连重试）
        val primaryError = primary.exceptionOrNull() ?: Exception(zh("AI 调用失败"))
        if (settings.hasFallback) {
            kotlinx.coroutines.delay(500)
            return runCatching {
                chat(settings.fallbackModel, messages, settings.fallbackEndpoint, settings.fallbackApiKey, onStream)
                    .copy(usedFallback = true)
            }.getOrElse { throw primaryError }
        }
        throw primaryError
    }

    /** 纯文本补全（不带 tools）——用于周报 AI 解读；同样支持备用降级 */
    suspend fun completeWithFallback(
        settings: AISettings,
        messages: List<Map<String, Any?>>
    ): String {
        val primary = runCatching {
            send(settings.model, messages, settings.apiEndpoint, settings.apiKey, useTools = false, onStream = null)
                .content.orEmpty().trim()
        }
        val primaryResult = primary.getOrNull()
        if (primaryResult != null) return primaryResult

        val primaryError = primary.exceptionOrNull() ?: Exception(zh("AI 调用失败"))
        if (settings.hasFallback) {
            kotlinx.coroutines.delay(500)
            return runCatching {
                send(settings.fallbackModel, messages, settings.fallbackEndpoint, settings.fallbackApiKey, useTools = false, onStream = null)
                    .content.orEmpty().trim()
            }.getOrElse { throw primaryError }
        }
        throw primaryError
    }

    suspend fun chat(
        model: String,
        messages: List<Map<String, Any?>>,
        endpoint: String,
        apiKey: String,
        onStream: ((String) -> Unit)? = null
    ): ChatResult {
        // v2.2.0: 流式只用于纯文本轮次；聊天请求带工具，工具调用轮次统一非流式
        val result = if (onStream != null) {
            streamSend(model, messages, endpoint, apiKey, useTools = true, onStream = onStream)
        } else {
            send(model, messages, endpoint, apiKey, useTools = true, onStream = null)
        }
        return result
    }

    suspend fun complete(
        model: String,
        messages: List<Map<String, Any?>>,
        endpoint: String,
        apiKey: String
    ): String = send(model, messages, endpoint, apiKey, useTools = false, onStream = null).content.orEmpty().trim()

    // ── 非流式请求 ──

    private suspend fun send(
        model: String,
        messages: List<Map<String, Any?>>,
        endpoint: String,
        apiKey: String,
        useTools: Boolean,
        onStream: ((String) -> Unit)?
    ): ChatResult = withContext(Dispatchers.IO) {
        val request = buildRequest(model, messages, endpoint, apiKey, useTools, stream = false)
        val responseBody = client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: throw Exception(zh("空响应"))
            if (response.code == 401) throw Exception(zh("API Key 无效，请检查设置"))
            if (!response.isSuccessful) throw Exception(zhf("API 错误 %1\$s: %2\$s", response.code, body.take(200)))
            body
        }
        parseResponse(responseBody)
    }

    // ── 流式请求（SSE）──

    private suspend fun streamSend(
        model: String,
        messages: List<Map<String, Any?>>,
        endpoint: String,
        apiKey: String,
        useTools: Boolean,
        onStream: ((String) -> Unit)
    ): ChatResult = withContext(Dispatchers.IO) {
        val request = buildRequest(model, messages, endpoint, apiKey, useTools, stream = true)
        client.newCall(request).execute().use { response ->
            val source = response.body?.source()
                ?: throw Exception(zh("空响应"))
            if (response.code == 401) throw Exception(zh("API Key 无效，请检查设置"))
            if (!response.isSuccessful) {
                throw Exception(zhf("API 错误 %1\$s: %2\$s", response.code, source.readUtf8().take(200)))
            }

            val content = StringBuilder()
            var finishReason: String? = null
            var usage: Usage? = null

            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break
                if (!line.startsWith("data:")) continue
                val payload = line.removePrefix("data:").trim()
                if (payload == "[DONE]") break
                val chunk = try {
                    gson.fromJson(payload, ChatResponse::class.java)
                } catch (e: Exception) {
                    continue // 忽略残缺分片（半包场景）
                }
                chunk.choices?.firstOrNull()?.let { choice ->
                    choice.delta?.content?.let { delta ->
                        content.append(delta)
                        onStream(delta)
                    }
                    if (choice.finish_reason != null) finishReason = choice.finish_reason
                }
                if (chunk.usage != null) usage = chunk.usage
            }

            // 流式轮次只可能出现在纯文本回复阶段（工具调用轮次走非流式）
            ChatResult(
                content = content.toString().ifEmpty { null },
                toolCalls = null,
                usage = usage,
                finishReason = finishReason,
                usedFallback = false
            )
        }
    }

    private fun buildRequest(
        model: String,
        messages: List<Map<String, Any?>>,
        endpoint: String,
        apiKey: String,
        useTools: Boolean,
        stream: Boolean
    ): Request {
        val body = mutableMapOf<String, Any?>(
            "model" to model,
            "messages" to messages
        )
        if (useTools) {
            body["tools"] = AITools.toolDefinitions()
            body["tool_choice"] = "auto"
        }
        if (stream) {
            body["stream"] = true
            // 流式模式下部分 provider 需要 stream_options 才返回 usage
            body["stream_options"] = mapOf("include_usage" to true)
        }

        val jsonBody = gson.toJson(body)
        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
        return Request.Builder()
            .url("${endpoint.trimEnd('/')}/chat/completions")
            .post(requestBody)
            .header("Content-Type", "application/json")
            // 本地模型（Ollama）无 key 时省略 Authorization
            .apply { if (apiKey.isNotBlank()) header("Authorization", "Bearer $apiKey") }
            .build()
    }

    private fun parseResponse(responseBody: String): ChatResult {
        val result = gson.fromJson(responseBody, ChatResponse::class.java)
            ?: throw Exception(zh("响应格式错误"))
        val choice = result.choices?.firstOrNull()
            ?: throw Exception(zh("AI 返回为空"))
        return ChatResult(
            content = choice.message?.content,
            toolCalls = choice.message?.tool_calls,
            usage = result.usage,
            finishReason = choice.finish_reason,
            usedFallback = false
        )
    }
}
