package com.reminderapp.ui.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.service.BackupService
import com.reminderapp.service.ReminderActions
import java.util.Calendar
import kotlinx.coroutines.launch

@Composable
fun BatchTimeDialog(
    selectedCount: Int,
    onApply: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var batchHour by remember { mutableIntStateOf(9) }
    var batchMinute by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(zhf("批量修改 %d 条提醒的时间", selectedCount)) },
        text = {
            Column {
                listOf(6, 7, 8, 9, 10, 12, 14, 16, 18, 20, 22).forEach { h ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(zhf("%d 点", h), Modifier.weight(1f))
                        RadioButton(selected = batchHour == h, onClick = { batchHour = h })
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(zh("分钟"), Modifier.weight(1f))
                    listOf(0, 15, 30, 45).forEach { m ->
                        TextButton(onClick = { batchMinute = m }) {
                            Text(
                                zhf("%02d", m),
                                color = if (batchMinute == m) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(batchHour, batchMinute) }) { Text(zh("应用")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(zh("取消")) }
        }
    )
}

@Composable
fun AnchorMismatchDialog(
    mismatches: List<ReminderEntity>,
    onFix: () -> Unit,
    onDismiss: () -> Unit
) {
    if (mismatches.isEmpty()) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(zh("提醒日错位")) },
        text = {
            Column {
                Text(zh("以下每周提醒的触发日与设定不符："))
                Spacer(modifier = Modifier.height(8.dp))
                val wd = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
                mismatches.take(5).forEach { r ->
                    val actualCal = Calendar.getInstance().apply { timeInMillis = r.firstTriggerAt }
                    val actualDow = ((actualCal.get(Calendar.DAY_OF_WEEK) + 5) % 7) + 1
                    Text(
                        zhf(
                            "· %1\$s：设定%2\$s，实际%3\$s",
                            r.title,
                            wd[((r.weeklyWeekday ?: 1) - 1).coerceIn(0, 6)],
                            wd[(actualDow - 1).coerceIn(0, 6)]
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onFix) { Text(zh("修正为设定星期")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(zh("忽略")) }
        }
    )
}

@Composable
fun HomeDeleteDialog(
    reminder: ReminderEntity?,
    onConfirm: (ReminderEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val target = reminder ?: return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(zh("删除提醒")) },
        text = { Text(zhf("确定要删除「%s」吗？此操作不可恢复。", target.title)) },
        confirmButton = {
            TextButton(onClick = { onConfirm(target) }) {
                Text(zh("删除"), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(zh("取消")) }
        }
    )
}

@Composable
fun ShareCardImportDialog(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (!visible) return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var cardImportText by remember { mutableStateOf("") }
    var cardImportMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(zh("导入分享卡片")) },
        text = {
            Column {
                Text(zh("把聊天里收到的提醒卡片 JSON 粘贴到下面，即可导入这条提醒："))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = cardImportText,
                    onValueChange = { cardImportText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    placeholder = { Text("{ \"version\": 1, \"reminders\": [ ... ] }") },
                    singleLine = false,
                    isError = cardImportMsg != null
                )
                cardImportMsg?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val json = cardImportText.trim()
                if (json.isEmpty()) {
                    cardImportMsg = zh("请先粘贴分享卡片内容")
                    return@TextButton
                }
                val entity = BackupService.importSingle(json)
                if (entity == null) {
                    cardImportMsg = zh("不是有效的分享卡片内容")
                    return@TextButton
                }
                try {
                    val imported = entity.copy(id = 0)
                    scope.launch {
                        ReminderActions(context).importItems(listOf(imported))
                        cardImportText = ""
                        cardImportMsg = null
                        onDismiss()
                    }
                } catch (e: Exception) {
                    cardImportMsg = zhf("导入失败：%s", e.message ?: zh("未知错误"))
                }
            }) {
                Text(zh("导入"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(zh("取消")) }
        }
    )
}
