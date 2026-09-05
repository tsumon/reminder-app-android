package com.reminderapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.service.ReminderEngine
import com.reminderapp.ui.theme.SoftCircleButton
import com.reminderapp.ui.theme.SoftScreenTitle
import com.reminderapp.ui.theme.SoftSectionHeader
import com.reminderapp.ui.theme.StatusOverdue
import com.reminderapp.ui.theme.StatusReminding
import com.reminderapp.ui.theme.StatusWaiting
import com.reminderapp.ui.theme.Tokens
import com.reminderapp.ui.theme.softCanvas
import com.reminderapp.ui.theme.softMuted
import com.reminderapp.ui.theme.softText
import com.reminderapp.ui.theme.softSurface
import java.util.*
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf

/**
 * 日历 Tab（v1.9.8 UI 对齐设计图）：
 * 整页月历（农历/节假日/任务标记）+ 点击日期展示「当天任务」列表
 * v2.5.0: 传入 streak/weekDone 时日历卡底部展示本周彩虹跑道（默认 null 隐藏）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    reminders: List<ReminderEntity>,
    onReminderClick: (Long) -> Unit,
    onConfirm: (ReminderEntity) -> Unit = {},
    streak: Int? = null,
    weekDone: Int? = null
) {
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    val todayCal = remember { Calendar.getInstance() }
    var displayYear by remember { mutableIntStateOf(todayCal.get(Calendar.YEAR)) }
    var displayMonth by remember { mutableIntStateOf(todayCal.get(Calendar.MONTH)) }

    fun shiftMonth(delta: Int) {
        var m = displayMonth + delta
        var y = displayYear
        if (m < 0) { m = 11; y-- }
        else if (m > 11) { m = 0; y++ }
        displayYear = y
        displayMonth = m
    }

    Box(Modifier.fillMaxSize().background(softCanvas())) {
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            SoftScreenTitle(
                title = zh("日历"),
                leading = {
                    SoftCircleButton(onClick = { shiftMonth(-1) }, size = 40.dp, contentDescription = zh("上一月")) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = null,
                            tint = Tokens.BrandPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                trailing = {
                    SoftCircleButton(onClick = { shiftMonth(1) }, size = 40.dp, contentDescription = zh("下一月")) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Tokens.BrandPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 整页月历
            item {
                CalendarCard(
                    reminders = reminders,
                    displayYear = displayYear,
                    displayMonth = displayMonth,
                    onYearMonthChange = { y, m ->
                        displayYear = y
                        displayMonth = m
                    },
                    onDateClick = { selectedDate = it },
                    streak = streak,
                    weekDone = weekDone,
                    showInCardNav = false
                )
            }

            // 当日任务标题
            val ts = selectedDate
            item {
                if (ts == null) {
                    SoftSectionHeader(zh("点击日期查看当天任务"), StatusWaiting)
                } else {
                    val cal = Calendar.getInstance().apply { timeInMillis = ts }
                    val names = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
                    val weekday = names[cal.get(Calendar.DAY_OF_WEEK) - 1]
                    SoftSectionHeader(
                        zhf("%1\$s月%2\$s日 · %3\$s的任务", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), weekday),
                        StatusReminding,
                        count = reminders.count {
                            it.isActive && ReminderEngine.occursOn(
                                it,
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH) + 1,
                                cal.get(Calendar.DAY_OF_MONTH)
                            )
                        }
                    )
                }
            }

            // 当日任务列表
            if (ts != null) {
                val cal = Calendar.getInstance().apply { timeInMillis = ts }
                val y = cal.get(Calendar.YEAR)
                val m = cal.get(Calendar.MONTH) + 1
                val d = cal.get(Calendar.DAY_OF_MONTH)
                val dateReminders = reminders.filter { it.isActive && ReminderEngine.occursOn(it, y, m, d) }

                if (dateReminders.isEmpty()) {
                    item {
                        Text(
                            zh("这一天没有提醒"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = softMuted(),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp)
                        )
                    }
                } else {
                    items(dateReminders, key = { it.id }) { r ->
                        val now = System.currentTimeMillis()
                        val due = r.isActive && r.status != "confirmed" &&
                            (r.status == "notifying" || r.status == "overdue" || r.status == "snoozed" ||
                                (r.status == "pending" && r.nextTriggerAt <= now))
                        ReminderCard(
                            r,
                            if (r.status == "overdue") StatusOverdue else StatusReminding,
                            onDelete = {},
                            onClick = { onReminderClick(r.id) },
                            onConfirm = if (due) ({ onConfirm(r) }) else null
                        )
                    }
                }
            }
        }
    }
    }
}