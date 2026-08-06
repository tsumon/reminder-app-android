package com.reminderapp.service

import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.model.Holiday
import com.reminderapp.model.ReminderKind
import com.reminderapp.model.ReminderStatus
import java.util.*

/**
 * 提醒引擎 — 周期计算、确认处理、递增重试、遗漏检查
 * 镜像 iOS ReminderEngine.swift
 */
object ReminderEngine {

    /**
     * 递增重试间隔（毫秒）
     */
    private val retryIntervals = listOf(
        3600_000L,      // 1 小时
        14400_000L,     // 4 小时
        43200_000L,     // 12 小时
        86400_000L      // 每天
    )

    /**
     * 根据提醒配置计算下次触发时间
     */
    fun calculateNextTrigger(reminder: ReminderEntity): Long {
        val now = System.currentTimeMillis()

        return when (reminder.kind) {
            "cycle" -> calculateCycleNextTrigger(reminder, now)
            "date" -> calculateDateNextTrigger(reminder, now)
            "rule" -> calculateRuleNextTrigger(reminder, now)
            else -> now + 86400_000L
        }
    }

    /**
     * 规则提醒：每月/每季度/每年 第 N 周 周 X
     * 例如「每季度第二周周二」→ rulePeriod=quarterly, ruleWeek=2, ruleWeekday=2
     */
    private fun calculateRuleNextTrigger(reminder: ReminderEntity, now: Long): Long {
        val period = reminder.rulePeriod ?: "quarterly"
        val week = (reminder.ruleWeek ?: 1).coerceIn(1, 5)
        val weekday = (reminder.ruleWeekday ?: 1).coerceIn(1, 7) // 1=周一 ... 7=周日
        val hour = reminder.reminderHour.coerceIn(0, 23)
        val minute = reminder.reminderMinute.coerceIn(0, 59)

        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1

        val monthStep = when (period) {
            "monthly" -> 1
            "yearly" -> 12
            else -> 3 // quarterly
        }
        // 当前周期单位的起始月
        val startMonth = when (period) {
            "monthly" -> month
            "yearly" -> 1
            else -> ((month - 1) / 3) * 3 + 1 // 1月/4月/7月/10月
        }

        // 从当前周期开始向后找（最多 16 步，覆盖多年）
        for (i in 0..16) {
            val candMonth = startMonth + i * monthStep
            val candYear = year + (candMonth - 1) / 12
            val m = (candMonth - 1) % 12 + 1

            val target = ruleDateInMonth(candYear, m, week, weekday, hour, minute) ?: continue
            if (target > now) return target
        }
        return now + 86400_000L // fallback：明天
    }

    /**
     * 计算某年某月「第 week 周的 weekday」的日期时间戳
     * 如果该月不存在第 N 个该星期（如第 5 周溢出），返回 null
     */
    private fun ruleDateInMonth(
        year: Int, month: Int, week: Int, weekday: Int, hour: Int, minute: Int
    ): Long? {
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(year, month - 1, 1, hour, minute, 0)

        // Calendar.DAY_OF_WEEK: 1=周日...7=周六 → 统一为 周一=1...周日=7
        val firstDayWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1
        val day = 1 + ((weekday - firstDayWeek + 7) % 7) + (week - 1) * 7

        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        if (day > maxDay) return null

        cal.set(Calendar.DAY_OF_MONTH, day)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * 周期提醒：锚点法防漂移
     */
    private fun calculateCycleNextTrigger(reminder: ReminderEntity, now: Long): Long {
        val anchor = reminder.firstTriggerAt
        var nextTime = anchor
        val interval = getCycleIntervalMs(reminder.cycle, reminder.customDays)

        while (nextTime <= now) {
            nextTime += interval
        }

        return nextTime
    }

    private fun getCycleIntervalMs(cycle: String, customDays: Int): Long {
        return when (cycle) {
            "daily" -> 86400_000L
            "weekly" -> 604800_000L
            "biweekly" -> 1209600_000L
            "monthly" -> 2592000_000L
            "quarterly" -> 7776000_000L
            "yearly" -> 31536000_000L
            "custom" -> customDays * 86400_000L
            else -> 604800_000L
        }
    }

    /**
     * 日期提醒：根据类型计算当年触发时间
     */
    private fun calculateDateNextTrigger(reminder: ReminderEntity, now: Long): Long {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return computeDateForYear(reminder, currentYear, now)
            ?: computeDateForYear(reminder, currentYear + 1, now)
            ?: (now + 31536000_000L)
    }

    /**
     * 计算某年某日期提醒的触发时间戳（要求大于 now）；年内已过的返回 null
     */
    private fun computeDateForYear(reminder: ReminderEntity, year: Int, now: Long): Long? {
        val hour = reminder.reminderHour.coerceIn(0, 23)
        val minute = reminder.reminderMinute.coerceIn(0, 59)
        val cal = Calendar.getInstance()

        return when (reminder.dateType) {
            "solar_birthday" -> {
                cal.set(year, (reminder.targetMonth ?: 1) - 1, reminder.targetDay ?: 1, hour, minute, 0)
                cal.set(Calendar.MILLISECOND, 0)
                if (cal.timeInMillis > now) cal.timeInMillis else null
            }
            "lunar_birthday" -> {
                val solar = LunarCalendar.lunarToSolar(year, reminder.targetMonth ?: 1, reminder.targetDay ?: 1)
                if (solar != null) {
                    val solarCal = Calendar.getInstance().apply { timeInMillis = solar }
                    solarCal.set(Calendar.HOUR_OF_DAY, hour)
                    solarCal.set(Calendar.MINUTE, minute)
                    solarCal.set(Calendar.SECOND, 0)
                    solarCal.set(Calendar.MILLISECOND, 0)
                    if (solarCal.timeInMillis > now) solarCal.timeInMillis else null
                } else null
            }
            "holiday" -> {
                val holiday = HolidayService.allHolidays.find { it.name == reminder.holidayName }
                if (holiday != null) {
                    val d = HolidayService.getHolidaySolarDate(year, holiday)
                    if (d != null) {
                        val dc = Calendar.getInstance().apply { timeInMillis = d }
                        dc.set(Calendar.HOUR_OF_DAY, hour)
                        dc.set(Calendar.MINUTE, minute)
                        dc.set(Calendar.SECOND, 0)
                        dc.set(Calendar.MILLISECOND, 0)
                        if (dc.timeInMillis > now) dc.timeInMillis else null
                    } else null
                } else null
            }
            else -> null
        }
    }

    /**
     * 日期提醒：判断是否在提前预告期内，返回应发送预告的天数
     */
    fun getAdvanceDaysToNotify(reminder: ReminderEntity, now: Long): List<Int> {
        if (reminder.kind != "date") return emptyList()

        val targetMs = calculateDateNextTrigger(reminder, now)
        val daysUntil = ((targetMs - now) / 86400_000L).toInt()

        return (reminder.advanceDays downTo 1).filter { it == daysUntil }
    }

    /**
     * 确认完成 → 周期前进
     */
    fun confirm(reminder: ReminderEntity): ReminderEntity {
        val nextTrigger = calculateNextTrigger(reminder)
        return reminder.copy(
            status = ReminderStatus.PENDING.name.lowercase(),
            nextTriggerAt = nextTrigger,
            lastConfirmedAt = System.currentTimeMillis(),
            retryCount = 0
        )
    }

    /**
     * 稍后提醒 → 15 分钟后重推
     */
    fun snooze(reminder: ReminderEntity): ReminderEntity {
        return reminder.copy(
            status = ReminderStatus.NOTIFYING.name.lowercase(),
            nextTriggerAt = System.currentTimeMillis() + 900_000L, // 15 分钟
            retryCount = reminder.retryCount + 1
        )
    }

    /**
     * 递增重试（未确认时自动调度）
     */
    fun escalate(reminder: ReminderEntity): ReminderEntity {
        val idx = minOf(reminder.retryCount, retryIntervals.size - 1)
        return reminder.copy(
            status = ReminderStatus.NOTIFYING.name.lowercase(),
            nextTriggerAt = System.currentTimeMillis() + retryIntervals[idx],
            retryCount = reminder.retryCount + 1
        )
    }

    /**
     * 开机/重启后检查遗漏提醒
     */
    fun checkMissed(reminders: List<ReminderEntity>, now: Long): List<ReminderEntity> {
        return reminders.filter { reminder ->
            reminder.nextTriggerAt <= now && reminder.status != ReminderStatus.CONFIRMED.name.lowercase()
        }
    }

    /**
     * 判断提醒是否在指定公历日期（year/month/day，month 1-based）触发。
     * 用于日历标记与「点击某天查看当日任务」。
     */
    fun occursOn(reminder: ReminderEntity, year: Int, month: Int, day: Int): Boolean {
        return when (reminder.kind) {
            "cycle" -> occursOnCycle(reminder, year, month, day)
            "date" -> occursOnDate(reminder, year, month, day)
            "rule" -> occursOnRule(reminder, year, month, day)
            else -> false
        }
    }

    private fun startOfDay(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(year, month - 1, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun daysBetween(y1: Int, m1: Int, d1: Int, y2: Int, m2: Int, d2: Int): Int {
        return ((startOfDay(y2, m2, d2) - startOfDay(y1, m1, d1)) / 86400_000L).toInt()
    }

    private fun weekdayMondayBased(year: Int, month: Int, day: Int): Int {
        val cal = Calendar.getInstance().apply { timeInMillis = startOfDay(year, month, day) }
        return (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1 // 1=周一..7=周日
    }

    private fun occursOnCycle(reminder: ReminderEntity, year: Int, month: Int, day: Int): Boolean {
        val anchor = Calendar.getInstance().apply { timeInMillis = reminder.firstTriggerAt }
        val aY = anchor.get(Calendar.YEAR)
        val aM = anchor.get(Calendar.MONTH) + 1
        val aD = anchor.get(Calendar.DAY_OF_MONTH)
        val aWeekday = weekdayMondayBased(aY, aM, aD)
        val targetWeekday = weekdayMondayBased(year, month, day)
        val diff = daysBetween(aY, aM, aD, year, month, day)
        if (diff < 0) return false

        return when (reminder.cycle) {
            "daily" -> true
            "weekly" -> targetWeekday == aWeekday
            "biweekly" -> targetWeekday == aWeekday && diff % 14 == 0
            "monthly" -> day == aD
            "quarterly" -> day == aD && (month - aM) % 3 == 0
            "yearly" -> month == aM && day == aD
            "custom" -> {
                val cd = reminder.customDays
                cd > 0 && targetWeekday == aWeekday && diff % cd == 0
            }
            else -> false
        }
    }

    private fun occursOnDate(reminder: ReminderEntity, year: Int, month: Int, day: Int): Boolean {
        return when (reminder.dateType) {
            "solar_birthday" -> month == reminder.targetMonth && day == reminder.targetDay
            "lunar_birthday" -> {
                val solar = LunarCalendar.lunarToSolar(year, reminder.targetMonth ?: 1, reminder.targetDay ?: 1)
                if (solar != null) {
                    val cal = Calendar.getInstance().apply { timeInMillis = solar }
                    cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) + 1 == month && cal.get(Calendar.DAY_OF_MONTH) == day
                } else false
            }
            "holiday" -> {
                val holiday = HolidayService.allHolidays.find { it.name == reminder.holidayName }
                if (holiday != null) {
                    val d = HolidayService.getHolidaySolarDate(year, holiday)
                    if (d != null) {
                        val cal = Calendar.getInstance().apply { timeInMillis = d }
                        cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) + 1 == month && cal.get(Calendar.DAY_OF_MONTH) == day
                    } else false
                } else false
            }
            else -> false
        }
    }

    private fun occursOnRule(reminder: ReminderEntity, year: Int, month: Int, day: Int): Boolean {
        val period = reminder.rulePeriod ?: "quarterly"
        val week = (reminder.ruleWeek ?: 1).coerceIn(1, 5)
        val weekday = (reminder.ruleWeekday ?: 1).coerceIn(1, 7)
        val hour = reminder.reminderHour.coerceIn(0, 23)
        val minute = reminder.reminderMinute.coerceIn(0, 59)
        val target = ruleDateInMonth(year, month, week, weekday, hour, minute) ?: return false
        val cal = Calendar.getInstance().apply { timeInMillis = target }
        return cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) + 1 == month && cal.get(Calendar.DAY_OF_MONTH) == day
    }
}
