package com.reminderapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.data.entity.ReminderRecordEntity
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.service.StatsService
import com.reminderapp.ui.theme.CheckInHeatmap
import com.reminderapp.ui.theme.SoftKind
import com.reminderapp.ui.theme.SoftDonutChart
import com.reminderapp.ui.theme.SoftScreenTitle
import com.reminderapp.ui.theme.StreakCastle
import com.reminderapp.ui.theme.Tokens
import com.reminderapp.ui.theme.softCard
import com.reminderapp.ui.theme.softCanvas
import com.reminderapp.ui.theme.softMuted
import com.reminderapp.ui.theme.softOk
import com.reminderapp.ui.theme.softText
import com.reminderapp.ui.theme.softWarn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    records: List<ReminderRecordEntity>,
    onBack: () -> Unit
) {
    val summary = remember(records) { StatsService.summarize(records) }
    val rate = summary.completionRate ?: 0.0

    Box(modifier = Modifier.fillMaxSize().background(softCanvas())) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                SoftScreenTitle(title = zh("统计洞察"))
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatMiniCard(zh("本月完成"), summary.confirmCount.toString(), softOk(), Modifier.weight(1f))
                        StatMiniCard(zh("连续天数"), summary.currentStreak.toString(), softWarn(), Modifier.weight(1f))
                        StatMiniCard(
                            zh("完成率"),
                            summary.completionRate?.let { "${(it * 100).toInt()}%" } ?: "0%",
                            Tokens.BrandPrimary,
                            Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Box(Modifier.fillMaxWidth().softCard(SoftKind.Card)) {
                        SoftDonutChart(rate = rate, confirm = summary.confirmCount, missed = summary.missedCount)
                    }
                }
                item {
                    Box(Modifier.fillMaxWidth().softCard(SoftKind.Card)) {
                        CheckInHeatmap(counts = summary.heatmap)
                    }
                }
                item { CastleCard(summary) }
                item { ForgetHoursCard(summary) }
            }
        }
    }
}

@Composable
private fun StatMiniCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .softCard(SoftKind.Elevated, Tokens.RadiusShallow)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = Tokens.TitleSize,
            lineHeight = Tokens.TitleLine,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.7).sp,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, fontSize = 11.sp, color = softMuted())
    }
}

@Composable
private fun CastleCard(summary: StatsService.Summary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .softCard(SoftKind.Card)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        StreakCastle(streakDays = summary.currentStreak)
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                zhf("打卡城堡 Lv.%s", StreakCastle.level(summary.currentStreak)),
                fontSize = Tokens.SectionSize,
                lineHeight = Tokens.SectionLine,
                fontWeight = FontWeight.SemiBold,
                color = softText()
            )
            Text(
                zhf("连续 %s 天，每 3 天加盖一层", summary.currentStreak),
                fontSize = 11.sp,
                color = softMuted()
            )
            Text(
                zhf("最长纪录 %s 天", summary.longestStreak),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Tokens.BrandPrimary
            )
        }
    }
}

@Composable
private fun ForgetHoursCard(summary: StatsService.Summary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .softCard(SoftKind.Card)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                zh("最常忘记时段"),
                fontSize = Tokens.SectionSize,
                lineHeight = Tokens.SectionLine,
                fontWeight = FontWeight.SemiBold,
                color = softText()
            )
            Spacer(Modifier.weight(1f))
            if (summary.forgetHours.isEmpty()) {
                Text(zh("暂无数据"), fontSize = 11.sp, color = softMuted())
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (summary.forgetHours.isEmpty()) {
            Text(zh("坚持得很好，没有漏掉过提醒"), fontSize = Tokens.BodySize, color = softMuted())
        } else {
            val medals = listOf("🥇", "🥈", "🥉")
            summary.forgetHours.forEachIndexed { idx, (hour, count) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(medals.getOrElse(idx) { "·" })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(zhf("%s:00 前后", hour), fontSize = Tokens.BodySize, fontWeight = FontWeight.Medium, color = softText())
                    Spacer(modifier = Modifier.weight(1f))
                    Text(zhf("漏 %s 次", count), fontSize = Tokens.BodySize, color = softMuted())
                }
            }
        }
    }
}
