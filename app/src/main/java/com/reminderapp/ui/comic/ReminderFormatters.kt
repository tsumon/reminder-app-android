package com.reminderapp.ui.comic

import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed class WaitingRow {
    data class BirthdayPair(val solar: ReminderEntity, val lunar: ReminderEntity) : WaitingRow() {
        val nearest: ReminderEntity
            get() = if (solar.nextTriggerAt <= lunar.nextTriggerAt) solar else lunar
    }

    data class Single(val reminder: ReminderEntity) : WaitingRow()

    val key: Long
        get() = when (this) {
            is BirthdayPair -> solar.id
            is Single -> reminder.id
        }
}

fun pairWaitingRows(waiting: List<ReminderEntity>, selectionMode: Boolean): List<WaitingRow> {
    if (selectionMode) return waiting.map { WaitingRow.Single(it) }
    val solar = mutableMapOf<String, ReminderEntity>()
    val lunar = mutableMapOf<String, ReminderEntity>()
    val rest = mutableListOf<ReminderEntity>()
    for (r in waiting) {
        val t = r.title
        when {
            r.kind == "date" && r.dateType == "solar_birthday" && t.endsWith("（公历）") ->
                solar[t.removeSuffix("（公历）")] = r
            r.kind == "date" && r.dateType == "lunar_birthday" && t.endsWith("（农历）") ->
                lunar[t.removeSuffix("（农历）")] = r
            else -> rest.add(r)
        }
    }
    val rows = mutableListOf<WaitingRow>()
    val singles = mutableListOf<ReminderEntity>()
    for ((base, s) in solar) {
        val l = lunar.remove(base)
        if (l != null) rows.add(WaitingRow.BirthdayPair(s, l)) else singles.add(s)
    }
    singles.addAll(lunar.values)
    singles.addAll(rest)
    rows.addAll(singles.map { WaitingRow.Single(it) })
    return rows.sortedBy {
        when (it) {
            is WaitingRow.BirthdayPair -> minOf(it.solar.nextTriggerAt, it.lunar.nextTriggerAt)
            is WaitingRow.Single -> it.reminder.nextTriggerAt
        }
    }
}

fun formatTabularTime(timestamp: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))

fun formatShortDateTime(timestamp: Long): String =
    SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))

fun formatCompactEventTime(timestamp: Long): String {
    val nowCal = Calendar.getInstance()
    val targetCal = Calendar.getInstance().apply { timeInMillis = timestamp }
    val sameDay = nowCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) &&
        nowCal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR)
    if (sameDay) return formatTabularTime(timestamp)
    val tomorrowCal = (nowCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
    val isTomorrow = tomorrowCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) &&
        tomorrowCal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR)
    if (isTomorrow) return zh("明天") + " " + formatTabularTime(timestamp)
    return SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date(timestamp))
}

fun relativeDaysLabel(target: Long): String {
    val start = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val end = Calendar.getInstance().apply {
        timeInMillis = target
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val days = ((end.timeInMillis - start.timeInMillis) / 86_400_000L).toInt()
    return when {
        days < 0 -> zh("已逾期")
        days == 0 -> zh("今天")
        days == 1 -> zh("明天")
        days < 7 -> zhf("%d天后", days)
        else -> zhf("%d周后", (days / 7).coerceAtLeast(1))
    }
}

fun lunarMonthDayLabel(month: Int, day: Int): String {
    val monthNames = arrayOf("", "正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊")
    val dayNames = arrayOf(
        "", "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )
    val m = monthNames.getOrElse(month.coerceIn(1, 12)) { "" }
    val d = dayNames.getOrElse(day.coerceIn(1, 30)) { "${day}日" }
    return m + "月" + d
}

fun ruleSummaryLabel(reminder: ReminderEntity): String {
    val periodLabel = when (reminder.rulePeriod) {
        "monthly" -> zh("每月")
        "yearly" -> zh("每年")
        else -> zh("每季度")
    }
    val weekday = arrayOf(zh("周一"), zh("周二"), zh("周三"), zh("周四"), zh("周五"), zh("周六"), zh("周日"))
        .getOrElse(((reminder.ruleWeekday ?: 1) - 1).coerceIn(0, 6)) { zhf("周%s", reminder.ruleWeekday) }
    return zhf("%1\$s第%2\$s周%3\$s", periodLabel, reminder.ruleWeek ?: 1, weekday)
}

fun cycleSummaryLabel(reminder: ReminderEntity): String {
    return when {
        reminder.kind == "date" && reminder.dateType == "solar_birthday" ->
            zhf("%1\$s月%2\$s日", reminder.targetMonth ?: 1, reminder.targetDay ?: 1)
        reminder.kind == "date" && reminder.dateType == "lunar_birthday" ->
            zhf("农历 %s", lunarMonthDayLabel(reminder.targetMonth ?: 1, reminder.targetDay ?: 1))
        reminder.kind == "date" && reminder.dateType == "holiday" ->
            reminder.holidayName ?: zh("节假日")
        reminder.kind == "rule" -> ruleSummaryLabel(reminder)
        else -> when (reminder.cycle) {
            "once" -> zh("仅一次")
            "daily" -> zh("每天")
            "weekly" -> {
                val wd = reminder.weeklyWeekday?.takeIf { it in 1..7 }
                if (wd != null) {
                    val names = arrayOf(zh("周一"), zh("周二"), zh("周三"), zh("周四"), zh("周五"), zh("周六"), zh("周日"))
                    zh("每") + names[wd - 1]
                } else zh("每周")
            }
            "biweekly" -> zh("每两周")
            "monthly" -> zh("每月")
            "quarterly" -> zh("每季度")
            "yearly" -> zh("每年")
            "custom" -> zhf("每%s天", reminder.customDays.coerceAtLeast(1))
            else -> reminder.cycle
        }
    }
}
