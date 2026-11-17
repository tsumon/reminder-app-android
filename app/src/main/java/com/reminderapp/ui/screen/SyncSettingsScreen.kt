package com.reminderapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.service.BackupService
import com.reminderapp.service.ReminderActions
import com.reminderapp.service.SyncStore
import com.reminderapp.service.WebDavSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncSettingsScreen(
    onBack: () -> Unit,
    onSyncResult: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var url by remember { mutableStateOf(SyncStore.url) }
    var username by remember { mutableStateOf(SyncStore.username) }
    var password by remember { mutableStateOf(SyncStore.password) }
    var autoSync by remember { mutableStateOf(SyncStore.autoSync) }
    val globalConflict by WebDavSync.pendingConflict.collectAsState()
    var conflict by remember { mutableStateOf<WebDavSync.SyncResult.Conflict?>(null) }
    val activeConflict = conflict ?: globalConflict
    var syncing by remember { mutableStateOf(false) }
    var testing by remember { mutableStateOf(false) }
    var resultMsg by remember { mutableStateOf("") }
    var resultIsError by remember { mutableStateOf(false) }
    var snapshotRefresh by remember { mutableIntStateOf(0) }
    var pendingRestoreSnapshot by remember { mutableStateOf<File?>(null) }
    val snapshots = remember(snapshotRefresh, activeConflict) {
        File(context.filesDir, "webdav_snapshots")
            .listFiles()
            ?.filter { it.isFile && it.name.endsWith(".json") }
            ?.sortedByDescending { it.lastModified() }
            .orEmpty()
    }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(zh("同步设置")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = zh("返回"))
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
                zh("通过 WebDAV 同步提醒数据。双端都有修改时请明确选择。选择前自动保存本地快照；远端缺少的本地提醒继续保留。旧协议无法同步删除。"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text(zh("WebDAV 地址")) },
                placeholder = { Text("https://example.com/dav/reminder") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(zh("用户名")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(zh("密码")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(zh("启动时自动同步"), style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = autoSync,
                    onCheckedChange = { autoSync = it }
                )
            }

            if (SyncStore.lastSyncAt > 0) {
                Text(
                    zhf("上次同步：%s", dateFormat.format(Date(SyncStore.lastSyncAt))),
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
                                resultMsg = zh("连接成功 ✓ 账号与路径可用，可以开始同步。")
                                resultIsError = false
                            }
                            is WebDavSync.SyncResult.Conflict -> { conflict=result;resultMsg=zh("双端都有修改，同步已停止，请选择保留本地或远程。") }
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
                Text(if (testing) zh("测试中...") else zh("测试连接"))
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
                                // v2.0.16: 双端都改过 → 提示已按版本覆盖
                                resultMsg = if (result.conflict) zh("已用最新版本覆盖（检测到双端都有修改，未合并）") else zh("同步完成")
                                resultIsError = false
                            }
                            is WebDavSync.SyncResult.Conflict -> { conflict=result;resultMsg=zh("双端都有修改，同步已停止，请选择保留本地或远程。") }
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
                Text(if (syncing) zh("同步中...") else zh("立即同步"))
            }

            activeConflict?.let { pending ->
                Text(zh("同步冲突：选择后会先保存本地快照。远程选择合并同 ID 内容，本地独有提醒仍保留。"))
                listOf(true to "保留本地并上传", false to "使用远程并合并").forEach { (useLocal,label) ->
                    OutlinedButton(enabled=!syncing,onClick={ scope.launch {
                        syncing=true
                        when(val resolved=WebDavSync.syncNow(ReminderApp_instance(),WebDavSync.Resolution(pending,useLocal))) {
                            is WebDavSync.SyncResult.Success -> {
                                conflict = null
                                WebDavSync.pendingConflict.value = null
                                resultMsg = zh("同步完成，本地快照已保留")
                                resultIsError = false
                                snapshotRefresh++
                            }
                            is WebDavSync.SyncResult.Error -> {
                                conflict = null
                                WebDavSync.pendingConflict.value = null
                                resultMsg = resolved.message
                                resultIsError = true
                            }
                            is WebDavSync.SyncResult.Conflict -> {
                                conflict = resolved
                            }
                        }
                        syncing=false
                    } }) { Text(zh(label)) }
                }
                TextButton(onClick={
                    conflict = null
                    WebDavSync.pendingConflict.value = null
                }) { Text(zh("取消，保留双方数据")) }
            }
            if (resultMsg.isNotEmpty()) {
                Text(
                    resultMsg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (resultIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider()
            Text(zh("同步前本地快照"), style = MaterialTheme.typography.titleSmall)
            if (snapshots.isEmpty()) {
                Text(
                    zh("发生同步冲突覆盖前会自动保存快照到这里"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                snapshots.forEach { file ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(file.name, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                dateFormat.format(Date(file.lastModified())),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            enabled = !syncing,
                            onClick = { pendingRestoreSnapshot = file }
                        ) { Text(zh("恢复")) }
                    }
                }
            }
        }
    }

    pendingRestoreSnapshot?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingRestoreSnapshot = null },
            title = { Text(zh("恢复同步快照")) },
            text = { Text(target.name) },
            confirmButton = {
                TextButton(onClick = {
                    val file = target
                    pendingRestoreSnapshot = null
                    scope.launch {
                        val json = withContext(Dispatchers.IO) { runCatching { file.readText() }.getOrNull() }
                        val parsed = json?.let { BackupService.importFromJson(it) }
                        if (parsed == null) {
                            resultMsg = zh("数据解析失败")
                            resultIsError = true
                            return@launch
                        }
                        val imported = runCatching {
                            ReminderActions(context, AppDatabase.getInstance(context)).importItems(parsed, json)
                        }.getOrNull()
                        if (imported == null) {
                            resultMsg = zh("恢复失败")
                            resultIsError = true
                        } else {
                            resultMsg = zhf("导入完成：新增 %1\$s 条，跳过重复 %2\$s 条", imported.saved.size, imported.skipped)
                            resultIsError = false
                        }
                    }
                }) { Text(zh("恢复")) }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestoreSnapshot = null }) { Text(zh("取消")) }
            }
        )
    }
}

private fun ReminderApp_instance(): android.content.Context = com.reminderapp.ReminderApp.instance
