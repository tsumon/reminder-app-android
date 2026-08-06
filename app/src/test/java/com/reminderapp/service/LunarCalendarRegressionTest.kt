package com.reminderapp.service

import com.ibm.icu.util.ChineseCalendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

/**
 * 历法引擎回归测试（v1.8.5 换系统历法后必跑）
 *
 * ⚠️ 重要说明：产品代码 [LunarCalendar] 使用 android.icu.util.ChineseCalendar
 * （Android framework API，JVM 单测无法加载），本测试用同源的
 * com.ibm.icu.util.ChineseCalendar（ICU4J）**复制**与产品代码完全相同的逻辑
 * （EXTENDED_YEAR-2637、IS_LEAP_MONTH、往返校验、官方口径修正表）来断言
 * 权威农历事实。android.icu 与 com.ibm.icu 由同一 ICU 项目派生，行为一致。
 *
 * 覆盖（用户要求的回归集）：
 *  - 近 10 年春节（2017-2026）
 *  - 2020 闰四月、2023 闰二月
 *  - 除夕/正月初一边界（2026 无年三十 → 2027 春节 02-06，官方口径修正）
 *  - 农历生日跨年（今年已过 → 明年）
 *  - 不存在的日期被往返校验拦截
 */
class LunarCalendarRegressionTest {

    // ===== 与产品代码相同的逻辑（com.ibm.icu 版）=====

    private fun solarToLunar(millis: Long): Lunar {
        val cc = ChineseCalendar()
        cc.timeInMillis = millis
        // 官方口径修正（同步产品代码 solarOverrides 反推表）
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        val key = String.format("%04d%02d%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
        if (key == "20270206") return Lunar(2027, 1, 1, false)
        return Lunar(
            cc.get(ChineseCalendar.EXTENDED_YEAR) - 2637,
            cc.get(ChineseCalendar.MONTH) + 1,
            cc.get(ChineseCalendar.DAY_OF_MONTH),
            cc.get(ChineseCalendar.IS_LEAP_MONTH) == 1
        )
    }

    private fun lunarToSolar(year: Int, month: Int, day: Int, isLeapMonth: Boolean = false): Long? {
        if (month < 1 || month > 12 || day < 1 || day > 30) return null
        // 官方口径修正（同步产品代码 lunarOverrides）
        val lunarKey = "$year:$month:$day"
        if (!isLeapMonth) {
            when (lunarKey) {
                "2027:1:1" -> return date(2027, 2, 6)
                "2026:12:30" -> return null
            }
        }
        return try {
            val cc = ChineseCalendar()
            cc.clear()
            cc.set(ChineseCalendar.EXTENDED_YEAR, year + 2637)
            cc.set(ChineseCalendar.MONTH, month - 1)
            cc.set(ChineseCalendar.IS_LEAP_MONTH, if (isLeapMonth) 1 else 0)
            cc.set(ChineseCalendar.DAY_OF_MONTH, day)
            cc.set(ChineseCalendar.HOUR_OF_DAY, 0)
            cc.set(ChineseCalendar.MINUTE, 0)
            cc.set(ChineseCalendar.SECOND, 0)
            cc.set(ChineseCalendar.MILLISECOND, 0)
            val millis = cc.timeInMillis
            val back = solarToLunar(millis)
            if (back.year == year && back.month == month && back.day == day && back.leap == isLeapMonth) millis else null
        } catch (e: Exception) {
            null
        }
    }

    private data class Lunar(val year: Int, val month: Int, val day: Int, val leap: Boolean)

    private fun date(y: Int, m: Int, d: Int): Long {
        val c = Calendar.getInstance()
        c.clear()
        c.set(y, m - 1, d, 0, 0, 0)
        return c.timeInMillis
    }

    // ===== 回归用例 =====

    @Test
    fun `近10年春节正月初一公历对照`() {
        val springs = listOf(
            intArrayOf(2017, 1, 28), intArrayOf(2018, 2, 16), intArrayOf(2019, 2, 5),
            intArrayOf(2020, 1, 25), intArrayOf(2021, 2, 12), intArrayOf(2022, 2, 1),
            intArrayOf(2023, 1, 22), intArrayOf(2024, 2, 10), intArrayOf(2025, 1, 29),
            intArrayOf(2026, 2, 17)
        )
        for (s in springs) {
            // 正向：公历 → 农历应为当年正月初一
            val l = solarToLunar(date(s[0], s[1], s[2]))
            assertEquals("春节 ${s[0]} 应为正月初一", Lunar(s[0], 1, 1, false), l)
            // 反向：农历正月初一 → 公历
            assertEquals("农历${s[0]}正月初一", date(s[0], s[1], s[2]), lunarToSolar(s[0], 1, 1))
        }
    }

    @Test
    fun `2027春节官方口径修正_ICU差1天`() {
        // ICU 裸数据：2026 腊月三十 = 02-06，2027 春节 = 02-07（CLDR 口径）
        // 官方口径（新华社/紫金山天文台）：2026 无年三十，2027 春节 = 02-06
        assertEquals("修正后 2027 春节 = 02-06", date(2027, 2, 6), lunarToSolar(2027, 1, 1))
        assertNull("官方口径 2026 年无腊月三十", lunarToSolar(2026, 12, 30))
        assertEquals("02-06 应判为 2027 正月初一", Lunar(2027, 1, 1, false), solarToLunar(date(2027, 2, 6)))
        assertEquals("02-05 是 2026 腊月廿九（除夕）", Lunar(2026, 12, 29, false), solarToLunar(date(2027, 2, 5)))
    }

    @Test
    fun `2020闰四月`() {
        assertEquals("闰四月十五 = 06-06", Lunar(2020, 4, 15, true), solarToLunar(date(2020, 6, 6)))
        assertEquals("lunarToSolar(2020 闰四月十五)", date(2020, 6, 6), lunarToSolar(2020, 4, 15, true))
    }

    @Test
    fun `2023闰二月`() {
        assertEquals("闰二月初一 = 03-22", Lunar(2023, 2, 1, true), solarToLunar(date(2023, 3, 22)))
        assertEquals("lunarToSolar(2023 闰二月初一)", date(2023, 3, 22), lunarToSolar(2023, 2, 1, true))
        // 普通二月三十存在（2023 普通二月大月 30 天）
        assertEquals("2023 普通二月三十 = 03-21", date(2023, 3, 21), lunarToSolar(2023, 2, 30, false))
    }

    @Test
    fun `农历生日跨年`() {
        // 2026-08-06 说「正月初一生日」：今年(02-17)已过 → 明年 2027 正月初一 = 02-06
        val thisYear = lunarToSolar(2026, 1, 1)!!
        assert(thisYear <= date(2026, 8, 6)) { "今年正月初一应已过" }
        assertEquals("明年正月初一", date(2027, 2, 6), lunarToSolar(2027, 1, 1))
    }

    @Test
    fun `不存在的日期被往返校验拦截`() {
        assertNull(lunarToSolar(2020, 4, 30, true)) // 2020 闰四月只有 29 天
        assertNull(lunarToSolar(2023, 2, 30, true)) // 2023 闰二月只有 29 天
    }
}
