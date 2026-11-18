package com.reminderapp.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.reminderapp.ReminderApp
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.data.entity.ReminderRecordEntity
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.model.ReminderStatus
import com.reminderapp.service.AILogStore
import com.reminderapp.service.NotificationManager
import com.reminderapp.service.ReminderActions
import com.reminderapp.service.ReminderEngine
import com.reminderapp.service.ReminderPolicy
import com.reminderapp.service.ReminderScheduler
import com.reminderapp.service.StatsService
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val pendingAIImportState = mutableStateOf<List<ReminderEntity>?>(null)

suspend fun commitAIImport(items: List<ReminderEntity>, database: AppDatabase) {
    ReminderActions(ReminderApp.instance, database).importItems(items)
}

fun describeAIReminder(e: ReminderEntity): String = when {
    e.kind == "date" && e.dateType == "holiday" -> e.holidayName ?: zh("节假日")
    e.kind == "date" -> zh("日期")
    else -> e.cycle
}

suspend fun executeAITool(
    name: String,
    argsJson: String,
    database: AppDatabase,
    scheduler: ReminderScheduler,
    notificationMgr: NotificationManager,
    gson: Gson
): String {
    val args: Map<String, Any?> = try {
        gson.fromJson(argsJson, object : TypeToken<Map<String, Any?>>() {}.type)
    } catch (_: Exception) {
        return zh("参数解析失败：工具参数不是合法 JSON，请检查后重试。")
    }

    return when (name) {
        "create_reminder" -> handleCreate(args, database)
        "list_reminders" -> handleList(database)
        "confirm_reminder" -> handleConfirm(args, database)
        "skip_reminder" -> handleSkip(args, database)
        "snooze_reminder" -> handleSnooze(args, database)
        "delete_reminder" -> handleDelete(args, database)
        "update_reminder" -> handleUpdate(args, database)
        "import_tasks" -> handleImportTasks(args)
        "get_stats_context" -> handleStatsContext(database)
        else -> zhf("未知工具: %s", name)
    }
}

private fun tryBuildEntity(args: Map<String, Any?>): Pair<ReminderEntity?, String?> {
    val title = (args["title"] as? String) ?: zh("未命名提醒")
    val kind = (args["kind"] as? String) ?: "cycle"
    val note = (args["note"] as? String) ?: ""
    val cycle = (args["cycle"] as? String) ?: "weekly"
    val customDays = (args["custom_days"] as? Double)?.toInt() ?: 0
    val dateType = args["date_type"] as? String
    val targetMonthRaw = (args["target_month"] as? Double)?.toInt()
        ?: (args["target_month"] as? Int)
        ?: (args["target_month"] as? String)?.toIntOrNull()
    val targetDayRaw = (args["target_day"] as? Double)?.toInt()
        ?: (args["target_day"] as? Int)
        ?: (args["target_day"] as? String)?.toIntOrNull()
    val targetMonth = targetMonthRaw ?: 0
    val targetDay = targetDayRaw ?: 0
    val advanceDays = (args["advance_days"] as? Double)?.toInt() ?: 3
    val reminderHour = (args["reminder_hour"] as? Double)?.toInt() ?: 9
    val reminderMinute = (args["reminder_minute"] as? Double)?.toInt() ?: 0
    val holidayName = args["holiday_name"] as? String
    val holidayAware = when (val v = args["holiday_aware"]) {
        is Boolean -> v
        is String -> v.equals("true", ignoreCase = true)
        is Number -> v.toInt() != 0
        else -> false
    }
    val rulePeriod = args["rule_period"] as? String
    val ruleWeek = (args["rule_week"] as? Double)?.toInt() ?: (args["rule_week"] as? Int)
    val ruleWeekday = (args["rule_weekday"] as? Double)?.toInt() ?: (args["rule_weekday"] as? Int)

    if (kind !in setOf("cycle", "date", "rule")) {
        return Pair(null, zhf("提醒类型无效（%s），只支持：周期(cycle)/日期(date)/规则(rule)。", kind))
    }
    if (kind == "cycle" && cycle !in setOf("daily", "weekly", "biweekly", "monthly", "quarterly", "yearly", "custom", "once")) {
        return Pair(null, zhf("周期类型无效（%s），只支持：每天/每周/每两周/每月/每季度/每年/自定义/一次。", cycle))
    }
    if (kind == "date" && dateType !in setOf("solar_birthday", "lunar_birthday", "holiday")) {
        return Pair(null, zh("日期提醒需要指定类型（公历生日/农历生日/节假日），例如：农历八月十五。请补充类型，我再为你创建。"))
    }
    if (cycle == "custom" && customDays < 1) {
        return Pair(null, zh("自定义周期需要指定间隔天数（如：每3天），custom_days 至少为 1。"))
    }

    val now = System.currentTimeMillis()
    val weekdayParam = listOf("weekday", "week_day").firstNotNullOfOrNull { args[it] }
        ?.let { (it as? Double)?.toInt() ?: (it as? Int) ?: (it as? String)?.toIntOrNull() }
        ?.takeIf { it in 1..7 }
    val cal = Calendar.getInstance().apply { timeInMillis = now }
    cal.set(Calendar.HOUR_OF_DAY, reminderHour.coerceIn(0, 23))
    cal.set(Calendar.MINUTE, reminderMinute.coerceIn(0, 59))
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)

    if ((cycle == "weekly" || cycle == "biweekly") && weekdayParam != null) {
        val cur = ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7) + 1
        var diff = (weekdayParam - cur + 7) % 7
        if (diff == 0 && cal.timeInMillis <= now) diff = 7
        cal.add(Calendar.DAY_OF_MONTH, diff)
    } else {
        val triggerDateRaw = args["trigger_date"] as? String
        val triggerDate = triggerDateRaw?.let { raw ->
            runCatching {
                SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(raw.trim())
            }.getOrNull()?.time
        }
        if (triggerDate != null && triggerDate > now) {
            cal.timeInMillis = triggerDate
            cal.set(Calendar.HOUR_OF_DAY, reminderHour.coerceIn(0, 23))
            cal.set(Calendar.MINUTE, reminderMinute.coerceIn(0, 59))
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= now) cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    val anchorNext = cal.timeInMillis

    if (kind == "date" && dateType != "holiday" && (targetMonth !in 1..12 || targetDay !in 1..31)) {
        return Pair(null, zh("需要具体的公历/农历月日才能创建日期提醒（例如：农历八月十五、公历5月1日）。请补充月日，我再为你创建。"))
    }
    if (kind == "date" && dateType == "lunar_birthday" && targetDay > 30) {
        return Pair(null, zh("农历日期没有 31 日，日期或历法可能有误。请与用户确认是新历还是农历后再创建。"))
    }
    if (kind == "date" && dateType == "holiday" && holidayName.isNullOrBlank()) {
        return Pair(null, zh("需要指定节假日名称（例如：春节、中秋节）才能创建节假日提醒。"))
    }
    if (kind == "rule" && (rulePeriod == null || ruleWeek == null || ruleWeekday == null)) {
        return Pair(null, zh("规则提醒需要指定频率（每月/每季度/每年）、第几周和星期几，例如：每季度第一周周四。请补充完整，我再为你创建。"))
    }

    val entity = ReminderEntity(
        title = title, note = note, kind = kind, cycle = cycle, customDays = customDays,
        dateType = dateType, targetMonth = targetMonth, targetDay = targetDay,
        advanceDays = advanceDays, reminderHour = reminderHour, reminderMinute = reminderMinute,
        holidayName = holidayName, rulePeriod = rulePeriod, ruleWeek = ruleWeek, ruleWeekday = ruleWeekday,
        weeklyWeekday = if (cycle == "weekly" || cycle == "biweekly") weekdayParam else null,
        holidayAware = holidayAware,
        firstTriggerAt = anchorNext, nextTriggerAt = anchorNext,
        status = ReminderStatus.PENDING.name.lowercase(), retryCount = 0, isActive = true
    )
    val configured = ReminderPolicy.withArguments(entity, args)
    val nextTrigger = ReminderEngine.calculateNextTrigger(configured, ReminderApp.instance)
    return Pair(configured.copy(nextTriggerAt = nextTrigger), null)
}

private suspend fun handleCreate(args: Map<String, Any?>, database: AppDatabase): String {
    val (entity, err) = tryBuildEntity(args)
    if (entity == null) return err ?: zh("创建失败")
    ReminderActions(ReminderApp.instance, database).create(entity)
    return zhf("已创建提醒：「%s」", entity.title)
}

private fun handleImportTasks(args: Map<String, Any?>): String {
    val rawItems = (args["items"] as? List<*>) ?: emptyList<Any?>()
    if (rawItems.isEmpty()) return zh("没有解析到可批量创建的提醒。")
    val built = mutableListOf<ReminderEntity>()
    val skipped = mutableListOf<String>()
    for (raw in rawItems) {
        val m = (raw as? Map<*, *>)?.mapNotNull { (k, v) -> (k as? String)?.let { it to v } }?.toMap() ?: emptyMap()
        val (e, err) = tryBuildEntity(m)
        if (e != null) built.add(e) else skipped.add(err ?: zh("无法解析"))
    }
    if (built.isEmpty()) return zh("解析失败：") + skipped.joinToString("；")
    pendingAIImportState.value = built
    val sb = StringBuilder(zhf("已解析出 %d 条提醒，请确认后批量创建：", built.size))
    built.forEachIndexed { i, e -> sb.append("\n${i + 1}. ${e.title} 〔${describeAIReminder(e)}〕") }
    if (skipped.isNotEmpty()) sb.append("\n（${skipped.size} 条无法解析已跳过）")
    return sb.toString()
}

private suspend fun handleList(database: AppDatabase): String {
    val list = database.reminderDao().getAllSync()
    if (list.isEmpty()) return zh("当前没有提醒。")
    val sb = StringBuilder(zhf("当前共有 %s 个提醒：", list.size))
    for (r in list) {
        sb.append("\n · ${r.title} [${describeAIReminder(r)}]")
    }
    return sb.toString()
}

private suspend fun handleStatsContext(database: AppDatabase): String {
    val records = database.reminderRecordDao().getAll()
    val summary = StatsService.summarize(records)
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
    val dow = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    cal.add(Calendar.DAY_OF_MONTH, -dow)
    val weekStart = cal.timeInMillis
    val weekConfirm = records.count { it.action == ReminderRecordEntity.ACTION_CONFIRMED && it.timestamp >= weekStart }
    val startOfDayLocal: (Long) -> Long = { ts ->
        val c = Calendar.getInstance().apply { timeInMillis = ts }
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        c.timeInMillis
    }
    val weekMissed = records.filter {
        it.action == ReminderRecordEntity.ACTION_NOTIFIED && it.note != "primary" && it.timestamp >= weekStart
    }.map { startOfDayLocal(it.timestamp) }.toSet().size
    val rate = if (weekConfirm + weekMissed > 0) {
        (weekConfirm.toDouble() / (weekConfirm + weekMissed) * 100).toInt()
    } else null

    val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val titleById = database.reminderDao().getAllSync().associate { it.id to it.title }
    val byReminder = mutableMapOf<String, Int>()
    records.filter { it.timestamp >= weekStart && it.action == ReminderRecordEntity.ACTION_CONFIRMED }
        .forEach { r ->
            val t = titleById[r.reminderId] ?: "已删除的提醒"
            byReminder[t] = (byReminder[t] ?: 0) + 1
        }

    val forget = summary.forgetHours.take(3).joinToString("、") { zhf("%s点(%s次)", it.first, it.second) }
    val aiLogs = AILogStore.recent(ReminderApp.instance).filter { it.time >= weekStart }
    val aiOk = aiLogs.count { it.ok }
    val aiFail = aiLogs.count { !it.ok }

    val sb = StringBuilder()
    sb.append(zhf("本周统计（%s 起 7 天）：确认 %s 次，错过 %s 天，完成率 %s。", df.format(Date(weekStart)), weekConfirm, weekMissed, rate?.let { "$it%" } ?: "暂无数据"))
    sb.append(zhf("当前连续 %s 天，最长连续 %s 天。", summary.currentStreak, summary.longestStreak))
    if (forget.isNotBlank()) sb.append(zhf("最常忘记的时段：%s。", forget))
    if (byReminder.isNotEmpty()) sb.append("本周各提醒确认次数：" + byReminder.entries.joinToString("、") { zhf("%s×%s", it.key, it.value) } + "。")
    sb.append(zhf("本周 AI 调用 %s 次（成功 %s，失败 %s）。", aiLogs.size, aiOk, aiFail))
    return sb.toString()
}

private suspend fun handleConfirm(args: Map<String, Any?>, database: AppDatabase): String {
    val keyword = (args["title_keyword"] as? String ?: "").lowercase()
    if (keyword.isEmpty()) return zh("请指定要确认的提醒标题（如：确认「交房租」）。")
    val match = database.reminderDao().getAllSync().find { it.title.lowercase().contains(keyword) }
        ?: return zhf("未找到包含「%s」的提醒", keyword)
    ReminderActions(ReminderApp.instance).apply(match, ReminderActions.Action.COMPLETE)
        ?: return zh("提醒已有新变更，请重试")
    return zhf("已确认「%s」，下次提醒时间已更新。", match.title)
}

private suspend fun handleSkip(args: Map<String, Any?>, database: AppDatabase): String {
    val keyword = (args["title_keyword"] as? String)?.trim()?.takeIf { it.isNotEmpty() }
        ?: return zh("请指定要确认的提醒标题（如：确认「交房租」）。")
    val r = database.reminderDao().getAllSync().find { it.title.contains(keyword, ignoreCase = true) }
        ?: return zhf("未找到包含「%s」的提醒", keyword)
    if (r.kind == "cycle" && r.cycle == "once") return zh("一次性提醒不能跳过")
    return if (ReminderActions(ReminderApp.instance).apply(r, ReminderActions.Action.SKIP) != null) {
        zh("已更新提醒")
    } else zh("提醒已有新变更，请重试")
}

private suspend fun handleSnooze(args: Map<String, Any?>, database: AppDatabase): String {
    val keyword = (args["title_keyword"] as? String ?: "").lowercase()
    if (keyword.isEmpty()) return zh("请指定要推迟的提醒标题（如：推迟「交房租」）。")
    val match = database.reminderDao().getAllSync().find { it.title.lowercase().contains(keyword) }
        ?: return zhf("未找到包含「%s」的提醒", keyword)
    ReminderActions(ReminderApp.instance).apply(match, ReminderActions.Action.SNOOZE)
        ?: return zh("提醒已有新变更，请重试")
    return zhf("已推迟「%s」，15 分钟后再次提醒。", match.title)
}

private suspend fun handleDelete(args: Map<String, Any?>, database: AppDatabase): String {
    val keyword = (args["title_keyword"] as? String ?: "").lowercase()
    if (keyword.isEmpty()) return zh("请指定要删除的提醒标题（如：删除「交房租」）。")
    val match = database.reminderDao().getAllSync().find { it.title.lowercase().contains(keyword) }
        ?: return zhf("未找到包含「%s」的提醒", keyword)
    if (!ReminderActions(ReminderApp.instance, database).delete(match)) return zh("提醒已有新变更，请重试")
    return zhf("已删除「%s」", match.title)
}

private suspend fun handleUpdate(args: Map<String, Any?>, database: AppDatabase): String {
    val keyword = (args["title_keyword"] as? String ?: "").lowercase()
    if (keyword.isEmpty()) return zh("请指定要修改的提醒标题（如：把「交房租」改成每月 5 号）。")
    val match = database.reminderDao().getAllSync().find { it.title.lowercase().contains(keyword) }
        ?: return zhf("未找到包含「%s」的提醒", keyword)
    val ctx = ReminderApp.instance

    val newTitle = args["new_title"] as? String
    val newNote = (args["note"] as? String) ?: match.note
    val cycleRaw = args["cycle"] as? String
    val customDays = (args["custom_days"] as? Double)?.toInt() ?: match.customDays
    val rulePeriod = args["rule_period"] as? String ?: match.rulePeriod
    val ruleWeek = (args["rule_week"] as? Double)?.toInt() ?: match.ruleWeek
    val ruleWeekday = (args["rule_weekday"] as? Double)?.toInt() ?: match.ruleWeekday
    val dateType = args["date_type"] as? String ?: match.dateType
    val targetMonth = (args["target_month"] as? Double)?.toInt() ?: match.targetMonth
    val targetDay = (args["target_day"] as? Double)?.toInt() ?: match.targetDay
    val advanceDays = (args["advance_days"] as? Double)?.toInt() ?: match.advanceDays
    val reminderHour = (args["reminder_hour"] as? Double)?.toInt() ?: match.reminderHour
    val reminderMinute = (args["reminder_minute"] as? Double)?.toInt() ?: match.reminderMinute
    val hasHour = args.containsKey("reminder_hour")
    val hasMinute = args.containsKey("reminder_minute")
    val holidayName = args["holiday_name"] as? String ?: match.holidayName
    val holidayAware = when (val v = args["holiday_aware"]) {
        is Boolean -> v
        is String -> v.equals("true", ignoreCase = true)
        is Number -> v.toInt() != 0
        else -> match.holidayAware
    }

    val effectiveCycle = cycleRaw ?: match.cycle
    if (effectiveCycle == "custom" && customDays < 1) {
        return zh("自定义周期需要指定间隔天数（如：每3天），custom_days 至少为 1。")
    }

    val wasOverdue = match.status == "overdue"
    val updated = match.copy(
        title = newTitle ?: match.title,
        note = newNote,
        cycle = cycleRaw ?: match.cycle,
        customDays = customDays,
        rulePeriod = rulePeriod,
        ruleWeek = ruleWeek,
        ruleWeekday = ruleWeekday,
        dateType = dateType,
        targetMonth = targetMonth,
        targetDay = targetDay,
        advanceDays = advanceDays,
        reminderHour = reminderHour,
        reminderMinute = reminderMinute,
        holidayName = holidayName,
        holidayAware = holidayAware,
        holidayAdjustNote = null
    )
    val updatedBase = if (wasOverdue) updated.copy(status = "pending", retryCount = 0) else updated
    var updatedForTime = updatedBase
    if (updatedBase.kind == "cycle" && updatedBase.cycle != "once" && (hasHour || hasMinute)) {
        val cal = Calendar.getInstance().apply { timeInMillis = updatedBase.firstTriggerAt }
        cal.set(Calendar.HOUR_OF_DAY, updatedBase.reminderHour)
        cal.set(Calendar.MINUTE, updatedBase.reminderMinute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        updatedForTime = updatedBase.copy(firstTriggerAt = cal.timeInMillis)
    }
    val nextTrigger = ReminderEngine.calculateNextTrigger(updatedForTime, ctx)
    val configured = ReminderPolicy.withArguments(updatedForTime, args)
    val scheduleChanged = listOf(
        "cycle", "custom_days", "rule_period", "rule_week", "rule_weekday",
        "date_type", "target_month", "target_day", "reminder_hour", "reminder_minute",
        "holiday_name", "holiday_aware"
    ).any { args.containsKey(it) }
    val finalUpdated = configured.copy(
        nextTriggerAt = if (scheduleChanged || wasOverdue) nextTrigger else match.nextTriggerAt,
        revision = match.revision + 1,
        holidayAdjustNote = null
    )
    if (ReminderActions(ctx, database).edit(match, finalUpdated) == null) {
        return zh("提醒已有新变更，请重试")
    }
    return zhf("已修改「%s」", finalUpdated.title)
}
