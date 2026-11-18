package com.reminderapp.ui.screen.editor

import android.content.Context
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zh
import com.reminderapp.model.Cycle
import com.reminderapp.model.DateReminderType
import com.reminderapp.model.RulePeriod
import com.reminderapp.service.HolidayService
import com.reminderapp.service.NaturalDateParser
import com.reminderapp.service.ReminderEditorMapper
import com.reminderapp.service.ReminderEngine
import java.util.Calendar

data class ParsedNlFormUpdate(
    val title: String,
    val triggerYear: Int,
    val triggerMonth: Int,
    val triggerDay: Int,
    val triggerHour: Int,
    val triggerMinute: Int,
    val isDateMode: Boolean,
    val isRuleMode: Boolean,
    val selectedCycle: Cycle,
    val selectedDateType: DateReminderType?,
    val targetMonth: Int?,
    val targetDay: Int?,
    val weeklyWeekday: Int?,
    val holidayAware: Boolean,
    val dualBirthday: Boolean,
    val hint: String
)

fun buildEditorFirstTriggerAt(
    selectedCycle: Cycle,
    isDateMode: Boolean,
    isRuleMode: Boolean,
    targetDay: Int,
    weeklyWeekday: Int,
    triggerYear: Int,
    triggerMonth: Int,
    triggerDay: Int,
    triggerHour: Int,
    triggerMinute: Int
): Long {
    val cal = Calendar.getInstance()
    if (selectedCycle == Cycle.MONTHLY && !isDateMode && !isRuleMode && targetDay in 1..31) {
        var tries = 0
        while (tries < 12) {
            val maxD = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            if (targetDay <= maxD) {
                cal.set(Calendar.DAY_OF_MONTH, targetDay)
                cal.set(Calendar.HOUR_OF_DAY, triggerHour)
                cal.set(Calendar.MINUTE, triggerMinute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                if (cal.timeInMillis > System.currentTimeMillis()) return cal.timeInMillis
            }
            cal.add(Calendar.MONTH, 1)
            tries++
        }
    }
    cal.set(triggerYear, triggerMonth - 1, triggerDay, triggerHour, triggerMinute, 0)
    cal.set(Calendar.MILLISECOND, 0)
    if (selectedCycle == Cycle.WEEKLY && weeklyWeekday in 1..7) {
        val cur = ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7) + 1
        var diff = (weeklyWeekday - cur + 7) % 7
        if (diff == 0 && cal.timeInMillis <= System.currentTimeMillis()) diff = 7
        cal.add(Calendar.DAY_OF_MONTH, diff)
    }
    return cal.timeInMillis
}

fun buildSavedReminderPair(
    context: Context,
    initial: ReminderEntity?,
    scheduleChanged: Boolean,
    isDateMode: Boolean,
    isRuleMode: Boolean,
    selectedCycle: Cycle,
    customDays: Int,
    weeklyWeekday: Int,
    selectedDateType: DateReminderType?,
    targetMonth: Int,
    targetDay: Int,
    selectedHolidayName: String?,
    advanceDays: Int,
    rulePeriod: RulePeriod,
    ruleWeek: Int,
    ruleWeekday: Int,
    triggerHour: Int,
    triggerMinute: Int,
    priority: String,
    title: String,
    note: String,
    firstTrigger: Long,
    isCritical: Boolean,
    holidayAware: Boolean,
    policy: ReminderEntity,
    nlDualBirthday: Boolean,
    nlText: String
): Pair<ReminderEntity, ReminderEntity?> {
    val entity = if (isDateMode) {
        ReminderEntity(
            kind = "date",
            cycle = "yearly",
            dateType = selectedDateType?.name?.lowercase(),
            targetMonth = targetMonth,
            targetDay = targetDay,
            holidayName = selectedHolidayName,
            holidayId = HolidayService.allHolidays.find { it.name == selectedHolidayName }?.id,
            advanceDays = advanceDays,
            reminderHour = triggerHour,
            reminderMinute = triggerMinute,
            priority = priority,
            title = title.trim(),
            note = note.trim(),
            firstTriggerAt = firstTrigger,
            nextTriggerAt = firstTrigger,
            isCritical = isCritical,
            holidayAware = holidayAware
        )
    } else if (isRuleMode) {
        ReminderEntity(
            kind = "rule",
            cycle = "monthly",
            rulePeriod = rulePeriod.name.lowercase(),
            ruleWeek = ruleWeek,
            ruleWeekday = ruleWeekday,
            reminderHour = triggerHour,
            reminderMinute = triggerMinute,
            priority = priority,
            title = title.trim(),
            note = note.trim(),
            firstTriggerAt = firstTrigger,
            nextTriggerAt = firstTrigger,
            isCritical = isCritical,
            holidayAware = holidayAware
        )
    } else {
        ReminderEntity(
            kind = "cycle",
            cycle = selectedCycle.name.lowercase(),
            customDays = customDays,
            priority = priority,
            title = title.trim(),
            note = note.trim(),
            firstTriggerAt = firstTrigger,
            nextTriggerAt = firstTrigger,
            isCritical = isCritical,
            holidayAware = holidayAware,
            weeklyWeekday = if (selectedCycle == Cycle.WEEKLY) weeklyWeekday.takeIf { it in 1..7 } else null
        )
    }
    val baseTitle = title.trim()
    val cleanTitle = baseTitle.removeSuffix("（公历）").removeSuffix("(公历)").trim().ifBlank { baseTitle }
    val lunarPair = if (nlDualBirthday) parseLunarMonthDayFromText(nlText) else null
    val isDualSave = nlDualBirthday && entity.kind == "date" && entity.dateType == "solar_birthday" &&
        lunarPair != null && (initial == null || initial.id <= 0L)
    val primaryBase = if (isDualSave && !baseTitle.endsWith("（公历）")) "$cleanTitle（公历）" else baseTitle
    val proposed = entity.copy(
        title = primaryBase,
        reminderHour = triggerHour,
        reminderMinute = triggerMinute,
        recurrenceMode = policy.recurrenceMode,
        reminderStyle = policy.reminderStyle,
        isCritical = policy.reminderStyle == "important",
        retryIntervalsMinutes = policy.retryIntervalsMinutes,
        maxReminderAttempts = policy.maxReminderAttempts,
        quietStartMinute = policy.quietStartMinute,
        quietEndMinute = policy.quietEndMinute
    )
    val mappedPrimary = ReminderEditorMapper.map(
        initial,
        proposed,
        scheduleChanged,
        System.currentTimeMillis()
    ) { reminder, from ->
        ReminderEngine.calculateNextTrigger(reminder, context, from)
    }
    val extraLunar = if (isDualSave && lunarPair != null) {
        val (lmM, lmD) = lunarPair
        val lunarDraft = proposed.copy(
            id = 0L,
            syncId = null,
            title = "$cleanTitle（农历）",
            dateType = "lunar_birthday",
            targetMonth = lmM,
            targetDay = lmD
        )
        val lunarTrigger = ReminderEngine.calculateNextTrigger(lunarDraft, context)
        lunarDraft.copy(firstTriggerAt = lunarTrigger, nextTriggerAt = lunarTrigger)
    } else null

    return mappedPrimary to extraLunar
}

fun parseEditorNaturalLanguage(
    nlText: String,
    existingTitle: String,
    currentDateType: DateReminderType?,
    isNewReminder: Boolean
): ParsedNlFormUpdate? {
    val p = NaturalDateParser.parse(nlText) ?: return null
    val c = Calendar.getInstance().apply { timeInMillis = p.nextTriggerAt }
    var dateMode = false
    var cycle = Cycle.ONCE
    var dateType = currentDateType
    when (p.repeatMode) {
        "lunar" -> {
            dateMode = true
            dateType = DateReminderType.LUNAR_BIRTHDAY
        }
        "yearly" -> {
            if (p.dateType == "solar_birthday") {
                dateMode = true
                dateType = DateReminderType.SOLAR_BIRTHDAY
            } else {
                cycle = Cycle.YEARLY
            }
        }
        "daily" -> cycle = Cycle.DAILY
        "weekly" -> cycle = Cycle.WEEKLY
        "quarterly" -> cycle = Cycle.QUARTERLY
        "biweekly" -> cycle = Cycle.BIWEEKLY
        "monthly" -> cycle = Cycle.MONTHLY
        else -> cycle = Cycle.ONCE
    }
    val cycleText = when (p.repeatMode) {
        "lunar" -> zh("农历每年")
        "yearly" -> zh("每年")
        "daily" -> zh("每天")
        "weekly" -> zh("每周")
        "biweekly" -> zh("每两周")
        "quarterly" -> zh("每季度")
        "monthly" -> zh("每月")
        else -> zh("仅一次")
    }
    val hasSolar = Regex("""(\d{1,2}|[正冬腊一二三四五六七八九十]{1,3})\s*月\s*(\d{1,2}|[初十廿卅一二三四五六七八九]{1,3})\s*[号日]""")
        .containsMatchIn(nlText)
    val hasLunar = parseLunarMonthDayFromText(nlText) != null
    val dual = hasSolar && hasLunar && isNewReminder
    val hint = "「${p.title}」· $cycleText · ${p.label}" +
        (if (dual) " · " + zh("检测到新旧历两个生日，将创建两条") else "")

    return ParsedNlFormUpdate(
        title = existingTitle.ifBlank { p.title },
        triggerYear = c.get(Calendar.YEAR),
        triggerMonth = c.get(Calendar.MONTH) + 1,
        triggerDay = c.get(Calendar.DAY_OF_MONTH),
        triggerHour = c.get(Calendar.HOUR_OF_DAY),
        triggerMinute = c.get(Calendar.MINUTE),
        isDateMode = dateMode,
        isRuleMode = false,
        selectedCycle = cycle,
        selectedDateType = dateType,
        targetMonth = p.targetMonth,
        targetDay = p.targetDay,
        weeklyWeekday = p.weekday,
        holidayAware = p.holidayAware,
        dualBirthday = dual,
        hint = hint
    )
}

fun parseLunarMonthDayFromText(text: String): Pair<Int, Int>? {
    val m = Regex("""(?:农历|旧历|阴历)\s*(?:闰)?\s*([正冬腊一二三四五六七八九十\d]{1,3})\s*月\s*([初十廿卅一二三四五六七八九\d]{1,3})\s*[日号]?""")
        .find(text) ?: return null
    val month = parseChineseNumeral(m.groupValues[1], isMonth = true)?.coerceIn(1, 12) ?: return null
    val day = parseChineseNumeral(m.groupValues[2], isMonth = false)?.coerceIn(1, 30) ?: return null
    return month to day
}

private fun parseChineseNumeral(raw: String, isMonth: Boolean): Int? {
    val token = raw.trim()
    token.toIntOrNull()?.let { return it }
    if (isMonth) {
        when (token) {
            "正" -> return 1
            "冬" -> return 11
            "腊" -> return 12
        }
    }
    val digits = mapOf(
        '零' to 0, '一' to 1, '二' to 2, '三' to 3, '四' to 4,
        '五' to 5, '六' to 6, '七' to 7, '八' to 8, '九' to 9
    )
    if (token == "十" || token == "初十") return 10
    if (token == "二十") return 20
    if (token == "三十" || token == "卅") return 30
    if (token.startsWith("初") && token.length == 2) return digits[token[1]]
    if (token.startsWith("廿")) {
        if (token.length == 1) return 20
        if (token.length == 2) return 20 + (digits[token[1]] ?: return null)
    }
    if (token.startsWith("十") && token.length == 2) return 10 + (digits[token[1]] ?: return null)
    if (token.startsWith("二十") && token.length == 3) return 20 + (digits[token[2]] ?: return null)
    return if (token.length == 1) digits[token[0]] else null
}
