package com.reminderapp.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.service.ReminderScheduler
import com.reminderapp.ui.comic.ComicCollapsibleRow
import com.reminderapp.ui.comic.ComicTextAction
import com.reminderapp.ui.comic.comicColors
import com.reminderapp.ui.comic.formatShortDateTime
import com.reminderapp.ui.comic.formatTabularTime

@Composable
fun BareNotificationStatusLine(onClick: () -> Unit) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var notifications by remember { mutableStateOf(true) }
    var exact by remember { mutableStateOf(true) }

    DisposableEffect(owner) {
        val refresh = {
            notifications = NotificationManagerCompat.from(context).areNotificationsEnabled()
            exact = ReminderScheduler(context).canScheduleExact()
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh()
        }
        owner.lifecycle.addObserver(observer)
        refresh()
        onDispose { owner.lifecycle.removeObserver(observer) }
    }

    val healthy = notifications && exact
    val label = when {
        !notifications -> zh("通知需要设置") + " · " + zh("点击自检")
        !exact -> zh("提醒可能延迟") + " · " + zh("点击自检")
        else -> zh("通知与精确闹钟已授权")
    }
    ComicCollapsibleRow(
        title = label,
        warning = !healthy,
        trailing = if (!healthy) zh("设置") else null,
        onClick = onClick
    )
}

@Composable
fun ExtraDueDrawer(
    due: List<ReminderEntity>,
    selectionMode: Boolean,
    selectedIds: Set<Long>,
    onToggleSelect: (Long) -> Unit,
    onReminderClick: (Long) -> Unit,
    onConfirm: (ReminderEntity) -> Unit
) {
    if (due.size <= 1) return
    var expanded by remember { mutableStateOf(false) }
    val colors = comicColors()
    Column(modifier = Modifier.fillMaxWidth()) {
        ComicCollapsibleRow(
            title = zhf("待处理 %s 项", due.size),
            expanded = expanded,
            warning = true,
            onClick = { expanded = !expanded }
        )
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(start = 12.dp, end = 4.dp, bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                due.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectionMode) onToggleSelect(item.id) else onReminderClick(item.id)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectionMode) {
                            Icon(
                                imageVector = if (item.id in selectedIds) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (item.id in selectedIds) colors.accent else colors.muted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = formatTabularTime(item.nextTriggerAt),
                            color = colors.muted,
                            fontSize = 12.5.sp,
                            style = TextStyle(fontFeatureSettings = "tnum"),
                            modifier = Modifier.width(48.dp)
                        )
                        Text(
                            text = item.title,
                            color = colors.ink,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (!selectionMode) {
                            ComicTextAction(
                                text = zh("完成"),
                                onClick = { onConfirm(item) },
                                accent = true,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BareCompletedDrawer(
    completed: List<ReminderEntity>,
    selectionMode: Boolean,
    selectedIds: Set<Long>,
    onToggleSelect: (Long) -> Unit,
    onReminderClick: (Long) -> Unit,
    onReopen: (ReminderEntity) -> Unit,
    onDeleteRequest: (ReminderEntity) -> Unit
) {
    if (completed.isEmpty()) return
    var expanded by remember { mutableStateOf(false) }
    val colors = comicColors()

    Column(modifier = Modifier.fillMaxWidth()) {
        ComicCollapsibleRow(
            title = zh("已完成") + " ${completed.size}",
            expanded = expanded,
            onClick = { expanded = !expanded }
        )
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                completed.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { if (selectionMode) onToggleSelect(item.id) else onReminderClick(item.id) },
                                onLongClick = { if (selectionMode) onToggleSelect(item.id) else onDeleteRequest(item) }
                            )
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectionMode) {
                            Icon(
                                imageVector = if (item.id in selectedIds) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (item.id in selectedIds) colors.accent else colors.muted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = item.title,
                            color = colors.muted,
                            fontSize = 13.5.sp,
                            textDecoration = TextDecoration.LineThrough,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = formatShortDateTime(item.nextTriggerAt),
                            color = colors.muted,
                            fontSize = 11.5.sp,
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )
                        if (!selectionMode) {
                            Spacer(Modifier.width(8.dp))
                            ComicTextAction(
                                text = zh("重新启用"),
                                onClick = { onReopen(item) },
                                accent = true,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
