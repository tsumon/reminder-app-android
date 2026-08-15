package com.reminderapp.service

import com.reminderapp.data.entity.ReminderEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * 周期触发计算（calculateNextTrigger / confirm）回归测试 — 对齐 iOS calculateNextTrigger 行为
 *
 * 覆盖（v2.0.17 补，对齐 iOS LunarCalendarCheckMain 2c 区 + 历轮 bug 回归）：
 *  - daily/weekly/biweekly/custom 间隔推进（整周期、不跳过多）
 *  - monthly 按日历月累加（月末对齐：31 号落 2 月钳月末，不漂移）
 *  - yearly 2/29 闰年边界（非闰年不停留在 2/28）
 *  - 锚点在未来 → 不推进
 *  - once（interval=0）→ 返回锚点不前进（防死循环）
 *  - 极端过去锚点（10 年前）→ 仍返回 now 之后（防返回过去时间）
 *  - confirm 后周期前进 + 重置 retryCount
 */
class ReminderEngineCycleTest {

    private val DAY = 86400_000L
    private val TOLERANCE = 5_000L // calculateNextTrigger 基于真实时钟，给 5 秒容差

    private fun date(y: Int, m: Int, d: Int, h: Int = 9, min: Int = 0): Long {
        val c = Calendar.getInstance()
        c.clear()
        c.set(y, m - 1, d, h, min, 0)
        return c.timeInMillis
    }

    private fun cycleReminder(cycle: String, firstTriggerAt: Long, customDays: Int = 0) = ReminderEntity(
        title = "测试提醒",
        kind = "cycle",
        cycle = cycle,
        customDays = customDays,
        firstTriggerAt = firstTriggerAt,
        nextTriggerAt = firstTriggerAt,
        status = "pending",
        retryCount = 0
    )

    @Test
    fun `daily 过去锚点推进为整周期`() {
        val anchor = System.currentTimeMillis() - 10 * DAY
        val next = ReminderEngine.calculateNextTrigger(cycleReminder("daily", anchor))
        assertTrue("下次触发应晚于 now", next > System.currentTimeMillis())
        val elapsed = (next - anchor).toDouble()
        val cycles = Math.round(elapsed / DAY)
        assertTrue("应为整周期推进（锚点起 N*24h），实际差 ${elapsed - cycles * DAY}ms",
            Math.abs(elapsed - cycles * DAY) <= TOLERANCE)
    }

    @Test
    fun `weekly 过去锚点推进为整周`() {
        val anchor = System.currentTimeMillis() - 30 * DAY
        val next = ReminderEngine.calculateNextTrigger(cycleReminder("weekly", anchor))
        assertTrue("下次触发应晚于 now", next > System.currentTimeMillis())
        val elapsed = (next - anchor).toDouble()
        val cycles = Math.round(elapsed / (7 * DAY))
        assertTrue("应为整周推进", Math.abs(elapsed - cycles * 7 * DAY) <= TOLERANCE)
    }

    @Test
    fun `biweekly 过去锚点推进为两周`() {
        val anchor = System.currentTimeMillis() - 20 * DAY
        val next = ReminderEngine.calculateNextTrigger(cycleReminder("biweekly", anchor))
        assertTrue("下次触发应晚于 now", next > System.currentTimeMillis())
        val elapsed = (next - anchor).toDouble()
        val cycles = Math.round(elapsed / (14 * DAY))
        assertTrue("应为两周整周期", Math.abs(elapsed - cycles * 14 * DAY) <= TOLERANCE)
    }

    @Test
    fun `custom 3天过去锚点推进为 3 天整周期`() {
        val anchor = System.currentTimeMillis() - 10 * DAY
        val next = ReminderEngine.calculateNextTrigger(cycleReminder("custom", anchor, customDays = 3))
        assertTrue("下次触发应晚于 now", next > System.currentTimeMillis())
        val elapsed = (next - anchor).toDouble()
        val cycles = Math.round(elapsed / (3 * DAY))
        assertTrue("应为 3 天整周期", Math.abs(elapsed - cycles * 3 * DAY) <= TOLERANCE)
    }

    @Test
    fun `monthly 按日历月累加且月末对齐不漂移`() {
        // 锚点 = 昨天 09:00（monthly）→ 下次 = 下月同日同时（日历月累加，非固定 30 天）。
        // 锚点必须取「已过去的时刻」：凌晨 0~9 点跑测试时「今天 09:00」还在未来，
        // 产品逻辑会直接返回锚点本身导致断言失败（v2.2.1 修复测试的日期敏感性）。
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        cal.add(Calendar.DAY_OF_YEAR, -1)
        cal.set(Calendar.HOUR_OF_DAY, 9); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val anchor = cal.timeInMillis

        val next = ReminderEngine.calculateNextTrigger(cycleReminder("monthly", anchor))

        val expected = Calendar.getInstance().apply { timeInMillis = anchor }
        expected.add(Calendar.MONTH, 1)
        val expectedMs = expected.timeInMillis
        assertTrue("下次应不早于下月同日（$expectedMs），实际 $next", next >= expectedMs)
        assertTrue("下次应不晚于下月同日 + 32 天（锚点 9 点已过时最多再推一个月）",
            next - expectedMs <= 32 * DAY)
        val nc = Calendar.getInstance().apply { timeInMillis = next }
        assertEquals("日号应与锚点对齐（或钳月末）", minOf(
            cal.get(Calendar.DAY_OF_MONTH),
            nc.getActualMaximum(Calendar.DAY_OF_MONTH)
        ), nc.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `yearly 锚点2月29日只落在闰年`() {
        // 锚点 2024-02-29（闰年，已过去）→ 下一个触发必须是下一个闰年的 2/29（2028-02-29），
        // 不能停留在非闰年的 2/28（advanceByCalendarMonths 的闰年补跳逻辑）。
        val anchor = date(2024, 2, 29, 9)
        val next = ReminderEngine.calculateNextTrigger(cycleReminder("yearly", anchor))
        assertTrue("下次触发应晚于 now", next > System.currentTimeMillis())
        val nc = Calendar.getInstance().apply { timeInMillis = next }
        assertEquals("月份应为 2 月", 2, nc.get(Calendar.MONTH) + 1)
        assertEquals("日号应为 29", 29, nc.get(Calendar.DAY_OF_MONTH))
        assertTrue("应为闰年（2 月有 29 天）", nc.getActualMaximum(Calendar.DAY_OF_MONTH) >= 29)
    }

    @Test
    fun `锚点在未来时不推进`() {
        val anchor = System.currentTimeMillis() + 5 * DAY
        val next = ReminderEngine.calculateNextTrigger(cycleReminder("daily", anchor))
        assertEquals("未来锚点应原样返回", anchor, next)
    }

    @Test
    fun `once 周期返回锚点不前进`() {
        val anchor = System.currentTimeMillis() - DAY
        val next = ReminderEngine.calculateNextTrigger(cycleReminder("once", anchor))
        assertEquals("once（interval=0）应返回锚点，防死循环", anchor, next)
    }

    @Test
    fun `极端过去锚点不返回过去时间`() {
        val anchor = date(2015, 1, 1, 9)
        val next = ReminderEngine.calculateNextTrigger(cycleReminder("daily", anchor))
        assertTrue("10 年前的锚点仍应推进到 now 之后", next > System.currentTimeMillis())
    }

    @Test
    fun `confirm 后周期前进并重置 retryCount`() {
        val anchor = System.currentTimeMillis() - 10 * DAY
        val r = cycleReminder("daily", anchor).copy(retryCount = 3, status = "notifying")
        val confirmed = ReminderEngine.confirm(r)
        assertEquals("confirm 后状态回 pending", "pending", confirmed.status)
        assertEquals("confirm 重置 retryCount", 0, confirmed.retryCount)
        assertTrue("confirm 后 nextTriggerAt 应晚于原值", confirmed.nextTriggerAt > r.nextTriggerAt)
        assertTrue("confirm 后 nextTriggerAt 应在 now 之后", confirmed.nextTriggerAt > System.currentTimeMillis())
    }

    @Test
    fun `confirm 一次性提醒归档为已完成`() {
        val anchor = System.currentTimeMillis() - DAY
        val r = cycleReminder("once", anchor).copy(status = "notifying")
        val confirmed = ReminderEngine.confirm(r)
        assertEquals("once 确认后状态为 confirmed", "confirmed", confirmed.status)
        assertEquals("once 确认后 nextTriggerAt 不变", anchor, confirmed.nextTriggerAt)
    }
}
