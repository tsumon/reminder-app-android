package com.reminderapp.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.service.ReminderEngine
import com.reminderapp.ui.theme.*
import com.reminderapp.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/** 智能清单：按时间/优先级/状态快速筛选 */
enum class SmartList(val label: String) {
    ALL("全部"),
    TODAY("今天"),
    TOMORROW("明天"),
    WEEK("本周"),
    MONTH("本月"),
    HIGH("高优先级"),
    DONE("已完成")
}

/** 判断提醒是否在 [fromDay, toDay] 天偏移范围内会发生 */
private fun occursWithinDays(reminder: ReminderEntity, fromDay: Int, toDay: Int): Boolean {
    if (toDay < fromDay) return false
    for (i in fromDay..toDay) {
        val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, i) }
        if (ReminderEngine.occursOn(
                reminder,
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH)
            )
        ) return true
    }
    return false
}

/** 本月剩余天数（含今天） */
private fun daysLeftInMonth(): Int {
    val c = Calendar.getInstance()
    return c.getActualMaximum(Calendar.DAY_OF_MONTH) - c.get(Calendar.DAY_OF_MONTH)
}

/** 智能清单过滤条件 */
private fun matchSmartList(reminder: ReminderEntity, list: SmartList): Boolean = when (list) {
    SmartList.ALL -> true
    SmartList.TODAY -> occursWithinDays(reminder, 0, 0)
    SmartList.TOMORROW -> occursWithinDays(reminder, 1, 1)
    SmartList.WEEK -> occursWithinDays(reminder, 0, 6)
    SmartList.MONTH -> occursWithinDays(reminder, 0, daysLeftInMonth())
    SmartList.HIGH -> reminder.priority == "high"
    SmartList.DONE -> reminder.status == "confirmed"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCreateReminder: () -> Unit,
    onReminderClick: (Long) -> Unit,
    onAIChat: () -> Unit,
    onDeleteReminder: (Long) -> Unit,
    onExport: () -> Unit = {},
    onImport: () -> Unit = {},
    onOpenSyncSettings: () -> Unit = {},
    onSyncNow: () -> Unit = {}
) {
    val grouped by viewModel.groupedReminders.collectAsState()
    val allReminders by viewModel.allReminders.collectAsState()

    // 进入首页时：确保所有提醒都有排期 + 补偿检测遗漏提醒
    // （系统可能清理掉 WorkManager 任务，不重排的话提醒就再也不响了）
    LaunchedEffect(Unit) {
        viewModel.ensureSchedules()
        viewModel.checkMissed()
    }

    // 长按删除确认框状态
    var pendingDelete by remember { mutableStateOf<ReminderEntity?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    // 点击日历某天 → 查看当日任务
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    // 智能清单
    var smartList by remember { mutableStateOf(SmartList.ALL) }

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
                ),
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "更多"
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("立即同步") },
                                leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onSyncNow()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("同步设置") },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onOpenSyncSettings()
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("导入提醒") },
                                leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onImport()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("导出提醒") },
                                leadingIcon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onExport()
                                }
                            )
                        }
                    }
                }
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
        val reminding = grouped.reminding.filter { matchSmartList(it, smartList) }
        val waiting = grouped.waiting.filter { matchSmartList(it, smartList) }
        val completed = grouped.completed.filter { matchSmartList(it, smartList) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 概览卡片（参考滴答清单：待处理数 + 最近提醒）
            item {
                OverviewCard(
                    unhandledCount = allReminders.count { it.isActive && it.status != "confirmed" },
                    nextReminder = allReminders.filter { it.isActive }.minByOrNull { it.nextTriggerAt }
                )
            }

            // 日历卡片（始终显示）
            item { CalendarCard(reminders = allReminders, onDateClick = { selectedDate = it }) }

            // 智能清单筛选条
            item {
                SmartListBar(
                    selected = smartList,
                    counts = SmartList.entries.associateWith { sl ->
                        allReminders.count { matchSmartList(it, sl) }
                    },
                    onSelect = { smartList = it }
                )
            }

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
                                if (smartList == SmartList.ALL) "暂无提醒" else "「${smartList.label}」没有提醒",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                if (smartList == SmartList.ALL) "点击右下角 + 创建新提醒" else "换个清单看看",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onCreateReminder) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("创建提醒")
                            }
                        }
                    }
                }
            } else {
                // 提醒中
                if (reminding.isNotEmpty()) {
                    item { SectionHeader("提醒中", StatusReminding) }
                    items(reminding, key = { it.id }) { reminder ->
                        SwipeableReminderCard(
                            reminder = reminder,
                            statusColor = StatusReminding,
                            onComplete = { viewModel.confirmReminder(reminder) },
                            onDelete = { pendingDelete = reminder },
                            onClick = { onReminderClick(reminder.id) },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }

                // 等待中
                if (waiting.isNotEmpty()) {
                    item { SectionHeader("等待中", StatusWaiting) }
                    items(waiting, key = { it.id }) { reminder ->
                        SwipeableReminderCard(
                            reminder = reminder,
                            statusColor = StatusWaiting,
                            onComplete = { viewModel.confirmReminder(reminder) },
                            onDelete = { pendingDelete = reminder },
                            onClick = { onReminderClick(reminder.id) },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }

                // 已完成
                if (completed.isNotEmpty()) {
                    item { SectionHeader("已完成", StatusCompleted) }
                    items(completed, key = { it.id }) { reminder ->
                        SwipeableReminderCard(
                            reminder = reminder,
                            statusColor = StatusCompleted,
                            onComplete = { viewModel.reopenReminder(reminder) },
                            onDelete = { pendingDelete = reminder },
                            onClick = { onReminderClick(reminder.id) },
                            modifier = Modifier.animateItemPlacement()
                        )
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

    // 点击日历日期 → 底部弹窗展示当日任务
    selectedDate?.let { ts ->
        val cal = Calendar.getInstance().apply { timeInMillis = ts }
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val dateReminders = allReminders.filter { it.isActive && ReminderEngine.occursOn(it, y, m, d) }

        ModalBottomSheet(
            onDismissRequest = { selectedDate = null },
            dragHandle = { Divider(thickness = 4.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "${y}年${m}月${d}日 的任务",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (dateReminders.isEmpty()) {
                    Text(
                        "这一天没有提醒",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(dateReminders, key = { it.id }) { r ->
                            ReminderCard(r, StatusReminding, onDelete = { pendingDelete = r }) { onReminderClick(r.id) }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
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

/** 智能清单筛选条：横向可滚动的 Chip 列表 */
@Composable
fun SmartListBar(
    selected: SmartList,
    counts: Map<SmartList, Int>,
    onSelect: (SmartList) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SmartList.entries.forEach { item ->
            val count = counts[item] ?: 0
            FilterChip(
                selected = selected == item,
                onClick = { onSelect(item) },
                label = {
                    Text(if (count > 0) "${item.label} $count" else item.label)
                }
            )
        }
    }
}

/**
 * 支持滑动手势的提醒卡片
 *  - 右滑：完成（已完成项则为「撤销」）
 *  - 左滑：删除
 */
@Composable
fun SwipeableReminderCard(
    reminder: ReminderEntity,
    statusColor: Color,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDone = reminder.status == "confirmed"
    val density = LocalDensity.current
    val maxOffset = with(density) { 120.dp.toPx() }
    val threshold = with(density) { 72.dp.toPx() }

    var offsetX by remember(reminder.id) { mutableStateOf(0f) }
    val animated by animateFloatAsState(targetValue = offsetX, label = "swipeOffset")

    val completeColor = if (isDone) StatusWaiting else StatusCompleted
    val deleteColor = MaterialTheme.colorScheme.error

    Box(modifier = modifier.fillMaxWidth()) {
        // 滑动背景层
        if (animated != 0f) {
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(if (animated > 0f) completeColor else deleteColor)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (animated > 0f) Arrangement.Start else Arrangement.End
            ) {
                Icon(
                    imageVector = when {
                        animated > 0f && isDone -> Icons.Default.Refresh
                        animated > 0f -> Icons.Default.Check
                        else -> Icons.Default.Delete
                    },
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when {
                        animated > 0f && isDone -> "撤销"
                        animated > 0f -> "完成"
                        else -> "删除"
                    },
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        // 前景卡片
        Box(
            modifier = Modifier
                .offset { IntOffset(animated.roundToInt(), 0) }
                .pointerInput(reminder.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetX >= threshold -> onComplete()
                                offsetX <= -threshold -> onDelete()
                            }
                            offsetX = 0f
                        },
                        onDragCancel = { offsetX = 0f },
                        onHorizontalDrag = { change, drag ->
                            change.consume()
                            offsetX = (offsetX + drag).coerceIn(-maxOffset, maxOffset)
                        }
                    )
                }
        ) {
            ReminderCard(reminder, statusColor, onDelete = onDelete, onClick = onClick)
        }
    }
}

/** 首页概览卡片：待处理数量 + 最近一次提醒（参考滴答清单） */
@Composable
fun OverviewCard(unhandledCount: Int, nextReminder: ReminderEntity?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "待处理 $unhandledCount 项",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                val nextText = nextReminder?.let {
                    val f = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                    "${it.title} · ${f.format(Date(it.nextTriggerAt))}"
                } ?: "暂无即将到来的提醒"
                Text(
                    nextText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
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
        else -> when (reminder.cycle) {
            "once" -> "仅一次"
            "daily" -> "每天"
            "weekly" -> "每周"
            "biweekly" -> "每两周"
            "monthly" -> "每月"
            "quarterly" -> "每季度"
            "yearly" -> "每年"
            "custom" -> "每${reminder.customDays}天"
            else -> reminder.cycle
        }
    }
    val priorityLabel = when (reminder.priority) {
        "high" -> "🔴高"
        "low" -> "⚪低"
        else -> "🟢中"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onDelete,
                onClickLabel = "打开详情",
                onLongClickLabel = "长按删除"
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
                    .clip(CircleShape)
                    .background(statusColor)
            )
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
                        text = "$priorityLabel · $kindLabel",
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
