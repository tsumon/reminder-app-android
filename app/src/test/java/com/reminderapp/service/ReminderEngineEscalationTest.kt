package com.reminderapp.service

import com.reminderapp.data.entity.ReminderEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 递增重试（escalate）回归测试 — 对齐 iOS escalateRetry
 *
 * 双端约定（v1.9.7）：
 *  - 间隔序列：1h → 4h → 12h → 24h → 24h
 *  - retryCount 达到 MAX_ESCALATION(5) 时标 overdue，不再重排（停止轰炸）
 *  - 重试期间不推进周期，周期只在「确认完成 / 重新打开」时前进
 */
class ReminderEngineEscalationTest {

    private val now = 1_750_000_000_000L // 固定时间基座（仅用于构造 ReminderEntity）

    private fun reminder(retryCount: Int = 0, status: String = "notifying") = ReminderEntity(
        title = "测试提醒",
        firstTriggerAt = now,
        nextTriggerAt = now,
        status = status,
        retryCount = retryCount
    )

    private val HOUR = 3_600_000L
    private val TOLERANCE = 5_000L  // escalate 基于真实时钟，给 5 秒容差

    @Test
    fun `escalate 序列 1h-4h-12h-24h-24h 然后 overdue`() {
        var r = reminder()
        val expectedIntervals = listOf(1L, 4L, 12L, 24L, 24L)

        for (i in 0 until 4) {
            r = ReminderEngine.escalate(r)
            val delay = r.nextTriggerAt - System.currentTimeMillis()
            val expected = expectedIntervals[i] * HOUR
            assertTrue("第 ${i + 1} 次重试间隔应为 ${expectedIntervals[i]}h (误差 ${TOLERANCE}ms), 实际 ${delay}ms",
                Math.abs(delay - expected) <= TOLERANCE)
            assertEquals("重试期间状态应为 notifying", "notifying", r.status)
            assertEquals("retryCount 应递增为 ${i + 1}", i + 1, r.retryCount)
        }

        // 第 5 次 escalate：retryCount 到 5 → overdue
        r = ReminderEngine.escalate(r)
        val delay = r.nextTriggerAt - System.currentTimeMillis()
        assertTrue("第 5 次间隔仍为 24h (误差 ${TOLERANCE}ms)",
            Math.abs(delay - 24L * HOUR) <= TOLERANCE)
        assertEquals("达到上限应标 overdue", "overdue", r.status)
        assertEquals("retryCount 封顶 5", 5, r.retryCount)
    }

    @Test
    fun `escalate 后 overdue 不再增长且保持 overdue`() {
        var r = reminder(retryCount = 5, status = "overdue")
        r = ReminderEngine.escalate(r)
        assertEquals("已 overdue 再 escalate 仍为 overdue", "overdue", r.status)
        assertEquals("retryCount 保持 5", 5, r.retryCount)
    }

    @Test
    fun `escalate 从 4 直接到封顶也标 overdue`() {
        val r = reminder(retryCount = 4, status = "notifying")
        val escalated = ReminderEngine.escalate(r)
        assertEquals("retryCount=4 再 escalate 应标 overdue", "overdue", escalated.status)
        assertEquals(5, escalated.retryCount)
    }

    @Test
    fun `snooze 只推迟 15 分钟且不动 retryCount`() {
        val r = reminder(retryCount = 2)
        val snoozed = ReminderEngine.snooze(r)
        val delay = snoozed.nextTriggerAt - System.currentTimeMillis()
        assertTrue("稍后 15 分钟 (误差 ${TOLERANCE}ms), 实际 ${delay}ms",
            Math.abs(delay - 15 * 60_000L) <= TOLERANCE)
        assertEquals("稍后不改 retryCount（对齐 iOS 不改 retryStage）", 2, snoozed.retryCount)
        assertEquals("稍后状态为 notifying", "notifying", snoozed.status)
    }

    @Test
    fun `confirm 重置 retryCount 并推进周期`() {
        val r = reminder(retryCount = 3, status = "notifying").copy(
            kind = "cycle", cycle = "daily", firstTriggerAt = now - HOUR
        )
        val confirmed = ReminderEngine.confirm(r)
        assertEquals("确认后重置 retryCount", 0, confirmed.retryCount)
        assertEquals("确认后回到 pending", "pending", confirmed.status)
        assertTrue("确认后推进到未来", confirmed.nextTriggerAt > now)
    }

    @Test
    fun `checkMissed 排除 overdue 与 confirmed`() {
        val past = System.currentTimeMillis() - 1000L
        val overdue = reminder(retryCount = 5, status = "overdue").copy(nextTriggerAt = past)
        val confirmed = reminder(status = "confirmed").copy(nextTriggerAt = past)
        val notifying = reminder(status = "notifying").copy(nextTriggerAt = past)

        val missed = ReminderEngine.checkMissed(
            listOf(overdue, confirmed, notifying),
            System.currentTimeMillis()
        )
        assertEquals("只有非 overdue/confirmed 的到期提醒应被返回", 1, missed.size)
        assertEquals("notifying", missed[0].status)
    }
}