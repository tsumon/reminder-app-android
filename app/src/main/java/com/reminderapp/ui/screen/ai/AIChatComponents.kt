package com.reminderapp.ui.screen.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zh
import com.reminderapp.ui.comic.ComicTextAction
import com.reminderapp.ui.comic.comicColors
import com.reminderapp.ui.comic.comicPanel
import com.reminderapp.ui.screen.ChatMessage
import com.reminderapp.ui.viewmodel.describeAIReminder

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComicChatPanel(
    msg: ChatMessage,
    onSelectOption: (String) -> Unit
) {
    val colors = comicColors()
    val isUser = msg.role == ChatMessage.Role.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (msg.toolSteps.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(horizontal = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                msg.toolSteps.forEach { step ->
                    val statusTag = when (step.status) {
                        "running" -> "…"
                        "error" -> "!"
                        else -> "✓"
                    }
                    Text(
                        text = "› [TOOL] ${step.name} $statusTag",
                        color = if (step.status == "error") colors.danger else colors.muted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (msg.content.isNotBlank()) {
            Box(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .comicPanel(
                        cut = if (isUser) 7.dp else 10.dp,
                        fill = if (isUser) colors.accent else colors.surface,
                        shadowOffset = 2.0.dp
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = msg.content,
                    color = if (isUser) colors.onAccent else colors.ink,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = if (isUser) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        if (!msg.options.isNullOrEmpty()) {
            Spacer(Modifier.height(2.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                msg.options.forEach { opt ->
                    ComicTextAction(
                        text = "[$opt]",
                        onClick = { onSelectOption(opt) },
                        accent = true,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BatchImportConfirmDialog(
    items: List<ReminderEntity>?,
    onConfirm: (List<ReminderEntity>) -> Unit,
    onDismiss: () -> Unit
) {
    val pending = items ?: return
    val colors = comicColors()
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(pending) }) { Text(zh("确认创建")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(zh("取消")) }
        },
        title = { Text(zh("批量创建提醒")) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                items(count = pending.size) { i ->
                    val e = pending[i]
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(e.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(describeAIReminder(e), style = MaterialTheme.typography.labelSmall, color = colors.muted)
                    }
                }
            }
        }
    )
}
