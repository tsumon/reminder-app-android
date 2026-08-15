package com.reminderapp.model
import com.reminderapp.i18n.zh

/**
 * 前端展示用的提醒模型（从 Entity 映射）
 */
data class Reminder(
    val id: Long,
    val kind: ReminderKind,
    val cycle: Cycle,
    val customDays: Int,
    val dateType: DateReminderType?,
    val targetMonth: Int?,
    val targetDay: Int?,
    val holidayName: String?,
    val advanceDays: Int,
    val rulePeriod: RulePeriod? = null,
    val ruleWeek: Int? = null,
    val ruleWeekday: Int? = null,
    val title: String,
    val note: String,
    val status: ReminderStatus,
    val firstTriggerAt: Long,
    val nextTriggerAt: Long,
    val lastConfirmedAt: Long?,
    val retryCount: Int,
    val createdAt: Long
)

enum class ReminderKind { CYCLE, DATE, RULE }

enum class RulePeriod(val label: String) {
    MONTHLY(zh("每月")),
    QUARTERLY(zh("每季度")),
    YEARLY(zh("每年"))
}

// 周一=1 ... 周日=7
val weekdayLabels = arrayOf(zh("周一"), zh("周二"), zh("周三"), zh("周四"), zh("周五"), zh("周六"), zh("周日"))
val weekLabels = arrayOf(zh("第1周"), zh("第2周"), zh("第3周"), zh("第4周"), zh("第5周"))

enum class Cycle(val label: String) {
    ONCE(zh("仅一次")),
    DAILY(zh("每天")),
    WEEKLY(zh("每周")),
    BIWEEKLY(zh("每两周")),
    MONTHLY(zh("每月")),
    QUARTERLY(zh("每季度")),
    YEARLY(zh("每年")),
    CUSTOM(zh("自定义天数"))
}

enum class DateReminderType(val label: String) {
    SOLAR_BIRTHDAY(zh("新历生日")),
    LUNAR_BIRTHDAY(zh("农历生日")),
    HOLIDAY(zh("节假日"))
}

enum class ReminderStatus { IDLE, PENDING, NOTIFYING, SNOOZED, OVERDUE, CONFIRMED }

data class ReminderRecord(
    val id: Long,
    val reminderId: Long,
    val action: String,
    val timestamp: Long
)

// 节假日数据类（id 与 iOS HolidayService 对齐，跨端协议用稳定 ID 识别）
data class Holiday(
    val id: String,
    val name: String,
    val month: Int,
    val day: Int,
    val isLunar: Boolean
)
