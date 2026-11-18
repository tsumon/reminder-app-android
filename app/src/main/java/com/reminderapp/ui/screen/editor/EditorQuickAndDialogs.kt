package com.reminderapp.ui.screen.editor

import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.service.ReminderEngine
import com.reminderapp.service.ReminderPolicy
import com.reminderapp.service.ReminderTemplate
import com.reminderapp.ui.comic.ComicCard
import com.reminderapp.ui.comic.ComicCollapsibleRow
import com.reminderapp.ui.comic.ComicHairline
import com.reminderapp.ui.comic.ComicTextAction
import com.reminderapp.ui.comic.comicColors
import com.reminderapp.ui.component.ReminderPolicyEditor
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

@Composable
fun EditorQuickAndTemplatesStrip(
    nlText: String,
    onNlTextChange: (String) -> Unit,
    nlHint: String?,
    nlError: Boolean,
    onParseNl: () -> Unit,
    onApplyTemplate: (ReminderTemplate) -> Unit
) {
    val colors = comicColors()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = zh("快速模板") + " :",
                color = colors.muted,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium
            )
            ReminderTemplate.all.forEach { tpl ->
                Text(
                    text = zh(tpl.title),
                    color = colors.accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .testTag("reminder-template-${tpl.id}")
                        .clickable { onApplyTemplate(tpl) }
                        .padding(vertical = 4.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("✎", color = colors.accent, fontSize = 13.sp)
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f)) {
                if (nlText.isEmpty()) {
                    Text(
                        text = zh("明天下午3点开会 / 每周一9点晨会 / 农历8月15 中秋"),
                        color = colors.muted.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
                BasicTextField(
                    value = nlText,
                    onValueChange = onNlTextChange,
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        color = colors.ink,
                        fontSize = 13.5.sp
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.width(8.dp))
            ComicTextAction(
                text = zh("智能识别"),
                onClick = onParseNl,
                enabled = nlText.isNotBlank(),
                accent = true,
                fontSize = 12.5.sp
            )
        }
        ComicHairline(isInner = false)

        if (!nlHint.isNullOrBlank()) {
            Text(
                text = nlHint,
                color = if (nlError) colors.danger else colors.muted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun EditorAdvancedDrawer(
    expanded: Boolean,
    onToggle: () -> Unit,
    policyEntity: ReminderEntity,
    previewDraft: ReminderEntity,
    firstPreview: Long,
    onPolicyChange: (ReminderEntity) -> Unit,
    onValidityChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val colors = comicColors()
    val styleLabel = when (policyEntity.reminderStyle) {
        "gentle" -> zh("轻柔")
        "important" -> zh("重要")
        else -> zh("持续")
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ComicCollapsibleRow(
            title = zh("高级策略与排期预览"),
            expanded = expanded,
            trailing = styleLabel,
            onClick = onToggle
        )
        AnimatedVisibility(visible = expanded) {
            ComicCard(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ReminderPolicyEditor(
                    value = policyEntity,
                    onChange = onPolicyChange,
                    onValidityChange = onValidityChange
                )
                ComicHairline(isInner = true)
                Text(
                    text = zh("提醒预览"),
                    color = colors.ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                ReminderEngine.futureTriggers(
                    previewDraft.copy(nextTriggerAt = firstPreview),
                    count = 3,
                    context = context
                ).forEachIndexed { index, date ->
                    val fmt = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                        .format(Date(date))
                    Text(
                        text = zhf("第 %s 次周期 · %s", index + 1, fmt),
                        color = colors.ink,
                        fontSize = 12.5.sp,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                }
                if (previewDraft.reminderStyle != "gentle") {
                    var retryAt = ReminderPolicy.adjustQuiet(previewDraft, firstPreview)
                    val gaps = ReminderPolicy.intervals(previewDraft)
                    for (index in 0 until minOf(3, previewDraft.maxReminderAttempts - 1)) {
                        retryAt = ReminderPolicy.adjustQuiet(
                            previewDraft,
                            retryAt + gaps[minOf(index, gaps.lastIndex)] * 60000L
                        )
                        val formatted = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                            .format(Date(retryAt))
                        Text(
                            text = formatted + zhf(" · 第%s次重试", index + 1),
                            color = colors.muted,
                            fontSize = 12.sp,
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )
                    }
                }
                Text(
                    text = zh("按设备日历计算；更换时区不改写原始时间戳，请检查下次时间。预览假设按时确认，节假日数据更新后可能调整。"),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorDateTimeAndPermissionDialogs(
    showDateDialog: Boolean,
    triggerYear: Int,
    triggerMonth: Int,
    triggerDay: Int,
    onDatePicked: (year: Int, month: Int, day: Int) -> Unit,
    onDismissDate: () -> Unit,
    showTimeDialog: Boolean,
    triggerHour: Int,
    triggerMinute: Int,
    onTimePicked: (hour: Int, minute: Int) -> Unit,
    onDismissTime: () -> Unit,
    showFullScreenPermissionDialog: Boolean,
    onDismissPermission: () -> Unit
) {
    val context = LocalContext.current

    if (showDateDialog) {
        val initialMillis = Calendar.getInstance().apply {
            set(triggerYear, triggerMonth - 1, triggerDay)
        }.timeInMillis
        val dateState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = onDismissDate,
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance().apply { timeInMillis = millis }
                        onDatePicked(
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.DAY_OF_MONTH)
                        )
                    }
                    onDismissDate()
                }) { Text(zh("确定")) }
            },
            dismissButton = { TextButton(onClick = onDismissDate) { Text(zh("取消")) } }
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showTimeDialog) {
        val timeState = rememberTimePickerState(initialHour = triggerHour, initialMinute = triggerMinute)
        AlertDialog(
            onDismissRequest = onDismissTime,
            confirmButton = {
                TextButton(onClick = {
                    onTimePicked(timeState.hour, timeState.minute)
                    onDismissTime()
                }) { Text(zh("确定")) }
            },
            dismissButton = { TextButton(onClick = onDismissTime) { Text(zh("取消")) } },
            text = { TimePicker(state = timeState) }
        )
    }

    if (showFullScreenPermissionDialog) {
        val nm = context.getSystemService(NotificationManager::class.java)
        val needFullScreen = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            (nm == null || !nm.canUseFullScreenIntent())
        val needPolicy = nm?.isNotificationPolicyAccessGranted != true
        AlertDialog(
            onDismissRequest = onDismissPermission,
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
                        onDismissPermission()
                        val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }) { Text(zh("去设置（全屏）")) }
                } else if (needPolicy) {
                    TextButton(onClick = {
                        onDismissPermission()
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                    }) { Text(zh("去设置（勿扰）")) }
                }
            },
            dismissButton = { TextButton(onClick = onDismissPermission) { Text(zh("稍后")) } }
        )
    }
}
