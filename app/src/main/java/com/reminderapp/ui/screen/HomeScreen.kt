package com.reminderapp.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCreateReminder: () -> Unit,
    onReminderClick: (Long) -> Unit,
    onAIChat: () -> Unit,
    onDeleteReminder: (Long) -> Unit
) {
    val grouped by viewModel.groupedReminders.collectAsState()
    val allReminders by viewModel.allReminders.collectAsState()

    // 长按删除确认框状态
    var pendingDelete by remember { mutableStateOf<ReminderEntity?>(null) }

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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 日历卡片（始终显示）
            item { CalendarCard(reminders = allReminders) }

            if (reminding.isEmpty() && waiting.isEmpty() && completed.isEmpty()) {
                // 空状态
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
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
                }
            } else {
                // 提醒中
                if (reminding.isNotEmpty()) {
                    item { SectionHeader("提醒中", StatusReminding) }
                    items(reminding, key = { it.id }) { reminder ->
                        ReminderCard(reminder, StatusReminding, onDelete = { pendingDelete = reminder }) { onReminderClick(reminder.id) }
                    }
                }

                // 等待中
                if (waiting.isNotEmpty()) {
                    item { SectionHeader("等待中", StatusWaiting) }
                    items(waiting, key = { it.id }) { reminder ->
                        ReminderCard(reminder, StatusWaiting, onDelete = { pendingDelete = reminder }) { onReminderClick(reminder.id) }
                    }
                }

                // 已完成
                if (completed.isNotEmpty()) {
                    item { SectionHeader("已完成", StatusCompleted) }
                    items(completed, key = { it.id }) { reminder ->
                        ReminderCard(reminder, StatusCompleted, onDelete = { pendingDelete = reminder }) { onReminderClick(reminder.id) }
                    }
                }
            }
        }
    }

    // 删除确认对话框
    pendingDelete?.let { reminder ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除提醒") },
            text = { Text("确定要删除「${reminder.title}」吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteReminder(reminder.id)
                        pendingDelete = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("取消")
                }
            }
        )
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

/** 规则提醒的显示文本，如「每季度第2周周二」 */
fun ruleLabel(reminder: ReminderEntity): String {
    val periodLabel = when (reminder.rulePeriod) {
        "monthly" -> "每月"
        "yearly" -> "每年"
        else -> "每季度"
    }
    val weekday = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        .getOrElse((reminder.ruleWeekday ?: 1) - 1) { "周${reminder.ruleWeekday}" }
    return "${periodLabel}第${reminder.ruleWeek ?: 1}周$weekday"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReminderCard(
    reminder: ReminderEntity,
    statusColor: Color,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
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
        reminder.kind == "rule" -> ruleLabel(reminder)
        else -> reminder.cycle
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onDelete
            ),
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
