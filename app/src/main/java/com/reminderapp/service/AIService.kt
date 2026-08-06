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

/**
 * OpenAI 兼容 API 调用服务
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
        val finish_reason: String?
    )

    data class ChatResponse(
        val choices: List<Choice>?
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    suspend fun chat(
        model: String,
        messages: List<Map<String, Any?>>,
        endpoint: String,
        apiKey: String
    ): ChatMessage = withContext(Dispatchers.IO) {
        val body = mapOf(
            "model" to model,
            "messages" to messages,
            "tools" to AITools.toolDefinitions(),
            "tool_choice" to "auto"
        )

        val jsonBody = gson.toJson(body)
        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${endpoint.trimEnd('/')}/chat/completions")
            .post(requestBody)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $apiKey")
            .build()

        // use{} 确保异常路径也关闭 response，避免连接泄漏
        val responseBody = client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: throw Exception("空响应")
            if (response.code == 401) throw Exception("API Key 无效，请检查设置")
            if (!response.isSuccessful) throw Exception("API 错误 ${response.code}: ${body.take(200)}")
            body
        }

        val result = gson.fromJson(responseBody, ChatResponse::class.java)
            ?: throw Exception("响应格式错误")

        result.choices?.firstOrNull()?.message ?: throw Exception("AI 返回为空")
    }
}
