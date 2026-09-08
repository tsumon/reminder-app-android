package com.reminderapp.service

/**
 * 一键填入创建表单的提醒模板（报税 / 域名续期 / 生日）。
 */
data class ReminderTemplate(
    val id: String,
    val title: String,
    val note: String,
    val kind: Kind,
    val cycle: Cycle?,
    val holidayAware: Boolean,
    val dateType: DateKind?,
    val advanceDays: Int,
    val subtitle: String
) {
    enum class Kind { CYCLE, DATE }
    enum class Cycle { MONTHLY, YEARLY }
    enum class DateKind { SOLAR_BIRTHDAY, LUNAR_BIRTHDAY }

    companion object {
        val all: List<ReminderTemplate> = listOf(
            ReminderTemplate(
                id = "tax",
                title = "报税",
                note = "工作日提醒，遇周末或法定节假日顺延到下一个工作日。",
                kind = Kind.CYCLE,
                cycle = Cycle.MONTHLY,
                holidayAware = true,
                dateType = null,
                advanceDays = 0,
                subtitle = "每月 · 避开节假日"
            ),
            ReminderTemplate(
                id = "domain",
                title = "域名续期",
                note = "每年到期前开始提醒，可改提前天数。",
                kind = Kind.DATE,
                cycle = null,
                holidayAware = false,
                dateType = DateKind.SOLAR_BIRTHDAY,
                advanceDays = 30,
                subtitle = "每年 · 提前 30 天"
            ),
            ReminderTemplate(
                id = "birthday",
                title = "生日",
                note = "默认为公历生日，可改为农历。",
                kind = Kind.DATE,
                cycle = null,
                holidayAware = false,
                dateType = DateKind.SOLAR_BIRTHDAY,
                advanceDays = 3,
                subtitle = "公历每年 · 可改农历"
            )
        )
    }
}
