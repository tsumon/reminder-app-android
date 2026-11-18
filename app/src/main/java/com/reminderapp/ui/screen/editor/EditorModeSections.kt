package com.reminderapp.ui.screen.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.model.Cycle
import com.reminderapp.model.DateReminderType
import com.reminderapp.model.RulePeriod
import com.reminderapp.model.weekLabels
import com.reminderapp.model.weekdayLabels
import com.reminderapp.service.HolidayService
import com.reminderapp.service.LunarCalendar
import com.reminderapp.ui.comic.ComicTextAction
import com.reminderapp.ui.comic.comicColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EditorKeyValueRow(
    label: String,
    trailing: @Composable () -> Unit
) {
    val colors = comicColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = colors.muted,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium
        )
        trailing()
    }
}

@Composable
fun EditorRuleSection(
    rulePeriod: RulePeriod,
    onRulePeriodChange: (RulePeriod) -> Unit,
    ruleWeek: Int,
    onRuleWeekChange: (Int) -> Unit,
    ruleWeekday: Int,
    onRuleWeekdayChange: (Int) -> Unit
) {
    val colors = comicColors()
    var rulePeriodMenu by remember { mutableStateOf(false) }
    var ruleWeekMenu by remember { mutableStateOf(false) }

    EditorKeyValueRow(label = zh("频率")) {
        Box {
            Text(
                text = rulePeriod.label + " ▾",
                color = colors.ink,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { rulePeriodMenu = true }
            )
            DropdownMenu(expanded = rulePeriodMenu, onDismissRequest = { rulePeriodMenu = false }) {
                RulePeriod.entries.forEach { p ->
                    DropdownMenuItem(
                        text = { Text(p.label) },
                        onClick = { onRulePeriodChange(p); rulePeriodMenu = false }
                    )
                }
            }
        }
    }
    EditorKeyValueRow(label = zh("第几周")) {
        Box {
            Text(
                text = weekLabels.getOrElse(ruleWeek - 1) { zhf("第%s周", ruleWeek) } + " ▾",
                color = colors.ink,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { ruleWeekMenu = true }
            )
            DropdownMenu(expanded = ruleWeekMenu, onDismissRequest = { ruleWeekMenu = false }) {
                (1..5).forEach { w ->
                    DropdownMenuItem(
                        text = { Text(zhf("第%s周", w)) },
                        onClick = { onRuleWeekChange(w); ruleWeekMenu = false }
                    )
                }
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(zh("星期几"), color = colors.muted, fontSize = 13.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            (1..7).forEach { w ->
                val active = ruleWeekday == w
                Text(
                    text = weekdayLabels[w - 1],
                    color = if (active) colors.accent else colors.muted,
                    fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium,
                    fontSize = 13.5.sp,
                    modifier = Modifier
                        .clickable { onRuleWeekdayChange(w) }
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorCycleSection(
    selectedCycle: Cycle,
    onCycleChange: (Cycle) -> Unit,
    weeklyWeekday: Int,
    onWeeklyWeekdayChange: (Int) -> Unit,
    customDaysText: String,
    onCustomDaysTextChange: (String) -> Unit,
    suggestionText: String?,
    onSuggestFrequency: () -> Unit
) {
    val colors = comicColors()
    var weekdayMenu by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(zh("周期"), color = colors.muted, fontSize = 13.5.sp, modifier = Modifier.weight(1f))
            ComicTextAction(
                text = zh("智能建议频率"),
                onClick = onSuggestFrequency,
                accent = true,
                fontSize = 12.sp
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Cycle.entries.forEach { option ->
                val active = selectedCycle == option
                val label = if (option == Cycle.CUSTOM) zh("自定义") else option.label
                Text(
                    text = (if (active) "● " else "") + label,
                    color = if (active) colors.accent else colors.ink,
                    fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onCycleChange(option) }
                )
            }
        }
        if (!suggestionText.isNullOrBlank()) {
            Text(suggestionText, color = colors.accent, fontSize = 12.sp)
        }
    }

    if (selectedCycle == Cycle.WEEKLY) {
        EditorKeyValueRow(label = zh("星期几")) {
            Box {
                val wdLabel = if (weeklyWeekday == 0) zh("今天")
                else weekdayLabels.getOrElse(weeklyWeekday - 1) { "" }
                Text(
                    text = "$wdLabel ▾",
                    color = colors.ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { weekdayMenu = true }
                )
                DropdownMenu(expanded = weekdayMenu, onDismissRequest = { weekdayMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(zh("今天")) },
                        onClick = { onWeeklyWeekdayChange(0); weekdayMenu = false }
                    )
                    (1..7).forEach { w ->
                        DropdownMenuItem(
                            text = { Text(weekdayLabels[w - 1]) },
                            onClick = { onWeeklyWeekdayChange(w); weekdayMenu = false }
                        )
                    }
                }
            }
        }
    }

    if (selectedCycle == Cycle.CUSTOM) {
        EditorKeyValueRow(label = zh("自定义天数")) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = customDaysText,
                    onValueChange = onCustomDaysTextChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = LocalTextStyle.current.copy(
                        color = colors.accent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFeatureSettings = "tnum"
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier.width(48.dp)
                )
                Text(zh("天"), color = colors.muted, fontSize = 13.5.sp)
            }
        }
    }
}

@Composable
fun EditorDateSection(
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
    triggerYear: Int,
    advanceDays: Int,
    onAdvanceDaysChange: (Int) -> Unit
) {
    val colors = comicColors()
    var holidayMenu by remember { mutableStateOf(false) }

    EditorKeyValueRow(label = zh("日期类型")) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            DateReminderType.entries.forEach { t ->
                val active = selectedDateType == t
                Text(
                    text = t.label,
                    color = if (active) colors.accent else colors.muted,
                    fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium,
                    fontSize = 13.5.sp,
                    modifier = Modifier.clickable { onDateTypeChange(t) }
                )
            }
        }
    }

    if (selectedDateType == DateReminderType.HOLIDAY) {
        EditorKeyValueRow(label = zh("节日")) {
            Box {
                Text(
                    text = (selectedHolidayName ?: zh("选择节日")) + " ▾",
                    color = if (selectedHolidayName == null) colors.accent else colors.ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { holidayMenu = true }
                )
                DropdownMenu(expanded = holidayMenu, onDismissRequest = { holidayMenu = false }) {
                    HolidayService.allHolidays.forEach { holiday ->
                        DropdownMenuItem(
                            text = { Text("${holiday.name}（${if (holiday.isLunar) "农历" else "公历"}）") },
                            onClick = {
                                onHolidaySelect(holiday.name, holiday.month, holiday.day)
                                holidayMenu = false
                            }
                        )
                    }
                }
            }
        }
    } else {
        EditorKeyValueRow(
            label = if (selectedDateType == DateReminderType.LUNAR_BIRTHDAY) zh("农历月日") else zh("月日")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = targetMonthText,
                    onValueChange = onTargetMonthTextChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = LocalTextStyle.current.copy(
                        color = colors.ink,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFeatureSettings = "tnum"
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier.width(28.dp)
                )
                Text(zh("月") + "  ", color = colors.muted, fontSize = 13.5.sp)
                BasicTextField(
                    value = targetDayText,
                    onValueChange = onTargetDayTextChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = LocalTextStyle.current.copy(
                        color = colors.ink,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFeatureSettings = "tnum"
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier.width(28.dp)
                )
                Text(zh("日"), color = colors.muted, fontSize = 13.5.sp)
            }
        }
        if (selectedDateType == DateReminderType.LUNAR_BIRTHDAY) {
            val solarDate = remember(targetMonth, targetDay, triggerYear) {
                LunarCalendar.lunarToSolar(triggerYear, targetMonth, targetDay)
            }
            if (solarDate != null) {
                val sdf = SimpleDateFormat(zh("yyyy年MM月dd日"), Locale.getDefault())
                Text(
                    zhf("%1\$s年对应公历：%2\$s", triggerYear, sdf.format(Date(solarDate))),
                    color = colors.muted,
                    fontSize = 12.sp,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }
        }
    }

    EditorKeyValueRow(label = zh("提前提醒")) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(1, 3, 7, 14, 30).forEach { days ->
                val active = advanceDays == days
                Text(
                    text = zhf("%s天", days),
                    color = if (active) colors.accent else colors.muted,
                    fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium,
                    fontSize = 13.sp,
                    style = TextStyle(fontFeatureSettings = "tnum"),
                    modifier = Modifier.clickable { onAdvanceDaysChange(days) }
                )
            }
        }
    }
}
