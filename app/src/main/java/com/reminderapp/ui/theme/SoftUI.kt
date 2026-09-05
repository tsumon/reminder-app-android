package com.reminderapp.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.i18n.zh
import java.util.Calendar
import kotlin.math.min

private val OutQuartMs = 180

enum class SoftKind { Sm, Card, Elevated }

@Composable
fun Modifier.softCard(
    kind: SoftKind = SoftKind.Card,
    radius: Dp = when (kind) {
        SoftKind.Sm -> Tokens.RadiusChip
        SoftKind.Card -> Tokens.RadiusCard
        SoftKind.Elevated -> Tokens.RadiusElevated
    },
    fill: Color = when (kind) {
        SoftKind.Elevated -> softElevated()
        else -> softSurface()
    }
): Modifier {
    val dark = isSoftDark()
    val shape = RoundedCornerShape(radius)
    val hairline = when (kind) {
        SoftKind.Sm -> 0.04f
        SoftKind.Card -> 0.06f
        SoftKind.Elevated -> 0.07f
    }
    return this
        .drawBehind {
            val cr = radius.toPx()
            if (dark) {
                drawRoundRect(
                    color = Color.Black.copy(alpha = if (kind == SoftKind.Elevated) 0.50f else 0.46f),
                    topLeft = Offset(0f, 10.dp.toPx()),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(cr, cr),
                    alpha = 0.62f
                )
            } else {
                val y1 = if (kind == SoftKind.Sm) 1.dp.toPx() else 1.dp.toPx()
                val blurHint = if (kind == SoftKind.Elevated) 8.dp.toPx() else 10.dp.toPx()
                drawRoundRect(
                    color = Color(0xFF111111).copy(alpha = 0.05f),
                    topLeft = Offset(0f, y1),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(cr, cr)
                )
                drawRoundRect(
                    color = Color(0xFF111111).copy(alpha = if (kind == SoftKind.Card) 0.08f else 0.05f),
                    topLeft = Offset(0f, blurHint * 0.35f),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(cr, cr)
                )
            }
        }
        .background(fill, shape)
        .then(
            if (dark) Modifier.drawWithContent {
                drawContent()
                drawRoundRect(
                    color = Color.White.copy(alpha = hairline),
                    cornerRadius = CornerRadius(radius.toPx(), radius.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            } else Modifier
        )
        .clip(shape)
}

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    kind: SoftKind = SoftKind.Card,
    radius: Dp = when (kind) {
        SoftKind.Elevated -> Tokens.RadiusElevated
        SoftKind.Sm -> Tokens.RadiusChip
        SoftKind.Card -> Tokens.RadiusCard
    },
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.softCard(kind, radius)) { content() }
}

@Composable
fun SoftCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = Tokens.BtnCircle,
    fill: Color = softSurface(),
    contentDescription: String? = null,
    content: @Composable () -> Unit
) {
    val dark = isSoftDark()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier = modifier
            .size(size)
            .drawBehind {
                if (dark) {
                    drawCircle(Color.Black.copy(alpha = 0.50f), radius = size.toPx() / 2f, center = center + Offset(0f, 6.dp.toPx()))
                } else {
                    drawCircle(Color(0xFF111111).copy(alpha = 0.05f), radius = size.toPx() / 2f, center = center + Offset(0f, 1.dp.toPx()))
                    drawCircle(Color(0xFF111111).copy(alpha = 0.08f), radius = size.toPx() / 2f, center = center + Offset(0f, 4.dp.toPx()))
                }
            }
            .clip(CircleShape)
            .background(fill)
            .drawWithContent {
                drawContent()
                drawCircle(
                    brush = Brush.verticalGradient(
                        if (dark) listOf(
                            Color.White.copy(alpha = 0.28f),
                            Color.White.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                        else listOf(Color.White.copy(alpha = 0.92f), Color.White.copy(alpha = 0.18f))
                    )
                )
                if (dark) {
                    val canvas = this.size
                    drawOval(
                        color = Color.White.copy(alpha = 0.16f),
                        topLeft = Offset(canvas.width * 0.19f, canvas.height * 0.08f),
                        size = androidx.compose.ui.geometry.Size(canvas.width * 0.62f, canvas.height * 0.22f)
                    )
                }
                if (pressed) {
                    drawCircle(Color(0xFF111111).copy(alpha = if (dark) 0.35f else 0.08f))
                }
                if (dark) {
                    drawCircle(Color.White.copy(alpha = 0.10f), style = Stroke(1.dp.toPx()))
                }
            }
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .then(if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription } else Modifier),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
fun SoftInsetSearch(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = zh("搜索提醒、日期...")
) {
    val dark = isSoftDark()
    val well = if (dark) Color.Black.copy(alpha = 0.35f) else Color(0xFFE8E8E4)
    val muted = softMuted()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(well)
            .drawBehind {
                drawRoundRect(
                    color = Color.Black.copy(alpha = if (dark) 0.45f else 0.06f),
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    style = Stroke(1.dp.toPx())
                )
            }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = muted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(placeholder, color = muted.copy(alpha = 0.85f), fontSize = Tokens.BodySize, lineHeight = Tokens.BodyLine)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = softText(),
                    fontSize = Tokens.BodySize,
                    lineHeight = Tokens.BodyLine
                ),
                cursorBrush = SolidColor(Tokens.BrandPrimary),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (value.isNotEmpty()) {
            Icon(
                Icons.Default.Close,
                contentDescription = zh("清除"),
                tint = muted,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onValueChange("") }
            )
        }
    }
}

@Composable
fun SoftChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        if (selected) Tokens.BrandPrimary else softSurface(),
        animationSpec = tween(OutQuartMs),
        label = "chipBg"
    )
    val fg by animateColorAsState(
        if (selected) Tokens.OnStrong else softMuted(),
        animationSpec = tween(OutQuartMs),
        label = "chipFg"
    )
    Box(
        modifier = Modifier
            .height(Tokens.ChipH)
            .softCard(SoftKind.Sm, Tokens.RadiusChip, fill = bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(title, color = fg, fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SoftSectionHeader(title: String, color: Color, count: Int? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 4.dp, end = 4.dp)
    ) {
        Box(
            Modifier
                .width(3.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            if (count != null) "$title · $count" else title,
            fontSize = Tokens.SectionSize,
            lineHeight = Tokens.SectionLine,
            fontWeight = FontWeight.SemiBold,
            color = softText()
        )
    }
}

@Composable
fun PendingCountRing(count: Int, progress: Float, modifier: Modifier = Modifier) {
    val track = softTrack()
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
            androidx.compose.foundation.Canvas(Modifier.size(52.dp)) {
                val stroke = 2.5.dp.toPx()
                drawCircle(track, style = Stroke(stroke))
                if (progress > 0.002f) {
                    drawArc(
                        color = Tokens.BrandPrimary,
                        startAngle = -90f,
                        sweepAngle = 360f * progress.coerceIn(0f, 1f),
                        useCenter = false,
                        style = Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                }
            }
            Text(
                "$count",
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.7).sp,
                color = Tokens.BrandPrimary
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(zh("待处理"), fontSize = 11.sp, lineHeight = 14.sp, color = softMuted())
    }
}

@Composable
fun SoftDonutChart(rate: Double, confirm: Int, missed: Int, modifier: Modifier = Modifier) {
    val ok = softOk()
    val danger = softDanger()
    val dark = isSoftDark()
    val track = if (dark) Color.White.copy(alpha = 0.06f) else Color(0xFF111111).copy(alpha = 0.06f)
    val confirmShare = if (confirm + missed > 0) confirm.toFloat() / (confirm + missed) else 0f
    val missedShare = if (confirm + missed > 0) missed.toFloat() / (confirm + missed) else 0f
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.Canvas(Modifier.size(124.dp)) {
            fun ring(radiusPx: Float, stroke: Float, progress: Float, color: Color) {
                val gap = 0.08f
                drawCircle(track, radius = radiusPx, style = Stroke(stroke))
                if (progress > 0.002f) {
                    val sweep = 360f * (progress - gap).coerceAtLeast(0f)
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                        size = Size(radiusPx * 2, radiusPx * 2),
                        topLeft = Offset(center.x - radiusPx, center.y - radiusPx)
                    )
                    drawArc(
                        color = Color.White.copy(alpha = if (dark) 0.28f else 0.40f),
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(2.2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
                        size = Size((radiusPx - stroke * 0.275f) * 2, (radiusPx - stroke * 0.275f) * 2),
                        topLeft = Offset(
                            center.x - (radiusPx - stroke * 0.275f),
                            center.y - (radiusPx - stroke * 0.275f)
                        )
                    )
                }
            }
            ring(54.dp.toPx(), 12.dp.toPx(), rate.toFloat(), Tokens.BrandPrimary)
            ring(38.dp.toPx(), 12.dp.toPx(), confirmShare, ok)
            ring(22.dp.toPx(), 11.dp.toPx(), missedShare, danger)
        }
        Spacer(Modifier.width(18.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DonutLegend(Tokens.BrandPrimary, zh("完成率"), "${(rate * 100).toInt()}%")
            DonutLegend(ok, zh("确认"), "$confirm")
            DonutLegend(danger, zh("漏掉"), "$missed")
        }
    }
}

@Composable
private fun DonutLegend(color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 11.sp, color = softMuted())
        Spacer(Modifier.weight(1f))
        Text(
            value,
            fontSize = Tokens.SectionSize,
            lineHeight = Tokens.SectionLine,
            fontWeight = FontWeight.SemiBold,
            color = softText()
        )
    }
}

@Composable
fun CheckInHeatmap(counts: Map<String, Int>, weeks: Int = 16, modifier: Modifier = Modifier) {
    val cal = rememberHeatStart(weeks)
    val cells = remember(counts, weeks) { heatCells(counts, weeks) }
    val rowLabels = listOf("一", "", "三", "", "五", "", "日")
    Column(modifier = modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(zh("本月打卡"), fontSize = Tokens.SectionSize, lineHeight = Tokens.SectionLine, fontWeight = FontWeight.SemiBold, color = softText())
            Spacer(Modifier.weight(1f))
            Text("${weeks} 周", fontSize = 11.sp, color = softMuted())
        }
        Row {
            Spacer(Modifier.width(16.dp))
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                cells.chunked(7).forEachIndexed { w, week ->
                    val weekMonths = week.map { it.month }.toSet()
                    val seen = if (w == 0) emptySet() else cells.take(w * 7).map { it.month }.toSet()
                    val fresh = weekMonths - seen
                    val month = fresh.minOrNull() ?: week.first().month
                    Text(
                        if (w == 0 || fresh.isNotEmpty()) "${month}月" else "",
                        fontSize = 9.sp,
                        color = softMuted(),
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                rowLabels.forEach { lab ->
                    Text(
                        lab,
                        fontSize = 9.sp,
                        color = softMuted(),
                        modifier = Modifier.width(14.dp).aspectRatio(1f)
                    )
                }
            }
            Spacer(Modifier.width(4.dp))
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                cells.chunked(7).forEach { week ->
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        week.forEach { cell ->
                            val lvl = min(cell.count, 4)
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(heatColor(lvl))
                                    .then(
                                        if (cell.isToday) Modifier.drawWithContent {
                                            drawContent()
                                            drawRoundRect(
                                                Tokens.BrandPrimary,
                                                cornerRadius = CornerRadius(3.dp.toPx()),
                                                style = Stroke(1.4.dp.toPx())
                                            )
                                        } else Modifier
                                    )
                            )
                        }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(zh("本周花园 · 打卡越多格子越深"), fontSize = 11.sp, color = softMuted(), modifier = Modifier.weight(1f))
            Text("少", fontSize = 9.sp, color = softMuted())
            Spacer(Modifier.width(3.dp))
            (0..4).forEach { i ->
                Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(heatColor(i)))
                Spacer(Modifier.width(3.dp))
            }
            Text("多", fontSize = 9.sp, color = softMuted())
        }
    }
}

private data class HeatCell(val count: Int, val isToday: Boolean, val month: Int)

@Composable
private fun rememberHeatStart(weeks: Int): Long {
    val c = Calendar.getInstance()
    return c.timeInMillis
}

private fun heatCells(counts: Map<String, Int>, weeks: Int): List<HeatCell> {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
    val today = cal.timeInMillis
    val weekday = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    cal.add(Calendar.DAY_OF_MONTH, -weekday)
    cal.add(Calendar.DAY_OF_MONTH, -(weeks - 1) * 7)
    val df = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    val out = ArrayList<HeatCell>(weeks * 7)
    repeat(weeks * 7) {
        val key = df.format(cal.time)
        out.add(
            HeatCell(
                count = counts[key] ?: 0,
                isToday = cal.timeInMillis == today,
                month = cal.get(Calendar.MONTH) + 1
            )
        )
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    return out
}

data class SoftTabSpec(val route: String, val title: String, val icon: ImageVector)

@Composable
fun SoftTabDock(
    currentRoute: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        SoftTabSpec("home", zh("首页"), Icons.Filled.Home),
        SoftTabSpec("calendar", zh("日历"), Icons.Filled.CalendarMonth),
        SoftTabSpec("stats", zh("统计"), Icons.Filled.BarChart),
        SoftTabSpec("settings", zh("设置"), Icons.Filled.Settings)
    )
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .softCard(SoftKind.Elevated)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(item.route) }
                    .padding(vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (selected) Tokens.BrandPrimary else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        item.icon,
                        contentDescription = item.title,
                        tint = if (selected) Tokens.OnStrong else softMuted(),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    item.title,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (selected) Tokens.BrandPrimary else softMuted()
                )
            }
        }
    }
}

@Composable
fun SoftFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val dark = isSoftDark()
    Box(
        modifier = modifier
            .padding(end = 16.dp, bottom = 8.dp)
            .size(Tokens.BtnCircle)
            .drawBehind {
                drawCircle(
                    Color.Black.copy(alpha = if (dark) 0.45f else 0.12f),
                    radius = size.minDimension / 2f,
                    center = center + Offset(0f, 6.dp.toPx())
                )
            }
            .clip(CircleShape)
            .background(Tokens.BrandPrimary)
            .drawWithContent {
                drawContent()
                drawCircle(
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.45f), Color.Transparent)
                    )
                )
                if (pressed) drawCircle(Color.Black.copy(alpha = 0.18f))
            }
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .semantics { contentDescription = zh("新建提醒") },
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = Tokens.OnStrong, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun SoftScreenTitle(
    title: String,
    modifier: Modifier = Modifier,
    leading: @Composable RowScope.() -> Unit = {},
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .heightIn(min = 52.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            title,
            fontSize = Tokens.TitleSize,
            lineHeight = Tokens.TitleLine,
            fontWeight = FontWeight.SemiBold,
            color = softText(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leading()
            Spacer(Modifier.weight(1f))
            trailing()
        }
    }
}
