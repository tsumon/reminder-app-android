package com.reminderapp.service

import com.reminderapp.model.Holiday

/**
 * 节假日服务 — 内置 13 个中国节假日
 * 镜像 iOS HolidayService.swift
 */
object HolidayService {

    val allHolidays: List<Holiday> = listOf(
        Holiday("元旦", 1, 1, false),
        Holiday("春节", 1, 1, true),
        Holiday("元宵节", 1, 15, true),
        Holiday("清明节", 4, 5, false),
        Holiday("端午节", 5, 5, true),
        Holiday("七夕", 7, 7, true),
        Holiday("中秋节", 8, 15, true),
        Holiday("国庆节", 10, 1, false),
        Holiday("重阳节", 9, 9, true),
        Holiday("冬至", 12, 22, false),
        Holiday("腊八节", 12, 8, true),
        Holiday("小年", 12, 23, true),
        Holiday("除夕", 12, 30, true)
    )

    /**
     * 获取指定年份中节日的公历日期
     */
    fun getHolidaySolarDate(year: Int, holiday: Holiday): Long? {
        // 除夕 = 农历正月初一的前一天（即腊月最后一天）。
        // 2025–2029 连续无年三十，硬写 12/30 会得到错误日期（甚至跨到下一年），
        // 故统一取「春节 - 1 天」，自动落到腊月廿九。
        if (holiday.name == "除夕") {
            val springFestival = LunarCalendar.lunarToSolar(year, 1, 1) ?: return null
            return springFestival - 24L * 60 * 60 * 1000
        }
        return if (holiday.isLunar) {
            LunarCalendar.lunarToSolar(year, holiday.month, holiday.day)
        } else {
            val calendar = java.util.Calendar.getInstance()
            calendar.set(year, holiday.month - 1, holiday.day, 9, 0, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }
    }
}
