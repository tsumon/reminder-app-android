package com.reminderapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.service.ReminderEngine
import com.reminderapp.ui.theme.StatusReminding
import com.reminderapp.ui.theme.StatusWaiting
import java.util.*

/**
 * 日历 Tab（v1.9.8 UI 对齐设计图）：
 * 整页月历（农历/节假日/任务标记）+ 点击日期展示「当天任务」列表
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    reminders: List<ReminderEntity>,
    onReminderClick: (Long) -> Unit
) {
    var selectedDate by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("日历", style = MaterialTheme.typography.headlineMedium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 整页月历
            item {
                CalendarCard(reminders = reminders, onDateClick = { selectedDate = it })
            }

            // 当日任务标题
            val ts = selectedDate
            item {
                if (ts == null) {
                    SectionHeader("点击日期查看当天任务", StatusWaiting)
                } else {
                    val cal = Calendar.getInstance().apply { timeInMillis = ts }
                    SectionHeader(
                        "${cal.get(Calendar.YEAR)}年${cal.get(Calendar.MONTH) + 1}月${cal.get(Calendar.DAY_OF_MONTH)}日 · 当天任务",
                        StatusReminding
                    )
                }
            }

            // 当日任务列表
            if (ts != null) {
                val cal = Calendar.getInstance().apply { timeInMillis = ts }
                val y = cal.get(Calendar.YEAR)
                val m = cal.get(Calendar.MONTH) + 1
                val d = cal.get(Calendar.DAY_OF_MONTH)
                val dateReminders = reminders.filter { it.isActive && ReminderEngine.occursOn(it, y, m, d) }

                if (dateReminders.isEmpty()) {
                    item {
                        Text(
                            "这一天没有提醒",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }
                } else {
                    items(dateReminders, key = { it.id }) { r ->
                        ReminderCard(r, StatusReminding, onDelete = {}) { onReminderClick(r.id) }
                    }
                }
            }
        }
    }
}