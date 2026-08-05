package com.reminderapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.model.Cycle
import com.reminderapp.model.DateReminderType
import com.reminderapp.service.HolidayService
import com.reminderapp.service.LunarCalendar
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
    var selectedCycle by remember { mutableStateOf(Cycle.WEEKLY) }
    var customDays by remember { mutableIntStateOf(1) }
    var selectedDateType by remember { mutableStateOf<DateReminderType?>(null) }
    var targetMonth by remember { mutableIntStateOf(1) }
    var targetDay by remember { mutableIntStateOf(1) }
    var selectedHolidayName by remember { mutableStateOf<String?>(null) }
    var advanceDays by remember { mutableIntStateOf(3) }

    // 日期时间选择
    val now = Calendar.getInstance()
    var triggerYear by remember { mutableIntStateOf(now.get(Calendar.YEAR)) }
    var triggerMonth by remember { mutableIntStateOf(now.get(Calendar.MONTH) + 1) }
    var triggerDay by remember { mutableIntStateOf(now.get(Calendar.DAY_OF_MONTH)) }
    var triggerHour by remember { mutableIntStateOf(9) }
    var triggerMinute by remember { mutableIntStateOf(0) }

    var cycleExpanded by remember { mutableStateOf(false) }
    var dateTypeExpanded by remember { mutableStateOf(false) }
    var holidayExpanded by remember { mutableStateOf(false) }

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
            }
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
                    selected = !isDateMode,
                    onClick = { isDateMode = false },
                    label = { Text("周期提醒") },
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = isDateMode,
                    onClick = { isDateMode = true },
                    label = { Text("日期提醒") }
                )
            }

            if (!isDateMode) {
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
