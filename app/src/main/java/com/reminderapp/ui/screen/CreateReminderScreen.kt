package com.reminderapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.model.Cycle
import com.reminderapp.model.DateReminderType
import com.reminderapp.model.RulePeriod
import com.reminderapp.model.weekLabels
import com.reminderapp.model.weekdayLabels
import com.reminderapp.service.HolidayService
import com.reminderapp.service.LunarCalendar
import com.reminderapp.service.NaturalDateParser
import com.reminderapp.service.ReminderEngine
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReminderScreen(
    onSave: (ReminderEntity) -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    // 表单状态
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isDateMode by remember { mutableStateOf(false) }
    var isRuleMode by remember { mutableStateOf(false) }
    var selectedCycle by remember { mutableStateOf(Cycle.WEEKLY) }
    var customDays by remember { mutableIntStateOf(1) }
    var selectedDateType by remember { mutableStateOf<DateReminderType?>(null) }
    var targetMonth by remember { mutableIntStateOf(1) }
    var targetDay by remember { mutableIntStateOf(1) }
    var selectedHolidayName by remember { mutableStateOf<String?>(null) }
    var advanceDays by remember { mutableIntStateOf(3) }
    // 规则提醒字段
    var rulePeriod by remember { mutableStateOf(RulePeriod.QUARTERLY) }
    var ruleWeek by remember { mutableIntStateOf(2) }
    var ruleWeekday by remember { mutableIntStateOf(2) } // 周二

    // 优先级
    var priority by remember { mutableStateOf("normal") } // high / normal / low

    // 自然语言快速创建
    var nlText by remember { mutableStateOf("") }
    var nlHint by remember { mutableStateOf<String?>(null) }
    var nlError by remember { mutableStateOf(false) }

    // 日期时间选择
    val now = Calendar.getInstance()
    var triggerYear by remember { mutableIntStateOf(now.get(Calendar.YEAR)) }
    var triggerMonth by remember { mutableIntStateOf(now.get(Calendar.MONTH) + 1) }
    var triggerDay by remember { mutableIntStateOf(now.get(Calendar.DAY_OF_MONTH)) }
    var triggerHour by remember { mutableIntStateOf(9) }
    var triggerMinute by remember { mutableIntStateOf(0) }

    // 下拉展开状态
    var cycleExpanded by remember { mutableStateOf(false) }
    var dateTypeExpanded by remember { mutableStateOf(false) }
    var holidayExpanded by remember { mutableStateOf(false) }
    var rulePeriodExpanded by remember { mutableStateOf(false) }
    var ruleWeekExpanded by remember { mutableStateOf(false) }
    var ruleWeekdayExpanded by remember { mutableStateOf(false) }

    fun buildFirstTriggerAt(): Long {
        val cal = Calendar.getInstance()
        cal.set(triggerYear, triggerMonth - 1, triggerDay, triggerHour, triggerMinute, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新建提醒") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isBlank()) return@TextButton

                            val firstTrigger = buildFirstTriggerAt()

                            val entity = if (isDateMode) {
                                ReminderEntity(
                                    kind = "date",
                                    cycle = "yearly",
                                    dateType = selectedDateType?.name?.lowercase(),
                                    targetMonth = targetMonth,
                                    targetDay = targetDay,
                                    holidayName = selectedHolidayName,
                                    advanceDays = advanceDays,
                                    priority = priority,
                                    title = title.trim(),
                                    note = note.trim(),
                                    firstTriggerAt = firstTrigger,
                                    nextTriggerAt = firstTrigger
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
                                    nextTriggerAt = firstTrigger
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
                                    nextTriggerAt = firstTrigger
                                )
                            }

                            onSave(entity)
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text("保存", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // === 自然语言快速创建 ===
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "一句话创建",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    OutlinedTextField(
                        value = nlText,
                        onValueChange = { nlText = it; nlHint = null; nlError = false },
                        placeholder = { Text("明天下午3点开会 / 每周一9点晨会 / 农历8月15 中秋") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val p = NaturalDateParser.parse(nlText)
                                if (p == null) {
                                    nlError = true
                                    nlHint = "没听懂，换个说法试试～"
                                } else {
                                    nlError = false
                                    // 标题
                                    if (title.isBlank()) title = p.title
                                    // 触发时间
                                    val c = Calendar.getInstance().apply { timeInMillis = p.nextTriggerAt }
                                    triggerYear = c.get(Calendar.YEAR)
                                    triggerMonth = c.get(Calendar.MONTH) + 1
                                    triggerDay = c.get(Calendar.DAY_OF_MONTH)
                                    triggerHour = c.get(Calendar.HOUR_OF_DAY)
                                    triggerMinute = c.get(Calendar.MINUTE)
                                    // 重复模式 → 表单
                                    when (p.repeatMode) {
                                        "lunar" -> {
                                            isDateMode = true; isRuleMode = false
                                            selectedDateType = DateReminderType.LUNAR_BIRTHDAY
                                            p.targetMonth?.let { targetMonth = it }
                                            p.targetDay?.let { targetDay = it }
                                        }
                                        "yearly" -> {
                                            if (p.dateType == "solar_birthday") {
                                                isDateMode = true; isRuleMode = false
                                                selectedDateType = DateReminderType.SOLAR_BIRTHDAY
                                                p.targetMonth?.let { targetMonth = it }
                                                p.targetDay?.let { targetDay = it }
                                            } else {
                                                isDateMode = false; isRuleMode = false
                                                selectedCycle = Cycle.YEARLY
                                            }
                                        }
                                        "daily" -> {
                                            isDateMode = false; isRuleMode = false
                                            selectedCycle = Cycle.DAILY
                                        }
                                        "weekly" -> {
                                            isDateMode = false; isRuleMode = false
                                            selectedCycle = Cycle.WEEKLY
                                        }
                                        "monthly" -> {
                                            isDateMode = false; isRuleMode = false
                                            selectedCycle = Cycle.MONTHLY
                                        }
                                        else -> {
                                            isDateMode = false; isRuleMode = false
                                            selectedCycle = Cycle.ONCE
                                        }
                                    }
                                    val cycleText = when (p.repeatMode) {
                                        "lunar" -> "农历每年"
                                        "yearly" -> "每年"
                                        "daily" -> "每天"
                                        "weekly" -> "每周"
                                        "monthly" -> "每月"
                                        else -> "仅一次"
                                    }
                                    nlHint = "「${p.title}」· $cycleText · ${p.label}"
                                }
                            },
                            enabled = nlText.isNotBlank()
                        ) {
                            Text("智能识别")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        nlHint?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (nlError) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // 标题
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 备注
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // 提醒类型切换
            Text("提醒类型", style = MaterialTheme.typography.labelLarge)
            Row {
                FilterChip(
                    selected = !isDateMode && !isRuleMode,
                    onClick = { isDateMode = false; isRuleMode = false },
                    label = { Text("周期提醒") },
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = isDateMode,
                    onClick = { isDateMode = true; isRuleMode = false },
                    label = { Text("日期提醒") },
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = isRuleMode,
                    onClick = { isDateMode = false; isRuleMode = true },
                    label = { Text("规则提醒") }
                )
            }

            if (isRuleMode) {
                // === 规则提醒：每月/每季度/每年 第N周 周X ===
                Text("频率", style = MaterialTheme.typography.labelLarge)
                ExposedDropdownMenuBox(
                    expanded = rulePeriodExpanded,
                    onExpandedChange = { rulePeriodExpanded = it }
                ) {
                    OutlinedTextField(
                        value = rulePeriod.label,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rulePeriodExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = rulePeriodExpanded,
                        onDismissRequest = { rulePeriodExpanded = false }
                    ) {
                        RulePeriod.entries.forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period.label) },
                                onClick = {
                                    rulePeriod = period
                                    rulePeriodExpanded = false
                                }
                            )
                        }
                    }
                }

                Text("第几周", style = MaterialTheme.typography.labelLarge)
                ExposedDropdownMenuBox(
                    expanded = ruleWeekExpanded,
                    onExpandedChange = { ruleWeekExpanded = it }
                ) {
                    OutlinedTextField(
                        value = weekLabels.getOrElse(ruleWeek - 1) { "第${ruleWeek}周" },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ruleWeekExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = ruleWeekExpanded,
                        onDismissRequest = { ruleWeekExpanded = false }
                    ) {
                        (1..5).forEach { w ->
                            DropdownMenuItem(
                                text = { Text("第${w}周") },
                                onClick = {
                                    ruleWeek = w
                                    ruleWeekExpanded = false
                                }
                            )
                        }
                    }
                }

                Text("星期几", style = MaterialTheme.typography.labelLarge)
                ExposedDropdownMenuBox(
                    expanded = ruleWeekdayExpanded,
                    onExpandedChange = { ruleWeekdayExpanded = it }
                ) {
                    OutlinedTextField(
                        value = weekdayLabels.getOrElse(ruleWeekday - 1) { "周${ruleWeekday}" },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ruleWeekdayExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = ruleWeekdayExpanded,
                        onDismissRequest = { ruleWeekdayExpanded = false }
                    ) {
                        (1..7).forEach { w ->
                            DropdownMenuItem(
                                text = { Text(weekdayLabels[w - 1]) },
                                onClick = {
                                    ruleWeekday = w
                                    ruleWeekdayExpanded = false
                                }
                            )
                        }
                    }
                }
            } else if (!isDateMode) {
                // === 周期提醒 ===
                // 周期选择
                Text("周期", style = MaterialTheme.typography.labelLarge)
                ExposedDropdownMenuBox(
                    expanded = cycleExpanded,
                    onExpandedChange = { cycleExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCycle.label,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cycleExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = cycleExpanded,
                        onDismissRequest = { cycleExpanded = false }
                    ) {
                        Cycle.entries.forEach { cycle ->
                            DropdownMenuItem(
                                text = { Text(cycle.label) },
                                onClick = {
                                    selectedCycle = cycle
                                    cycleExpanded = false
                                }
                            )
                        }
                    }
                }

                if (selectedCycle == Cycle.CUSTOM) {
                    OutlinedTextField(
                        value = customDays.toString(),
                        onValueChange = { customDays = it.toIntOrNull() ?: 1 },
                        label = { Text("自定义天数") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            } else {
                // === 日期提醒 ===
                Text("日期类型", style = MaterialTheme.typography.labelLarge)
                ExposedDropdownMenuBox(
                    expanded = dateTypeExpanded,
                    onExpandedChange = { dateTypeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedDateType?.label ?: "请选择",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dateTypeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = dateTypeExpanded,
                        onDismissRequest = { dateTypeExpanded = false }
                    ) {
                        DateReminderType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.label) },
                                onClick = {
                                    selectedDateType = type
                                    dateTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                when {
                    selectedDateType == DateReminderType.HOLIDAY -> {
                        ExposedDropdownMenuBox(
                            expanded = holidayExpanded,
                            onExpandedChange = { holidayExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedHolidayName ?: "选择节日",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = holidayExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = holidayExpanded,
                                onDismissRequest = { holidayExpanded = false }
                            ) {
                                HolidayService.allHolidays.forEach { holiday ->
                                    DropdownMenuItem(
                                        text = { Text("${holiday.name}（${if (holiday.isLunar) "农历" else "公历"}）") },
                                        onClick = {
                                            selectedHolidayName = holiday.name
                                            targetMonth = holiday.month
                                            targetDay = holiday.day
                                            holidayExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    selectedDateType == DateReminderType.LUNAR_BIRTHDAY -> {
                        Text("农历月日", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = targetMonth.toString(),
                                onValueChange = { targetMonth = it.toIntOrNull()?.coerceIn(1, 12) ?: 1 },
                                label = { Text("月") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = targetDay.toString(),
                                onValueChange = { targetDay = it.toIntOrNull()?.coerceIn(1, 30) ?: 1 },
                                label = { Text("日") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        // 显示当年对应的公历日期
                        val solarDate = remember(targetMonth, targetDay, triggerYear) {
                            LunarCalendar.lunarToSolar(triggerYear, targetMonth, targetDay)
                        }
                        if (solarDate != null) {
                            val sdf = java.text.SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
                            Text(
                                "${triggerYear}年对应公历：${sdf.format(Date(solarDate))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    else -> {
                        Text("月日", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = targetMonth.toString(),
                                onValueChange = { targetMonth = it.toIntOrNull()?.coerceIn(1, 12) ?: 1 },
                                label = { Text("月") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = targetDay.toString(),
                                onValueChange = { targetDay = it.toIntOrNull()?.coerceIn(1, 31) ?: 1 },
                                label = { Text("日") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                }

                // 提前提醒天数
                Text("提前提醒", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 3, 7, 14).forEach { days ->
                        FilterChip(
                            selected = advanceDays == days,
                            onClick = { advanceDays = days },
                            label = { Text("${days}天") }
                        )
                    }
                }
            }

            // 优先级
            Text("优先级", style = MaterialTheme.typography.labelLarge)
            Row {
                FilterChip(
                    selected = priority == "high",
                    onClick = { priority = "high" },
                    label = { Text("🔴 高") },
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = priority == "normal",
                    onClick = { priority = "normal" },
                    label = { Text("🟢 中") },
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = priority == "low",
                    onClick = { priority = "low" },
                    label = { Text("⚪ 低") }
                )
            }

            // 提醒时间
            Text("提醒时间", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = String.format("%02d:%02d", triggerHour, triggerMinute),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("时间") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = "${triggerMonth}月${triggerDay}日",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("日期") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
