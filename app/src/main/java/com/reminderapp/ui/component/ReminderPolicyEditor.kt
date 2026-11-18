package com.reminderapp.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.service.ReminderPolicy
import com.reminderapp.ui.comic.ComicHairline
import com.reminderapp.ui.comic.ComicTextAction
import com.reminderapp.ui.comic.comicColors
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReminderPolicyEditor(
    value: ReminderEntity,
    onChange: (ReminderEntity) -> Unit,
    onValidityChange: (Boolean) -> Unit = {}
) {
    val colors = comicColors()
    var intervals by remember(value.id) { mutableStateOf(value.retryIntervalsMinutes) }
    var cap by remember(value.id) { mutableStateOf(value.maxReminderAttempts.toString()) }
    var quietStart by remember(value.id) {
        mutableStateOf(
            value.quietStartMinute?.let { String.format(Locale.ROOT, "%02d:%02d", it / 60, it % 60) } ?: ""
        )
    }
    var quietEnd by remember(value.id) {
        mutableStateOf(
            value.quietEndMinute?.let { String.format(Locale.ROOT, "%02d:%02d", it / 60, it % 60) } ?: ""
        )
    }
    var showQuietStartPicker by remember { mutableStateOf(false) }
    var showQuietEndPicker by remember { mutableStateOf(false) }

    val retryValid = ReminderPolicy.validEditorInput(value.reminderStyle, intervals, cap, "", "")
    val quietValid = ReminderPolicy.validEditorInput("gentle", intervals, cap, quietStart, quietEnd)
    LaunchedEffect(retryValid, quietValid) { onValidityChange(retryValid && quietValid) }

    fun minute(text: String): Int? {
        val p = text.split(":").map { it.toIntOrNull() }
        return if (p.size == 2 && p[0] in 0..23 && p[1] in 0..59) p[0]!! * 60 + p[1]!! else null
    }

    fun updateQuiet() {
        val start = minute(quietStart)
        val end = minute(quietEnd)
        if (start != null && end != null) {
            onChange(value.copy(quietStartMinute = start, quietEndMinute = end))
        }
        if (quietStart.isBlank() && quietEnd.isBlank()) {
            onChange(value.copy(quietStartMinute = null, quietEndMinute = null))
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(zh("提醒方式"), color = colors.muted, fontSize = 13.5.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                listOf("gentle" to "轻柔", "persistent" to "持续", "important" to "重要").forEach { (style, label) ->
                    val selected = value.reminderStyle == style
                    Text(
                        text = zh(label),
                        color = if (selected) colors.accent else colors.muted,
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable {
                            onChange(value.copy(reminderStyle = style, isCritical = style == "important"))
                        }
                    )
                }
            }
        }
        if (value.reminderStyle == "important") {
            Text(
                zh("重要提醒使用高优先级通知；全屏和勿扰权限由系统控制。"),
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted
            )
        }
        if (value.kind == "cycle" && value.cycle != "once") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("fixed" to "固定周期", "afterCompletion" to "完成后重新计时").forEach { (mode, label) ->
                    val selected = value.recurrenceMode == mode
                    Text(
                        text = (if (selected) "● " else "○ ") + zh(label),
                        color = if (selected) colors.ink else colors.muted,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.5.sp,
                        modifier = Modifier.clickable { onChange(value.copy(recurrenceMode = mode)) }
                    )
                }
            }
        }
        if (value.reminderStyle != "gentle") {
            ComicHairline(isInner = true)
            intervals.split(",").forEachIndexed { index, text ->
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { input ->
                            val parts = intervals.split(",").toMutableList()
                            if (index !in parts.indices) return@OutlinedTextField
                            parts[index] = input
                            intervals = parts.joinToString(",")
                            if (parts.all { it.toIntOrNull() in 1..10080 }) {
                                onChange(value.copy(retryIntervalsMinutes = intervals))
                            }
                        },
                        label = { Text(zhf("第 %s 次重试（分钟）", index + 1)) },
                        isError = text.toIntOrNull() !in 1..10080,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    ComicTextAction(
                        text = zh("移除"),
                        enabled = intervals.split(",").size > 1,
                        danger = true,
                        onClick = {
                            val parts = intervals.split(",").toMutableList()
                            if (index !in parts.indices) return@ComicTextAction
                            parts.removeAt(index)
                            intervals = parts.joinToString(",")
                            if (parts.all { it.toIntOrNull() in 1..10080 }) {
                                onChange(value.copy(retryIntervalsMinutes = intervals))
                            }
                        }
                    )
                }
            }
            ComicTextAction(
                text = "+ " + zh("添加重试间隔"),
                enabled = intervals.split(",").size < 10,
                accent = true,
                onClick = {
                    intervals += ",60"
                    if (intervals.split(",").all { it.toIntOrNull() in 1..10080 }) {
                        onChange(value.copy(retryIntervalsMinutes = intervals))
                    }
                }
            )
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    zhf("最多提醒 %s 次（含首次）", value.maxReminderAttempts),
                    color = colors.ink,
                    fontSize = 13.5.sp,
                    modifier = Modifier.weight(1f)
                )
                ComicTextAction(
                    text = " − ",
                    enabled = value.maxReminderAttempts > 1,
                    accent = true,
                    fontSize = 16.sp,
                    onClick = {
                        cap = (value.maxReminderAttempts - 1).toString()
                        onChange(value.copy(maxReminderAttempts = value.maxReminderAttempts - 1))
                    }
                )
                ComicTextAction(
                    text = " + ",
                    enabled = value.maxReminderAttempts < 10,
                    accent = true,
                    fontSize = 16.sp,
                    onClick = {
                        cap = (value.maxReminderAttempts + 1).toString()
                        onChange(value.copy(maxReminderAttempts = value.maxReminderAttempts + 1))
                    }
                )
            }
            Text(
                zh("最多 10 项；最后一个间隔重复使用"),
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted
            )
        }

        ComicHairline(isInner = true)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ComicTextAction(
                text = if (quietStart.isBlank()) zh("勿扰开始") else "${zh("勿扰开始")} $quietStart",
                accent = true,
                onClick = { showQuietStartPicker = true }
            )
            ComicTextAction(
                text = if (quietEnd.isBlank()) zh("勿扰结束") else "${zh("勿扰结束")} $quietEnd",
                accent = true,
                onClick = { showQuietEndPicker = true }
            )
            if (quietStart.isNotBlank() || quietEnd.isNotBlank()) {
                ComicTextAction(
                    text = zh("清除"),
                    danger = true,
                    onClick = {
                        quietStart = ""
                        quietEnd = ""
                        updateQuiet()
                    }
                )
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = quietStart,
                onValueChange = { quietStart = it; updateQuiet() },
                label = { Text(zh("勿扰开始 HH:mm（留空关闭）")) },
                isError = !quietValid,
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = quietEnd,
                onValueChange = { quietEnd = it; updateQuiet() },
                label = { Text(zh("勿扰结束 HH:mm")) },
                isError = !quietValid,
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        if (!retryValid || !quietValid) {
            Text(
                zh("请修正标红的设置后保存"),
                color = colors.danger,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    if (showQuietStartPicker) {
        val initMin = minute(quietStart) ?: (22 * 60)
        val pickerState = rememberTimePickerState(initialHour = initMin / 60, initialMinute = initMin % 60)
        AlertDialog(
            onDismissRequest = { showQuietStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    quietStart = String.format(Locale.ROOT, "%02d:%02d", pickerState.hour, pickerState.minute)
                    if (quietEnd.isBlank()) quietEnd = "08:00"
                    updateQuiet()
                    showQuietStartPicker = false
                }) { Text(zh("确定")) }
            },
            dismissButton = {
                TextButton(onClick = { showQuietStartPicker = false }) { Text(zh("取消")) }
            },
            text = { TimePicker(state = pickerState) }
        )
    }
    if (showQuietEndPicker) {
        val initMin = minute(quietEnd) ?: (8 * 60)
        val pickerState = rememberTimePickerState(initialHour = initMin / 60, initialMinute = initMin % 60)
        AlertDialog(
            onDismissRequest = { showQuietEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    quietEnd = String.format(Locale.ROOT, "%02d:%02d", pickerState.hour, pickerState.minute)
                    if (quietStart.isBlank()) quietStart = "22:00"
                    updateQuiet()
                    showQuietEndPicker = false
                }) { Text(zh("确定")) }
            },
            dismissButton = {
                TextButton(onClick = { showQuietEndPicker = false }) { Text(zh("取消")) }
            },
            text = { TimePicker(state = pickerState) }
        )
    }
}
