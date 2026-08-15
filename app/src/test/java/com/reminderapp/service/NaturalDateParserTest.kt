package com.reminderapp.service

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

/**
 * 自然语言日期解析回归测试 — 固定 now，断言「上周/本周/下周」跨端一致性（对齐 iOS NaturalDateParser）
 *
 * 基座：2026-08-17（周一）12:00。
 *  - 「上周一」→ 2026-08-10（上周一，iOS 同样减 7 天）
 *  - 「周一」（本周已过的同一天）→ 下周一 2026-08-24
 *  - 「下周一」→ 2026-08-24
 */
class NaturalDateParserTest {

    private val now: Long = Calendar.getInstance().apply {
        clear()
        set(2026, Calendar.AUGUST, 17, 12, 0, 0)
    }.timeInMillis

    private fun date(y: Int, m: Int, d: Int, h: Int = 9): Long =
        Calendar.getInstance().apply {
            clear()
            set(y, m - 1, d, h, 0, 0)
        }.timeInMillis

    @Test
    fun `上周一 解析为上周一`() {
        val parsed = NaturalDateParser.parse("上周一提醒我开会", now) ?: throw AssertionError("解析失败")
        assertEquals("「上周一」应为 2026-08-10 09:00", date(2026, 8, 10), parsed.nextTriggerAt)
    }

    @Test
    fun `周一 在本周已过时顺延到下周一`() {
        val parsed = NaturalDateParser.parse("周一开会", now) ?: throw AssertionError("解析失败")
        assertEquals("本周一已过，应顺延到下周一 2026-08-24", date(2026, 8, 24), parsed.nextTriggerAt)
    }

    @Test
    fun `下周一 解析为下周一`() {
        val parsed = NaturalDateParser.parse("下周一开会", now) ?: throw AssertionError("解析失败")
        assertEquals("「下周一」应为 2026-08-24", date(2026, 8, 24), parsed.nextTriggerAt)
    }

    @Test
    fun `上周日 解析为上周日`() {
        val parsed = NaturalDateParser.parse("上周日聚餐", now) ?: throw AssertionError("解析失败")
        assertEquals("「上周日」应为 2026-08-16", date(2026, 8, 16), parsed.nextTriggerAt)
    }
}
