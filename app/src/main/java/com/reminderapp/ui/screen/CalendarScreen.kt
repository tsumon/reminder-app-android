package com.reminderapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.service.ReminderEngine
import com.reminderapp.ui.comic.ComicCard
import com.reminderapp.ui.comic.ComicTextAction
import com.reminderapp.ui.comic.ComicTopBar
import com.reminderapp.ui.comic.comicColors
import com.reminderapp.ui.comic.cycleSummaryLabel
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarScreen(
    reminders: List<ReminderEntity>,
    onReminderClick: (Long) -> Unit,
    onConfirm: (ReminderEntity) -> Unit = {},
    onCreateOnDay: (Long) -> Unit = {},
    streak: Int? = null,
    weekDone: Int? = null
) {
    val colors = comicColors()
    var selectedDate by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }
    val todayCal = remember { Calendar.getInstance() }
    var displayYear by remember { mutableIntStateOf(todayCal.get(Calendar.YEAR)) }
    var displayMonth by remember { mutableIntStateOf(todayCal.get(Calendar.MONTH)) }

    fun shiftMonth(delta: Int) {
        var m = displayMonth + delta
        var y = displayYear
        if (m < 0) {
            m = 11; y--
        } else if (m > 11) {
            m = 0; y++
        }
        displayYear = y
        displayMonth = m
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
    ) {
        ComicTopBar(
            title = zh("日历"),
            showShard = true,
            trailing = {
                ComicTextAction(
                    text = "‹ " + zh("上一月"),
                    onClick = { shiftMonth(-1) },
                    modifier = Modifier.semantics { contentDescription = zh("上一月") }
                )
                ComicTextAction(
                    text = zh("下一月") + " ›",
                    onClick = { shiftMonth(1) },
                    modifier = Modifier.semantics { contentDescription = zh("下一月") }
                )
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1 — Clean Unlined Month ComicCard
            item(key = "calendar-month") {
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

            // Card 2 — Unlined Day Schedule ComicCard
            item(key = "calendar-day-list") {
                val ts = selectedDate ?: System.currentTimeMillis()
                val cal = Calendar.getInstance().apply { timeInMillis = ts }
                val y = cal.get(Calendar.YEAR)
                val m = cal.get(Calendar.MONTH) + 1
                val d = cal.get(Calendar.DAY_OF_MONTH)
                val names = arrayOf(zh("周日"), zh("周一"), zh("周二"), zh("周三"), zh("周四"), zh("周五"), zh("周六"))
                val weekday = names[cal.get(Calendar.DAY_OF_WEEK) - 1]
                val dateReminders = reminders.filter {
                    it.isActive && ReminderEngine.occursOn(it, y, m, d)
                }.sortedBy { it.reminderHour * 60 + it.reminderMinute }

                ComicCard(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = zhf("%1\$s月%2\$s日", m, d) + " · $weekday",
                            color = colors.ink,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f),
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )
                        ComicTextAction(
                            text = "+ " + zh("添加当天提醒"),
                            onClick = { onCreateOnDay(ts) },
                            accent = true,
                            fontSize = 12.5.sp
                        )
                    }

                    if (dateReminders.isEmpty()) {
                        Text(
                            text = zh("这一天没有提醒"),
                            color = colors.muted,
                            fontSize = 13.5.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        val now = System.currentTimeMillis()
                        dateReminders.forEach { r ->
                            val due = r.isActive && r.status != "confirmed" &&
                                (r.status == "notifying" || r.status == "overdue" || r.status == "snoozed" ||
                                    (r.status == "pending" && r.nextTriggerAt <= now))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onReminderClick(r.id) }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = String.format(Locale.US, "%02d:%02d", r.reminderHour, r.reminderMinute),
                                    color = colors.muted,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    style = TextStyle(fontFeatureSettings = "tnum"),
                                    modifier = Modifier.width(52.dp)
                                )
                                Text(
                                    text = r.title,
                                    color = colors.ink,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = cycleSummaryLabel(r),
                                    color = colors.muted,
                                    fontSize = 12.sp
                                )
                                if (due) {
                                    Spacer(Modifier.width(10.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = zh("确认完成"),
                                        tint = colors.accent,
                                        modifier = Modifier
                                            .size(17.dp)
                                            .clickable { onConfirm(r) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
