package com.reminderapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reminderapp.service.SyncStore
import com.reminderapp.service.WebDavSync
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncSettingsScreen(
    onBack: () -> Unit,
    onSyncResult: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var url by remember { mutableStateOf(SyncStore.url) }
    var username by remember { mutableStateOf(SyncStore.username) }
    var password by remember { mutableStateOf(SyncStore.password) }
    var autoSync by remember { mutableStateOf(SyncStore.autoSync) }
    var syncing by remember { mutableStateOf(false) }
    var testing by remember { mutableStateOf(false) }
    var resultMsg by remember { mutableStateOf("") }
    var resultIsError by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("同步设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "通过 WebDAV 同步提醒数据（支持甲骨文 VPS 自建、坚果云等）。同步以最后修改时间为准，较新的覆盖较旧的。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("WebDAV 地址") },
                placeholder = { Text("https://example.com/dav/reminder") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("用户名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("启动时自动同步", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = autoSync,
                    onCheckedChange = { autoSync = it }
                )
            }

            if (SyncStore.lastSyncAt > 0) {
                Text(
                    "上次同步：${dateFormat.format(Date(SyncStore.lastSyncAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // v1.9.1: 测试连接（先验证账号/路径，坚果云友好提示）
            Button(
                onClick = {
                    SyncStore.url = url
                    SyncStore.username = username
                    SyncStore.password = password
                    SyncStore.autoSync = autoSync
                    testing = true
                    resultMsg = ""
                    scope.launch {
                        val result = WebDavSync.testConnection()
                        testing = false
                        when (result) {
                            is WebDavSync.SyncResult.Success -> {
                                resultMsg = "连接成功 ✓ 账号与路径可用，可以开始同步。"
                                resultIsError = false
                            }
                            is WebDavSync.SyncResult.Error -> {
                                resultMsg = result.message
                                resultIsError = true
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !testing && !syncing && url.isNotBlank() && username.isNotBlank() && password.isNotBlank()
            ) {
                Text(if (testing) "测试中..." else "测试连接")
            }

            Button(
                onClick = {
                    SyncStore.url = url
                    SyncStore.username = username
                    SyncStore.password = password
                    SyncStore.autoSync = autoSync
                    syncing = true
                    resultMsg = ""
                    scope.launch {
                        val result = WebDavSync.syncNow(ReminderApp_instance())
                        syncing = false
                        when (result) {
                            is WebDavSync.SyncResult.Success -> {
                                resultMsg = "同步完成"
                                resultIsError = false
                            }
                            is WebDavSync.SyncResult.Error -> {
                                resultMsg = result.message
                                resultIsError = true
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !syncing && url.isNotBlank() && username.isNotBlank()
            ) {
                Text(if (syncing) "同步中..." else "立即同步")
            }

            if (resultMsg.isNotEmpty()) {
                Text(
                    resultMsg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (resultIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun ReminderApp_instance(): android.content.Context = com.reminderapp.ReminderApp.instance
