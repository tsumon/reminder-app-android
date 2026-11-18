package com.reminderapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.service.HolidayRemoteService
import com.reminderapp.service.LunarCalendar
import com.reminderapp.service.ReminderEngine
import com.reminderapp.ui.comic.ComicCard
import com.reminderapp.ui.comic.ComicTextAction
import com.reminderapp.ui.comic.comicColors
import com.reminderapp.ui.screen.calendar.CalendarMonthPickerDialog
import com.reminderapp.ui.screen.calendar.ComicDayCell
import com.reminderapp.ui.screen.calendar.calendarDateKey
import com.reminderapp.ui.screen.calendar.calendarLunarTextFor
import com.reminderapp.ui.screen.calendar.paddedMonthCells
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Suppress("UNUSED_PARAMETER")
@Composable
fun CalendarCard(
    reminders: List<ReminderEntity>,
    displayYear: Int,
    displayMonth: Int,
    onYearMonthChange: (year: Int, month: Int) -> Unit,
    modifier: Modifier = Modifier,
    onDateClick: (Long) -> Unit = {},
    streak: Int? = null,
    weekDone: Int? = null,
    showInCardNav: Boolean = false
) {
    val colors = comicColors()
    val context = LocalContext.current
    val todayCal = remember { Calendar.getInstance() }
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(todayCal.time) }

    fun shiftMonth(delta: Int) {
        var m = displayMonth + delta
        var y = displayYear
        if (m < 0) { m = 11; y-- } else if (m > 11) { m = 0; y++ }
        onYearMonthChange(y, m)
    }

    var selectedDateKey by remember { mutableStateOf<String?>(todayDate) }
    LaunchedEffect(Unit) {
        val t0 = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        onDateClick(t0)
    }

    var showMonthPicker by remember { mutableStateOf(false) }

    val taskDates = remember(reminders, displayYear, displayMonth) {
        val map = mutableMapOf<String, Int>()
        val cal = Calendar.getInstance().apply { set(displayYear, displayMonth, 1) }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (d in 1..daysInMonth) {
            val count = reminders.count {
                it.isActive && ReminderEngine.occursOn(it, displayYear, displayMonth + 1, d)
            }
            if (count > 0) map[calendarDateKey(displayYear, displayMonth, d)] = count
        }
        map
    }

    val todayLunar = remember { LunarCalendar.solarToLunar(todayCal.timeInMillis)?.description ?: "" }
    val weekDayNames = arrayOf(zh("周日"), zh("周一"), zh("周二"), zh("周三"), zh("周四"), zh("周五"), zh("周六"))
    val todayWeekday = weekDayNames[todayCal.get(Calendar.DAY_OF_WEEK) - 1]
    val todayStatusSuffix = HolidayRemoteService.status(
        context, todayCal.get(Calendar.YEAR), todayCal.get(Calendar.MONTH) + 1, todayCal.get(Calendar.DAY_OF_MONTH)
    )?.let { if (it.isHoliday) zhf(" · %s休", it.name) else zh(" · 调休上班") } ?: ""

    val isCurrentMonth = displayYear == todayCal.get(Calendar.YEAR) && displayMonth == todayCal.get(Calendar.MONTH)

    ComicCard(
        modifier = modifier.pointerInput(displayYear, displayMonth) {
            var totalDrag = 0f
            detectHorizontalDragGestures(
                onDragStart = { totalDrag = 0f },
                onDragEnd = {
                    if (totalDrag > 80) shiftMonth(-1) else if (totalDrag < -80) shiftMonth(1)
                },
                onHorizontalDrag = { change, amount -> change.consume(); totalDrag += amount }
            )
        },
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showInCardNav) {
                ComicTextAction(text = "‹", onClick = { shiftMonth(-1) }, fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f).clickable { showMonthPicker = true }) {
                Text(
                    text = zhf("%1\$s年%2\$s月", displayYear, displayMonth + 1) + " ▾",
                    color = colors.ink,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
                if (isCurrentMonth) {
                    Text(
                        text = zhf("今天 · 农历%1\$s · %2\$s%3\$s", todayLunar, todayWeekday, todayStatusSuffix),
                        color = colors.muted,
                        fontSize = 12.sp
                    )
                }
            }
            if (!isCurrentMonth) {
                ComicTextAction(
                    text = zh("今天"),
                    accent = true,
                    onClick = { onYearMonthChange(todayCal.get(Calendar.YEAR), todayCal.get(Calendar.MONTH)) }
                )
            } else if (streak != null && streak > 0) {
                Text(
                    text = zhf("连续 %s 天", streak),
                    color = colors.accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }
            if (showInCardNav) {
                Spacer(Modifier.width(8.dp))
                ComicTextAction(text = "›", onClick = { shiftMonth(1) }, fontSize = 18.sp)
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.muted
                )
            }
        }

        val cells = remember(displayYear, displayMonth) { paddedMonthCells(displayYear, displayMonth) }

        cells.chunked(7).forEach { weekRow ->
            Row(modifier = Modifier.fillMaxWidth()) {
                weekRow.forEach { cell ->
                    val key = calendarDateKey(cell.year, cell.month, cell.day)
                    val isTodayCell = cell.year == todayCal.get(Calendar.YEAR) &&
                        cell.month == todayCal.get(Calendar.MONTH) &&
                        cell.day == todayCal.get(Calendar.DAY_OF_MONTH)
                    Box(modifier = Modifier.weight(1f)) {
                        ComicDayCell(
                            day = cell.day,
                            isToday = isTodayCell,
                            isSelected = selectedDateKey == key,
                            lunarText = calendarLunarTextFor(cell.year, cell.month, cell.day),
                            taskCount = if (cell.inMonth) taskDates[key] ?: 0 else 0,
                            muted = !cell.inMonth,
                            holidayStatus = HolidayRemoteService.status(context, cell.year, cell.month + 1, cell.day),
                            onClick = {
                                selectedDateKey = key
                                val t = Calendar.getInstance().apply {
                                    set(cell.year, cell.month, cell.day, 0, 0, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                                onDateClick(t)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showMonthPicker) {
        CalendarMonthPickerDialog(
            displayYear = displayYear,
            displayMonth = displayMonth,
            onYearMonthChange = onYearMonthChange,
            onDismiss = { showMonthPicker = false }
        )
    }
}
