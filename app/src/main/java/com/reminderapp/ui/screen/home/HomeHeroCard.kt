package com.reminderapp.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.ui.comic.ComicButton
import com.reminderapp.ui.comic.ComicCard
import com.reminderapp.ui.comic.ComicHairline
import com.reminderapp.ui.comic.ComicHeroNumber
import com.reminderapp.ui.comic.ComicTextAction
import com.reminderapp.ui.comic.comicColors
import com.reminderapp.ui.comic.cycleSummaryLabel
import com.reminderapp.ui.comic.formatShortDateTime
import com.reminderapp.ui.comic.formatTabularTime
import java.util.Locale

/**
 * Card 1 — Sculptural `#Count` Hero Card (1:1 homage to `#221` panel in IOTA MONITOR).
 * ZERO nested boxes inside: left `#02` ComicHeroNumber, right pure typographic metadata,
 * 1dp innerRule hairline, left quiet text actions, right SINGLE `确认完成` ComicButton.
 */
@Composable
fun HomeHeroCard(
    due: List<ReminderEntity>,
    nextUpcoming: ReminderEntity?,
    waitingCount: Int,
    dbEmpty: Boolean,
    onReminderClick: (Long) -> Unit,
    onConfirm: (ReminderEntity) -> Unit,
    onMakeUp: (ReminderEntity) -> Unit,
    onSnooze: (ReminderEntity) -> Unit,
    onSkip: (ReminderEntity) -> Unit,
    onCreateReminder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = comicColors()
    var focusIndex by remember(due.size) { mutableIntStateOf(0) }

    ComicCard(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (due.isNotEmpty()) {
            val safeIdx = focusIndex.coerceIn(0, due.lastIndex)
            val focus = due[safeIdx]
            val canSkip = !(focus.kind == "cycle" && focus.cycle == "once")
            val isOverdue = focus.status == "overdue"

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(zh("待确认提醒"), color = colors.muted, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        if (due.size > 1) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${safeIdx + 1}/${due.size} ›",
                                color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                style = TextStyle(fontFeatureSettings = "tnum"),
                                modifier = Modifier.clickable { focusIndex = (safeIdx + 1) % due.size }
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    ComicHeroNumber(text = String.format(Locale.US, "#%02d", due.size), fontSize = 48.sp)
                }
                Spacer(Modifier.width(14.dp))
                Column(
                    modifier = Modifier.weight(1f).clickable { onReminderClick(focus.id) },
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = focus.title, color = colors.ink, fontSize = 18.sp, lineHeight = 23.sp,
                        fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.End,
                        maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${formatTabularTime(focus.nextTriggerAt)} · ${cycleSummaryLabel(focus)}",
                        color = colors.muted, fontSize = 12.5.sp, textAlign = TextAlign.End,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                    val retryNote = when {
                        isOverdue -> zh("已达提醒次数上限，等待手动处理")
                        focus.retryCount > 0 -> zhf("第%s次重试", focus.retryCount) + " · " + formatTabularTime(focus.nextTriggerAt)
                        else -> zh("需要确认")
                    }
                    Text(
                        text = retryNote, color = if (isOverdue) colors.danger else colors.accent,
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFeatureSettings = "tnum")
                    )
                    focus.scheduledOccurrenceAt?.let { orig ->
                        Text(
                            text = zhf("原定 %s", formatShortDateTime(orig)),
                            color = colors.muted, fontSize = 11.5.sp, textAlign = TextAlign.End,
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )
                    }
                }
            }

            ComicHairline(isInner = true)

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ComicTextAction(text = zh("稍后") + " 15m", onClick = { onSnooze(focus) }, fontSize = 12.5.sp)
                    if (canSkip) ComicTextAction(text = zh("跳过本次"), onClick = { onSkip(focus) }, fontSize = 12.5.sp)
                    if (isOverdue) ComicTextAction(text = zh("补打今天"), onClick = { onMakeUp(focus) }, accent = true, fontSize = 12.5.sp)
                }
                ComicButton(
                    text = zh("确认完成"),
                    onClick = { onConfirm(focus) },
                    cut = 5.5.dp,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    fontSize = 13.5.sp,
                    modifier = Modifier.testTag("row-confirm")
                )
            }
        } else if (dbEmpty) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column {
                    Text(zh("当前状态"), color = colors.muted, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    ComicHeroNumber(text = "#00", fontSize = 46.sp)
                }
                Spacer(Modifier.width(14.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(zh("暂无提醒"), color = colors.ink, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text(zh("创建第一个循环提醒，准时记住每件小事"), color = colors.muted, fontSize = 12.5.sp, textAlign = TextAlign.End)
                }
            }
            ComicHairline(isInner = true)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(zh("每次确认，都有记录"), color = colors.muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                ComicButton(
                    text = "+ " + zh("创建提醒"),
                    onClick = onCreateReminder,
                    cut = 5.5.dp,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column {
                    Text(zh("今日待办已清空"), color = colors.muted, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    ComicHeroNumber(text = "#00", fontSize = 46.sp)
                }
                Spacer(Modifier.width(14.dp))
                Column(
                    modifier = Modifier.weight(1f).then(
                        if (nextUpcoming != null) Modifier.clickable { onReminderClick(nextUpcoming.id) } else Modifier
                    ),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = nextUpcoming?.title ?: zh("暂无即将到来的提醒"),
                        color = colors.ink, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.End, maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                    if (nextUpcoming != null) {
                        Text(
                            text = zhf("下次：%s", formatShortDateTime(nextUpcoming.nextTriggerAt)),
                            color = colors.muted, fontSize = 12.5.sp, style = TextStyle(fontFeatureSettings = "tnum")
                        )
                        Text(cycleSummaryLabel(nextUpcoming), color = colors.muted, fontSize = 12.sp)
                    }
                }
            }
            ComicHairline(isInner = true)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(zh("等待中") + " · $waitingCount", color = colors.muted, fontSize = 12.sp, style = TextStyle(fontFeatureSettings = "tnum"))
                Text(zh("排期正常"), color = colors.ok, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
