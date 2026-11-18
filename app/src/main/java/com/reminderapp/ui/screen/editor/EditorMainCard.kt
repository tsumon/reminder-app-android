package com.reminderapp.ui.screen.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.model.Cycle
import com.reminderapp.model.DateReminderType
import com.reminderapp.model.RulePeriod
import com.reminderapp.ui.comic.ComicCard
import com.reminderapp.ui.comic.ComicHairline
import com.reminderapp.ui.comic.comicColors
import java.util.Locale

@Composable
fun EditorMainCard(
    title: String,
    onTitleChange: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    isDateMode: Boolean,
    isRuleMode: Boolean,
    onModeChange: (date: Boolean, rule: Boolean) -> Unit,
    selectedCycle: Cycle,
    onCycleChange: (Cycle) -> Unit,
    weeklyWeekday: Int,
    onWeeklyWeekdayChange: (Int) -> Unit,
    customDaysText: String,
    onCustomDaysTextChange: (String) -> Unit,
    suggestionText: String?,
    onSuggestFrequency: () -> Unit,
    selectedDateType: DateReminderType?,
    onDateTypeChange: (DateReminderType) -> Unit,
    selectedHolidayName: String?,
    onHolidaySelect: (name: String, month: Int, day: Int) -> Unit,
    targetMonthText: String,
    onTargetMonthTextChange: (String) -> Unit,
    targetDayText: String,
    onTargetDayTextChange: (String) -> Unit,
    targetMonth: Int,
    targetDay: Int,
    advanceDays: Int,
    onAdvanceDaysChange: (Int) -> Unit,
    rulePeriod: RulePeriod,
    onRulePeriodChange: (RulePeriod) -> Unit,
    ruleWeek: Int,
    onRuleWeekChange: (Int) -> Unit,
    ruleWeekday: Int,
    onRuleWeekdayChange: (Int) -> Unit,
    triggerYear: Int,
    triggerMonth: Int,
    triggerDay: Int,
    triggerHour: Int,
    triggerMinute: Int,
    onOpenDatePicker: () -> Unit,
    onOpenTimePicker: () -> Unit,
    priority: String,
    onPriorityChange: (String) -> Unit,
    holidayAware: Boolean,
    onHolidayAwareChange: (Boolean) -> Unit
) {
    val colors = comicColors()

    ComicCard(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(Modifier.fillMaxWidth()) {
            if (title.isEmpty()) {
                Text(
                    text = zh("标题"),
                    color = colors.muted.copy(alpha = 0.7f),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            BasicTextField(
                value = title,
                onValueChange = onTitleChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = colors.ink,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                cursorBrush = SolidColor(colors.accent),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Box(Modifier.fillMaxWidth()) {
            if (note.isEmpty()) {
                Text(
                    text = zh("备注（可选）"),
                    color = colors.muted.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
            BasicTextField(
                value = note,
                onValueChange = onNoteChange,
                maxLines = 3,
                textStyle = LocalTextStyle.current.copy(
                    color = colors.ink,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(colors.accent),
                modifier = Modifier.fillMaxWidth()
            )
        }

        ComicHairline(isInner = true)

        EditorKeyValueRow(label = zh("提醒类型")) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                listOf(
                    Triple(!isDateMode && !isRuleMode, zh("周期提醒")) { onModeChange(false, false) },
                    Triple(isDateMode, zh("日期提醒")) { onModeChange(true, false) },
                    Triple(isRuleMode, zh("规则提醒")) { onModeChange(false, true) }
                ).forEach { (active, label, action) ->
                    Text(
                        text = label,
                        color = if (active) colors.accent else colors.muted,
                        fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium,
                        fontSize = 13.5.sp,
                        modifier = Modifier.clickable(onClick = action)
                    )
                }
            }
        }

        when {
            isRuleMode -> EditorRuleSection(
                rulePeriod = rulePeriod,
                onRulePeriodChange = onRulePeriodChange,
                ruleWeek = ruleWeek,
                onRuleWeekChange = onRuleWeekChange,
                ruleWeekday = ruleWeekday,
                onRuleWeekdayChange = onRuleWeekdayChange
            )
            !isDateMode -> EditorCycleSection(
                selectedCycle = selectedCycle,
                onCycleChange = onCycleChange,
                weeklyWeekday = weeklyWeekday,
                onWeeklyWeekdayChange = onWeeklyWeekdayChange,
                customDaysText = customDaysText,
                onCustomDaysTextChange = onCustomDaysTextChange,
                suggestionText = suggestionText,
                onSuggestFrequency = onSuggestFrequency
            )
            else -> EditorDateSection(
                selectedDateType = selectedDateType,
                onDateTypeChange = onDateTypeChange,
                selectedHolidayName = selectedHolidayName,
                onHolidaySelect = onHolidaySelect,
                targetMonthText = targetMonthText,
                onTargetMonthTextChange = onTargetMonthTextChange,
                targetDayText = targetDayText,
                onTargetDayTextChange = onTargetDayTextChange,
                targetMonth = targetMonth,
                targetDay = targetDay,
                triggerYear = triggerYear,
                advanceDays = advanceDays,
                onAdvanceDaysChange = onAdvanceDaysChange
            )
        }

        ComicHairline(isInner = true)

        if (!isDateMode && !isRuleMode) {
            EditorKeyValueRow(label = zh("提醒日期")) {
                Text(
                    text = zhf("%1\$s年%2\$s月%3\$s日", triggerYear, triggerMonth, triggerDay) + " ›",
                    color = colors.ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    style = TextStyle(fontFeatureSettings = "tnum"),
                    modifier = Modifier.clickable(onClick = onOpenDatePicker)
                )
            }
        }
        EditorKeyValueRow(label = zh("提醒时间")) {
            Text(
                text = String.format(Locale.US, "%02d:%02d", triggerHour, triggerMinute) + " ›",
                color = colors.accent,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                style = TextStyle(fontFeatureSettings = "tnum"),
                modifier = Modifier.clickable(onClick = onOpenTimePicker)
            )
        }

        EditorKeyValueRow(label = zh("优先级")) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf("high" to "高", "normal" to "中", "low" to "低").forEach { (key, label) ->
                    val active = priority == key
                    Text(
                        text = zh(label),
                        color = if (active) colors.accent else colors.muted,
                        fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium,
                        fontSize = 13.5.sp,
                        modifier = Modifier.clickable { onPriorityChange(key) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(zh("避开节假日/周末"), color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    zh("落在周末或法定节假日时顺延至下一工作日"),
                    color = colors.muted,
                    fontSize = 11.5.sp
                )
            }
            Switch(
                checked = holidayAware,
                onCheckedChange = onHolidayAwareChange,
                modifier = Modifier.semantics {
                    contentDescription = zh("避开节假日周末开关：触发日期落在周六日或法定节假日时顺延到下一个工作日")
                }
            )
        }
    }
}
