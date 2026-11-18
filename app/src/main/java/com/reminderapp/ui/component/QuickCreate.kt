package com.reminderapp.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zh
import com.reminderapp.service.NaturalDateParser
import com.reminderapp.ui.comic.ComicHairline
import com.reminderapp.ui.comic.ComicTextAction
import com.reminderapp.ui.comic.comicColors
import java.util.Calendar

/**
 * Clean single-hairline natural-language quick input strip sitting bare on canvas.
 * Preserves `testTag("quick-reminder-input")` and `编辑规则并创建` / `AI / 语音` flows.
 */
@Composable
fun QuickCreate(
    onEdit: (ReminderEntity) -> Unit,
    onAI: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = comicColors()
    var text by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    fun submitQuick() {
        val unsupported = Regex(
            "最后|工作日|第[一二三四五1-5].*周|每隔|每[0-9]+天|last|weekday",
            RegexOption.IGNORE_CASE
        ).containsMatchIn(text)
        val p = if (unsupported) null else NaturalDateParser.parse(text)
        if (p == null) {
            error = true
        } else {
            val clock = Calendar.getInstance().apply { timeInMillis = p.nextTriggerAt }
            onEdit(
                ReminderEntity(
                    title = p.title.ifBlank { text },
                    kind = if (p.dateType != null) "date" else "cycle",
                    cycle = if (p.repeatMode == "lunar") "yearly" else p.repeatMode,
                    dateType = p.dateType,
                    targetMonth = p.targetMonth,
                    targetDay = p.targetDay,
                    weeklyWeekday = p.weekday,
                    holidayAware = p.holidayAware,
                    reminderHour = clock.get(Calendar.HOUR_OF_DAY),
                    reminderMinute = clock.get(Calendar.MINUTE),
                    firstTriggerAt = p.nextTriggerAt,
                    nextTriggerAt = p.nextTriggerAt
                )
            )
            text = ""
            error = false
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("✎", color = colors.accent, fontSize = 13.sp)
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f)) {
                if (text.isEmpty()) {
                    Text(
                        text = zh("一句话创建：每周一9点晨会"),
                        color = colors.muted.copy(alpha = 0.82f),
                        fontSize = 13.5.sp,
                        maxLines = 1
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        error = false
                    },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        color = colors.ink,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick-reminder-input")
                )
            }
            Spacer(Modifier.width(8.dp))
            if (text.isNotBlank()) {
                ComicTextAction(
                    text = zh("编辑规则并创建"),
                    onClick = ::submitQuick,
                    accent = true,
                    fontSize = 12.5.sp
                )
            } else {
                ComicTextAction(
                    text = zh("AI / 语音"),
                    onClick = onAI,
                    accent = false,
                    fontSize = 12.5.sp
                )
            }
        }
        ComicHairline(isInner = false)
        if (error) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = zh("暂未识别时间，请使用手动创建或 AI 助手"),
                    color = colors.danger,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                ComicTextAction(text = zh("AI / 语音"), onClick = onAI, accent = true, fontSize = 12.sp)
            }
        }
    }
}
