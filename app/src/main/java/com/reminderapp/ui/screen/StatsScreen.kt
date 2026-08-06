package com.reminderapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
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
import com.reminderapp.data.entity.ReminderRecordEntity
import com.reminderapp.service.StatsService
import com.reminderapp.ui.theme.Tokens
import java.util.Calendar
import java.util.Locale

/**
 * 统计洞察页（v1.8.7 任务③）：完成率 / 连续打卡 / 最常忘记时段 / 月历热力图
 * 镜像 iOS StatsView.swift
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    records: List<ReminderRecordEntity>,
    onBack: () -> Unit
) {
    val summary = remember(records) { StatsService.summarize(records) }
    var displayMonth by remember { mutableStateOf(Calendar.getInstance()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计洞察") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
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
                        title = "本月完成",
                        value = summary.confirmCount.toString(),
                        color = Tokens.StatusCompleted,
                        modifier = Modifier.weight(1f)
                    )
                    StatMiniCard(
                        title = "连续天数",
                        value = summary.currentStreak.toString(),
                        color = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                    StatMiniCard(
                        title = "完成率",
                        value = summary.completionRate?.let { "${(it * 100).toInt()}%" } ?: "—",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { CompletionCard(summary) }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StreakCard(
                        title = "当前连续",
                        value = summary.currentStreak,
                        icon = Icons.Filled.Check,
                        color = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                    StreakCard(
                        title = "最长连续",
                        value = summary.longestStreak,
                        icon = Icons.Filled.Star,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { ForgetHoursCard(summary) }

            item {
                HeatmapCard(
                    summary = summary,
                    displayMonth = displayMonth,
                    onPrevMonth = {
                        displayMonth = Calendar.getInstance().apply {
                            timeInMillis = displayMonth.timeInMillis
                            add(Calendar.MONTH, -1)
                        }
                    },
                    onNextMonth = {
                        displayMonth = Calendar.getInstance().apply {
                            timeInMillis = displayMonth.timeInMillis
                            add(Calendar.MONTH, 1)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CompletionCard(summary: StatsService.Summary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 完成率环形进度
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { summary.completionRate?.toFloat() ?: 0f },
                    modifier = Modifier.size(140.dp),
                    strokeWidth = 12.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = summary.completionRate?.let { "${(it * 100).toInt()}%" } ?: "—",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "完成率",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                LabelChip("确认 ${summary.confirmCount}", Icons.Filled.CheckCircle, Tokens.StatusCompleted)
                LabelChip("漏掉 ${summary.missedCount}", Icons.Filled.Notifications, Tokens.StatusReminding)
            }
        }
    }
}

/** v1.9.8 设计图风格：数字概览小卡（大数字 + 小标题） */
@Composable
private fun StatMiniCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Tokens.RadiusCell),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
}

@Composable
private fun LabelChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
private fun StreakCard(
    title: String,
    value: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text("$value 天", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ForgetHoursCard(summary: StatsService.Summary) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("最常忘记时段", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (summary.forgetHours.isEmpty()) {
                Text(
                    "坚持得很好，没有漏掉过提醒 🎉",
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
                        Text("$hour:00 前后", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("漏 $count 次", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapCard(
    summary: StatsService.Summary,
    displayMonth: Calendar,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val year = displayMonth.get(Calendar.YEAR)
    val month = displayMonth.get(Calendar.MONTH) // 0-based

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("月历热力图", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onPrevMonth, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上一月")
                }
                Text("${year}年${month + 1}月", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onNextMonth, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下一月")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 星期表头
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

            Spacer(modifier = Modifier.height(6.dp))

            // 热力格子
            val cal = Calendar.getInstance().apply { set(year, month, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0) }
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val firstWeekday = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1
            val leading = firstWeekday - 1
            val total = ((leading + daysInMonth + 6) / 7) * 7

            val df = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                for (row in 0 until total / 7) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val idx = row * 7 + col
                            val day = idx - leading + 1
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                if (day in 1..daysInMonth) {
                                    val cal2 = Calendar.getInstance().apply {
                                        set(year, month, day, 12, 0, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    val key = df.format(cal2.time)
                                    val count = summary.heatmap[key] ?: 0
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 3.dp)
                                                .height(28.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(heatColor(heatLevel(count)))
                                        )
                                        Text("$day", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(34.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 图例
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("少", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(6.dp))
                for (lv in 0..3) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(heatColor(lv))
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("多", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Filled.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            }
        }
    }
}

/** 0=无，1=1次，2=2-3次，3=4+次（色阶令牌化，与 iOS 一致） */
private fun heatLevel(count: Int): Int = when {
    count <= 0 -> 0
    count == 1 -> 1
    count in 2..3 -> 2
    else -> 3
}

private fun heatColor(level: Int): Color = when (level) {
    0 -> Tokens.Heatmap0
    1 -> Tokens.Heatmap1
    2 -> Tokens.Heatmap2
    else -> Tokens.Heatmap3
}
