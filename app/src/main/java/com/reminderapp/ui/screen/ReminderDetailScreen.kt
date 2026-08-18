package com.reminderapp.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.data.entity.ReminderRecordEntity
import com.reminderapp.ui.theme.*
import com.reminderapp.ui.viewmodel.ReminderDetailViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    viewModel: ReminderDetailViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val reminder by viewModel.reminder.collectAsState()
    val records by viewModel.records.collectAsState()
    val checkInFeedback by viewModel.checkInFeedback.collectAsState()

    // v2.0.22: 通知点击到已删除/不存在的提醒时不再白屏——
    // 原实现 reminder == null 直接 return，页面既无标题栏也无返回按钮
    if (reminder == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(zh("提醒详情")) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = zh("返回"))
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        zh("提醒不存在或已被删除"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onBack) {
                        Text(zh("返回首页"))
                    }
                }
            }
        }
        return
    }
    val currentReminder = reminder!!

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
    // H2: 关键提醒全屏权限引导对话框（Android 14+ 未授予时弹出）
    var showFullScreenPermissionDialog by remember { mutableStateOf(false) }
    // v2.1.0: 统一稍后选项
    var snoozeMenuExpanded by remember { mutableStateOf(false) }
    var showCustomSnoozeDialog by remember { mutableStateOf(false) }
    var customSnoozeMinutes by remember { mutableStateOf("15") }
    // v2.1.1: 勿扰时段
    var quietHoursEnabled by remember { mutableStateOf(false) }
    var quietStartMinute by remember { mutableStateOf(22 * 60) }
    var quietEndMinute by remember { mutableStateOf(8 * 60) }
    var showQuietStartPicker by remember { mutableStateOf(false) }
    var showQuietEndPicker by remember { mutableStateOf(false) }
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        appeared = true
        // v2.1.1: 从 QuietHoursStore 恢复勿扰时段
        val r = viewModel.reminder.value
        if (r != null) {
            val start = com.reminderapp.service.QuietHoursStore.startMinute(context, r.id)
            val end = com.reminderapp.service.QuietHoursStore.endMinute(context, r.id)
            if (start != null && end != null) {
                quietHoursEnabled = true
                quietStartMinute = start
                quietEndMinute = end
            }
        }
    }

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
                    val context = LocalContext.current
                    // 批次3 功能6: 分享单条提醒卡片（导出 JSON 经系统分享面板发出）
                    IconButton(onClick = {
                        val json = com.reminderapp.service.BackupService.exportSingle(currentReminder)
                        val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TITLE, currentReminder.title)
                            putExtra(android.content.Intent.EXTRA_TEXT, json)
                        }
                        context.startActivity(android.content.Intent.createChooser(sendIntent, zh("分享提醒卡片")))
                    }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = zh("分享提醒卡片"),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
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

                // v2.1.0: 统一稍后选项（15 分钟 / 1 小时 / 明天 / 自定义分钟）
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { snoozeMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Secondary)
                    ) {
                        Icon(Icons.Default.Snooze, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(zh("稍后提醒"))
                    }
                    DropdownMenu(
                        expanded = snoozeMenuExpanded,
                        onDismissRequest = { snoozeMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(zh("15 分钟")) },
                            onClick = {
                                snoozeMenuExpanded = false
                                viewModel.snooze(minutes = 15)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(zh("1 小时")) },
                            onClick = {
                                snoozeMenuExpanded = false
                                viewModel.snooze(minutes = 60)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(zh("明天")) },
                            onClick = {
                                snoozeMenuExpanded = false
                                viewModel.snoozeTomorrow()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(zh("自定义…")) },
                            onClick = {
                                snoozeMenuExpanded = false
                                customSnoozeMinutes = "15"
                                showCustomSnoozeDialog = true
                            }
                        )
                    }
                }
            }

            // 已逾期：给一个明确的「补打今天」入口——按今天完成、推进周期、计入统计
            if (currentReminder.status == "overdue") {
                OutlinedButton(
                    onClick = { viewModel.confirm(isMakeUp = true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = zh("补打今天：把这条逾期提醒按今天完成，并推进到下一个周期")
                        },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusOverdue)
                ) {
                    Icon(Icons.Default.EventAvailable, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(zh("补打今天"))
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
                    // 批次3 功能5: 关键提醒开关
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = zh("关键提醒开关：开启后触发时全屏弹出，确保不被漏看")
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                zh("关键提醒"),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                zh("重要事项，触发时全屏弹出并不穿透勿扰"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        val nm = LocalContext.current.getSystemService(NotificationManager::class.java)
                        Switch(
                            checked = currentReminder.isCritical,
                            onCheckedChange = {
                                if (it) {
                                    // H2 + I6: 首次开启关键提醒，若全屏权限或勿扰访问权限未授予则引导授权
                                    val needFullScreen = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                                        (nm == null || !nm.canUseFullScreenIntent())
                                    // I6: 勿扰访问权限用框架 NotificationManager
                                    val needPolicy = nm?.isNotificationPolicyAccessGranted != true
                                    if (needFullScreen || needPolicy) {
                                        showFullScreenPermissionDialog = true
                                    }
                                }
                                viewModel.setCritical(it)
                            }
                        )
                    }
                    // v2.1.1: 勿扰时段——每日静默窗口内到点的提醒顺延到窗口结束（对齐 iOS QuietHoursStore）
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = zh("勿扰时段开关") },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                zh("勿扰时段"),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                zh("该时段内到点的提醒顺延到时段结束"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = quietHoursEnabled,
                            onCheckedChange = { enabled ->
                                quietHoursEnabled = enabled
                                if (enabled) {
                                    com.reminderapp.service.QuietHoursStore.set(context, currentReminder.id, quietStartMinute, quietEndMinute)
                                } else {
                                    com.reminderapp.service.QuietHoursStore.set(context, currentReminder.id, null, null)
                                }
                                viewModel.rescheduleQuietHours()
                            }
                        )
                    }
                    if (quietHoursEnabled) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showQuietStartPicker = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(zh("开始"), style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                zhf("%02d:%02d", quietStartMinute / 60, quietStartMinute % 60),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showQuietEndPicker = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(zh("结束"), style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                zhf("%02d:%02d", quietEndMinute / 60, quietEndMinute % 60),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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

            // v2.1.1: 未来触发预览（验证周期计算是否正确）
            val futureDates = com.reminderapp.service.ReminderEngine.futureTriggers(currentReminder, count = 10, context = com.reminderapp.ReminderApp.instance)
            if (futureDates.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarMonth, contentDescription = null,
                                tint = Tokens.BrandPrimary, modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                zh("未来触发"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                zhf("%s 次预览", futureDates.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        futureDates.forEachIndexed { index, ts ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Tokens.BrandPrimary)
                                        .wrapContentSize(Alignment.Center)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    dateFormat.format(Date(ts)),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
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

    // H2: 关键提醒全屏权限引导对话框
    if (showFullScreenPermissionDialog) {
        FullScreenPermissionDialog(onDismiss = { showFullScreenPermissionDialog = false })
    }

    // v2.1.0: 自定义稍后分钟数
    if (showCustomSnoozeDialog) {
        CustomSnoozeDialog(
            initialMinutes = customSnoozeMinutes,
            onConfirm = { minutes ->
                showCustomSnoozeDialog = false
                viewModel.snooze(minutes = minutes.toLong())
            },
            onDismiss = { showCustomSnoozeDialog = false }
        )
    }

    // v2.1.1: 勿扰时段起止时间选择
    if (showQuietStartPicker) {
        QuietTimePickerDialog(
            initialMinute = quietStartMinute,
            title = zh("勿扰开始"),
            onConfirm = { minute ->
                showQuietStartPicker = false
                quietStartMinute = minute
                com.reminderapp.service.QuietHoursStore.set(context, viewModel.reminder.value?.id ?: return@QuietTimePickerDialog, quietStartMinute, quietEndMinute)
                viewModel.rescheduleQuietHours()
            },
            onDismiss = { showQuietStartPicker = false }
        )
    }
    if (showQuietEndPicker) {
        QuietTimePickerDialog(
            initialMinute = quietEndMinute,
            title = zh("勿扰结束"),
            onConfirm = { minute ->
                showQuietEndPicker = false
                quietEndMinute = minute
                com.reminderapp.service.QuietHoursStore.set(context, viewModel.reminder.value?.id ?: return@QuietTimePickerDialog, quietStartMinute, quietEndMinute)
                viewModel.rescheduleQuietHours()
            },
            onDismiss = { showQuietEndPicker = false }
        )
    }

    // 批次2 功能2: 打卡成功正向反馈卡片（顶部浮层，自动消失）
    Box(modifier = Modifier.fillMaxSize()) {
        com.reminderapp.ui.component.CheckInFeedbackCard(
            text = checkInFeedback,
            onDismiss = viewModel::consumeCheckInFeedback
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

// v2.1.0: 自定义稍后分钟数对话框
@Composable
private fun CustomSnoozeDialog(
    initialMinutes: String,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var minutes by remember { mutableStateOf(initialMinutes) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(zh("自定义稍后")) },
        text = {
            OutlinedTextField(
                value = minutes,
                onValueChange = { minutes = it.filter { c -> c.isDigit() }.take(4) },
                label = { Text(zh("分钟数")) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(minutes.toIntOrNull()?.coerceAtLeast(1) ?: 15) }
            ) {
                Text(zh("确定"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(zh("取消"))
            }
        }
    )
}

// v2.1.1: 勿扰时段时间选择对话框
@Composable
private fun QuietTimePickerDialog(
    initialMinute: Int,
    title: String,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var hour by remember { mutableStateOf(initialMinute / 60) }
    var minute by remember { mutableStateOf(initialMinute % 60) }
    val timePicker = @Composable {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = hour.toString(),
                onValueChange = { hour = it.filter { c -> c.isDigit() }.take(2).toIntOrNull() ?: 0 },
                modifier = Modifier.width(72.dp),
                label = { Text(zh("时")) }
            )
            Text(zh(":"), style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = minute.toString(),
                onValueChange = { minute = it.filter { c -> c.isDigit() }.take(2).toIntOrNull() ?: 0 },
                modifier = Modifier.width(72.dp),
                label = { Text(zh("分")) }
            )
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { timePicker() },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(((hour.coerceIn(0, 23)) * 60 + minute.coerceIn(0, 59)))
                }
            ) { Text(zh("确定")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(zh("取消")) }
        }
    )
}

// H2 + I6: 关键提醒全屏 / 勿扰访问权限引导对话框
@Composable
private fun FullScreenPermissionDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val nm = context.getSystemService(NotificationManager::class.java)
    val needFullScreen = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
        (nm == null || !nm.canUseFullScreenIntent())
    // I6: 勿扰访问权限用框架 NotificationManager
    val needPolicy = nm?.isNotificationPolicyAccessGranted != true
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(zh("开启关键提醒所需权限")) },
        text = {
            Text(
                zh(
                    "关键提醒需要以下权限才能正常弹出并穿透勿扰：" +
                        (if (needFullScreen) "\n· 全屏界面（Android 14+ 默认不授予，需手动开启）" else "") +
                        (if (needPolicy) "\n· 勿扰访问（允许关键提醒在勿扰模式下响铃）" else "")
                )
            )
        },
        confirmButton = {
            if (needFullScreen) {
                TextButton(onClick = {
                    onDismiss()
                    val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) { Text(zh("去设置（全屏）")) }
            } else if (needPolicy) {
                TextButton(onClick = {
                    onDismiss()
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                }) { Text(zh("去设置（勿扰）")) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(zh("稍后")) }
        }
    )
}
