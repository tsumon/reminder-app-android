package com.reminderapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.service.HolidayRemoteService
import com.reminderapp.service.LunarCalendar
import com.reminderapp.service.ReminderEngine
import com.reminderapp.ui.theme.Primary
import com.reminderapp.ui.theme.Tokens
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * 主页日历卡片：公历 + 农历 + 星期几 + 任务缩略标记
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarCard(
    reminders: List<ReminderEntity>,
    modifier: Modifier = Modifier,
    onDateClick: (Long) -> Unit = {}
) {
    val todayCal = remember { Calendar.getInstance() }
    val todayDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(todayCal.time)
    }
    val context = LocalContext.current
    var displayYear by remember { mutableIntStateOf(todayCal.get(Calendar.YEAR)) }
    var displayMonth by remember { mutableIntStateOf(todayCal.get(Calendar.MONTH)) } // 0-based
    var selectedDateKey by remember { mutableStateOf<String?>(null) }

    // 任务日期 → 数量映射（按 displayed 月份逐日判断是否触发，贴合所显示月份）
    val taskDates = remember(reminders, displayYear, displayMonth) {
        val map = mutableMapOf<String, Int>()
        val cal = Calendar.getInstance().apply { set(displayYear, displayMonth, 1) }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (d in 1..daysInMonth) {
            val count = reminders.count { it.isActive && ReminderEngine.occursOn(it, displayYear, displayMonth + 1, d) }
            if (count > 0) {
                map[String.format(Locale.getDefault(), "%04d-%02d-%02d", displayYear, displayMonth + 1, d)] = count
            }
        }
        map
    }

    // 今天农历 + 星期
    val todayLunar = remember {
        LunarCalendar.solarToLunar(todayCal.timeInMillis)?.description ?: ""
    }
    val weekDayNames = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
    val todayWeekday = weekDayNames[todayCal.get(Calendar.DAY_OF_WEEK) - 1]
    // v1.8.7 任务②: 今天的节假日状态后缀（联网数据，无则空）
    val todayStatusSuffix = HolidayRemoteService.status(
        context,
        todayCal.get(Calendar.YEAR),
        todayCal.get(Calendar.MONTH) + 1,
        todayCal.get(Calendar.DAY_OF_MONTH)
    )?.let { if (it.isHoliday) " · ${it.name}休" else " · 调休上班" } ?: ""

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            // === 头部：月份 + 切换 + 今天信息 ===
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        displayMonth--
                        if (displayMonth < 0) { displayMonth = 11; displayYear-- }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上一月")
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${displayYear}年${displayMonth + 1}月",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "农历$todayLunar · $todayWeekday$todayStatusSuffix",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = {
                        displayMonth++
                        if (displayMonth > 11) { displayMonth = 0; displayYear++ }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下一月")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // === 星期表头（周一开头）===
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("一", "二", "三", "四", "五", "六", "日").forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // === 日期网格 ===
            val cal = remember(displayYear, displayMonth) {
                Calendar.getInstance().apply {
                    set(displayYear, displayMonth, 1, 0, 0, 0)
                }
            }
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            // Calendar.DAY_OF_WEEK: 1=周日...7=周六 → 周一=1...周日=7
            val firstDayWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1
            val leadingBlanks = firstDayWeek - 1

            val totalCells = ((leadingBlanks + daysInMonth + 6) / 7) * 7
            val cells = List(totalCells) { idx ->
                val dayNum = idx - leadingBlanks + 1
                if (dayNum in 1..daysInMonth) dayNum else null
            }

            cells.chunked(7).forEach { weekRow ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    weekRow.forEach { dayNum ->
                        Box(modifier = Modifier.weight(1f)) {
                            if (dayNum != null) {
                                val key = dateKey(displayYear, displayMonth, dayNum)
                                DayCell(
                                    day = dayNum,
                                    isToday = displayYear == todayCal.get(Calendar.YEAR) &&
                                        displayMonth == todayCal.get(Calendar.MONTH) &&
                                        dayNum == todayCal.get(Calendar.DAY_OF_MONTH),
                                    isSelected = selectedDateKey == key,
                                    lunarText = lunarTextFor(displayYear, displayMonth, dayNum),
                                    taskCount = taskDates[key] ?: 0,
                                    isFutureMonth = false,
                                    holidayStatus = HolidayRemoteService.status(
                                        context, displayYear, displayMonth + 1, dayNum
                                    ),
                                    onClick = {
                                        selectedDateKey = key
                                        val t = Calendar.getInstance().apply {
                                            set(displayYear, displayMonth, dayNum, 0, 0, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }.timeInMillis
                                        onDateClick(t)
                                    }
                                )
                            } else {
                                Spacer(modifier = Modifier.height(52.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 单个日期格子：公历数字 + 农历 + 「休/班」角标 + 任务角标（数字右上）
 */
@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    lunarText: String,
    taskCount: Int,
    isFutureMonth: Boolean,
    holidayStatus: HolidayRemoteService.DayStatus?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isToday) Primary else if (isSelected) Primary.copy(alpha = 0.15f) else Color.Transparent)
                .then(if (isSelected && !isToday) Modifier.border(1.5.dp, Primary, CircleShape) else Modifier)
                .padding(1.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isToday -> Color.White
                    isSelected -> Primary
                    isFutureMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            // 任务角标：数字右上角小圆点（v1.8.7 移出第三行，腾位给「休/班」）
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(if (taskCount > 0) Primary else Color.Transparent)
            )
        }
        // 农历（初二~三十 简化显示）
        Text(
            text = lunarText,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            maxLines = 1
        )
        // 休/班角标：放假红「休」、调休上班橙「班」；普通日占位保持对齐（v1.8.7 任务②）
        Text(
            text = holidayStatus?.let { if (it.isHoliday) "休" else "班" } ?: "",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = Tokens.FontTiny, fontWeight = FontWeight.Bold),
            color = if (holidayStatus?.isHoliday == true) Tokens.HolidayRest else Tokens.HolidayWork,
            maxLines = 1
        )
    }
}

/** 获取某公历日期的农历显示文本 */
private fun lunarTextFor(year: Int, month: Int, day: Int): String {
    val cal = Calendar.getInstance().apply {
        set(year, month, day, 12, 0, 0) // 中午避免日期边界误差
        set(Calendar.MILLISECOND, 0)
    }
    val lunar = LunarCalendar.solarToLunar(cal.timeInMillis) ?: return ""
    return when (lunar.day) {
        1 -> "初一"
        else -> {
            val dayNames = arrayOf(
                "", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
                "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
                "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
            )
            dayNames.getOrElse(lunar.day) { "" }
        }
    }
}

/** 生成 yyyy-MM-dd 日期键 */
private fun dateKey(year: Int, month: Int, day: Int): String {
    return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)
}
