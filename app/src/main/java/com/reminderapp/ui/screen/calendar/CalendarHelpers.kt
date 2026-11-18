package com.reminderapp.ui.screen.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.service.HolidayRemoteService
import com.reminderapp.service.LunarCalendar
import com.reminderapp.ui.comic.ChamferShape
import com.reminderapp.ui.comic.comicColors
import com.reminderapp.ui.theme.Tokens
import java.util.Calendar
import java.util.Locale

data class MonthCell(val year: Int, val month: Int, val day: Int, val inMonth: Boolean)

fun calendarDateKey(year: Int, month: Int, day: Int): String =
    String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)

fun calendarLunarTextFor(year: Int, month: Int, day: Int): String {
    val cal = Calendar.getInstance().apply {
        set(year, month, day, 12, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val lunar = LunarCalendar.solarToLunar(cal.timeInMillis) ?: return ""
    val dayNames = arrayOf(
        "", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )
    return when (lunar.day) {
        1 -> "初一"
        in 2..30 -> dayNames[lunar.day - 1]
        else -> ""
    }
}

fun paddedMonthCells(displayYear: Int, displayMonth: Int): List<MonthCell> {
    val cal = Calendar.getInstance().apply {
        set(displayYear, displayMonth, 1, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1
    val leading = firstDayWeek - 1
    val cells = mutableListOf<MonthCell>()
    if (leading > 0) {
        val prev = Calendar.getInstance().apply {
            set(displayYear, displayMonth, 1, 0, 0, 0)
            add(Calendar.MONTH, -1)
        }
        val prevDays = prev.getActualMaximum(Calendar.DAY_OF_MONTH)
        val py = prev.get(Calendar.YEAR)
        val pm = prev.get(Calendar.MONTH)
        for (d in (prevDays - leading + 1)..prevDays) cells.add(MonthCell(py, pm, d, false))
    }
    for (d in 1..daysInMonth) cells.add(MonthCell(displayYear, displayMonth, d, true))
    val next = Calendar.getInstance().apply {
        set(displayYear, displayMonth, 1, 0, 0, 0)
        add(Calendar.MONTH, 1)
    }
    val ny = next.get(Calendar.YEAR)
    val nm = next.get(Calendar.MONTH)
    var n = 1
    while (cells.size % 7 != 0) {
        cells.add(MonthCell(ny, nm, n, false))
        n++
    }
    return cells
}

@Composable
fun ComicDayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    lunarText: String,
    taskCount: Int,
    muted: Boolean,
    holidayStatus: HolidayRemoteService.DayStatus?,
    onClick: () -> Unit
) {
    val colors = comicColors()
    val holidayMark = holidayStatus?.let { if (it.isHoliday) zh("休") else zh("班") }
    val scale = LocalDensity.current.fontScale.coerceAtMost(1.4f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((50 * scale).dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(38.dp)
                .height((44 * scale).dp)
                .clip(ChamferShape(5.dp))
                .background(if (isSelected) colors.accent else Color.Transparent),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                fontSize = 14.5.sp,
                fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Medium,
                color = when {
                    isSelected -> colors.onAccent
                    muted -> colors.muted.copy(alpha = 0.42f)
                    isToday -> colors.accent
                    else -> colors.ink
                },
                style = TextStyle(fontFeatureSettings = "tnum")
            )
            Text(
                text = holidayMark ?: lunarText,
                fontSize = 9.sp,
                fontWeight = if (holidayMark != null) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> colors.onAccent.copy(alpha = 0.85f)
                    holidayMark != null -> if (holidayStatus.isHoliday) Tokens.HolidayRest else Tokens.HolidayWork
                    muted -> colors.muted.copy(alpha = 0.42f)
                    else -> colors.muted
                },
                maxLines = 1
            )
            Spacer(Modifier.height(1.dp))
            Box(
                Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            taskCount <= 0 -> Color.Transparent
                            isSelected -> colors.onAccent
                            else -> colors.accent
                        }
                    )
            )
        }
    }
}

@Composable
fun CalendarMonthPickerDialog(
    displayYear: Int,
    displayMonth: Int,
    onYearMonthChange: (year: Int, month: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = comicColors()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onYearMonthChange(displayYear - 1, displayMonth) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = zh("上一年"))
                }
                Text(
                    text = zhf("%s 年", displayYear),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { onYearMonthChange(displayYear + 1, displayMonth) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = zh("下一年"))
                }
            }
        },
        text = {
            val monthNames = listOf(
                zh("一月"), zh("二月"), zh("三月"), zh("四月"), zh("五月"), zh("六月"),
                zh("七月"), zh("八月"), zh("九月"), zh("十月"), zh("十一月"), zh("十二月")
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                monthNames.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { name ->
                            val idx = monthNames.indexOf(name)
                            val active = idx == displayMonth
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(ChamferShape(5.dp))
                                    .background(if (active) colors.accent else Color.Transparent)
                                    .clickable {
                                        onYearMonthChange(displayYear, idx)
                                        onDismiss()
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    name,
                                    color = if (active) colors.onAccent else colors.ink,
                                    fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(zh("取消")) } }
    )
}
