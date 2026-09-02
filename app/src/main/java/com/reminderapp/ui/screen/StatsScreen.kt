package com.reminderapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.data.entity.ReminderRecordEntity
import com.reminderapp.service.StatsService
import com.reminderapp.ui.theme.PastelBackground
import com.reminderapp.ui.theme.Playful
import com.reminderapp.ui.theme.StreakCastle
import com.reminderapp.ui.theme.Tokens
import com.reminderapp.ui.theme.clayCard
import com.reminderapp.ui.theme.weekDayConfirmCounts
import java.util.Calendar
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf

/**
 * 统计洞察页：本月完成 / 连续天数 / 完成率 + 打卡城堡 / 本周花园 / 最常忘记时段
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    records: List<ReminderRecordEntity>,
    onBack: () -> Unit
) {
    val summary = remember(records) { StatsService.summarize(records) }

    // v2.5.0: 治愈游戏化——桃粉薰衣草渐变背景透出
    Box(modifier = Modifier.fillMaxSize()) {
        PastelBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(zh("统计洞察")) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // v1.9.8 设计图风格：顶部 3 数字概览卡（本月完成 / 连续天数 / 完成率）
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatMiniCard(
                        title = zh("本月完成"),
                        value = summary.confirmCount.toString(),
                        color = Tokens.StatusCompleted,
                        modifier = Modifier.weight(1f)
                    )
                    StatMiniCard(
                        title = zh("连续天数"),
                        value = summary.currentStreak.toString(),
                        color = Tokens.StatusSnoozed,
                        modifier = Modifier.weight(1f)
                    )
                    StatMiniCard(
                        title = zh("完成率"),
                        value = summary.completionRate?.let { "${(it * 100).toInt()}%" } ?: "—",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LabelChip(zhf("确认 %s", summary.confirmCount), Icons.Filled.CheckCircle, Tokens.StatusCompleted)
                    Spacer(modifier = Modifier.width(16.dp))
                    LabelChip(zhf("漏掉 %s", summary.missedCount), Icons.Filled.Notifications, Tokens.StatusReminding)
                }
            }

            item { CastleCard(summary) }
            item { GardenCard(records) }
            item { ForgetHoursCard(summary) }
        }
        } // Scaffold
    } // 背景 Box
}

/** v1.9.8 设计图风格：数字概览小卡（大数字 + 小标题） */
@Composable
private fun StatMiniCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clayCard(radiusDp = 16.dp)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LabelChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

/** v2.5.0: 连续打卡城堡大卡（替代两张 streak 小卡） */
@Composable
private fun CastleCard(summary: StatsService.Summary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clayCard(radiusDp = 20.dp)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        StreakCastle(streakDays = summary.currentStreak)
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                "打卡城堡 Lv.${StreakCastle.level(summary.currentStreak)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "连续 ${summary.currentStreak} 天，每 3 天加盖一层",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "最长纪录 ${summary.longestStreak} 天 🏆",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Playful.purple
            )
        }
    }
}

/** v2.5.0: 本周盆栽花园——周一~日 7 盆植物按当日打卡次数生长（🌰→🌱→🌿→🌳），今日金 15% 高亮 */
@Composable
private fun GardenCard(records: List<ReminderRecordEntity>) {
    val counts = remember(records) { weekDayConfirmCounts(records) }
    val todayIdx = remember {
        (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7 // 0=周一
    }
    val names = listOf("一", "二", "三", "四", "五", "六", "日")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clayCard(radiusDp = 20.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🌿 本周花园", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "打卡越多长得越高 🌱",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(verticalAlignment = Alignment.Bottom) {
            names.forEachIndexed { idx, name ->
                val count = counts[idx]
                val isFuture = idx > todayIdx
                val isToday = idx == todayIdx
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isToday) Playful.gold.copy(alpha = 0.15f) else Color.Transparent)
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        plantEmoji(count),
                        fontSize = when {
                            count >= 5 -> 27.sp
                            count >= 3 -> 23.sp
                            else -> 18.sp
                        },
                        modifier = Modifier.alpha(if (isFuture) 0.35f else 1f)
                    )
                    Text(
                        name,
                        fontSize = 10.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) Playful.purple else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$count",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

/** 盆栽生长阶段：0=种子，1-2=发芽，3-4=长叶，5+=成树 */
private fun plantEmoji(count: Int): String = when (count) {
    0 -> "🌰"
    in 1..2 -> "🌱"
    in 3..4 -> "🌿"
    else -> "🌳"
}

@Composable
private fun ForgetHoursCard(summary: StatsService.Summary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clayCard(radiusDp = 20.dp)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("🕘 最常忘记时段", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (summary.forgetHours.isEmpty()) {
            Text(
                zh("坚持得很好，没有漏掉过提醒 🎉"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val medals = listOf("🥇", "🥈", "🥉")
            summary.forgetHours.forEachIndexed { idx, (hour, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(medals.getOrElse(idx) { "·" })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(zhf("%s:00 前后", hour), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(zhf("漏 %s 次", count), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
