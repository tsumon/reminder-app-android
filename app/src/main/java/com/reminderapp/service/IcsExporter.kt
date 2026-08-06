package com.reminderapp.service

import com.reminderapp.data.entity.ReminderEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * .ics 日历导出（v1.8.7 任务④）— 镜像 iOS IcsExporter.swift
 *
 * 把提醒导出为 iCalendar（RFC 5545）VEVENT，供系统日历/Outlook/Google 日历订阅。
 * RRULE 映射（双端一致）：
 *   once → 无 RRULE；daily → FREQ=DAILY；weekly → FREQ=WEEKLY；
 *   biweekly → FREQ=WEEKLY;INTERVAL=2；monthly → FREQ=MONTHLY；
 *   quarterly → FREQ=MONTHLY;INTERVAL=3；yearly → FREQ=YEARLY；
 *   custom → FREQ=DAILY;INTERVAL=N
 *   rule 类(第N周周X) → FREQ=对应周期;BYDAY=周几;BYSETPOS=N
 */
object IcsExporter {

    fun generateIcs(reminders: List<ReminderEntity>): String {
        val lines = mutableListOf(
            "BEGIN:VCALENDAR",
            "VERSION:2.0",
            "PRODID:-//ReminderApp//循环提醒//CN",
            "CALSCALE:GREGORIAN"
        )

        val df = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US).apply { timeZone = java.util.TimeZone.getDefault() }

        reminders.filter { it.isActive }.forEach { r ->
            lines += "BEGIN:VEVENT"
            lines += "UID:reminder-${r.id}@reminder.app"
            lines += "DTSTAMP:${df.format(Date())}"
            lines += "DTSTART:${df.format(Date(r.nextTriggerAt))}"
            lines += "SUMMARY:${escape(r.title)}"
            if (r.note.isNotEmpty()) lines += "DESCRIPTION:${escape(r.note)}"
            rrule(r)?.let { lines += "RRULE:$it" }
            lines += "END:VEVENT"
        }

        lines += "END:VCALENDAR"
        return lines.joinToString("\r\n")
    }

    private fun rrule(r: ReminderEntity): String? = when (r.kind) {
        "cycle" -> when (r.cycle) {
            "once" -> null
            "daily" -> "FREQ=DAILY"
            "weekly" -> "FREQ=WEEKLY"
            "biweekly" -> "FREQ=WEEKLY;INTERVAL=2"
            "monthly" -> "FREQ=MONTHLY"
            "quarterly" -> "FREQ=MONTHLY;INTERVAL=3"
            "yearly" -> "FREQ=YEARLY"
            "custom" -> "FREQ=DAILY;INTERVAL=${r.customDays.coerceAtLeast(1)}"
            else -> null
        }
        "rule" -> {
            val byday = weekdayCode(r.ruleWeekday ?: 1)
            val interval = when (r.rulePeriod) {
                "monthly" -> "FREQ=MONTHLY"
                "quarterly" -> "FREQ=MONTHLY;INTERVAL=3"
                "yearly" -> "FREQ=YEARLY"
                else -> "FREQ=MONTHLY"
            }
            "$interval;BYDAY=$byday;BYSETPOS=${r.ruleWeek ?: 1}"
        }
        // date 类（生日/节假日）：单次事件，无 RRULE
        else -> null
    }

    /** 周几 1=周一..7=周日 → iCal 周几代码 */
    private fun weekdayCode(wd: Int): String =
        arrayOf("MO", "TU", "WE", "TH", "FR", "SA", "SU").getOrElse(wd - 1) { "MO" }

    /** RFC 5545 文本转义 */
    private fun escape(text: String): String = text
        .replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\r\n", "\\n")
        .replace("\n", "\\n")
}
