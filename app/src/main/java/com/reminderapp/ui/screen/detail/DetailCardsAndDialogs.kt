package com.reminderapp.ui.screen.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.ui.comic.ComicButton
import com.reminderapp.ui.comic.ComicCard
import com.reminderapp.ui.comic.ComicHairline
import com.reminderapp.ui.comic.ComicHeroNumber
import com.reminderapp.ui.comic.ComicTextAction
import com.reminderapp.ui.comic.comicColors
import com.reminderapp.ui.comic.cycleSummaryLabel
import com.reminderapp.ui.comic.formatShortDateTime
import com.reminderapp.ui.comic.formatTabularTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetailHeroCard(
    reminder: ReminderEntity,
    canUndo: Boolean,
    highlightConfirm: Boolean,
    onEdit: () -> Unit,
    onConfirm: () -> Unit,
    onMakeUp: () -> Unit,
    onSnoozeMinutes: (Long) -> Unit,
    onSnoozeTomorrow: () -> Unit,
    onOpenCustomSnooze: () -> Unit,
    onSkip: () -> Unit,
    onUndo: () -> Unit
) {
    val colors = comicColors()
    var snoozeMenu by remember { mutableStateOf(false) }
    val statusLabel = when (reminder.status) {
        "notifying" -> zh("需要确认")
        "overdue" -> zh("已逾期")
        "confirmed" -> zh("已完成")
        else -> zh("等待中")
    }
    val canSkip = !(reminder.kind == "cycle" && reminder.cycle == "once") && reminder.status != "confirmed"

    ComicCard(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = statusLabel +
                        (if (reminder.retryCount > 0) zhf(" · 第%s次重试", reminder.retryCount) else ""),
                    color = if (reminder.status == "overdue") colors.danger else colors.muted,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
                ComicHeroNumber(
                    text = formatTabularTime(reminder.nextTriggerAt),
                    fontSize = 38.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = reminder.title,
                    color = colors.ink,
                    fontSize = 18.sp,
                    lineHeight = 23.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.End
                )
                Text(
                    text = cycleSummaryLabel(reminder),
                    color = colors.muted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.End
                )
                if (reminder.nextTriggerAt > 0) {
                    Text(
                        text = zhf("下次：%s", formatShortDateTime(reminder.nextTriggerAt)),
                        color = colors.muted,
                        fontSize = 12.sp,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                }
                if (reminder.note.isNotBlank()) {
                    Text(
                        text = reminder.note,
                        color = colors.muted,
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        if (!reminder.holidayAdjustNote.isNullOrEmpty()) {
            Text(
                text = "※ " + reminder.holidayAdjustNote,
                color = colors.accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        ComicHairline(isInner = true)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ComicTextAction(text = zh("编辑提醒"), onClick = onEdit, accent = true, fontSize = 12.5.sp)

                Box {
                    ComicTextAction(text = zh("稍后") + " ▾", onClick = { snoozeMenu = true }, fontSize = 12.5.sp)
                    DropdownMenu(expanded = snoozeMenu, onDismissRequest = { snoozeMenu = false }) {
                        DropdownMenuItem(text = { Text(zh("15 分钟")) }, onClick = { snoozeMenu = false; onSnoozeMinutes(15) })
                        DropdownMenuItem(text = { Text(zh("1 小时")) }, onClick = { snoozeMenu = false; onSnoozeMinutes(60) })
                        DropdownMenuItem(text = { Text(zh("明天")) }, onClick = { snoozeMenu = false; onSnoozeTomorrow() })
                        DropdownMenuItem(text = { Text(zh("自定义…")) }, onClick = { snoozeMenu = false; onOpenCustomSnooze() })
                    }
                }

                if (canSkip) ComicTextAction(text = zh("跳过本次"), onClick = onSkip, fontSize = 12.5.sp)
                if (reminder.status == "overdue") ComicTextAction(text = zh("补打今天"), onClick = onMakeUp, accent = true, fontSize = 12.5.sp)
                if (canUndo) ComicTextAction(text = zh("撤销完成"), onClick = onUndo, accent = true, fontSize = 12.5.sp)
            }

            ComicButton(
                text = zh("确认完成"),
                onClick = onConfirm,
                cut = 5.5.dp,
                contentPadding = PaddingValues(
                    horizontal = if (highlightConfirm) 18.dp else 14.dp,
                    vertical = 8.dp
                )
            )
        }
    }
}

@Composable
fun DetailSettingsCard(
    reminder: ReminderEntity,
    quietHoursEnabled: Boolean,
    quietStartMinute: Int,
    quietEndMinute: Int,
    onToggleCritical: (Boolean) -> Unit,
    onToggleQuietHours: (Boolean) -> Unit,
    onPickQuietStart: () -> Unit,
    onPickQuietEnd: () -> Unit
) {
    val colors = comicColors()
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    ComicCard(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DetailInfoRow(
            label = zh("优先级"),
            value = when (reminder.priority) { "high" -> zh("高"); "low" -> zh("低"); else -> zh("中") }
        )
        DetailInfoRow(label = zh("周期"), value = cycleSummaryLabel(reminder))
        if (reminder.kind == "date") {
            DetailInfoRow(label = zh("提前提醒"), value = zhf("%s 天", reminder.advanceDays))
        }
        DetailInfoRow(label = zh("首次提醒"), value = dateFormat.format(Date(reminder.firstTriggerAt)))
        DetailInfoRow(label = zh("下次提醒"), value = dateFormat.format(Date(reminder.nextTriggerAt)))
        if (reminder.retryCount > 0) {
            DetailInfoRow(label = zh("重试次数"), value = zhf("%s 次", reminder.retryCount))
        }

        ComicHairline(isInner = true)

        Row(
            modifier = Modifier.fillMaxWidth().semantics {
                contentDescription = zh("关键提醒开关：开启后触发时全屏弹出，确保不被漏看")
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(zh("关键提醒"), color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(zh("重要事项，触发时高优先级通知提醒"), color = colors.muted, fontSize = 11.5.sp)
            }
            Switch(checked = reminder.isCritical, onCheckedChange = onToggleCritical)
        }

        Row(
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = zh("勿扰时段开关") },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(zh("勿扰时段"), color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(zh("该时段内到点的提醒顺延到时段结束"), color = colors.muted, fontSize = 11.5.sp)
            }
            Switch(checked = quietHoursEnabled, onCheckedChange = onToggleQuietHours)
        }

        if (quietHoursEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = zh("开始") + " " + String.format(Locale.US, "%02d:%02d", quietStartMinute / 60, quietStartMinute % 60),
                    color = colors.accent,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontFeatureSettings = "tnum"),
                    modifier = Modifier.clickable(onClick = onPickQuietStart)
                )
                Text(
                    text = zh("结束") + " " + String.format(Locale.US, "%02d:%02d", quietEndMinute / 60, quietEndMinute % 60),
                    color = colors.accent,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontFeatureSettings = "tnum"),
                    modifier = Modifier.clickable(onClick = onPickQuietEnd)
                )
            }
        }
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
    val colors = comicColors()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = colors.muted, fontSize = 13.5.sp)
        Text(
            text = value,
            color = colors.ink,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            style = TextStyle(fontFeatureSettings = "tnum")
        )
    }
}
