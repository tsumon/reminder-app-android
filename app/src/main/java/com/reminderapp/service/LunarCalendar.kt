package com.reminderapp.service

import java.util.*

/**
 * 农历日历转换 — 1900-2100 查表法，含闰月
 * 镜像 iOS LunarCalendar.swift
 */
object LunarCalendar {

    private const val START_YEAR = 1900
    private const val END_YEAR = 2100

    // 农历数据（每年 4 字节），共 201 年
    private val lunarInfo = intArrayOf(
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0,
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6,
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x05ac0, 0x0ab60, 0x096d5, 0x092e0,
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
        0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0,
        0x0a2e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4,
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0,
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160,
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a4d0, 0x0d150, 0x0f252,
        0x0d520
    )

    // 农历月份中文名
    private val lunarMonthNames = arrayOf(
        "", "正月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "冬月", "腊月"
    )

    // 农历日期中文名
    private val lunarDayNames = arrayOf(
        "", "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )

    /**
     * 将农历月日转换为当年公历日期
     * @return 公历日期的毫秒时间戳，如果该年没有该农历日期则返回 null
     */
    fun lunarToSolar(year: Int, month: Int, day: Int): Long? {
        if (year < START_YEAR || year > END_YEAR) return null
        if (month < 1 || month > 12) return null
        if (day < 1 || day > 30) return null

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, Calendar.JANUARY)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val baseDate = calendar.timeInMillis
        val offset = year - START_YEAR
        val info = lunarInfo[offset]

        // 闰月月份（0 表示无闰月）
        val leapMonth = (info and 0xf0000) shr 16

        // 遍历农历月份找到目标日
        var currentLunarMonth = 1
        var currentLunarDay = 1
        var daysElapsed = 0

        while (currentLunarMonth <= 12 || (leapMonth > 0 && currentLunarMonth <= leapMonth + 12)) {
            val actualMonth = if (currentLunarMonth > 12) currentLunarMonth - 12 else currentLunarMonth
            val isLeap = currentLunarMonth > 12
            val daysInMonth = getLunarMonthDays(year, actualMonth, isLeap)

            if ((isLeap && actualMonth == leapMonth) || (!isLeap && (actualMonth == month || (isLeap && leapMonth == month)))) {
                // 找到了目标月份
                if (currentLunarMonth == month || (isLeap && actualMonth == month)) {
                    val targetDays = daysElapsed + day
                    return baseDate + targetDays * 86400000L
                }
            }

            daysElapsed += daysInMonth
            currentLunarMonth++

            // 处理闰月
            if (leapMonth > 0 && currentLunarMonth == leapMonth + 1) {
                val leapDays = getLunarMonthDays(year, leapMonth, true)
                if (month == leapMonth && !leapMonth.equals(currentLunarMonth - 1)) {
                    // 目标就是闰月
                    val targetDays = daysElapsed + day
                    return baseDate + targetDays * 86400000L
                }
                daysElapsed += leapDays
            }
        }

        // 简化：有些边缘情况可能找不到，用估算
        return null
    }

    /**
     * 获取农历年某月天数
     */
    private fun getLunarMonthDays(year: Int, month: Int, isLeap: Boolean): Int {
        val offset = year - START_YEAR
        val info = lunarInfo[offset]

        val leapMonth = (info and 0xf0000) shr 16
        if (isLeap && month == leapMonth) {
            return if ((info and 0x10000) != 0) 30 else 29
        }

        return if ((info and (0x8000 shr (month - 1))) != 0) 30 else 29
    }

    /**
     * 农历年份总天数
     */
    private fun getLunarYearDays(year: Int): Int {
        val offset = year - START_YEAR
        var sum = 348 // 12个月 × 29天
        for (i in 0..11) {
            if ((lunarInfo[offset] and (0x8000 shr i)) != 0) sum++
        }
        // 闰月
        val leapMonth = (lunarInfo[offset] and 0xf0000) shr 16
        if (leapMonth != 0) {
            sum += if ((lunarInfo[offset] and 0x10000) != 0) 30 else 29
        }
        return sum
    }
}
