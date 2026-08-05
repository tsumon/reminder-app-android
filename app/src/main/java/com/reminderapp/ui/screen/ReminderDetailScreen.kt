package com.reminderapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.data.entity.ReminderRecordEntity
import com.reminderapp.ui.theme.*
import com.reminderapp.ui.viewmodel.ReminderDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    viewModel: ReminderDetailViewModel,
    onBack: () -> Unit
) {
    val reminder by viewModel.reminder.collectAsState()
    val records by viewModel.records.collectAsState()

    val currentReminder = reminder ?: return

    val statusColor = when (currentReminder.status) {
        "notifying" -> StatusReminding
        "pending" -> StatusWaiting
        "confirmed" -> StatusCompleted
        else -> StatusWaiting
    }

    val statusBg = when (currentReminder.status) {
        "notifying" -> StatusReminding.copy(alpha = 0.1f)
        "pending" -> StatusWaiting.copy(alpha = 0.1f)
        "confirmed" -> StatusCompleted.copy(alpha = 0.1f)
        else -> StatusWaiting.copy(alpha = 0.1f)
    }

    val statusLabel = when (currentReminder.status) {
        "notifying" -> "需要确认"
        "pending" -> "等待中"
        "confirmed" -> "已完成"
        else -> "等待中"
    }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val shortFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("提醒详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 状态大卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = statusBg)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentReminder.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (currentReminder.note.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentReminder.note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.confirm() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusCompleted)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("确认完成")
                }

                OutlinedButton(
                    onClick = { viewModel.snooze() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Secondary)
                ) {
                    Icon(Icons.Default.Snooze, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("稍后提醒")
                }
            }

            // 信息卡片
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (currentReminder.kind == "date") {
                        infoRow("类型", "日期提醒")
                        val dateTypeLabel = when (currentReminder.dateType) {
                            "solar_birthday" -> "新历生日"
                            "lunar_birthday" -> "农历生日"
                            "holiday" -> "节假日"
                            else -> ""
                        }
                        infoRow("日期类型", dateTypeLabel)
                        if (currentReminder.dateType == "holiday") {
                            infoRow("节日", currentReminder.holidayName ?: "")
                        } else {
                            infoRow("日期", "${currentReminder.targetMonth ?: 0}月${currentReminder.targetDay ?: 0}日")
                        }
                        infoRow("提前提醒", "${currentReminder.advanceDays} 天")
                    } else if (currentReminder.kind == "rule") {
                        infoRow("类型", "规则提醒")
                        infoRow("频率", when (currentReminder.rulePeriod) {
                            "monthly" -> "每月"
                            "yearly" -> "每年"
                            else -> "每季度"
                        })
                        infoRow("周次", "第${currentReminder.ruleWeek ?: 1}周")
                        infoRow("星期", arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
                            .getOrElse((currentReminder.ruleWeekday ?: 1) - 1) { "周${currentReminder.ruleWeekday}" })
                    } else {
                        infoRow("周期", currentReminder.cycle)
                    }
                    infoRow("首次提醒", dateFormat.format(Date(currentReminder.firstTriggerAt)))
                    infoRow("下次提醒", dateFormat.format(Date(currentReminder.nextTriggerAt)))
                    if (currentReminder.retryCount > 0) {
                        infoRow("重试次数", "${currentReminder.retryCount} 次")
                    }
                }
            }

            // 历史记录
            if (records.isNotEmpty()) {
                Text(
                    "操作记录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                records.forEach { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = when (record.action) {
                                    "confirmed" -> "✅ 已确认完成"
                                    "snoozed" -> "⏰ 稍后提醒"
                                    "notified" -> "🔔 已发送通知"
                                    else -> record.action
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = shortFormat.format(Date(record.timestamp)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        DeleteConfirmDialog(
            title = currentReminder.title,
            onConfirm = {
                showDeleteDialog = false
                viewModel.delete()
                onBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun infoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// 删除确认对话框（挂在整个 Scaffold 之外）
@Composable
private fun DeleteConfirmDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("删除提醒") },
        text = { Text("确定要删除「$title」吗？此操作不可恢复。") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("删除", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
