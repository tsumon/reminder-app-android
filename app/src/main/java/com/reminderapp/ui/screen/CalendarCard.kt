package com.reminderapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.service.HolidayRemoteService
import com.reminderapp.service.LunarCalendar
import com.reminderapp.service.ReminderEngine
import com.reminderapp.ui.theme.Primary
import com.reminderapp.ui.theme.SoftKind
import com.reminderapp.ui.theme.Tokens
import com.reminderapp.ui.theme.softCard
import com.reminderapp.ui.theme.softMuted
import com.reminderapp.ui.theme.softText
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf

/**
 * 主页日历卡片：公历 + 农历 + 星期几 + 任务缩略标记
 * v2.5.0 治愈游戏化：粘土卡底 + 逐格热力密度 + 今日吉祥物
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarCard(
    reminders: List<ReminderEntity>,
    displayYear: Int,
    displayMonth: Int,
    onYearMonthChange: (year: Int, month: Int) -> Unit,
    modifier: Modifier = Modifier,
    onDateClick: (Long) -> Unit = {},
    /** v2.5.0: 连续打卡天数（预留口径，与 iOS 对齐） */
    streak: Int? = null,
    /** v2.5.0: 本周打卡天数（彩虹跑道/吉祥物心情），null 隐藏 */
    weekDone: Int? = null,
    showInCardNav: Boolean = false
) {
    val todayCal = remember { Calendar.getInstance() }
    val todayDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(todayCal.time)
    }
    val context = LocalContext.current
    fun shiftMonth(delta: Int) {
        var m = displayMonth + delta
        var y = displayYear
        if (m < 0) { m = 11; y-- }
        else if (m > 11) { m = 0; y++ }
        onYearMonthChange(y, m)
    }
    var selectedDateKey by remember { mutableStateOf<String?>(todayDate) }
    LaunchedEffect(Unit) {
        val t0 = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        onDateClick(t0)
    }
    // v1.8.7 UI 优化: 月份选择器弹窗
    var showMonthPicker by remember { mutableStateOf(false) }

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
        // v2.4.5 诊断：日历任务点不渲染时定位是计算层还是渲染层
        android.util.Log.d("CalendarCard", "taskDates=$map (n=${reminders.size})")
        map
    }

    // 今天农历 + 星期
    val todayLunar = remember {
        LunarCalendar.solarToLunar(todayCal.timeInMillis)?.description ?: ""
    }
    val weekDayNames = arrayOf(zh("周日"), zh("周一"), zh("周二"), zh("周三"), zh("周四"), zh("周五"), zh("周六"))
    val todayWeekday = weekDayNames[todayCal.get(Calendar.DAY_OF_WEEK) - 1]
    // v1.8.7 任务②: 今天的节假日状态后缀（联网数据，无则空）
    val todayStatusSuffix = HolidayRemoteService.status(
        context,
        todayCal.get(Calendar.YEAR),
        todayCal.get(Calendar.MONTH) + 1,
        todayCal.get(Calendar.DAY_OF_MONTH)
    )?.let { if (it.isHoliday) zhf(" · %s休", it.name) else zh(" · 调休上班") } ?: ""

    Box(
        modifier = modifier
            .fillMaxWidth()
            .softCard(SoftKind.Elevated)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 12.dp)
                // v1.8.7 UI 优化: 左右滑动快速切月（与纵向滚动不冲突）
                .pointerInput(Unit) {
                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onDragEnd = {
                            if (totalDrag > 80) shiftMonth(-1)
                            else if (totalDrag < -80) shiftMonth(1)
                        },
                        onHorizontalDrag = { change, amount ->
                            change.consume()
                            totalDrag += amount
                        }
                    )
                }
        ) {
            // === 头部：月份 + 切换 + 今天信息 ===
            // v2.0.22: 是否正在浏览当月（副标题据此显示「今天」信息，避免跨月语义错位）
            val isCurrentMonth = displayYear == todayCal.get(Calendar.YEAR) &&
                displayMonth == todayCal.get(Calendar.MONTH)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showInCardNav) {
                    IconButton(onClick = { shiftMonth(-1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = zh("上一月"))
                    }
                }
                // 月份标题：点击弹「月份选择器」（v1.8.7 UI 优化）
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showMonthPicker = true }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = zhf("%1\$s年%2\$s月", displayYear, displayMonth + 1),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = zh("选择月份"),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // v2.0.22: 副标题只描述当前浏览月份——原实现始终显示「今天」的
                    // 农历/星期/节假日，切到明年某月时标题与副标题语义错位
                    Text(
                        text = if (isCurrentMonth) {
                            zhf("今天 · 农历%1\$s · %2\$s%3\$s", todayLunar, todayWeekday, todayStatusSuffix)
                        } else {
                            ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // 回到今天（非当月时显示，v1.8.7 UI 优化）
                if (!isCurrentMonth) {
                    TextButton(onClick = {
                        onYearMonthChange(todayCal.get(Calendar.YEAR), todayCal.get(Calendar.MONTH))
                    }) {
                        Text(zh("今天"), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (showInCardNav) {
                    IconButton(onClick = { shiftMonth(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = zh("下一月"))
                    }
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

            // === 日期网格（含邻月日期，对齐 iOS / comps）===
            val cells = remember(displayYear, displayMonth) {
                paddedMonthCells(displayYear, displayMonth)
            }

            cells.chunked(7).forEach { weekRow ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    weekRow.forEach { cell ->
                        Box(modifier = Modifier.weight(1f)) {
                            val key = dateKey(cell.year, cell.month, cell.day)
                            val isTodayCell = cell.year == todayCal.get(Calendar.YEAR) &&
                                cell.month == todayCal.get(Calendar.MONTH) &&
                                cell.day == todayCal.get(Calendar.DAY_OF_MONTH)
                            DayCell(
                                day = cell.day,
                                isToday = isTodayCell,
                                isSelected = selectedDateKey == key,
                                lunarText = lunarTextFor(cell.year, cell.month, cell.day),
                                taskCount = if (cell.inMonth) taskDates[key] ?: 0 else 0,
                                muted = !cell.inMonth,
                                holidayStatus = HolidayRemoteService.status(
                                    context, cell.year, cell.month + 1, cell.day
                                ),
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
    }

    // v1.8.7 UI 优化: 月份选择器（年份 +/- + 12 个月网格）
    if (showMonthPicker) {
        AlertDialog(
            onDismissRequest = { showMonthPicker = false },
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
                val monthNames = listOf(zh("一月"), zh("二月"), zh("三月"), zh("四月"), zh("五月"), zh("六月"),
                    zh("七月"), zh("八月"), zh("九月"), zh("十月"), zh("十一月"), zh("十二月"))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    monthNames.chunked(4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { name ->
                                val idx = monthNames.indexOf(name)
                                Box(modifier = Modifier.weight(1f)) {
                                    TextButton(
                                        onClick = {
                                            onYearMonthChange(displayYear, idx)
                                            showMonthPicker = false
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.textButtonColors(
                                            containerColor = if (idx == displayMonth) Tokens.BrandPrimary
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            contentColor = if (idx == displayMonth) Color.White
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                    ) {
                                        Text(name, fontWeight = if (idx == displayMonth) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMonthPicker = false }) { Text(zh("取消")) }
            }
        )
    }
}

@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    lunarText: String,
    taskCount: Int,
    muted: Boolean,
    holidayStatus: HolidayRemoteService.DayStatus?,
    onClick: () -> Unit
) {
    val holidayMark = holidayStatus?.let { if (it.isHoliday) zh("休") else zh("班") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isToday) {
            Column(
                modifier = Modifier
                    .width(32.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Tokens.BrandPrimary),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🦊", fontSize = 11.sp)
                Text(
                    "$day",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Tokens.OnStrong
                )
                Text(
                    holidayMark ?: lunarText,
                    fontSize = 9.sp,
                    color = Tokens.OnStrong.copy(alpha = 0.9f),
                    maxLines = 1
                )
            }
        } else {
            Text(
                text = day.toString(),
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = when {
                    muted -> softMuted().copy(alpha = 0.45f)
                    isSelected -> Tokens.BrandPrimary
                    else -> softText()
                }
            )
            if (holidayMark != null) {
                Text(
                    holidayMark,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (holidayStatus?.isHoliday == true) Tokens.HolidayRest else Tokens.HolidayWork,
                    maxLines = 1
                )
            } else {
                Text(
                    lunarText,
                    fontSize = Tokens.FontTiny,
                    color = softMuted().copy(alpha = if (muted) 0.45f else 1f),
                    maxLines = 1
                )
            }
            Box(
                Modifier
                    .padding(top = 1.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(if (taskCount > 0) Tokens.BrandPrimary else Color.Transparent)
            )
        }
    }
}

/** 获取某公历日期的农历显示文本 */
private fun lunarTextFor(year: Int, month: Int, day: Int): String {
    val cal = Calendar.getInstance().apply {
        set(year, month, day, 12, 0, 0) // 中午避免日期边界误差
        set(Calendar.MILLISECOND, 0)
    }
    val lunar = LunarCalendar.solarToLunar(cal.timeInMillis) ?: return ""
    // LunarDate.day 是 1-based（1=初一…30=三十）。注意索引 day-1：
    // 旧实现用 getOrElse(lunar.day) 直接索引导致「初二」起错位一天、
    // 「三十」(day=30)越界返回空白（如 2026-08-12 六月三十不显示）。
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

/** 生成 yyyy-MM-dd 日期键 */
private fun dateKey(year: Int, month: Int, day: Int): String {
    return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)
}

/** 月格单元：本月 + 前后邻月补齐整周（month 为 Calendar 0-based） */
private data class MonthCell(
    val year: Int,
    val month: Int,
    val day: Int,
    val inMonth: Boolean
)

private fun paddedMonthCells(displayYear: Int, displayMonth: Int): List<MonthCell> {
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
        for (d in (prevDays - leading + 1)..prevDays) {
            cells.add(MonthCell(py, pm, d, false))
        }
    }
    for (d in 1..daysInMonth) {
        cells.add(MonthCell(displayYear, displayMonth, d, true))
    }
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
