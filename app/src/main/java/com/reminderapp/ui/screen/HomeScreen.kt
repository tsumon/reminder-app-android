package com.reminderapp.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.ReminderApp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.service.BackupService
import com.reminderapp.service.ReminderEngine
import com.reminderapp.ui.theme.*
import com.reminderapp.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf

/** 智能清单：按时间/优先级/状态快速筛选 */
enum class SmartList(val label: String) {
    ALL(zh("全部")),
    TODAY(zh("今天")),
    TOMORROW(zh("明天")),
    WEEK(zh("本周")),
    MONTH(zh("本月")),
    HIGH(zh("高优先级")),
    DONE(zh("已完成"))
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
    onOpenStats: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onExportICS: () -> Unit = {},
    onCheckUpdate: () -> Unit = {},
    onNearbyShare: () -> Unit = {},
    onSyncNow: () -> Unit = {}
) {
    val grouped by viewModel.groupedReminders.collectAsState()
    val allReminders by viewModel.allReminders.collectAsState()
    val checkInFeedback by viewModel.checkInFeedback.collectAsState()

    // 进入首页时：确保所有提醒都有排期 + 补偿检测遗漏提醒
    // （系统可能清理掉 WorkManager 任务，不重排的话提醒就再也不响了）
    LaunchedEffect(Unit) {
        viewModel.ensureSchedules()
        viewModel.checkMissed()
    }

    // v2.3.0: 今日完成数（hero 完成率环）
    val database = com.reminderapp.data.database.AppDatabase.getInstance(androidx.compose.ui.platform.LocalContext.current)
    var todayDone by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        // v2.4.0: 防御——统计失败不影响首页渲染
        runCatching {
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0); cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0); cal.set(java.util.Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            todayDone = database.reminderRecordDao().getAll()
                .count { it.action == com.reminderapp.data.entity.ReminderRecordEntity.ACTION_CONFIRMED && it.timestamp >= start }
        }
    }

    // v2.4.2: 锚点星期修正——weekly 意图星期与实际锚点不符的提醒
    var anchorMismatches by remember { mutableStateOf<List<ReminderEntity>>(emptyList()) }
    LaunchedEffect(allReminders.size) {
        anchorMismatches = allReminders.filter { r ->
            r.weeklyWeekday in 1..7 && r.isActive && r.status != "confirmed" && run {
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = r.firstTriggerAt }
                val anchorDow = ((cal.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7) + 1
                anchorDow != r.weeklyWeekday
            }
        }
    }

    // 长按删除确认框状态
    var pendingDelete by remember { mutableStateOf<ReminderEntity?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    // v2.1.1: 批量管理（长按进入多选，批量完成/删除）
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    // v2.4.9: 批量改提醒时间对话框
    var showBatchTimeDialog by remember { mutableStateOf(false) }
    var batchHour by remember { mutableStateOf(9) }
    var batchMinute by remember { mutableStateOf(0) }
    // 点击日历某天 → 查看当日任务
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    // 智能清单
    var smartList by remember { mutableStateOf(SmartList.ALL) }

    // 批次3 功能6: 单条分享卡片粘贴导入
    var showCardImportDialog by remember { mutableStateOf(false) }
    var cardImportText by remember { mutableStateOf("") }
    var cardImportMsg by remember { mutableStateOf<String?>(null) }
    // I10: 导入分享卡片需写库（suspend），用协程作用域
    val scope = rememberCoroutineScope()
    val cardImportContext = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (selectionMode) {
                        Text(zhf("已选 %s 项", selectedIds.size), style = MaterialTheme.typography.headlineMedium)
                    } else {
                        Text(zh("循环提醒器"), style = MaterialTheme.typography.headlineMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onAIChat) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = zh("AI 助手"),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    if (selectionMode) {
                        // v2.1.1: 批量操作（全选/完成/删除/退出）
                        TextButton(onClick = {
                            val all = allReminders.map { it.id }.toSet()
                            selectedIds = if (selectedIds.size == all.size) emptySet() else all
                        }) { Text(zh("全选")) }
                        TextButton(
                            onClick = {
                                viewModel.batchComplete(selectedIds)
                                selectedIds = emptySet()
                                selectionMode = false
                            },
                            enabled = selectedIds.isNotEmpty()
                        ) { Text(zh("完成"), color = MaterialTheme.colorScheme.primary) }
                        TextButton(
                            onClick = {
                                viewModel.batchDelete(selectedIds)
                                selectedIds = emptySet()
                                selectionMode = false
                            },
                            enabled = selectedIds.isNotEmpty()
                        ) { Text(zh("删除"), color = MaterialTheme.colorScheme.error) }
                        // v2.4.9: 批量改提醒时间
                        TextButton(
                            onClick = { showBatchTimeDialog = true },
                            enabled = selectedIds.isNotEmpty()
                        ) { Text(zh("改时间"), color = MaterialTheme.colorScheme.primary) }
                        TextButton(onClick = {
                            selectedIds = emptySet()
                            selectionMode = false
                        }) { Text(zh("取消")) }
                    } else {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = zh("更多")
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            // v2.1.1: 批量管理
                            DropdownMenuItem(
                                text = { Text(zh("批量管理")) },
                                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    selectionMode = true
                                    selectedIds = emptySet()
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text(zh("立即同步")) },
                                leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onSyncNow()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(zh("同步设置")) },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onOpenSyncSettings()
                                }
                            )
                            Divider()
                            // v1.8.7 任务③: 统计洞察
                            DropdownMenuItem(
                                text = { Text(zh("统计洞察")) },
                                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onOpenStats()
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text(zh("导入提醒")) },
                                leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onImport()
                                }
                            )
                            // 批次3 功能6: 单条分享卡片粘贴导入（聊天里收到的 JSON 直接粘进来）
                            DropdownMenuItem(
                                text = { Text(zh("导入分享卡片")) },
                                leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    showCardImportDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(zh("导出提醒")) },
                                leadingIcon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onExport()
                                }
                            )
                            // v1.8.7 任务④: 导出 .ics 日历
                            DropdownMenuItem(
                                text = { Text(zh("导出日历(.ics)")) },
                                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onExportICS()
                                }
                            )
                            Divider()
                            // 近场传输: 同一局域网互传提醒
                            DropdownMenuItem(
                                text = { Text(zh("附近传输")) },
                                leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onNearbyShare()
                                }
                            )
                            Divider()
                            // v1.9.0: 主动检查更新
                            DropdownMenuItem(
                                text = { Text(zh("检查更新")) },
                                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onCheckUpdate()
                                }
                            )
                            Divider()
                            // v1.9.2: 设置（版本号/更新日志/AI/同步）
                            DropdownMenuItem(
                                text = { Text(zh("设置")) },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onOpenSettings()
                                }
                            )
                        }
                    }
                    }
                }
            )
        },
        floatingActionButton = {
            // v2.1.0: 动态颜色下用 primaryContainer 保持主题一致（固定 Primary 会与壁纸色割裂）
            FloatingActionButton(
                onClick = onCreateReminder,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = zh("新建提醒"))
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
                    todayDone = todayDone,
                    // v1.9.6 fix: 过滤已确认/已过期——is_active=1 包含 confirmed 的 once 提醒
                    // （nextTriggerAt 是过去值），会被误选成「下一条提醒」显示历史时间
                    nextReminder = allReminders
                        .filter { it.isActive && it.status != "confirmed" && it.nextTriggerAt > System.currentTimeMillis() }
                        .minByOrNull { it.nextTriggerAt }
                )
            }

            // v2.4.9: 遗漏补办卡——已触发未确认 / 已到时间未确认的提醒，一键补确认或推到明天
            val now = System.currentTimeMillis()
            val missedReminders = allReminders
                .filter {
                    it.isActive && it.status != "confirmed" &&
                        (it.status == "notifying" || it.status == "overdue" || (it.status == "pending" && it.nextTriggerAt <= now))
                }
                .sortedBy { it.nextTriggerAt }
            if (missedReminders.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    zhf("你错过了 %d 条提醒", missedReminders.size),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            missedReminders.take(5).forEach { r ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(r.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                                        Text(
                                            zhf("错过于 %s", java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(r.nextTriggerAt))),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                    TextButton(onClick = { viewModel.confirmReminder(r, isMakeUp = true) }) {
                                        Text(zh("补确认"), color = MaterialTheme.colorScheme.error)
                                    }
                                    TextButton(onClick = { viewModel.snoozeTomorrow(r) }) {
                                        Text(zh("明天"), color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // v2.4.0: 今日安排时间线（今天要触发的提醒按时间排列，布局重设计核心）
            val cal0 = java.util.Calendar.getInstance()
            cal0.set(java.util.Calendar.HOUR_OF_DAY, 0); cal0.set(java.util.Calendar.MINUTE, 0)
            cal0.set(java.util.Calendar.SECOND, 0); cal0.set(java.util.Calendar.MILLISECOND, 0)
            val dayStart = cal0.timeInMillis
            val dayEnd = dayStart + 86_400_000L
            val todayReminders = allReminders
                .filter { it.isActive && it.status != "confirmed" && it.nextTriggerAt in dayStart until dayEnd }
                .sortedBy { it.nextTriggerAt }
            if (todayReminders.isNotEmpty()) {
                item { SectionHeader(zh("今日安排"), Tokens.BrandPrimary, count = todayReminders.size) }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        todayReminders.forEachIndexed { index, r ->
                            TodayTimelineRow(reminder = r, isLast = index == todayReminders.size - 1, onClick = { onReminderClick(r.id) })
                        }
                    }
                }
            }

            // v1.9.8 UI 对齐设计图：日历卡移到「日历」Tab（CalendarScreen），首页只保留列表

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
                                if (smartList == SmartList.ALL) zh("暂无提醒") else zhf("「%s」没有提醒", smartList.label),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                if (smartList == SmartList.ALL) zh("点击右下角 + 创建新提醒") else zh("换个清单看看"),
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
                                Text(zh("创建提醒"))
                            }
                        }
                    }
                }
            } else {
                // 提醒中
                if (reminding.isNotEmpty()) {
                    item { SectionHeader(zh("提醒中"), StatusReminding, count = reminding.size) }
                    items(reminding, key = { it.id }) { reminder ->
                        SwipeableReminderCard(
                            reminder = reminder,
                            // v1.9.7: overdue 用更深一档红色区分
                            statusColor = if (reminder.status == "overdue") StatusOverdue else StatusReminding,
                            onComplete = { viewModel.confirmReminder(reminder) },
                            onDelete = { pendingDelete = reminder },
                            onClick = { onReminderClick(reminder.id) },
                            modifier = Modifier.animateItemPlacement(),
                            onMakeUp = { viewModel.confirmReminder(reminder, isMakeUp = true) },
                            selectionMode = selectionMode,
                            selected = reminder.id in selectedIds,
                            onToggleSelect = {
                                selectedIds = if (reminder.id in selectedIds) selectedIds - reminder.id else selectedIds + reminder.id
                            }
                        )
                    }
                }

                // 等待中（同一人公历+农历生日合并显示一行；批量选择时按原始条目）
                val waitingRows = pairWaitingRows(waiting, selectionMode)
                if (waitingRows.isNotEmpty()) {
                    item { SectionHeader(zh("等待中"), StatusWaiting, count = waitingRows.size) }
                    items(waitingRows, key = { it.key }) { row ->
                        when (row) {
                            is WaitingRow.BirthdayPair -> MergedBirthdayCard(
                                solar = row.solar,
                                lunar = row.lunar,
                                onClick = { onReminderClick(row.nearest.id) }
                            )
                            is WaitingRow.Single -> {
                                val reminder = row.reminder
                                SwipeableReminderCard(
                                    reminder = reminder,
                                    statusColor = StatusWaiting,
                                    onComplete = { viewModel.confirmReminder(reminder) },
                                    onDelete = { pendingDelete = reminder },
                                    onClick = { onReminderClick(reminder.id) },
                                    modifier = Modifier.animateItemPlacement(),
                                    selectionMode = selectionMode,
                                    selected = reminder.id in selectedIds,
                                    onToggleSelect = {
                                        selectedIds = if (reminder.id in selectedIds) selectedIds - reminder.id else selectedIds + reminder.id
                                    }
                                )
                            }
                        }
                    }
                }

                // 已完成
                if (completed.isNotEmpty()) {
                    item { SectionHeader(zh("已完成"), StatusCompleted, count = completed.size) }
                    items(completed, key = { it.id }) { reminder ->
                        SwipeableReminderCard(
                            reminder = reminder,
                            statusColor = StatusCompleted,
                            onComplete = { viewModel.reopenReminder(reminder) },
                            onDelete = { pendingDelete = reminder },
                            onClick = { onReminderClick(reminder.id) },
                            modifier = Modifier.animateItemPlacement(),
                            selectionMode = selectionMode,
                            selected = reminder.id in selectedIds,
                            onToggleSelect = {
                                selectedIds = if (reminder.id in selectedIds) selectedIds - reminder.id else selectedIds + reminder.id
                            }
                        )
                    }
                }
            }
        }
    }

    // v2.4.9: 批量改提醒时间
    if (showBatchTimeDialog) {
        AlertDialog(
            onDismissRequest = { showBatchTimeDialog = false },
            title = { Text(zhf("批量修改 %d 条提醒的时间", selectedIds.size)) },
            text = {
                Column {
                    listOf(6, 7, 8, 9, 10, 12, 14, 16, 18, 20, 22).forEach { h ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(zhf("%d 点", h), Modifier.weight(1f))
                            RadioButton(selected = batchHour == h, onClick = { batchHour = h })
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(zh("分钟"), Modifier.weight(1f))
                        listOf(0, 15, 30, 45).forEach { m ->
                            TextButton(onClick = { batchMinute = m }) {
                                Text(zhf("%02d", m), color = if (batchMinute == m) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.batchChangeTime(selectedIds, batchHour, batchMinute)
                    showBatchTimeDialog = false
                    selectedIds = emptySet()
                    selectionMode = false
                }) { Text(zh("应用")) }
            },
            dismissButton = {
                TextButton(onClick = { showBatchTimeDialog = false }) { Text(zh("取消")) }
            }
        )
    }

    // v2.4.2: 锚点星期修正对话框
    if (anchorMismatches.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { anchorMismatches = emptyList() },
            title = { Text(zh("提醒日错位")) },
            text = {
                Column {
                    Text(zh("以下每周提醒的触发日与设定不符："))
                    Spacer(modifier = Modifier.height(8.dp))
                    anchorMismatches.take(5).forEach { r ->
                        val wd = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
                        val actualCal = java.util.Calendar.getInstance().apply { timeInMillis = r.firstTriggerAt }
                        val actualDow = ((actualCal.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7) + 1
                        Text(
                            zhf("· %1\$s：设定%2\$s，实际%3\$s", r.title, wd[(r.weeklyWeekday ?: 1) - 1], wd[actualDow - 1]),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        viewModel.fixAnchorWeekdays(anchorMismatches)
                        anchorMismatches = emptyList()
                    }
                }) { Text(zh("修正为设定星期")) }
            },
            dismissButton = {
                TextButton(onClick = { anchorMismatches = emptyList() }) { Text(zh("忽略")) }
            }
        )
    }

    // 删除确认对话框
    pendingDelete?.let { reminder ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(zh("删除提醒")) },
            text = { Text(zhf("确定要删除「%s」吗？此操作不可恢复。", reminder.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteReminder(reminder.id)
                        pendingDelete = null
                    }
                ) {
                    Text(zh("删除"), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(zh("取消"))
                }
            }
        )
    }

    // 批次3 功能6: 单条分享卡片粘贴导入对话框（聊天里收到的 JSON 直接粘进来）
    if (showCardImportDialog) {
        AlertDialog(
            onDismissRequest = { showCardImportDialog = false },
            title = { Text(zh("导入分享卡片")) },
            text = {
                Column {
                    Text(zh("把聊天里收到的提醒卡片 JSON 粘贴到下面，即可导入这条提醒："))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cardImportText,
                        onValueChange = { cardImportText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        placeholder = { Text("{ \"version\": 1, \"reminders\": [ ... ] }") },
                        singleLine = false,
                        isError = cardImportMsg != null
                    )
                    cardImportMsg?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val json = cardImportText.trim()
                    if (json.isEmpty()) {
                        cardImportMsg = zh("请先粘贴分享卡片内容")
                        return@TextButton
                    }
                    val entity = BackupService.importSingle(json)
                    if (entity == null) {
                        cardImportMsg = zh("不是有效的分享卡片内容")
                        return@TextButton
                    }
                    try {
                        // 强制 id=0 让 Room 重新自增，避免与现有行主键冲突
                        var imported = entity.copy(id = 0)
                        // I10: 导入时重算下次触发时间（对齐 WebDAV replaceLocal），
                        //     避免备份里的过期 nextTriggerAt 导致立即触发或永不触发
                        imported = imported.copy(nextTriggerAt = com.reminderapp.service.ReminderEngine.calculateNextTrigger(imported, com.reminderapp.ReminderApp.instance))
                        scope.launch {
                            val newId = ReminderApp.instance.database.reminderDao().insert(imported)
                            ReminderApp.instance.scheduler.schedule(imported.copy(id = newId))
                            com.reminderapp.service.SyncStore.touchLocalChange()
                            com.reminderapp.receiver.ReminderWidgetProvider.refresh(cardImportContext)
                            cardImportText = ""
                            cardImportMsg = null
                            showCardImportDialog = false
                        }
                    } catch (e: Exception) {
                        cardImportMsg = zhf("导入失败：%s", e.message ?: zh("未知错误"))
                    }
                }) {
                    Text(zh("导入"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCardImportDialog = false }) {
                    Text(zh("取消"))
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
                    zhf("%1\$s年%2\$s月%3\$s日 的任务", y, m, d),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (dateReminders.isEmpty()) {
                    Text(
                        zh("这一天没有提醒"),
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
                            ReminderCard(r, StatusReminding, onDelete = { pendingDelete = r }, onClick = { onReminderClick(r.id) })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // 批次2 功能2: 打卡成功正向反馈卡片（顶部浮层，自动消失）
    Box(modifier = Modifier.fillMaxSize()) {
        com.reminderapp.ui.component.CheckInFeedbackCard(
            text = checkInFeedback,
            onDismiss = viewModel::consumeCheckInFeedback
        )
    }
}

/** 分组标题：小色条 + 标题 + 数量（设计图风格） */
@Composable
fun SectionHeader(title: String, color: Color, count: Int? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        if (count != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "· $count",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
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
    modifier: Modifier = Modifier,
    /** 批次3 功能2：逾期提醒的「补打今天」入口，传 null 则不显示 */
    onMakeUp: (() -> Unit)? = null,
    /** v2.1.1: 批量管理透传 */
    selectionMode: Boolean = false,
    selected: Boolean = false,
    onToggleSelect: (() -> Unit)? = null
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
                        animated > 0f && isDone -> zh("撤销")
                        animated > 0f -> zh("完成")
                        else -> zh("删除")
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
            ReminderCard(
                reminder, statusColor, onDelete = onDelete, onClick = onClick, onMakeUp = onMakeUp,
                selectionMode = selectionMode, selected = selected, onToggleSelect = onToggleSelect
            )
        }
    }
}

/** 首页概览卡片：待处理数量 + 最近一次提醒（v1.8.7 改品牌渐变卡，滴答清单风格） */
@Composable
fun OverviewCard(unhandledCount: Int, nextReminder: ReminderEntity?, todayDone: Int = 0) {
    // v2.2.1 设计语言：日期大标题 + 农历徽章 + 待办强调 + 光斑装饰（对齐 iOS OverviewCard）
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Tokens.RadiusCard),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Tokens.RadiusCard))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Tokens.BrandGradientStart, Primary, Tokens.BrandPrimaryDark)
                    )
                )
        ) {
            // 装饰光斑（右上角柔光圆）
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(130.dp)
                    .offset(x = 45.dp, y = (-55).dp)
                    .background(Color.White.copy(alpha = 0.14f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )
                    .padding(18.dp)
            ) {
                Column {
                    // 日期大标题 + 农历徽章
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            overviewDateTitle(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            overviewWeekday(),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        com.reminderapp.service.LunarCalendar.solarToLunar(System.currentTimeMillis())
                            ?.let { lunar ->
                                Row(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.18f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.DateRange, contentDescription = null,
                                        tint = Color.White, modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        overviewLunar(lunar),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
                    Spacer(modifier = Modifier.height(12.dp))
                    // 待处理 + 下次提醒
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    zhf("待处理 %s 项", unhandledCount),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            val nextText = nextReminder?.let {
                                val f = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                                "${it.title} · ${f.format(Date(it.nextTriggerAt))}"
                            } ?: zh("暂无即将到来的提醒")
                            Text(
                                nextText,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        // v2.3.0: 今日完成率环（感知强的核心视觉，对齐 iOS）
                        Box(
                            modifier = Modifier.size(62.dp).padding(end = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val stroke = 6.dp.toPx()
                                val inset = stroke / 2
                                val arcSize = androidx.compose.ui.geometry.Size(
                                    (size.width - stroke).coerceAtLeast(1f),
                                    (size.height - stroke).coerceAtLeast(1f)
                                )
                                drawArc(
                                    color = Color.White.copy(alpha = 0.22f),
                                    startAngle = 0f, sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                                    size = arcSize,
                                    style = Stroke(width = stroke)
                                )
                                val total = todayDone + unhandledCount
                                val progress = if (total > 0) todayDone.toFloat() / total else 0f
                                drawArc(
                                    color = Color.White,
                                    startAngle = -90f,
                                    sweepAngle = 360f * progress.coerceIn(0.02f, 1f),
                                    useCenter = false,
                                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                                    size = arcSize,
                                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${if (todayDone + unhandledCount > 0) (todayDone * 100 / (todayDone + unhandledCount)) else 0}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    zh("今日完成"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 今天日期标题（如「8月16日」） */
private fun overviewDateTitle(): String =
    SimpleDateFormat("M月d日", Locale.getDefault()).format(Date())

/** 今天周几（如「周六」） */
private fun overviewWeekday(): String =
    SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())

/** 今天农历（如「六月廿三」） */
private fun overviewLunar(lunar: com.reminderapp.service.LunarCalendar.LunarDate): String {
    val monthNames = arrayOf("", "正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊")
    val dayNames = arrayOf("", "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十")
    val month = if (lunar.isLeapMonth) "闰" + monthNames.getOrElse(lunar.month) { "" } else monthNames.getOrElse(lunar.month) { "" }
    val day = if (lunar.day in 1..30) dayNames[lunar.day] else "${lunar.day}日"
    return month + "月" + day
}

/** v2.4.0: 今日安排时间线行（左侧时间 + 竖线圆点 + 右侧卡片，对齐 iOS TodayTimelineView） */
@Composable
fun TodayTimelineRow(reminder: ReminderEntity, isLast: Boolean, onClick: () -> Unit) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // 左侧时间 + 时间线
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(46.dp)
        ) {
            Text(
                timeFormat.format(Date(reminder.nextTriggerAt)),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Tokens.BrandPrimary
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .padding(vertical = 2.dp)
                        .background(Tokens.BrandPrimary.copy(alpha = 0.25f))
                )
            }
        }
        // 圆点
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(9.dp)
                .clip(CircleShape)
                .background(Tokens.BrandPrimary)
                .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        // 右侧卡片
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // emoji 图标（渐变底）
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Tokens.BrandPrimary.copy(alpha = 0.30f),
                                    Tokens.BrandPrimary.copy(alpha = 0.10f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(reminderEmoji(reminder), fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        reminder.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (reminder.status == "overdue") zh("已逾期") else zh("等待中"),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (reminder.status == "overdue") Tokens.StatusOverdue else Tokens.StatusWaiting
                    )
                }
                Text(
                    zhf("%s 后", relativeMinutes(reminder.nextTriggerAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** 相对时间（分钟/小时） */
private fun relativeMinutes(target: Long): String {
    val minutes = ((target - System.currentTimeMillis()) / 60_000L).toInt()
    return when {
        minutes <= 0 -> zh("已到点")
        minutes < 60 -> zhf("%s 分", minutes)
        else -> zhf("%s 小时 %s 分", minutes / 60, minutes % 60)
    }
}

/** 规则提醒的显示文本，如「每季度第2周周二」 */
fun ruleLabel(reminder: ReminderEntity): String {
    val periodLabel = when (reminder.rulePeriod) {
        "monthly" -> zh("每月")
        "yearly" -> zh("每年")
        else -> zh("每季度")
    }
    val weekday = arrayOf(zh("周一"), zh("周二"), zh("周三"), zh("周四"), zh("周五"), zh("周六"), zh("周日"))
        .getOrElse((reminder.ruleWeekday ?: 1) - 1) { zhf("周%s", reminder.ruleWeekday) }
    return zhf("%1\$s第%2\$s周%3\$s", periodLabel, reminder.ruleWeek ?: 1, weekday)
}

/** 提醒类型 → 展示用 emoji（v2.2.1: 恢复——Material 线条图标视觉存在感弱，emoji 更醒目） */
fun reminderEmoji(reminder: ReminderEntity): String = when {
    reminder.kind == "date" && reminder.dateType == "holiday" -> "🎉"
    reminder.kind == "date" && reminder.dateType == "lunar_birthday" -> "🌙"
    reminder.kind == "date" && reminder.dateType == "solar_birthday" -> "🎂"
    reminder.kind == "rule" -> "📅"
    else -> when (reminder.cycle) {
        "once" -> "⏰"
        "daily" -> "🔁"
        "weekly" -> "📆"
        "biweekly" -> "📆"
        "monthly" -> "🗓"
        "quarterly" -> "📊"
        "yearly" -> "🎯"
        "custom" -> "⏳"
        else -> "💡"
    }
}


/** 提醒类型 → 图标容器底色（与 iOS kindBadgeColor 对应） */
fun reminderKindColor(reminder: ReminderEntity): Color = when {
    reminder.kind == "date" && reminder.dateType == "holiday" -> Color(0xFFF39C12)
    reminder.kind == "date" && reminder.dateType == "lunar_birthday" -> Color(0xFF9C27B0)
    reminder.kind == "date" && reminder.dateType == "solar_birthday" -> Color(0xFFE91E63)
    reminder.kind == "rule" -> Color(0xFF1ABC9C)
    else -> Color(0xFF3498DB)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReminderCard(
    reminder: ReminderEntity,
    statusColor: Color,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    /** 批次3 功能2：逾期提醒的「补打今天」入口，传 null 则不显示 */
    onMakeUp: (() -> Unit)? = null,
    /** v2.1.1: 批量管理——选择模式标记/选择回调，非空时卡片进入多选态 */
    selectionMode: Boolean = false,
    selected: Boolean = false,
    onToggleSelect: (() -> Unit)? = null
) {
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    val statusText = when (reminder.status) {
        "notifying" -> zh("需要确认")
        "overdue" -> zh("已逾期")   // v1.9.7: 递增重试到上限
        "idle", "pending" -> zh("等待中")
        "confirmed" -> zh("已完成")
        else -> ""
    }
    val kindLabel = when {
        reminder.kind == "date" && reminder.dateType == "holiday" -> "🎉${reminder.holidayName ?: ""}"
        reminder.kind == "date" && reminder.dateType == "lunar_birthday" -> zh("🌙农历生日")
        reminder.kind == "date" && reminder.dateType == "solar_birthday" -> zh("🎂生日")
        reminder.kind == "rule" -> ruleLabel(reminder)
        else -> when (reminder.cycle) {
            "once" -> zh("仅一次")
            "daily" -> zh("每天")
            "weekly" -> zh("每周")
            "biweekly" -> zh("每两周")
            "monthly" -> zh("每月")
            "quarterly" -> zh("每季度")
            "yearly" -> zh("每年")
            "custom" -> zhf("每%s天", reminder.customDays)
            else -> reminder.cycle
        }
    }
    val priorityLabel = when (reminder.priority) {
        "high" -> zh("🔴高")
        "low" -> zh("⚪低")
        else -> zh("🟢中")
    }
    val isDone = reminder.status == "confirmed"

    // 液态玻璃卡片：半透明白 + 大圆角 + 高光描边 + 柔和阴影（对齐设计图）
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (selectionMode) onToggleSelect?.invoke() else onClick() },
                onLongClick = { if (selectionMode) onToggleSelect?.invoke() else onDelete() },
                onClickLabel = zh("打开详情"),
                onLongClickLabel = zh("长按删除")
            ),
        shape = RoundedCornerShape(Tokens.RadiusCell),
        colors = CardDefaults.cardColors(
            // v2.0.22: 深色模式下固定白色卡片突兀、层级错乱，改用主题 surface
            //（浅色下仍是白色玻璃观感，深色下自动适配）
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // v2.1.1: 批量选择指示（选择模式下显示圆点/对勾）
            if (selectionMode) {
                Icon(
                    imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (selected) zh("已选择") else zh("未选择"),
                    tint = if (selected) Tokens.BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            // v2.2.1: 彩色渐变底图标容器（同色系由深到浅），emoji 更有层次
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                reminderKindColor(reminder).copy(alpha = 0.32f),
                                reminderKindColor(reminder).copy(alpha = 0.12f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = reminderEmoji(reminder),
                    fontSize = 22.sp
                )
            }
            Spacer(modifier = Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                // 标题（已完成划线变灰）
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                // meta：类型 · 优先级 · 重试
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$kindLabel · $priorityLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (reminder.retryCount > 0 && !isDone) {
                        Text(
                            text = zhf(" · 第%s次重试", reminder.retryCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF39C12)
                        )
                    }
                }
                // 状态胶囊（设计图独立 chip）+ 逾期时的「补打今天」快捷入口
                if (statusText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = statusColor,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(statusColor.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                        if (reminder.status == "overdue" && onMakeUp != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = zh("补打今天"),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(StatusOverdue)
                                    .clickable(
                                        onClickLabel = zh("补打今天：按今天完成并推进到下一个周期")
                                    ) { onMakeUp() }
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                // v2.0.22: 原条件 statusText.isEmpty() 永远为 false（所有状态都有文案），
                // 备注在首页被完全隐藏，只能进详情看；去掉该条件，备注跟随状态胶囊展示
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
            // 右侧时间：状态色（设计图）
            Text(
                text = dateFormat.format(Date(reminder.nextTriggerAt)),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = statusColor
            )
        }
    }
}

// ---- 等待中同人生日合并（显示层一行，底层两条独立提醒） ----

/** 等待中显示条目：同一人的公历+农历生日对，或单条提醒 */
private sealed class WaitingRow {
    data class BirthdayPair(val solar: ReminderEntity, val lunar: ReminderEntity) : WaitingRow() {
        val nearest: ReminderEntity get() = if (solar.nextTriggerAt <= lunar.nextTriggerAt) solar else lunar
    }
    data class Single(val reminder: ReminderEntity) : WaitingRow()

    val key: Long
        get() = when (this) {
            is BirthdayPair -> solar.id
            is Single -> reminder.id
        }
}

/**
 * 配对规则：kind=date、dateType 为 solar/lunar、标题分别以「（公历）/（农历）」结尾
 * 且去后缀后同名。批量选择模式下不合并（按原始条目逐条可选）。
 */
private fun pairWaitingRows(waiting: List<ReminderEntity>, selectionMode: Boolean): List<WaitingRow> {
    if (selectionMode) return waiting.map { WaitingRow.Single(it) }
    val solar = mutableMapOf<String, ReminderEntity>()
    val lunar = mutableMapOf<String, ReminderEntity>()
    val rest = mutableListOf<ReminderEntity>()
    for (r in waiting) {
        val t = r.title
        when {
            r.kind == "date" && r.dateType == "solar_birthday" && t.endsWith("（公历）") ->
                solar[t.removeSuffix("（公历）")] = r
            r.kind == "date" && r.dateType == "lunar_birthday" && t.endsWith("（农历）") ->
                lunar[t.removeSuffix("（农历）")] = r
            else -> rest.add(r)
        }
    }
    val rows = mutableListOf<WaitingRow>()
    val singles = mutableListOf<ReminderEntity>()
    for ((base, s) in solar) {
        val l = lunar.remove(base)
        if (l != null) rows.add(WaitingRow.BirthdayPair(s, l)) else singles.add(s)
    }
    singles.addAll(lunar.values)
    singles.addAll(rest)
    rows.addAll(singles.map { WaitingRow.Single(it) })
    return rows.sortedBy {
        when (it) {
            is WaitingRow.BirthdayPair -> minOf(it.solar.nextTriggerAt, it.lunar.nextTriggerAt)
            is WaitingRow.Single -> it.reminder.nextTriggerAt
        }
    }
}

/** 同一人公历+农历生日合并卡：点击进入「下次先到」那条的详情 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun MergedBirthdayCard(solar: ReminderEntity, lunar: ReminderEntity, onClick: () -> Unit) {
    val nearest = if (solar.nextTriggerAt <= lunar.nextTriggerAt) solar else lunar
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onClickLabel = zh("打开详情")),
        shape = RoundedCornerShape(Tokens.RadiusCell),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFFE91E63).copy(alpha = 0.32f),
                                Color(0xFF8E24AA).copy(alpha = 0.12f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(reminderEmoji(nearest), fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    solar.title.removeSuffix("（公历）"),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        zhf("公历 %d月%d日", solar.targetMonth ?: 1, solar.targetDay ?: 1),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD81B60),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFD81B60).copy(alpha = 0.10f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                    Text(
                        zhf("农历 %d月%d", lunar.targetMonth ?: 1, lunar.targetDay ?: 1),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8E24AA),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF8E24AA).copy(alpha = 0.10f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Text(
                    zh("等待中"), style = MaterialTheme.typography.labelSmall, color = StatusWaiting
                )
            }
            Text(
                zhf("%s 后", relativeMinutes(nearest.nextTriggerAt)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
