package com.reminderapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.reminderapp.ReminderApp
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.ui.theme.Tokens
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * v2.1.1: 提醒可靠性诊断页——自签环境权限/排期状态经常异常，一键查看问题在哪。
 * 显示：通知权限、数据规模、WebDAV 配置、最近触发时间。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val scope=rememberCoroutineScope()
    var testMessage by remember { mutableStateOf("") }
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    var refreshKey by remember { mutableStateOf(0) }
    var total by remember { mutableStateOf(0) }
    var unconfirmed by remember { mutableStateOf(0) }
    var disabled by remember { mutableStateOf(0) }
    var critical by remember { mutableStateOf(0) }
    var upcoming by remember { mutableStateOf<List<com.reminderapp.data.entity.ReminderEntity>>(emptyList()) }

    LaunchedEffect(refreshKey) {
        val all = db.reminderDao().getAllSync()
        total = all.size
        unconfirmed = all.count { it.status != "confirmed" }
        disabled = all.count { !it.isActive }
        critical = all.count { it.isCritical }
        upcoming = all
            .filter { it.isActive && it.status != "confirmed" }
            .sortedBy { it.nextTriggerAt }
            .take(5)
    }

    val notificationsEnabled = androidx.core.app.NotificationManagerCompat.from(context)
        .areNotificationsEnabled()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(zh("提醒诊断")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = zh("返回"))
                    }
                },
                actions = {
                    IconButton(onClick = { refreshKey++ }) {
                        Icon(Icons.Filled.Refresh, contentDescription = zh("重新检查"))
                    }
                }
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
            item {
                SettingsCard {
                    val exact=com.reminderapp.service.ReminderScheduler(context).canScheduleExact()
                    Text(zh("提醒能力自检"),style=MaterialTheme.typography.titleMedium)
                    Text(if(exact) zh("精确闹钟已允许") else zh("精确闹钟未允许：目前使用非精确排期，可能延迟"))
                    Text(zh("普通通知受系统权限、通知渠道、静音及勿扰影响。重要提醒也需要相应系统授权。"),style=MaterialTheme.typography.bodySmall)
                    TextButton(onClick={
                        val intent=if(android.os.Build.VERSION.SDK_INT>=31) android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,android.net.Uri.parse("package:${context.packageName}")) else android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:${context.packageName}"))
                        context.startActivity(intent)
                    }) { Text(zh("设置精确闹钟权限")) }
                    TextButton(onClick={ context.startActivity(android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,context.packageName)) }) { Text(zh("设置通知权限与渠道")) }
                    Button(onClick={scope.launch {
                        val at=System.currentTimeMillis()+60_000
                        val r=com.reminderapp.data.entity.ReminderEntity(title=zh("提醒自检（1 分钟）"),cycle="once",firstTriggerAt=at,nextTriggerAt=at,reminderStyle="gentle")
                        val id=db.reminderDao().insert(r)
                        com.reminderapp.service.ReminderScheduler(context).schedule(r.copy(id=id))
                        com.reminderapp.service.SyncStore.touchLocalChange()
                        testMessage=zh("测试提醒已安排，请锁屏等待约 1 分钟；可在首页完成或删除。")
                    }}) { Text(zh("创建测试提醒")) }
                    if(testMessage.isNotEmpty()) Text(testMessage)
                }
            }
            item {
                SettingsCard {
                    Text(zh("隐私与数据"),style=MaterialTheme.typography.titleMedium)
                    Text(zh("提醒与完成记录保存在本机。使用 AI 时，对话及相关提醒内容会发往你配置的模型服务地址。启用 WebDAV 后，提醒备份上传至你配置的服务器。本地备份也可能出现在下载目录，请妥善保管。"))
                    Text(zh("同步冲突会停止并等待你选择。WebDAV 快照保存在应用私有目录；同步不会传播删除。"))
                }
            }
            item {
                SettingsCard {
                    Text(zh("通知"), style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    DiagRow(
                        label = zh("通知权限"),
                        value = if (notificationsEnabled) zh("已开启") else zh("已关闭"),
                        ok = notificationsEnabled
                    )
                    DiagRow(
                        label = zh("关键提醒"),
                        value = zhf("%s 条", critical),
                        ok = true
                    )
                }
            }

            item {
                SettingsCard {
                    Text(zh("数据"), style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    DiagRow(label = zh("提醒总数"), value = zhf("%s 条", total), ok = true)
                    DiagRow(label = zh("未完成"), value = zhf("%s 条", unconfirmed), ok = true)
                    DiagRow(label = zh("已停用"), value = zhf("%s 条", disabled), ok = true)
                }
            }

            item {
                SettingsCard {
                    Text(zh("同步"), style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    DiagRow(
                        label = zh("WebDAV"),
                        value = if (com.reminderapp.service.SyncStore.isConfigured) zh("已配置") else zh("未配置"),
                        ok = com.reminderapp.service.SyncStore.isConfigured
                    )
                }
            }

            item {
                SettingsCard {
                    Text(zh("调试：模拟通知动作"), style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    val target = upcoming.firstOrNull()
                    if (target == null) {
                        Text(zh("没有可模拟的未完成提醒"), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text(
                            zh("走与通知按钮相同的 Broadcast → ReminderEngine.confirm / snooze。真机通知仍需在系统通知里点。"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val intent = android.content.Intent(context, com.reminderapp.receiver.NotificationActionReceiver::class.java).apply {
                                    action = com.reminderapp.service.NotificationManager.ACTION_CONFIRM
                                    putExtra(com.reminderapp.service.NotificationManager.EXTRA_REMINDER_ID, target.id)
                                    putExtra("expected_at",target.nextTriggerAt);putExtra("revision",target.revision);putExtra("expected_state",target.toString())
                                }
                                context.sendBroadcast(intent)
                                android.widget.Toast.makeText(
                                    context,
                                    zhf("已模拟确认：%s", target.title),
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                refreshKey++
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(zhf("模拟确认：%s", target.title)) }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                val intent = android.content.Intent(context, com.reminderapp.receiver.NotificationActionReceiver::class.java).apply {
                                    action = com.reminderapp.service.NotificationManager.ACTION_SNOOZE
                                    putExtra(com.reminderapp.service.NotificationManager.EXTRA_REMINDER_ID, target.id)
                                    putExtra("expected_at",target.nextTriggerAt);putExtra("revision",target.revision);putExtra("expected_state",target.toString())
                                }
                                context.sendBroadcast(intent)
                                android.widget.Toast.makeText(
                                    context,
                                    zhf("已模拟稍后：%s", target.title),
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                refreshKey++
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(zhf("模拟稍后：%s", target.title)) }
                    }
                }
            }

            item {
                SettingsCard {
                    Text(zh("最近触发"), style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (upcoming.isEmpty()) {
                        Text(zh("暂无待触发的提醒"), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    upcoming.forEach { r ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(r.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                dateFormat.format(Date(r.nextTriggerAt)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // v2.2.0: 最近 AI 调用（可观测性）
            item {
                val aiLogs = com.reminderapp.service.AILogStore.recent(context).take(5)
                SettingsCard {
                    Text(zh("最近 AI 调用"), style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (aiLogs.isEmpty()) {
                        Text(zh("暂无 AI 调用记录"), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    aiLogs.forEach { e ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                e.timeText(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                e.model,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                if (e.ok) {
                                    zhf("%s 轮 · %sms%s", e.turns, e.durationMs,
                                        if (e.provider == "fallback") " · " + zh("降级") else "")
                                } else zh("失败"),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (e.ok) MaterialTheme.colorScheme.onSurfaceVariant else Tokens.StatusOverdue
                            )
                        }
                    }
                }
            }

            // v2.4.0: 最近崩溃日志（TelemetryService crash.log 尾部，设备特定闪退自查用）
            item {
                val crashLog = runCatching {
                    java.io.File(ReminderApp.instance.filesDir, "telemetry/crash.log")
                        .takeIf { it.exists() }
                        ?.readLines()?.takeLast(3) ?: emptyList()
                }.getOrDefault(emptyList())
                SettingsCard {
                    Text(zh("最近崩溃"), style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (crashLog.isEmpty()) {
                        Text(zh("无崩溃记录"), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    crashLog.forEach { line ->
                        Text(
                            line.take(200),
                            style = MaterialTheme.typography.bodySmall,
                            color = Tokens.StatusOverdue,
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            item {
                Text(
                    zhf("检查时间：%s", dateFormat.format(Date())),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DiagRow(label: String, value: String, ok: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (ok) MaterialTheme.colorScheme.onSurfaceVariant else Tokens.StatusOverdue
        )
    }
}
