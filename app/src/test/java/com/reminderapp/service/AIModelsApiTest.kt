package com.reminderapp.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * OpenAI 兼容模型列表：URL 拼接不重复 /v1，解析 data[].id。
 */
class AIModelsApiTest {

    @Test
    fun modelsUrl_appendsModelsWithoutDoublingV1() {
        assertEquals(
            "https://api.openai.com/v1/models",
            AIService.modelsUrl("https://api.openai.com/v1")
        )
        assertEquals(
            "https://api.openai.com/v1/models",
            AIService.modelsUrl("https://api.openai.com/v1/")
        )
        assertEquals(
            "http://localhost:11434/v1/models",
            AIService.modelsUrl("http://localhost:11434/v1")
        )
        assertEquals(
            "https://api.deepseek.com/v1/models",
            AIService.modelsUrl("  https://api.deepseek.com/v1  ")
        )
    }

    @Test
    fun modelsUrl_emptyIsNull() {
        assertNull(AIService.modelsUrl(""))
        assertNull(AIService.modelsUrl("   "))
        assertNull(AIService.modelsUrl("/"))
    }

    @Test
    fun parseModelIds_readsDataIdInOrderUniquely() {
        val json = """
            {"object":"list","data":[
              {"id":"gpt-4o","object":"model"},
              {"id":" gpt-4o-mini "},
              {"id":"gpt-4o"},
              {"id":""},
              {"object":"model"}
            ]}
        """.trimIndent()
        assertEquals(listOf("gpt-4o", "gpt-4o-mini"), AIService.parseModelIds(json))
    }

    @Test
    fun parseModelIds_emptyData() {
        assertTrue(AIService.parseModelIds("""{"data":[]}""").isEmpty())
        assertTrue(AIService.parseModelIds("{}").isEmpty())
    }

    @Test
    fun formatHttpError_401KeepsRawBody() {
        val msg = AIService.formatHttpError(401, """{"error":"invalid_api_key"}""")
        assertEquals("HTTP 401: {\"error\":\"invalid_api_key\"}", msg)
    }
}
