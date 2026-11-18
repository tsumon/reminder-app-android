package com.reminderapp.ui.screen

import android.app.NotificationManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.reminderapp.ReminderApp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.data.entity.ReminderRecordEntity
import com.reminderapp.i18n.zh
import com.reminderapp.model.Cycle
import com.reminderapp.model.DateReminderType
import com.reminderapp.model.RulePeriod
import com.reminderapp.service.FrequencySuggester
import com.reminderapp.service.HolidayService
import com.reminderapp.service.ReminderEditorMapper
import com.reminderapp.service.ReminderEngine
import com.reminderapp.service.ReminderScheduler
import com.reminderapp.service.ReminderTemplate
import com.reminderapp.ui.comic.ComicButton
import com.reminderapp.ui.comic.ComicTopBar
import com.reminderapp.ui.comic.comicColors
import com.reminderapp.ui.screen.editor.EditorAdvancedDrawer
import com.reminderapp.ui.screen.editor.EditorDateTimeAndPermissionDialogs
import com.reminderapp.ui.screen.editor.EditorMainCard
import com.reminderapp.ui.screen.editor.EditorQuickAndTemplatesStrip
import com.reminderapp.ui.screen.editor.buildEditorFirstTriggerAt
import com.reminderapp.ui.screen.editor.buildSavedReminderPair
import com.reminderapp.ui.screen.editor.parseEditorNaturalLanguage
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CreateReminderScreen(
    onSave: (ReminderEntity) -> Unit,
    onBack: () -> Unit,
    initial: ReminderEntity? = null,
    onSaveAll: ((List<ReminderEntity>) -> Unit)? = null
) {
    val colors = comicColors()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var saving by remember { mutableStateOf(false) }
    var policyValid by remember { mutableStateOf(true) }
    var showAdvanced by remember { mutableStateOf(false) }
    val seeded = remember(initial) { initial?.let(ReminderEditorMapper::seed) }
    var policy by remember { mutableStateOf(initial ?: ReminderEntity(title = "", firstTriggerAt = 0, nextTriggerAt = 0)) }

    var title by remember { mutableStateOf(seeded?.title ?: "") }
    var note by remember { mutableStateOf(seeded?.note ?: "") }
    var isDateMode by remember { mutableStateOf(seeded?.kind == "date") }
    var isRuleMode by remember { mutableStateOf(seeded?.kind == "rule") }
    var selectedCycle by remember {
        mutableStateOf(Cycle.entries.find { it.name.lowercase() == seeded?.cycle } ?: Cycle.WEEKLY)
    }
    var weeklyWeekday by remember { mutableIntStateOf(seeded?.weeklyWeekday ?: 0) }
    var customDays by remember { mutableIntStateOf(seeded?.customDays?.coerceAtLeast(1) ?: 1) }
    var customDaysText by remember(initial?.id) { mutableStateOf(customDays.toString()) }
    LaunchedEffect(customDays) {
        if (customDaysText.toIntOrNull() != customDays && customDaysText.isNotEmpty()) {
            customDaysText = customDays.toString()
        }
    }

    var selectedDateType by remember {
        mutableStateOf(DateReminderType.entries.find { it.name.lowercase() == seeded?.dateType })
    }
    var targetMonth by remember { mutableIntStateOf(seeded?.targetMonth ?: 1) }
    var targetMonthText by remember(initial?.id) { mutableStateOf(targetMonth.toString()) }
    LaunchedEffect(targetMonth) {
        if (targetMonthText.toIntOrNull() != targetMonth && targetMonthText.isNotEmpty()) {
            targetMonthText = targetMonth.toString()
        }
    }
    var targetDay by remember { mutableIntStateOf(seeded?.targetDay ?: 1) }
    var targetDayText by remember(initial?.id) { mutableStateOf(targetDay.toString()) }
    LaunchedEffect(targetDay) {
        if (targetDayText.toIntOrNull() != targetDay && targetDayText.isNotEmpty()) {
            targetDayText = targetDay.toString()
        }
    }
    var selectedHolidayName by remember { mutableStateOf(seeded?.holidayName) }
    var advanceDays by remember { mutableIntStateOf(seeded?.advanceDays ?: 3) }

    var rulePeriod by remember {
        mutableStateOf(RulePeriod.entries.find { it.name.lowercase() == seeded?.rulePeriod } ?: RulePeriod.QUARTERLY)
    }
    var ruleWeek by remember { mutableIntStateOf(seeded?.ruleWeek ?: 2) }
    var ruleWeekday by remember { mutableIntStateOf(seeded?.ruleWeekday ?: 2) }
    var priority by remember { mutableStateOf(seeded?.priority ?: "normal") }
    var isCritical by remember { mutableStateOf(seeded?.isCritical ?: false) }
    var holidayAware by remember { mutableStateOf(seeded?.holidayAware ?: false) }
    var showFullScreenPermissionDialog by remember { mutableStateOf(false) }
    var suggestionText by remember { mutableStateOf<String?>(null) }

    var nlText by remember { mutableStateOf("") }
    var nlHint by remember { mutableStateOf<String?>(null) }
    var nlError by remember { mutableStateOf(false) }
    var nlDualBirthday by remember { mutableStateOf(false) }

    val now = remember(initial) {
        Calendar.getInstance().apply {
            if (initial != null && seeded != null) {
                timeInMillis = initial.firstTriggerAt
                set(Calendar.HOUR_OF_DAY, seeded.reminderHour)
                set(Calendar.MINUTE, seeded.reminderMinute)
            } else {
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
            }
        }
    }
    var triggerYear by remember { mutableIntStateOf(now.get(Calendar.YEAR)) }
    var triggerMonth by remember { mutableIntStateOf(now.get(Calendar.MONTH) + 1) }
    var triggerDay by remember { mutableIntStateOf(now.get(Calendar.DAY_OF_MONTH)) }
    var triggerHour by remember { mutableIntStateOf(now.get(Calendar.HOUR_OF_DAY)) }
    var triggerMinute by remember { mutableIntStateOf(now.get(Calendar.MINUTE)) }
    var showDateDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }

    fun scheduleValues(): List<Any?> = listOf(
        isDateMode, isRuleMode, if (!isDateMode && !isRuleMode) selectedCycle else null,
        if (selectedCycle == Cycle.CUSTOM) customDays else 0, weeklyWeekday,
        if (isDateMode) selectedDateType else null, if (isDateMode) targetMonth else null,
        if (isDateMode || selectedCycle == Cycle.MONTHLY) targetDay else null,
        if (isDateMode) selectedHolidayName else null,
        if (isRuleMode) rulePeriod else null, if (isRuleMode) ruleWeek else null,
        if (isRuleMode) ruleWeekday else null,
        if (!isDateMode && !isRuleMode) triggerYear else 0,
        if (!isDateMode && !isRuleMode) triggerMonth else 0,
        if (!isDateMode && !isRuleMode) triggerDay else 0,
        triggerHour, triggerMinute, holidayAware
    )
    val originalSchedule = remember { scheduleValues() }

    fun applyTemplate(tpl: ReminderTemplate) {
        title = tpl.title; note = tpl.note; holidayAware = tpl.holidayAware
        when (tpl.kind) {
            ReminderTemplate.Kind.CYCLE -> {
                isDateMode = false; isRuleMode = false
                selectedCycle = if (tpl.cycle == ReminderTemplate.Cycle.YEARLY) Cycle.YEARLY else Cycle.MONTHLY
            }
            ReminderTemplate.Kind.DATE -> {
                isDateMode = true; isRuleMode = false
                selectedDateType = if (tpl.dateType == ReminderTemplate.DateKind.LUNAR_BIRTHDAY) {
                    DateReminderType.LUNAR_BIRTHDAY
                } else DateReminderType.SOLAR_BIRTHDAY
                selectedCycle = Cycle.YEARLY; advanceDays = tpl.advanceDays
            }
        }
    }

    fun performSave() {
        if (title.isBlank()) return
        if (isDateMode && selectedDateType == null) {
            Toast.makeText(context, zh("请选择日期类型（公历生日/农历生日/节假日）"), Toast.LENGTH_SHORT).show()
            return
        }
        if (isDateMode && selectedDateType == DateReminderType.HOLIDAY && selectedHolidayName.isNullOrBlank()) {
            Toast.makeText(context, zh("请选择节假日"), Toast.LENGTH_SHORT).show()
            return
        }
        val firstTrigger = buildEditorFirstTriggerAt(
            selectedCycle, isDateMode, isRuleMode, targetDay, weeklyWeekday,
            triggerYear, triggerMonth, triggerDay, triggerHour, triggerMinute
        )
        val (mappedPrimary, extraLunar) = buildSavedReminderPair(
            context = context, initial = initial, scheduleChanged = scheduleValues() != originalSchedule,
            isDateMode = isDateMode, isRuleMode = isRuleMode, selectedCycle = selectedCycle,
            customDays = customDays, weeklyWeekday = weeklyWeekday, selectedDateType = selectedDateType,
            targetMonth = targetMonth, targetDay = targetDay, selectedHolidayName = selectedHolidayName,
            advanceDays = advanceDays, rulePeriod = rulePeriod, ruleWeek = ruleWeek, ruleWeekday = ruleWeekday,
            triggerHour = triggerHour, triggerMinute = triggerMinute, priority = priority,
            title = title, note = note, firstTrigger = firstTrigger, isCritical = isCritical,
            holidayAware = holidayAware, policy = policy, nlDualBirthday = nlDualBirthday, nlText = nlText
        )
        saving = true
        if (extraLunar != null && onSaveAll != null) onSaveAll(listOf(mappedPrimary, extraLunar))
        else { onSave(mappedPrimary); if (extraLunar != null) onSave(extraLunar) }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(colors.canvas).navigationBarsPadding()
    ) {
        ComicTopBar(
            title = zh(if (initial != null && initial.id > 0L) "编辑提醒" else "新建提醒"),
            onBack = onBack,
            backText = zh("取消"),
            trailing = {
                ComicButton(
                    text = zh("保存"),
                    onClick = ::performSave,
                    enabled = title.isNotBlank() && !saving && policyValid,
                    cut = 5.5.dp,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp),
                    fontSize = 13.5.sp
                )
            }
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!ReminderScheduler(context).canScheduleExact()) {
                Text(zh("当前为非精确提醒，可能延迟。可在设置 → 提醒诊断中授权精确闹钟。"), color = colors.warn, fontSize = 12.sp)
            }
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                Text(zh("通知未开启。保存后请到设置 → 提醒诊断中开启通知并测试。"), color = colors.danger, fontSize = 12.sp)
            }

            EditorQuickAndTemplatesStrip(
                nlText = nlText,
                onNlTextChange = { nlText = it; nlHint = null; nlError = false },
                nlHint = nlHint,
                nlError = nlError,
                onParseNl = {
                    val parsed = parseEditorNaturalLanguage(nlText, title, selectedDateType, initial == null || initial.id <= 0L)
                    if (parsed == null) {
                        nlError = true; nlHint = zh("没听懂，换个说法试试～")
                    } else {
                        nlError = false; title = parsed.title
                        triggerYear = parsed.triggerYear; triggerMonth = parsed.triggerMonth; triggerDay = parsed.triggerDay
                        triggerHour = parsed.triggerHour; triggerMinute = parsed.triggerMinute
                        isDateMode = parsed.isDateMode; isRuleMode = parsed.isRuleMode
                        selectedCycle = parsed.selectedCycle; selectedDateType = parsed.selectedDateType
                        parsed.targetMonth?.let { targetMonth = it }
                        parsed.targetDay?.let { if (it in 1..31) targetDay = it }
                        parsed.weeklyWeekday?.let { weeklyWeekday = it }
                        holidayAware = parsed.holidayAware
                        nlDualBirthday = parsed.dualBirthday; nlHint = parsed.hint
                    }
                },
                onApplyTemplate = ::applyTemplate
            )

            EditorMainCard(
                title = title, onTitleChange = { title = it },
                note = note, onNoteChange = { note = it },
                isDateMode = isDateMode, isRuleMode = isRuleMode,
                onModeChange = { d, r -> isDateMode = d; isRuleMode = r },
                selectedCycle = selectedCycle, onCycleChange = { selectedCycle = it },
                weeklyWeekday = weeklyWeekday, onWeeklyWeekdayChange = { weeklyWeekday = it },
                customDaysText = customDaysText,
                onCustomDaysTextChange = { input ->
                    val filtered = input.filter(Char::isDigit).take(4)
                    customDaysText = filtered; filtered.toIntOrNull()?.let { customDays = maxOf(1, it) }
                },
                suggestionText = suggestionText,
                onSuggestFrequency = {
                    suggestionText = null
                    scope.launch(Dispatchers.IO) {
                        val db = ReminderApp.instance.database
                        val all = db.reminderDao().getAllSyncBlocking()
                        val confirmByReminder = db.reminderRecordDao().getAll()
                            .filter { it.action == ReminderRecordEntity.ACTION_CONFIRMED }
                            .groupBy { it.reminderId }.mapValues { (_, v) -> v.map { it.timestamp } }
                        val s = FrequencySuggester.suggest(title, all, confirmByReminder)
                        withContext(Dispatchers.Main) {
                            selectedCycle = s.cycle; customDays = s.customDays; suggestionText = s.reason
                        }
                    }
                },
                selectedDateType = selectedDateType, onDateTypeChange = { selectedDateType = it },
                selectedHolidayName = selectedHolidayName,
                onHolidaySelect = { name, m, d -> selectedHolidayName = name; targetMonth = m; targetDay = d },
                targetMonthText = targetMonthText,
                onTargetMonthTextChange = { input ->
                    val filtered = input.filter(Char::isDigit).take(2)
                    targetMonthText = filtered; filtered.toIntOrNull()?.let { targetMonth = it.coerceIn(1, 12) }
                },
                targetDayText = targetDayText,
                onTargetDayTextChange = { input ->
                    val filtered = input.filter(Char::isDigit).take(2)
                    targetDayText = filtered
                    val maxD = if (selectedDateType == DateReminderType.LUNAR_BIRTHDAY) 30 else 31
                    filtered.toIntOrNull()?.let { targetDay = it.coerceIn(1, maxD) }
                },
                targetMonth = targetMonth, targetDay = targetDay,
                advanceDays = advanceDays, onAdvanceDaysChange = { advanceDays = it },
                rulePeriod = rulePeriod, onRulePeriodChange = { rulePeriod = it },
                ruleWeek = ruleWeek, onRuleWeekChange = { ruleWeek = it },
                ruleWeekday = ruleWeekday, onRuleWeekdayChange = { ruleWeekday = it },
                triggerYear = triggerYear, triggerMonth = triggerMonth, triggerDay = triggerDay,
                triggerHour = triggerHour, triggerMinute = triggerMinute,
                onOpenDatePicker = { showDateDialog = true }, onOpenTimePicker = { showTimeDialog = true },
                priority = priority, onPriorityChange = { priority = it },
                holidayAware = holidayAware, onHolidayAwareChange = { holidayAware = it }
            )

            val previewAnchor = buildEditorFirstTriggerAt(
                selectedCycle, isDateMode, isRuleMode, targetDay, weeklyWeekday,
                triggerYear, triggerMonth, triggerDay, triggerHour, triggerMinute
            )
            val currentKind = if (isDateMode) "date" else if (isRuleMode) "rule" else "cycle"
            val previewDraft = policy.copy(
                title = title, kind = currentKind, cycle = selectedCycle.name.lowercase(), customDays = customDays,
                dateType = selectedDateType?.name?.lowercase(), targetMonth = targetMonth, targetDay = targetDay,
                holidayId = HolidayService.allHolidays.find { it.name == selectedHolidayName }?.id,
                holidayName = selectedHolidayName, rulePeriod = rulePeriod.name.lowercase(),
                ruleWeek = ruleWeek, ruleWeekday = ruleWeekday, reminderHour = triggerHour, reminderMinute = triggerMinute,
                firstTriggerAt = previewAnchor, nextTriggerAt = previewAnchor, holidayAware = holidayAware
            )
            val firstPreview = if (isDateMode || isRuleMode) ReminderEngine.calculateNextTrigger(previewDraft, context) else previewAnchor

            EditorAdvancedDrawer(
                expanded = showAdvanced, onToggle = { showAdvanced = !showAdvanced },
                policyEntity = policy.copy(kind = currentKind, cycle = selectedCycle.name.lowercase()),
                previewDraft = previewDraft, firstPreview = firstPreview,
                onPolicyChange = {
                    policy = it; isCritical = it.isCritical
                    if (it.isCritical) {
                        val nm = context.getSystemService(NotificationManager::class.java)
                        val needFs = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && (nm == null || !nm.canUseFullScreenIntent())
                        if (needFs || nm?.isNotificationPolicyAccessGranted != true) showFullScreenPermissionDialog = true
                    }
                },
                onValidityChange = { policyValid = it }
            )
            Spacer(Modifier.height(24.dp))
        }
    }

    EditorDateTimeAndPermissionDialogs(
        showDateDialog = showDateDialog, triggerYear = triggerYear, triggerMonth = triggerMonth, triggerDay = triggerDay,
        onDatePicked = { y, m, d ->
            triggerYear = y; triggerMonth = m; triggerDay = d
            if (selectedCycle == Cycle.MONTHLY && !isDateMode && !isRuleMode) targetDay = d
        },
        onDismissDate = { showDateDialog = false },
        showTimeDialog = showTimeDialog, triggerHour = triggerHour, triggerMinute = triggerMinute,
        onTimePicked = { h, m -> triggerHour = h; triggerMinute = m },
        onDismissTime = { showTimeDialog = false },
        showFullScreenPermissionDialog = showFullScreenPermissionDialog,
        onDismissPermission = { showFullScreenPermissionDialog = false }
    )
}
