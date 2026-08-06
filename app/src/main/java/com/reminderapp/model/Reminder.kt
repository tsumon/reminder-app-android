package com.reminderapp.model

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
    MONTHLY("每月"),
    QUARTERLY("每季度"),
    YEARLY("每年")
}

// 周一=1 ... 周日=7
val weekdayLabels = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
val weekLabels = arrayOf("第1周", "第2周", "第3周", "第4周", "第5周")

enum class Cycle(val label: String) {
    ONCE("仅一次"),
    DAILY("每天"),
    WEEKLY("每周"),
    BIWEEKLY("每两周"),
    MONTHLY("每月"),
    QUARTERLY("每季度"),
    YEARLY("每年"),
    CUSTOM("自定义天数")
}

enum class DateReminderType(val label: String) {
    SOLAR_BIRTHDAY("新历生日"),
    LUNAR_BIRTHDAY("农历生日"),
    HOLIDAY("节假日")
}

enum class ReminderStatus { IDLE, PENDING, NOTIFYING, CONFIRMED }

data class ReminderRecord(
    val id: Long,
    val reminderId: Long,
    val action: String,
    val timestamp: Long
)

// 节假日数据类
data class Holiday(
    val name: String,
    val month: Int,
    val day: Int,
    val isLunar: Boolean
)
