package com.reminderapp.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.data.entity.ReminderRecordEntity
import com.reminderapp.ui.theme.*
import com.reminderapp.ui.viewmodel.ReminderDetailViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf

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
        "overdue" -> StatusOverdue   // v1.9.7: 递增重试到上限
        "pending" -> StatusWaiting
        "confirmed" -> StatusCompleted
        else -> StatusWaiting
    }

    val statusBg = when (currentReminder.status) {
        "notifying" -> StatusReminding.copy(alpha = 0.1f)
        "overdue" -> StatusOverdue.copy(alpha = 0.1f)
        "pending" -> StatusWaiting.copy(alpha = 0.1f)
        "confirmed" -> StatusCompleted.copy(alpha = 0.1f)
        else -> StatusWaiting.copy(alpha = 0.1f)
    }

    val statusLabel = when (currentReminder.status) {
        "notifying" -> zh("需要确认")
        "overdue" -> zh("已逾期")
        "pending" -> zh("等待中")
        "confirmed" -> zh("已完成")
        else -> zh("等待中")
    }

    val statusIcon = when (currentReminder.status) {
        "notifying" -> Icons.Default.Notifications
        "overdue" -> Icons.Default.Warning
        "confirmed" -> Icons.Default.CheckCircle
        else -> Icons.Default.Snooze
    }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val shortFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    var showDeleteDialog by remember { mutableStateOf(false) }
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(zh("提醒详情")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = zh("返回"))
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = zh("删除"),
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
            // 状态大卡片（v1.9.8 设计图风格：状态色渐变 + 大图标 + 阴影，入场动画）
            AnimatedVisibility(
                visible = appeared,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(com.reminderapp.ui.theme.Tokens.RadiusCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(com.reminderapp.ui.theme.Tokens.RadiusCard))
                            .background(
                                Brush.linearGradient(
                                    listOf(statusColor, statusColor.copy(alpha = 0.72f))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 大图标容器（玻璃质感）
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = statusIcon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = zh("当前状态"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = statusLabel +
                                        (if (currentReminder.retryCount > 0) zhf(" · 第%s次重试", currentReminder.retryCount) else ""),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (currentReminder.nextTriggerAt > 0) {
                                    val nextFmt = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                                    Text(
                                        text = zhf("下次：%s", nextFmt.format(Date(currentReminder.nextTriggerAt))),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
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
                    Text(zh("确认完成"))
                }

                OutlinedButton(
                    onClick = { viewModel.snooze() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Secondary)
                ) {
                    Icon(Icons.Default.Snooze, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(zh("稍后提醒"))
                }
            }

            // 信息卡片
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Item 2: 本次触发因节假日前移的说明
                    if (!currentReminder.holidayAdjustNote.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentReminder.holidayAdjustNote!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                    infoRow(zh("优先级"), when (currentReminder.priority) {
                        "high" -> zh("🔴 高")
                        "low" -> zh("⚪ 低")
                        else -> zh("🟢 中")
                    })
                    if (currentReminder.kind == "date") {
                        infoRow(zh("类型"), zh("日期提醒"))
                        val dateTypeLabel = when (currentReminder.dateType) {
                            "solar_birthday" -> zh("新历生日")
                            "lunar_birthday" -> zh("农历生日")
                            "holiday" -> zh("节假日")
                            else -> ""
                        }
                        infoRow(zh("日期类型"), dateTypeLabel)
                        if (currentReminder.dateType == "holiday") {
                            infoRow(zh("节日"), currentReminder.holidayName ?: "")
                        } else {
                            infoRow(zh("日期"), zhf("%1\$s月%2\$s日", currentReminder.targetMonth ?: 0, currentReminder.targetDay ?: 0))
                        }
                        infoRow(zh("提前提醒"), zhf("%s 天", currentReminder.advanceDays))
                    } else if (currentReminder.kind == "rule") {
                        infoRow(zh("类型"), zh("规则提醒"))
                        infoRow(zh("频率"), when (currentReminder.rulePeriod) {
                            "monthly" -> zh("每月")
                            "yearly" -> zh("每年")
                            else -> zh("每季度")
                        })
                        infoRow(zh("周次"), zhf("第%s周", currentReminder.ruleWeek ?: 1))
                        infoRow(zh("星期"), arrayOf(zh("周一"), zh("周二"), zh("周三"), zh("周四"), zh("周五"), zh("周六"), zh("周日"))
                            .getOrElse((currentReminder.ruleWeekday ?: 1) - 1) { zhf("周%s", currentReminder.ruleWeekday) })
                    } else {
                        infoRow(zh("周期"), currentReminder.cycle)
                    }
                    infoRow(zh("首次提醒"), dateFormat.format(Date(currentReminder.firstTriggerAt)))
                    infoRow(zh("下次提醒"), dateFormat.format(Date(currentReminder.nextTriggerAt)))
                    if (currentReminder.retryCount > 0) {
                        infoRow(zh("重试次数"), zhf("%s 次", currentReminder.retryCount))
                    }
                }
            }

            // 历史记录
            if (records.isNotEmpty()) {
                Text(
                    zh("操作记录"),
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
                                    "confirmed" -> zh("✅ 已确认完成")
                                    "snoozed" -> zh("⏰ 稍后提醒")
                                    "notified" -> zh("🔔 已发送通知")
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
        title = { Text(zh("删除提醒")) },
        text = { Text(zhf("确定要删除「%s」吗？此操作不可恢复。", title)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(zh("删除"), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(zh("取消"))
            }
        }
    )
}
