package com.reminderapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zh
import com.reminderapp.ui.comic.comicColors
import com.reminderapp.ui.comic.pairWaitingRows
import com.reminderapp.ui.component.CheckInFeedbackCard
import com.reminderapp.ui.screen.home.AnchorMismatchDialog
import com.reminderapp.ui.screen.home.BareCompletedDrawer
import com.reminderapp.ui.screen.home.BareNotificationStatusLine
import com.reminderapp.ui.screen.home.BatchTimeDialog
import com.reminderapp.ui.screen.home.ExtraDueDrawer
import com.reminderapp.ui.screen.home.HomeDeleteDialog
import com.reminderapp.ui.screen.home.HomeHeaderSection
import com.reminderapp.ui.screen.home.HomeHeroCard
import com.reminderapp.ui.screen.home.HomeUpcomingCard
import com.reminderapp.ui.screen.home.ShareCardImportDialog
import com.reminderapp.ui.screen.home.matchSmartList
import com.reminderapp.ui.screen.home.matchesSearch
import com.reminderapp.ui.viewmodel.HomeViewModel
import java.util.Calendar
import kotlinx.coroutines.launch

enum class SmartList(val label: String) {
    ALL(zh("全部")),
    TODAY(zh("今天")),
    TOMORROW(zh("明天")),
    WEEK(zh("本周")),
    MONTH(zh("本月")),
    HIGH(zh("高优先级")),
    DONE(zh("已完成"))
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCreateReminder: () -> Unit,
    onQuickCreate: (ReminderEntity) -> Unit = {},
    canUndoCreate: Boolean = false,
    onUndoCreate: () -> Unit = {},
    onReminderClick: (Long) -> Unit,
    onAIChat: () -> Unit,
    onDeleteReminder: (Long) -> Unit,
    onExport: () -> Unit = {},
    onImport: () -> Unit = {},
    onOpenSyncSettings: () -> Unit = {},
    onOpenStats: () -> Unit = {},
    onOpenDiagnostics: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onExportICS: () -> Unit = {},
    onCheckUpdate: () -> Unit = {},
    onNearbyShare: () -> Unit = {},
    onSyncNow: () -> Unit = {}
) {
    val colors = comicColors()
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var kindFilter by remember { mutableStateOf("all") }
    var sortOrder by remember { mutableStateOf("time") }
    val order = when (sortOrder) {
        "title" -> compareBy<ReminderEntity> { it.title }
        "priority" -> compareBy<ReminderEntity> {
            when (it.priority) { "high" -> 0; "normal" -> 1; else -> 2 }
        }.thenBy { it.nextTriggerAt }
        else -> compareBy { it.nextTriggerAt }
    }
    fun matchesKind(r: ReminderEntity) = kindFilter == "all" || r.kind == kindFilter

    val grouped by viewModel.groupedReminders.collectAsState()
    val allReminders by viewModel.allReminders.collectAsState()
    val checkInFeedback by viewModel.checkInFeedback.collectAsState()
    val undo by viewModel.undo.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.ensureSchedules()
        viewModel.checkMissed()
    }

    var anchorMismatches by remember { mutableStateOf<List<ReminderEntity>>(emptyList()) }
    LaunchedEffect(allReminders.size) {
        anchorMismatches = allReminders.filter { r ->
            r.weeklyWeekday in 1..7 && r.isActive && r.status != "confirmed" && run {
                val cal = Calendar.getInstance().apply { timeInMillis = r.firstTriggerAt }
                val anchorDow = ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7) + 1
                anchorDow != r.weeklyWeekday
            }
        }
    }

    var pendingDelete by remember { mutableStateOf<ReminderEntity?>(null) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showBatchTimeDialog by remember { mutableStateOf(false) }
    var showCardImportDialog by remember { mutableStateOf(false) }
    var smartList by remember { mutableStateOf(SmartList.ALL) }
    var searchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val now = System.currentTimeMillis()
    val due = allReminders.filter { r ->
        r.isActive && r.status != "confirmed" &&
            (r.status == "notifying" || r.status == "overdue" ||
                (r.status == "snoozed" && r.nextTriggerAt <= now) ||
                (r.status == "pending" && r.nextTriggerAt <= now)) &&
            matchSmartList(r, smartList) && matchesSearch(r, searchQuery) && matchesKind(r)
    }.sortedWith(order)
    val dueIds = due.map { it.id }.toSet()
    val waiting = grouped.waiting.sortedWith(order).filter {
        matchSmartList(it, smartList) && matchesSearch(it, searchQuery) && matchesKind(it) && it.id !in dueIds
    }
    val completed = grouped.completed.sortedWith(order).filter {
        matchSmartList(it, smartList) && matchesSearch(it, searchQuery) && matchesKind(it)
    }
    val waitingRows = remember(waiting, selectionMode) { pairWaitingRows(waiting, selectionMode) }

    fun toggleSelect(id: Long) {
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.canvas)) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeHeaderSection(
                smartList = smartList, onSelectSmartList = { smartList = it },
                kindFilter = kindFilter, onKindFilterChange = { kindFilter = it },
                sortOrder = sortOrder, onSortOrderChange = { sortOrder = it },
                searchOpen = searchOpen,
                onToggleSearch = { if (searchOpen) searchQuery = ""; searchOpen = !searchOpen },
                searchQuery = searchQuery, onSearchQueryChange = { searchQuery = it },
                selectionMode = selectionMode, selectedCount = selectedIds.size,
                onToggleSelectAll = {
                    val all = allReminders.map { it.id }.toSet()
                    selectedIds = if (selectedIds.size == all.size) emptySet() else all
                },
                onBatchComplete = { viewModel.batchComplete(selectedIds); selectedIds = emptySet(); selectionMode = false },
                onBatchDelete = { viewModel.batchDelete(selectedIds); selectedIds = emptySet(); selectionMode = false },
                onBatchChangeTime = { showBatchTimeDialog = true },
                onExitSelection = { selectedIds = emptySet(); selectionMode = false },
                onEnterSelection = { selectionMode = true; selectedIds = emptySet() },
                onCreateReminder = onCreateReminder, onQuickCreate = onQuickCreate, onAIChat = onAIChat,
                canUndoCreate = canUndoCreate, onUndoCreate = onUndoCreate,
                canUndoComplete = undo != null, onUndoComplete = { viewModel.undoCompletion() },
                onSyncNow = onSyncNow, onOpenSyncSettings = onOpenSyncSettings, onOpenStats = onOpenStats,
                onImport = onImport, onShowCardImport = { showCardImportDialog = true },
                onExport = onExport, onExportICS = onExportICS, onNearbyShare = onNearbyShare,
                onCheckUpdate = onCheckUpdate, onOpenSettings = onOpenSettings
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "hero-card") {
                    HomeHeroCard(
                        due = due, nextUpcoming = waiting.firstOrNull(),
                        waitingCount = waiting.size, dbEmpty = allReminders.isEmpty(),
                        onReminderClick = onReminderClick,
                        onConfirm = { r -> haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.confirmReminder(r) },
                        onMakeUp = { r -> viewModel.confirmReminder(r, isMakeUp = true) },
                        onSnooze = { r -> viewModel.snoozeLater(r, 15) },
                        onSkip = { r -> viewModel.skip(r) },
                        onCreateReminder = onCreateReminder
                    )
                }
                item(key = "bare-lines") {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        ExtraDueDrawer(
                            due = due, selectionMode = selectionMode, selectedIds = selectedIds,
                            onToggleSelect = ::toggleSelect, onReminderClick = onReminderClick,
                            onConfirm = { viewModel.confirmReminder(it) }
                        )
                        BareNotificationStatusLine(onClick = onOpenDiagnostics)
                        BareCompletedDrawer(
                            completed = completed, selectionMode = selectionMode, selectedIds = selectedIds,
                            onToggleSelect = ::toggleSelect, onReminderClick = onReminderClick,
                            onReopen = { viewModel.reopenReminder(it) }, onDeleteRequest = { pendingDelete = it }
                        )
                    }
                }
                if (allReminders.isNotEmpty()) {
                    item(key = "upcoming-card") {
                        HomeUpcomingCard(
                            rows = waitingRows, selectionMode = selectionMode, selectedIds = selectedIds,
                            onToggleSelect = ::toggleSelect, onReminderClick = onReminderClick,
                            onConfirm = { r -> haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.confirmReminder(r) },
                            onDeleteRequest = { pendingDelete = it }
                        )
                    }
                }
            }
        }

        CheckInFeedbackCard(text = checkInFeedback, onDismiss = viewModel::consumeCheckInFeedback)
    }

    if (showBatchTimeDialog) {
        BatchTimeDialog(
            selectedCount = selectedIds.size,
            onApply = { h, m -> viewModel.batchChangeTime(selectedIds, h, m); showBatchTimeDialog = false; selectedIds = emptySet(); selectionMode = false },
            onDismiss = { showBatchTimeDialog = false }
        )
    }
    AnchorMismatchDialog(
        mismatches = anchorMismatches,
        onFix = { scope.launch { viewModel.fixAnchorWeekdays(anchorMismatches); anchorMismatches = emptyList() } },
        onDismiss = { anchorMismatches = emptyList() }
    )
    HomeDeleteDialog(
        reminder = pendingDelete,
        onConfirm = { target -> onDeleteReminder(target.id); pendingDelete = null },
        onDismiss = { pendingDelete = null }
    )
    ShareCardImportDialog(visible = showCardImportDialog, onDismiss = { showCardImportDialog = false })
}
