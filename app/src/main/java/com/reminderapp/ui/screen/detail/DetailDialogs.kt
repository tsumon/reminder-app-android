package com.reminderapp.ui.screen.detail

import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf

@Composable
fun DetailDialogs(
    reminderTitle: String,
    showDeleteDialog: Boolean,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    showFullScreenPermissionDialog: Boolean,
    onDismissPermission: () -> Unit,
    showCustomSnoozeDialog: Boolean,
    onConfirmCustomSnooze: (Int) -> Unit,
    onDismissCustomSnooze: () -> Unit,
    showQuietStartPicker: Boolean,
    quietStartMinute: Int,
    onConfirmQuietStart: (Int) -> Unit,
    onDismissQuietStart: () -> Unit,
    showQuietEndPicker: Boolean,
    quietEndMinute: Int,
    onConfirmQuietEnd: (Int) -> Unit,
    onDismissQuietEnd: () -> Unit
) {
    val context = LocalContext.current

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text(zh("删除提醒")) },
            text = { Text(zhf("确定要删除「%s」吗？此操作不可恢复。", reminderTitle)) },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text(zh("删除"), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) { Text(zh("取消")) }
            }
        )
    }

    if (showCustomSnoozeDialog) {
        var minutes by remember { mutableStateOf("15") }
        AlertDialog(
            onDismissRequest = onDismissCustomSnooze,
            title = { Text(zh("自定义稍后")) },
            text = {
                OutlinedTextField(
                    value = minutes,
                    onValueChange = { minutes = it.filter(Char::isDigit).take(4) },
                    label = { Text(zh("分钟数")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = { onConfirmCustomSnooze(minutes.toIntOrNull()?.coerceAtLeast(1) ?: 15) }) {
                    Text(zh("确定"))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissCustomSnooze) { Text(zh("取消")) }
            }
        )
    }

    if (showQuietStartPicker) {
        QuietMinuteDialog(
            initialMinute = quietStartMinute,
            title = zh("勿扰开始"),
            onConfirm = onConfirmQuietStart,
            onDismiss = onDismissQuietStart
        )
    }
    if (showQuietEndPicker) {
        QuietMinuteDialog(
            initialMinute = quietEndMinute,
            title = zh("勿扰结束"),
            onConfirm = onConfirmQuietEnd,
            onDismiss = onDismissQuietEnd
        )
    }

    if (showFullScreenPermissionDialog) {
        val nm = context.getSystemService(NotificationManager::class.java)
        val needFullScreen = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            (nm == null || !nm.canUseFullScreenIntent())
        val needPolicy = nm?.isNotificationPolicyAccessGranted != true
        AlertDialog(
            onDismissRequest = onDismissPermission,
            title = { Text(zh("开启关键提醒所需权限")) },
            text = {
                Text(
                    zh(
                        "关键提醒需要以下权限才能正常弹出并穿透勿扰：" +
                            (if (needFullScreen) "\n· 全屏界面（Android 14+ 默认不授予，需手动开启）" else "") +
                            (if (needPolicy) "\n· 勿扰访问（允许关键提醒在勿扰模式下响铃）" else "")
                    )
                )
            },
            confirmButton = {
                if (needFullScreen) {
                    TextButton(onClick = {
                        onDismissPermission()
                        val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }) { Text(zh("去设置（全屏）")) }
                } else if (needPolicy) {
                    TextButton(onClick = {
                        onDismissPermission()
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                    }) { Text(zh("去设置（勿扰）")) }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissPermission) { Text(zh("稍后")) }
            }
        )
    }
}

@Composable
private fun QuietMinuteDialog(
    initialMinute: Int,
    title: String,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var hour by remember { mutableIntStateOf(initialMinute / 60) }
    var minute by remember { mutableIntStateOf(initialMinute % 60) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = hour.toString(),
                    onValueChange = { hour = it.filter(Char::isDigit).take(2).toIntOrNull() ?: 0 },
                    modifier = Modifier.width(76.dp),
                    label = { Text(zh("时")) },
                    singleLine = true
                )
                Text(" : ", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = minute.toString(),
                    onValueChange = { minute = it.filter(Char::isDigit).take(2).toIntOrNull() ?: 0 },
                    modifier = Modifier.width(76.dp),
                    label = { Text(zh("分")) },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(hour.coerceIn(0, 23) * 60 + minute.coerceIn(0, 59)) }) {
                Text(zh("确定"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(zh("取消")) }
        }
    )
}
