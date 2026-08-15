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
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    var refreshKey by remember { mutableStateOf(0) }
    var total by remember { mutableStateOf(0) }
    var unconfirmed by remember { mutableStateOf(0) }
    var disabled by remember { mutableStateOf(0) }
    var critical by remember { mutableStateOf(0) }
    var upcoming by remember { mutableStateOf<List<com.reminderapp.data.entity.ReminderEntity>>(emptyList()) }

    LaunchedEffect(refreshKey) {
        val all = db.reminderDao().getAllSyncBlocking()
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
