package com.reminderapp.service

import android.icu.util.ChineseCalendar

/**
 * 农历日历转换 — 基于 Android 系统内置 ICU 历法（android.icu.util.ChineseCalendar）
 *
 * 「系统时间为主」：不再维护手写 1900-2100 查表数据，直接用系统历法引擎计算。
 * 好处：覆盖范围由系统保证、闰月/大小月与系统日历一致、纯本地计算离线可用。
 *
 * ⚠️ 中国官方口径修正：ICU/CLDR 农历数据在个别年份与中国大陆官方口径
 * （紫金山天文台，新华社发布口径）有 1 天差异。例如农历 2026 年腊月，
 * ICU 认为有三十（→ 2027 春节 02-07），官方口径无年三十（2027 春节 02-06）。
 * 提醒 App 面向中国用户，春节等关键日期必须与官方一致，故用 [officialOverrides]
 * 做定向修正。新差异由回归测试（LunarCalendarRegressionTest）持续发现并补充。
 *
 * 对外接口与旧实现完全一致（lunarToSolar / solarToLunar / LunarDate），调用点零改动。
 */
object LunarCalendar {

    // ICU ChineseCalendar：农历年 = EXTENDED_YEAR - 2637
    private const val EXTENDED_YEAR_OFFSET = 2637

    /**
     * 农历日期结构
     */
    data class LunarDate(
        val year: Int,
        val month: Int,
        val day: Int,
        val isLeapMonth: Boolean
    ) {
        /** 中文描述，如「正月初一」「闰四月初五」 */
        val description: String
            get() {
                val monthNames = arrayOf("正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊")
                val dayNames = arrayOf(
                    "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
                    "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
                    "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
                )
                val prefix = if (isLeapMonth) "闰" else ""
                return "$prefix${monthNames[month - 1]}月${dayNames[day - 1]}"
            }
    }

    /**
     * 官方口径修正表（农历 → 公历）
     * key = "农历年:月:日"（普通月），value = 官方公历 (y, m, d)；value=null 表示官方不存在该日。
     * 目前 1 处：ICU 认为农历 2026 年腊月有三十、2027 春节 02-07；
     * 官方口径（新华社/紫金山天文台）2025-2029 连续 5 年无年三十，2027 春节 = 02-06。
     */
    private val lunarOverrides: Map<String, IntArray?> = mapOf(
        "2027:1:1" to intArrayOf(2027, 2, 6),
        "2026:12:30" to null // 官方无腊月三十
    )

    /**
     * 官方口径修正表（公历 → 农历），由 [lunarOverrides] 反推。
     * key = "yyyyMMdd"，value = "农历年:月:日"。
     */
    private val solarOverrides: Map<String, String> = buildMap {
        lunarOverrides.forEach { (lunarKey, solar) ->
            if (solar != null) {
                put(String.format("%04d%02d%02d", solar[0], solar[1], solar[2]), lunarKey)
            }
        }
    }

    /**
     * 将农历月日转换为当年公历日期（毫秒时间戳，当天 00:00）
     * [isLeapMonth] 指定闰月；若该农历日期不存在（如该年无此闰月/无此普通月）返回 null
     */
    fun lunarToSolar(year: Int, month: Int, day: Int, isLeapMonth: Boolean = false): Long? {
        if (month < 1 || month > 12 || day < 1 || day > 30) return null

        // 官方口径修正优先（仅普通月；闰月不存在于官方差异表中）
        if (!isLeapMonth) {
            val override = lunarOverrides["$year:$month:$day"]
            if (override != null) {
                val cal = java.util.Calendar.getInstance().apply {
                    set(override[0], override[1] - 1, override[2], 0, 0, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                return cal.timeInMillis
            }
            if (lunarOverrides.containsKey("$year:$month:$day")) return null
        }

        return try {
            val cc = ChineseCalendar()
            cc.clear() // 清掉默认「当前时间」字段，避免污染
            cc.set(ChineseCalendar.EXTENDED_YEAR, year + EXTENDED_YEAR_OFFSET)
            cc.set(ChineseCalendar.MONTH, month - 1) // 0-based
            cc.set(ChineseCalendar.IS_LEAP_MONTH, if (isLeapMonth) 1 else 0)
            cc.set(ChineseCalendar.DAY_OF_MONTH, day)
            cc.set(ChineseCalendar.HOUR_OF_DAY, 0)
            cc.set(ChineseCalendar.MINUTE, 0)
            cc.set(ChineseCalendar.SECOND, 0)
            cc.set(ChineseCalendar.MILLISECOND, 0)

            val millis = cc.timeInMillis

            // 往返校验：ICU 对不存在的日期会自动进位，反查确认后再返回
            val back = solarToLunar(millis) ?: return null
            if (back.year == year && back.month == month && back.day == day &&
                back.isLeapMonth == isLeapMonth
            ) {
                millis
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 将公历时间戳转换为农历日期
     */
    fun solarToLunar(timestamp: Long): LunarDate? {
        // 官方口径修正：ICU 把 2027-02-06 判为农历 2026 腊月三十，官方为正月初一
        val dateKey = try {
            val c = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
            String.format("%04d%02d%02d", c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH))
        } catch (e: Exception) { null }
        val override = dateKey?.let { solarOverrides[it] }
        if (override != null) {
            val parts = override.split(":")
            return LunarDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt(), false)
        }

        return try {
            val cc = ChineseCalendar()
            cc.timeInMillis = timestamp
            val year = cc.get(ChineseCalendar.EXTENDED_YEAR) - EXTENDED_YEAR_OFFSET
            val month = cc.get(ChineseCalendar.MONTH) + 1
            val day = cc.get(ChineseCalendar.DAY_OF_MONTH)
            val isLeap = cc.get(ChineseCalendar.IS_LEAP_MONTH) == 1
            LunarDate(year, month, day, isLeap)
        } catch (e: Exception) {
            null
        }
    }
}
