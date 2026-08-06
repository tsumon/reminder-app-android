package com.reminderapp.service

import android.icu.util.ChineseCalendar

/**
 * 农历日历转换 — 基于 Android 系统内置 ICU 历法（android.icu.util.ChineseCalendar）
 *
 * 「系统时间为主」：不再维护手写 1900-2100 查表数据，直接用系统历法引擎计算。
 * 好处：
 *   - 覆盖范围由系统保证（远大于 1900-2100），无查表边界 bug；
 *   - 闰月、大小月等规则与系统日历一致，消除此前手写算法的偏移误差；
 *   - 纯本地计算，离线可用（联网获取仅作为后续节假日的可选增强）。
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
     * 将农历月日转换为当年公历日期（毫秒时间戳，当天 00:00）
     * 若该农历日期不存在（如某年无此闰月）返回 null
     */
    fun lunarToSolar(year: Int, month: Int, day: Int): Long? {
        if (month < 1 || month > 12 || day < 1 || day > 30) return null
        return try {
            val cc = ChineseCalendar()
            cc.clear() // 清掉默认「当前时间」字段，避免污染
            cc.set(ChineseCalendar.EXTENDED_YEAR, year + EXTENDED_YEAR_OFFSET)
            cc.set(ChineseCalendar.MONTH, month - 1) // 0-based；未设置 IS_LEAP_MONTH，默认普通月
            cc.set(ChineseCalendar.DAY_OF_MONTH, day)
            cc.set(ChineseCalendar.HOUR_OF_DAY, 0)
            cc.set(ChineseCalendar.MINUTE, 0)
            cc.set(ChineseCalendar.SECOND, 0)
            cc.set(ChineseCalendar.MILLISECOND, 0)

            val millis = cc.timeInMillis

            // 往返校验：ICU 对不存在的日期（如该年没有这个普通月）会自动进位到别的日期，
            // 必须反查确认「我设的农历月日」确实落在 millis 当天，否则返回 null。
            val back = solarToLunar(millis) ?: return null
            if (back.year == year && back.month == month && back.day == day && !back.isLeapMonth) {
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
