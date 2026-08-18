package com.reminderapp.service

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

/**
 * v2.4.8: 避开节假日/周末顺延核心逻辑回归。
 * deferToWorkdayCore 是纯 JVM 逻辑（isHoliday 由调用方注入），
 * 直接构造周六/周日/节假日的固定时间点断言顺延结果。
 */
class ReminderEngineHolidayTest {

    /** 构造某年某月某日 09:00 的时间戳 */
    private fun at(year: Int, month: Int, day: Int, hour: Int = 9): Long {
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(year, month - 1, day, hour, 0, 0)
        return cal.timeInMillis
    }

    private fun isWeekend(ts: Long): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = ts }
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        return dow == Calendar.SATURDAY || dow == Calendar.SUNDAY
    }

    @Test
    fun `工作日不顺延`() {
        // 2026-08-17 是周一
        val ts = at(2026, 8, 17)
        val result = ReminderEngine.deferToWorkdayCore(ts) { false }
        assertEquals("工作日原样返回", ts, result)
    }

    @Test
    fun `周六顺延到周一`() {
        // 2026-08-22 是周六
        val sat = at(2026, 8, 22)
        assert(isWeekend(sat))
        val result = ReminderEngine.deferToWorkdayCore(sat) { false }
        val cal = Calendar.getInstance().apply { timeInMillis = result }
        assertEquals("顺延后是周一", Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK))
        assertEquals("保留时分 9 点", 9, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals("保留日期 8/24", 24, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `周日顺延到周一`() {
        // 2026-08-23 是周日
        val sun = at(2026, 8, 23)
        assert(isWeekend(sun))
        val result = ReminderEngine.deferToWorkdayCore(sun) { false }
        val cal = Calendar.getInstance().apply { timeInMillis = result }
        assertEquals("顺延后是周一", Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun `连续节假日加周末一路顺延到第一个工作日`() {
        // 模拟国庆：10/1-10/7 放假，10/1 是周四 → 下一个工作日是 10/8（周四）
        val oct1 = at(2026, 10, 1) // 2026-10-01 周四
        val holidays = setOf("2026-10-01", "2026-10-02", "2026-10-03", "2026-10-04", "2026-10-05", "2026-10-06", "2026-10-07")
        val result = ReminderEngine.deferToWorkdayCore(oct1) { cal ->
            val key = "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
            key in holidays
        }
        val cal = Calendar.getInstance().apply { timeInMillis = result }
        assertEquals("跳过整个黄金周，落到 10/8", 8, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `法定节假日顺延_周末不算节假日但顺延_重复判重防死循环`() {
        // 中秋 2026-09-25（周五）是节假日 → 顺延到 9/28 周一
        val mid = at(2026, 9, 25)
        val result = ReminderEngine.deferToWorkdayCore(mid) { cal ->
            cal.get(Calendar.MONTH) + 1 == 9 && cal.get(Calendar.DAY_OF_MONTH) == 25
        }
        val cal = Calendar.getInstance().apply { timeInMillis = result }
        assertEquals("9/25 中秋 → 顺延 9/28（周一）", 28, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals("9/28 是周一", Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK))
    }
}
