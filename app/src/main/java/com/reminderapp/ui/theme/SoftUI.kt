package com.reminderapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.i18n.zh
import com.reminderapp.ui.comic.ComicHairline
import com.reminderapp.ui.comic.comicColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.min

/**
 * Clean borderless search strip with a single bottom rule on canvas.
 */
@Composable
fun ComicSearchStrip(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = zh("搜索提醒、备注...")
) {
    val colors = comicColors()
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = colors.muted,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        color = colors.muted.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        color = colors.ink,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (value.isNotEmpty()) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = zh("清除"),
                    tint = colors.muted,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onValueChange("") }
                )
            }
        }
        ComicHairline(isInner = false)
    }
}

/**
 * Unbordered warm rounded dot-matrix 16-week heatmap (inside a ComicCard).
 */
@Composable
fun CheckInHeatmap(
    counts: Map<String, Int>,
    weeks: Int = 16,
    modifier: Modifier = Modifier
) {
    val colors = comicColors()
    val cells = remember(counts, weeks) { heatCells(counts, weeks) }
    val rowLabels = listOf("一", "", "三", "", "五", "", "日")

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                zh("完成记录"),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.ink
            )
            Spacer(Modifier.weight(1f))
            Text(
                "${weeks} " + zh("周"),
                fontSize = 12.sp,
                color = colors.muted,
                style = TextStyle(fontFeatureSettings = "tnum")
            )
        }
        Row {
            Spacer(Modifier.width(16.dp))
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.5.dp)) {
                cells.chunked(7).forEachIndexed { w, week ->
                    val weekMonths = week.map { it.month }.toSet()
                    val seen = if (w == 0) emptySet() else cells.take(w * 7).map { it.month }.toSet()
                    val fresh = weekMonths - seen
                    val month = fresh.minOrNull() ?: week.first().month
                    Text(
                        if (w == 0 || fresh.isNotEmpty()) "${month}月" else "",
                        fontSize = 9.sp,
                        color = colors.muted,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(3.5.dp)) {
                rowLabels.forEach { lab ->
                    Text(
                        lab,
                        fontSize = 9.sp,
                        color = colors.muted,
                        modifier = Modifier.width(14.dp).aspectRatio(1f)
                    )
                }
            }
            Spacer(Modifier.width(4.dp))
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.5.dp)) {
                cells.chunked(7).forEach { week ->
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.5.dp)
                    ) {
                        week.forEach { cell ->
                            val lvl = min(cell.count, 4)
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(3.5.dp))
                                    .background(heatColor(lvl))
                                    .then(
                                        if (cell.isToday) {
                                            Modifier.drawWithContent {
                                                drawContent()
                                                drawRoundRect(
                                                    colors.ink,
                                                    cornerRadius = CornerRadius(3.5.dp.toPx()),
                                                    style = Stroke(1.2.dp.toPx())
                                                )
                                            }
                                        } else Modifier
                                    )
                            )
                        }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                zh("每次确认，都有记录"),
                fontSize = 11.5.sp,
                color = colors.muted,
                modifier = Modifier.weight(1f)
            )
            Text(zh("少"), fontSize = 10.sp, color = colors.muted)
            Spacer(Modifier.width(4.dp))
            (0..4).forEach { i ->
                Box(
                    Modifier
                        .size(9.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(heatColor(i))
                )
                Spacer(Modifier.width(3.dp))
            }
            Text(zh("多"), fontSize = 10.sp, color = colors.muted)
        }
    }
}

private data class HeatCell(val count: Int, val isToday: Boolean, val month: Int)

private fun heatCells(counts: Map<String, Int>, weeks: Int): List<HeatCell> {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val today = cal.timeInMillis
    val weekday = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    cal.add(Calendar.DAY_OF_MONTH, -weekday)
    cal.add(Calendar.DAY_OF_MONTH, -(weeks - 1) * 7)
    val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
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
