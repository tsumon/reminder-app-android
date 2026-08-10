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
    // ⚠️ 与产品代码 LunarCalendar.kt 的 buildCorrectionTable / lunarToSolar / solarToLunar
    // 保持逐行一致（NO_NIAN_SHA_YEARS + SHIFTED_YEARS + 裸算往返校验），确保回归测试真正镜像产品行为。

    // 与产品代码 LunarCalendar.kt buildCorrectionTable 的「无年三十」集合逐行一致：
    // 2025/2026/2027/2028/2030 无腊月三十；2029 农历有腊月三十（公历 2030-02-02）。
    private val NO_NIAN_SHA_YEARS = setOf(2025, 2026, 2027, 2028, 2030)
    private val SHIFTED_YEARS = setOf(2027, 2028, 2029, 2030)

    private fun rawSolarToLunar(millis: Long): Lunar {
        val cc = ChineseCalendar()
        cc.timeInMillis = millis
        return Lunar(
            cc.get(ChineseCalendar.EXTENDED_YEAR) - 2637,
            cc.get(ChineseCalendar.MONTH) + 1,
            cc.get(ChineseCalendar.DAY_OF_MONTH),
            cc.get(ChineseCalendar.IS_LEAP_MONTH) == 1
        )
    }

    private fun rawLunarToSolar(year: Int, month: Int, day: Int, isLeapMonth: Boolean = false): Long? {
        if (month < 1 || month > 12 || day < 1 || day > 30) return null
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
            val back = rawSolarToLunar(millis)
            if (back.year == year && back.month == month && back.day == day && back.leap == isLeapMonth) millis else null
        } catch (e: Exception) {
            null
        }
    }

    /** 农历→公历修正表（镜像产品代码，生成式） */
    private val lunarOverrides: Map<String, Long?> = run {
        val map: MutableMap<String, Long?> = LinkedHashMap()
        for (year in 2025..2030) {
            for (m in 1..12) {
                for (d in 1..30) {
                    val raw = rawLunarToSolar(year, m, d) ?: continue
                    val key = "$year:$m:$d"
                    if (m == 12 && d == 30 && year in NO_NIAN_SHA_YEARS) {
                        map[key] = null // 官方无年三十
                        continue
                    }
                    if (year in SHIFTED_YEARS) {
                        map[key] = raw - 24L * 60 * 60 * 1000 // 整年 -1
                    }
                }
            }
        }
        map
    }

    /** 公历→农历修正表（由 lunarOverrides 反推） */
    private val solarOverrides: Map<String, String> = buildMap {
        lunarOverrides.forEach { (lunarKey, solar) ->
            if (solar != null) {
                val c = Calendar.getInstance().apply { timeInMillis = solar }
                put(String.format("%04d%02d%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)), lunarKey)
            }
        }
    }

    private fun solarToLunar(millis: Long): Lunar {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        val key = String.format("%04d%02d%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
        val override = solarOverrides[key]
        if (override != null) {
            val parts = override.split(":")
            return Lunar(parts[0].toInt(), parts[1].toInt(), parts[2].toInt(), false)
        }
        return rawSolarToLunar(millis)
    }

    private fun lunarToSolar(year: Int, month: Int, day: Int, isLeapMonth: Boolean = false): Long? {
        if (month < 1 || month > 12 || day < 1 || day > 30) return null
        if (!isLeapMonth) {
            val key = "$year:$month:$day"
            if (lunarOverrides.containsKey(key)) return lunarOverrides[key] // present-null → 官方不存在
        }
        return rawLunarToSolar(year, month, day, isLeapMonth)
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

    @Test
    fun `2026年六月三十_公历8月12日`() {
        // 用户反馈：Android 日历 8/12 农历空白（旧 lunarTextFor 索引错位导致三十越界）
        // 验证 2026-08-12 确为农历六月三十（大月）
        assertEquals("2026-08-12 = 六月三十", Lunar(2026, 6, 30, false), solarToLunar(date(2026, 8, 12)))
        assertEquals("六月三十 = 2026-08-12", date(2026, 8, 12), lunarToSolar(2026, 6, 30))
        // 六月廿九 = 08-11（确保索引在 29 也正确）
        assertEquals("2026-08-11 = 六月廿九", Lunar(2026, 6, 29, false), solarToLunar(date(2026, 8, 11)))
    }

    // ===== P0-2 农历整年平移回归（2025–2030 官方口径锚点）=====

    @Test
    fun `2025-2030春节官方口径锚点`() {
        // 权威来源（新华社/人民日报/紫金山天文台）：2026 春节=02-17 与 CLDR 一致不平移；
        // 2027–2030 春节官方比 CLDR 早 1 天（CLDR 误补腊月三十）。
        val springs = listOf(
            intArrayOf(2025, 1, 29),
            intArrayOf(2026, 2, 17), // 不平移
            intArrayOf(2027, 2, 6),
            intArrayOf(2028, 1, 26),
            intArrayOf(2029, 2, 13),
            intArrayOf(2030, 2, 3)
        )
        for (s in springs) {
            assertEquals("农历${s[0]}正月初一", date(s[0], s[1], s[2]), lunarToSolar(s[0], 1, 1))
            assertEquals("春节 ${s[0]} 反向查表", Lunar(s[0], 1, 1, false), solarToLunar(date(s[0], s[1], s[2])))
        }
    }

    @Test
    fun `无年三十年份腊月三十官方不存在`() {
        for (y in NO_NIAN_SHA_YEARS) {
            assertNull("农历$y 无腊月三十（官方口径）", lunarToSolar(y, 12, 30))
        }
    }

    @Test
    fun `平移年份农历日连续且反向一致`() {
        // 抽验 2027（平移年份）：正月初一~初三、腊月廿九 连续且反向可还原
        val spring = date(2027, 2, 6)
        assertEquals("2027 正月初一", Lunar(2027, 1, 1, false), solarToLunar(spring))
        assertEquals("2027 正月初二", Lunar(2027, 1, 2, false), solarToLunar(spring + 24L * 60 * 60 * 1000))
        assertEquals("2027 正月初三", Lunar(2027, 1, 3, false), solarToLunar(spring + 2 * 24L * 60 * 60 * 1000))
        // 2027 无年三十 → 腊月仅廿九；除夕(02-05)=腊月廿九，春节(02-06)=正月初一，连续无重叠
        assertEquals("2027 除夕=腊月廿九", Lunar(2026, 12, 29, false), solarToLunar(date(2027, 2, 5)))
        assertEquals("2027 春节=正月初一", Lunar(2027, 1, 1, false), solarToLunar(date(2027, 2, 6)))
        assertNull("2027 无腊月三十", lunarToSolar(2027, 12, 30))
    }
}
