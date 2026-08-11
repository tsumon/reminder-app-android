package com.reminderapp.service

import com.reminderapp.data.entity.ReminderEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * .ics 日历导出（v1.8.7 任务④；v2.0.20 批次2 增强）— 镜像 iOS IcsExporter.swift
 *
 * 把提醒导出为 iCalendar（RFC 5545）VEVENT，供系统日历/Outlook/Google 日历导入。
 * 批次2 增强：补 DTEND（30 分钟时长）、X-WR-CALNAME / X-WR-TIMEZONE（时区正确导入）、
 * RFC 5545 行折叠（>75 字符折行，续行空格前缀，长标题/备注不截断）。
 * RRULE 映射（双端一致）：
 *   once → 无 RRULE；daily → FREQ=DAILY；weekly → FREQ=WEEKLY；
 *   biweekly → FREQ=WEEKLY;INTERVAL=2；monthly → FREQ=MONTHLY；
 *   quarterly → FREQ=MONTHLY;INTERVAL=3；yearly → FREQ=YEARLY；
 *   custom → FREQ=DAILY;INTERVAL=N
 *   rule 类(第N周周X) → FREQ=对应周期;BYDAY=周几;BYSETPOS=N
 */
object IcsExporter {

    private const val EVENT_DURATION_MINUTES = 30L

    fun generateIcs(reminders: List<ReminderEntity>): String {
        val timeZone = java.util.TimeZone.getDefault()
        val lines = mutableListOf(
            "BEGIN:VCALENDAR",
            "VERSION:2.0",
            "PRODID:-//ReminderApp//循环提醒//CN",
            "CALSCALE:GREGORIAN",
            "X-WR-CALNAME:循环提醒器",
            "X-WR-TIMEZONE:${timeZone.id}"
        )

        val df = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US).apply { this.timeZone = timeZone }
        // I9: DTSTAMP 用 UTC（带 Z），与带 TZID 的本地时间事件区分，避免跨时区设备误读
        val dfUtc = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
            this.timeZone = java.util.TimeZone.getTimeZone("UTC")
        }

        reminders.filter { it.isActive }.forEach { r ->
            lines += "BEGIN:VEVENT"
            lines += "UID:reminder-${r.id}@reminder.app"
            // I9: DTSTART/DTEND 带 TZID（与 X-WR-TIMEZONE 互补），跨时区订阅设备才能按本地时区还原事件，
            //     避免浮动本地时间被当成其它时区导致周期整体偏移（DST 也由 TZID 解析）
            lines += "DTSTAMP:${dfUtc.format(Date())}"
            lines += "DTSTART;TZID=${timeZone.id}:${df.format(Date(r.nextTriggerAt))}"
            lines += "DTEND;TZID=${timeZone.id}:${df.format(Date(r.nextTriggerAt + EVENT_DURATION_MINUTES * 60_000L))}"
            lines += "SUMMARY:${escape(r.title)}"
            if (r.note.isNotEmpty()) lines += "DESCRIPTION:${escape(r.note)}"
            rrule(r)?.let { lines += "RRULE:$it" }
            lines += "END:VEVENT"
        }

        lines += "END:VCALENDAR"

        // RFC 5545 §3.1：物理行最长 75 octets（不含 CRLF），超长需折行，续行以单个空格开头
        return lines.flatMap { foldLine(it) }.joinToString("\r\n")
    }

    /** RFC 5545 行折叠：按 UTF-8 字节数折到 ≤75（续行 ≤74，预留续行空格位） */
    private fun foldLine(line: String): List<String> {
        val bytes = line.toByteArray(Charsets.UTF_8)
        if (bytes.size <= 75) return listOf(line)

        val folded = mutableListOf<String>()
        var start = 0
        var first = true
        while (start < bytes.size) {
            val limit = if (first) 75 else 74
            var end = start + limit
            if (end >= bytes.size) {
                // 剩余不足一折行：收尾（续行仍带空格前缀）
                val tail = String(bytes, start, bytes.size - start, Charsets.UTF_8)
                folded += if (first) tail else " $tail"
                break
            }
            // 不能把 UTF-8 多字节字符截断：若断点落在续字节(10xxxxxx)，回退到完整字符前
            while (end > start && end < bytes.size && (bytes[end].toInt() and 0xC0) == 0x80) {
                end--
            }
            if (end <= start) end = start + 1 // 防御：单字节字符占满 limit 的极端情况
            val part = String(bytes, start, end - start, Charsets.UTF_8)
            folded += if (first) part else " $part"
            start = end
            first = false
        }
        return folded
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
