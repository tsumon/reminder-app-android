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
            else -> now + 86400_000L
        }
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
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)

        val targetMs = when (reminder.dateType) {
            "solar_birthday" -> {
                cal.set(currentYear, (reminder.targetMonth ?: 1) - 1, reminder.targetDay ?: 1, 9, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "lunar_birthday" -> {
                val solar = LunarCalendar.lunarToSolar(currentYear, reminder.targetMonth ?: 1, reminder.targetDay ?: 1)
                val triggerTime = reminder.firstTriggerAt
                if (solar != null) {
                    val triggerCal = Calendar.getInstance().apply { timeInMillis = triggerTime }
                    val solarCal = Calendar.getInstance().apply { timeInMillis = solar }
                    solarCal.set(Calendar.HOUR_OF_DAY, triggerCal.get(Calendar.HOUR_OF_DAY))
                    solarCal.set(Calendar.MINUTE, triggerCal.get(Calendar.MINUTE))
                    solarCal.set(Calendar.SECOND, 0)
                    solarCal.set(Calendar.MILLISECOND, 0)
                    solarCal.timeInMillis
                } else {
                    cal.timeInMillis + 31536000_000L // fallback: 一年后
                }
            }
            "holiday" -> {
                val holiday = HolidayService.allHolidays.find { it.name == reminder.holidayName }
                if (holiday != null) {
                    HolidayService.getHolidaySolarDate(currentYear, holiday) ?: (now + 31536000_000L)
                } else {
                    now + 31536000_000L
                }
            }
            else -> now + 31536000_000L
        }

        // 如果今年已经过了，算明年的
        return if (targetMs <= now) {
            cal.set(Calendar.YEAR, currentYear + 1)
            when (reminder.dateType) {
                "solar_birthday" -> {
                    cal.set(Calendar.MONTH, (reminder.targetMonth ?: 1) - 1)
                    cal.set(Calendar.DAY_OF_MONTH, reminder.targetDay ?: 1)
                }
                else -> {} // 农历需要递归，简化处理
            }
            cal.set(Calendar.HOUR_OF_DAY, 9)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        } else {
            targetMs
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
}
