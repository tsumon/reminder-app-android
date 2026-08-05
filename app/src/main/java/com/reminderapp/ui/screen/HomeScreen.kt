package com.reminderapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.ui.theme.*
import com.reminderapp.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCreateReminder: () -> Unit,
    onReminderClick: (Long) -> Unit,
    onAIChat: () -> Unit
) {
    val grouped by viewModel.groupedReminders.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("循环提醒器", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = onAIChat) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = "AI 助手",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateReminder,
                containerColor = Primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "新建提醒")
            }
        }
    ) { padding ->
        val reminding = grouped.reminding
        val waiting = grouped.waiting
        val completed = grouped.completed

        if (reminding.isEmpty() && waiting.isEmpty() && completed.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "📭",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "暂无提醒",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "点击右下角 + 创建新提醒",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 提醒中
                if (reminding.isNotEmpty()) {
                    item { SectionHeader("提醒中", StatusReminding) }
                    items(reminding, key = { it.id }) { reminder ->
                        ReminderCard(reminder, StatusReminding) { onReminderClick(reminder.id) }
                    }
                }

                // 等待中
                if (waiting.isNotEmpty()) {
                    item { SectionHeader("等待中", StatusWaiting) }
                    items(waiting, key = { it.id }) { reminder ->
                        ReminderCard(reminder, StatusWaiting) { onReminderClick(reminder.id) }
                    }
                }

                // 已完成
                if (completed.isNotEmpty()) {
                    item { SectionHeader("已完成", StatusCompleted) }
                    items(completed, key = { it.id }) { reminder ->
                        ReminderCard(reminder, StatusCompleted) { onReminderClick(reminder.id) }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = color,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@Composable
fun ReminderCard(reminder: ReminderEntity, statusColor: Color, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    val statusText = when (reminder.status) {
        "notifying" -> "需要确认"
        "idle", "pending" -> "等待中"
        "confirmed" -> "已完成"
        else -> ""
    }
    val kindLabel = when {
        reminder.kind == "date" && reminder.dateType == "holiday" -> "🎉${reminder.holidayName ?: ""}"
        reminder.kind == "date" && reminder.dateType == "lunar_birthday" -> "🌙农历生日"
        reminder.kind == "date" && reminder.dateType == "solar_birthday" -> "🎂生日"
        else -> reminder.cycle
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .padding(end = 8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    shape = MaterialTheme.shapes.small,
                    color = statusColor
                ) {}
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = kindLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " · $statusText",
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor
                    )
                }
                if (reminder.note.isNotEmpty()) {
                    Text(
                        text = reminder.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Text(
                text = dateFormat.format(Date(reminder.nextTriggerAt)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
