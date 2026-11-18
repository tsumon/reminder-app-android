package com.reminderapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.data.entity.ReminderRecordEntity
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.service.StatsService
import com.reminderapp.ui.comic.ComicCard
import com.reminderapp.ui.comic.ComicHairline
import com.reminderapp.ui.comic.ComicHeroNumber
import com.reminderapp.ui.comic.ComicTextAction
import com.reminderapp.ui.comic.ComicTopBar
import com.reminderapp.ui.comic.comicColors
import com.reminderapp.ui.theme.CheckInHeatmap
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun StatsScreen(
    allRecords: List<ReminderRecordEntity>,
    onBack: () -> Unit
) {
    val colors = comicColors()
    var range by remember { mutableStateOf("week") }
    var offset by remember { mutableIntStateOf(0) }

    val today = LocalDate.now()
    val start = if (range == "month") {
        today.withDayOfMonth(1).plusMonths(offset.toLong())
    } else {
        today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(offset.toLong())
    }
    val end = if (range == "month") start.plusMonths(1) else start.plusWeeks(1)
    val zone = ZoneId.systemDefault()
    val records = allRecords.filter {
        range == "all" || (
            it.timestamp >= start.atStartOfDay(zone).toInstant().toEpochMilli() &&
                it.timestamp < end.atStartOfDay(zone).toInstant().toEpochMilli()
            )
    }
    val summary = remember(records) { StatsService.summarize(records) }
    val allTimeSummary = remember(allRecords) { StatsService.summarize(allRecords) }
    val completedOps = records.count { it.action == "confirmed" }
    val skippedOps = records.count { it.action == "skipped" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
    ) {
        ComicTopBar(title = zh("统计洞察"), showShard = true)

        // Bare Range Filter Strip on canvas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf("week" to "本周", "month" to "本月", "all" to "全部").forEach { (id, label) ->
                    val active = range == id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            range = id
                            offset = 0
                        }
                    ) {
                        Text(
                            text = zh(label),
                            color = if (active) colors.ink else colors.muted,
                            fontSize = 13.5.sp,
                            fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium
                        )
                        Spacer(Modifier.height(2.dp))
                        Box(
                            Modifier
                                .width(18.dp)
                                .height(2.dp)
                                .background(if (active) colors.accent else colors.canvas)
                        )
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            if (range != "all") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ComicTextAction(text = "‹", onClick = { offset-- }, fontSize = 15.sp)
                    Text(
                        text = "$start – ${end.minusDays(1)}",
                        color = colors.muted,
                        fontSize = 11.5.sp,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                    ComicTextAction(text = "›", onClick = { offset++ }, fontSize = 15.sp)
                }
            }
        }
        ComicHairline(isInner = false)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1 — #Streak ComicHeroNumber + Tabular Breakdown (NO cartoon castle!)
            item(key = "stats-hero") {
                ComicCard(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = zh("当前连续") + " (" + zh("天") + ")",
                                color = colors.muted,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            ComicHeroNumber(
                                text = String.format(Locale.US, "#%02d", allTimeSummary.currentStreak),
                                fontSize = 48.sp
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val rateText = summary.completionRate?.let { "${(it * 100).toInt()}%" } ?: "—"
                            Text(
                                text = zh("完成率") + " $rateText",
                                color = colors.ink,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.End,
                                style = TextStyle(fontFeatureSettings = "tnum")
                            )
                            Text(
                                text = zh("最长连续") + " · " + zhf("%s 天", allTimeSummary.longestStreak),
                                color = colors.muted,
                                fontSize = 12.5.sp,
                                style = TextStyle(fontFeatureSettings = "tnum")
                            )
                            Text(
                                text = "${zh("已完成")} $completedOps · ${zh("跳过")} $skippedOps · ${zh("漏掉")} ${summary.missedCount}",
                                color = colors.muted,
                                fontSize = 12.5.sp,
                                textAlign = TextAlign.End,
                                style = TextStyle(fontFeatureSettings = "tnum")
                            )
                        }
                    }

                    ComicHairline(isInner = true)

                    Text(
                        text = zh("漏掉按出现重试的日期计数；完成与跳过按操作次数计数。"),
                        color = colors.muted,
                        fontSize = 11.5.sp
                    )
                }
            }

            // Card 2 — Unbordered Warm Rounded Dot Matrix 16-Week Heatmap
            item(key = "stats-heatmap") {
                ComicCard(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    CheckInHeatmap(counts = allTimeSummary.heatmap)
                }
            }

            // Card 3 — Forget-Hour Insight List (Unlined)
            item(key = "stats-forget-hours") {
                ComicCard(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = zh("最常忘记时段"),
                            color = colors.ink,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f)
                        )
                        if (summary.forgetHours.isEmpty()) {
                            Text(zh("暂无"), color = colors.muted, fontSize = 12.sp)
                        }
                    }
                    if (summary.forgetHours.isEmpty()) {
                        Text(
                            text = zh("坚持得很好，没有漏掉过提醒 🎉"),
                            color = colors.muted,
                            fontSize = 13.5.sp
                        )
                    } else {
                        summary.forgetHours.forEachIndexed { idx, (hour, count) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${idx + 1}",
                                    color = colors.accent,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = TextStyle(fontFeatureSettings = "tnum"),
                                    modifier = Modifier.width(32.dp)
                                )
                                Text(
                                    text = zhf("%s:00 前后", hour),
                                    color = colors.ink,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    style = TextStyle(fontFeatureSettings = "tnum"),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = zhf("漏 %s 次", count),
                                    color = colors.muted,
                                    fontSize = 12.5.sp,
                                    style = TextStyle(fontFeatureSettings = "tnum")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
