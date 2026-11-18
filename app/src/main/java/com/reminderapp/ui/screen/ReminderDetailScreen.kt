package com.reminderapp.ui.screen

import android.app.NotificationManager
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.ReminderApp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.service.QuietHoursStore
import com.reminderapp.service.ReminderEngine
import com.reminderapp.service.ReminderShare
import com.reminderapp.ui.comic.ComicButton
import com.reminderapp.ui.comic.ComicCollapsibleRow
import com.reminderapp.ui.comic.ComicTextAction
import com.reminderapp.ui.comic.ComicTopBar
import com.reminderapp.ui.comic.comicColors
import com.reminderapp.ui.comic.formatShortDateTime
import com.reminderapp.ui.component.CheckInFeedbackCard
import com.reminderapp.ui.screen.detail.DetailDialogs
import com.reminderapp.ui.screen.detail.DetailHeroCard
import com.reminderapp.ui.screen.detail.DetailSettingsCard
import com.reminderapp.ui.viewmodel.ReminderDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReminderDetailScreen(
    viewModel: ReminderDetailViewModel,
    onBack: () -> Unit,
    highlightConfirm: Boolean = false,
    onEdit: (ReminderEntity) -> Unit = {}
) {
    val context = LocalContext.current
    val colors = comicColors()
    val reminder by viewModel.reminder.collectAsState()
    val records by viewModel.records.collectAsState()
    val checkInFeedback by viewModel.checkInFeedback.collectAsState()
    val undo by viewModel.undo.collectAsState()

    if (reminder == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.canvas)
                .navigationBarsPadding()
        ) {
            ComicTopBar(title = zh("提醒详情"), onBack = onBack)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(zh("提醒不存在或已被删除"), color = colors.muted, fontSize = 15.sp)
                    ComicButton(text = zh("返回首页"), onClick = onBack)
                }
            }
        }
        return
    }

    val currentReminder = reminder!!
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFullScreenPermissionDialog by remember { mutableStateOf(false) }
    var showCustomSnoozeDialog by remember { mutableStateOf(false) }
    var quietHoursEnabled by remember { mutableStateOf(false) }
    var quietStartMinute by remember { mutableIntStateOf(22 * 60) }
    var quietEndMinute by remember { mutableIntStateOf(8 * 60) }
    var showQuietStartPicker by remember { mutableStateOf(false) }
    var showQuietEndPicker by remember { mutableStateOf(false) }
    var previewExpanded by remember { mutableStateOf(false) }
    var historyExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(currentReminder.id) {
        val start = currentReminder.quietStartMinute ?: QuietHoursStore.startMinute(context, currentReminder.id)
        val end = currentReminder.quietEndMinute ?: QuietHoursStore.endMinute(context, currentReminder.id)
        if (start != null && end != null) {
            quietHoursEnabled = true
            quietStartMinute = start
            quietEndMinute = end
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            ComicTopBar(
                title = zh("提醒详情"),
                onBack = onBack,
                trailing = {
                    ComicTextAction(
                        text = zh("分享"),
                        onClick = { ReminderShare.share(context, currentReminder) },
                        accent = true
                    )
                    ComicTextAction(
                        text = zh("删除"),
                        onClick = { showDeleteDialog = true },
                        danger = true
                    )
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailHeroCard(
                    reminder = currentReminder,
                    canUndo = undo != null,
                    highlightConfirm = highlightConfirm,
                    onEdit = { onEdit(currentReminder) },
                    onConfirm = { viewModel.confirm() },
                    onMakeUp = { viewModel.confirm(isMakeUp = true) },
                    onSnoozeMinutes = { m -> viewModel.snooze(minutes = m) },
                    onSnoozeTomorrow = { viewModel.snoozeTomorrow() },
                    onOpenCustomSnooze = { showCustomSnoozeDialog = true },
                    onSkip = { viewModel.skip() },
                    onUndo = { viewModel.undoCompletion() }
                )

                DetailSettingsCard(
                    reminder = currentReminder,
                    quietHoursEnabled = quietHoursEnabled,
                    quietStartMinute = quietStartMinute,
                    quietEndMinute = quietEndMinute,
                    onToggleCritical = { enabled ->
                        if (enabled) {
                            val nm = context.getSystemService(NotificationManager::class.java)
                            val needFullScreen = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                                (nm == null || !nm.canUseFullScreenIntent())
                            val needPolicy = nm?.isNotificationPolicyAccessGranted != true
                            if (needFullScreen || needPolicy) showFullScreenPermissionDialog = true
                        }
                        viewModel.setCritical(enabled)
                    },
                    onToggleQuietHours = { enabled ->
                        quietHoursEnabled = enabled
                        if (enabled) {
                            QuietHoursStore.set(context, currentReminder.id, quietStartMinute, quietEndMinute)
                        } else {
                            QuietHoursStore.set(context, currentReminder.id, null, null)
                        }
                        viewModel.rescheduleQuietHours()
                    },
                    onPickQuietStart = { showQuietStartPicker = true },
                    onPickQuietEnd = { showQuietEndPicker = true }
                )

                // Bare Collapsible Future Triggers Preview
                val futureDates = remember(currentReminder) {
                    ReminderEngine.futureTriggers(currentReminder, count = 10, context = ReminderApp.instance)
                }
                if (futureDates.isNotEmpty()) {
                    ComicCollapsibleRow(
                        title = zh("未来触发"),
                        trailing = zhf("%s 次预览", futureDates.size),
                        expanded = previewExpanded,
                        onClick = { previewExpanded = !previewExpanded }
                    )
                    AnimatedVisibility(visible = previewExpanded) {
                        Column(
                            modifier = Modifier.padding(start = 14.dp, end = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            futureDates.forEachIndexed { idx, ts ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "#${idx + 1}",
                                        color = colors.muted,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        style = TextStyle(fontFeatureSettings = "tnum")
                                    )
                                    Text(
                                        text = dateFormat.format(Date(ts)),
                                        color = colors.ink,
                                        fontSize = 13.sp,
                                        style = TextStyle(fontFeatureSettings = "tnum")
                                    )
                                }
                            }
                        }
                    }
                }

                // Bare Collapsible History Records
                if (records.isNotEmpty()) {
                    ComicCollapsibleRow(
                        title = zh("操作记录"),
                        trailing = "${records.size}",
                        expanded = historyExpanded,
                        onClick = { historyExpanded = !historyExpanded }
                    )
                    AnimatedVisibility(visible = historyExpanded) {
                        Column(
                            modifier = Modifier.padding(start = 14.dp, end = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            records.forEach { record ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = when (record.action) {
                                            "confirmed" -> "✓ " + zh("确认完成")
                                            "snoozed" -> "◷ " + zh("稍后提醒")
                                            "notified" -> "• " + zh("提醒")
                                            "skipped" -> "↷ " + zh("跳过")
                                            else -> record.action
                                        },
                                        color = colors.ink,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = formatShortDateTime(record.timestamp),
                                        color = colors.muted,
                                        fontSize = 12.sp,
                                        style = TextStyle(fontFeatureSettings = "tnum")
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }

        CheckInFeedbackCard(
            text = checkInFeedback,
            onDismiss = viewModel::consumeCheckInFeedback
        )
    }

    DetailDialogs(
        reminderTitle = currentReminder.title,
        showDeleteDialog = showDeleteDialog,
        onConfirmDelete = {
            showDeleteDialog = false
            viewModel.delete()
            onBack()
        },
        onDismissDelete = { showDeleteDialog = false },
        showFullScreenPermissionDialog = showFullScreenPermissionDialog,
        onDismissPermission = { showFullScreenPermissionDialog = false },
        showCustomSnoozeDialog = showCustomSnoozeDialog,
        onConfirmCustomSnooze = { m ->
            showCustomSnoozeDialog = false
            viewModel.snooze(minutes = m.toLong())
        },
        onDismissCustomSnooze = { showCustomSnoozeDialog = false },
        showQuietStartPicker = showQuietStartPicker,
        quietStartMinute = quietStartMinute,
        onConfirmQuietStart = { m ->
            showQuietStartPicker = false
            quietStartMinute = m
            QuietHoursStore.set(context, currentReminder.id, quietStartMinute, quietEndMinute)
            viewModel.rescheduleQuietHours()
        },
        onDismissQuietStart = { showQuietStartPicker = false },
        showQuietEndPicker = showQuietEndPicker,
        quietEndMinute = quietEndMinute,
        onConfirmQuietEnd = { m ->
            showQuietEndPicker = false
            quietEndMinute = m
            QuietHoursStore.set(context, currentReminder.id, quietStartMinute, quietEndMinute)
            viewModel.rescheduleQuietHours()
        },
        onDismissQuietEnd = { showQuietEndPicker = false }
    )
}
