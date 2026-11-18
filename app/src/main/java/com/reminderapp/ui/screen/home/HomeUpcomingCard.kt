package com.reminderapp.ui.screen.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.ui.comic.ComicCard
import com.reminderapp.ui.comic.WaitingRow
import com.reminderapp.ui.comic.comicColors
import com.reminderapp.ui.comic.cycleSummaryLabel
import com.reminderapp.ui.comic.formatCompactEventTime
import com.reminderapp.ui.comic.lunarMonthDayLabel
import kotlin.math.roundToInt

/**
 * Card 2 — Unlined Breathable Event List (`即将到来`, 1:1 like `最近事件` in IOTA MONITOR).
 * ZERO horizontal divider lines between rows and ZERO bordered tags!
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeUpcomingCard(
    rows: List<WaitingRow>,
    selectionMode: Boolean,
    selectedIds: Set<Long>,
    onToggleSelect: (Long) -> Unit,
    onReminderClick: (Long) -> Unit,
    onConfirm: (ReminderEntity) -> Unit,
    onDeleteRequest: (ReminderEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = comicColors()
    ComicCard(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = zh("即将到来"),
                color = colors.ink,
                fontSize = 16.5.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${rows.size}",
                color = colors.muted,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(fontFeatureSettings = "tnum")
            )
        }

        if (rows.isEmpty()) {
            Text(
                text = zh("暂无即将到来的提醒"),
                color = colors.muted,
                fontSize = 13.5.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            rows.forEach { row ->
                when (row) {
                    is WaitingRow.BirthdayPair -> {
                        val nearest = row.nearest
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { onReminderClick(nearest.id) },
                                    onLongClick = { onDeleteRequest(nearest) }
                                )
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatCompactEventTime(nearest.nextTriggerAt),
                                color = colors.muted,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                style = TextStyle(fontFeatureSettings = "tnum"),
                                modifier = Modifier.width(74.dp)
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = row.solar.title.removeSuffix("（公历）"),
                                    color = colors.ink,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = zhf("公历 %d月%d日", row.solar.targetMonth ?: 1, row.solar.targetDay ?: 1) +
                                        " · " +
                                        zhf("农历 %s", lunarMonthDayLabel(row.lunar.targetMonth ?: 1, row.lunar.targetDay ?: 1)),
                                    color = colors.muted,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFeatureSettings = "tnum")
                                )
                            }
                            if (!selectionMode) {
                                Spacer(Modifier.width(10.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = zh("确认完成"),
                                    tint = colors.muted.copy(alpha = 0.72f),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { onConfirm(nearest) }
                                        .semantics { contentDescription = zh("确认完成") }
                                )
                            }
                        }
                    }
                    is WaitingRow.Single -> {
                        UnlinedEventRow(
                            reminder = row.reminder,
                            selectionMode = selectionMode,
                            selected = row.reminder.id in selectedIds,
                            onToggleSelect = { onToggleSelect(row.reminder.id) },
                            onClick = { onReminderClick(row.reminder.id) },
                            onConfirm = { onConfirm(row.reminder) },
                            onDelete = { onDeleteRequest(row.reminder) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UnlinedEventRow(
    reminder: ReminderEntity,
    selectionMode: Boolean,
    selected: Boolean,
    onToggleSelect: () -> Unit,
    onClick: () -> Unit,
    onConfirm: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = comicColors()
    val density = LocalDensity.current
    val maxOffset = with(density) { 88.dp.toPx() }
    val threshold = with(density) { 56.dp.toPx() }
    var dragX by remember(reminder.id) { mutableFloatStateOf(0f) }
    val animatedX by animateFloatAsState(targetValue = dragX, label = "rowSwipe")

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedX.roundToInt(), 0) }
                .pointerInput(reminder.id, selectionMode) {
                    if (selectionMode) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                dragX >= threshold -> onConfirm()
                                dragX <= -threshold -> onDelete()
                            }
                            dragX = 0f
                        },
                        onDragCancel = { dragX = 0f },
                        onHorizontalDrag = { change, amount ->
                            change.consume()
                            dragX = (dragX + amount).coerceIn(-maxOffset, maxOffset)
                        }
                    )
                }
                .combinedClickable(
                    onClick = { if (selectionMode) onToggleSelect() else onClick() },
                    onLongClick = { if (selectionMode) onToggleSelect() else onDelete() },
                    onClickLabel = zh("打开详情"),
                    onLongClickLabel = zh("长按删除")
                )
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectionMode) {
                Icon(
                    imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (selected) zh("已选择") else zh("未选择"),
                    tint = if (selected) colors.accent else colors.muted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = formatCompactEventTime(reminder.nextTriggerAt),
                color = colors.muted,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                style = TextStyle(fontFeatureSettings = "tnum"),
                modifier = Modifier.width(74.dp)
            )
            Text(
                text = reminder.title,
                color = colors.ink,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = cycleSummaryLabel(reminder),
                color = colors.muted,
                fontSize = 12.sp,
                maxLines = 1,
                style = TextStyle(fontFeatureSettings = "tnum")
            )
            if (!selectionMode) {
                Spacer(Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = zh("确认完成"),
                    tint = colors.muted.copy(alpha = 0.72f),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(onClick = onConfirm)
                        .semantics { contentDescription = zh("确认完成") }
                )
            }
        }
    }
}
