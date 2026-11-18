package com.reminderapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.ReminderApp
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.service.BackupService
import com.reminderapp.service.ReminderActions
import com.reminderapp.service.SyncStore
import com.reminderapp.service.WebDavSync
import com.reminderapp.ui.comic.ComicButton
import com.reminderapp.ui.comic.ComicCard
import com.reminderapp.ui.comic.ComicHairline
import com.reminderapp.ui.comic.ComicTextAction
import com.reminderapp.ui.comic.ComicTopBar
import com.reminderapp.ui.comic.comicColors
import com.reminderapp.ui.screen.settings.SyncConflictAndSnapshotsSection
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SyncSettingsScreen(
    onBack: () -> Unit,
    onSyncResult: (String) -> Unit
) {
    val context = LocalContext.current
    val colors = comicColors()
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
            .listFiles()?.filter { it.isFile && it.name.endsWith(".json") }
            ?.sortedByDescending { it.lastModified() }.orEmpty()
    }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().background(colors.canvas).navigationBarsPadding()) {
        ComicTopBar(title = zh("同步设置"), onBack = onBack)

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ComicCard(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    zh("通过 WebDAV 同步提醒数据。双端都有修改时请明确选择。选择前自动保存本地快照；远端缺少的本地提醒继续保留。旧协议无法同步删除。"),
                    color = colors.muted, fontSize = 12.sp
                )
                OutlinedTextField(
                    value = url, onValueChange = { url = it }, label = { Text(zh("WebDAV 地址")) },
                    placeholder = { Text("https://dav.jianguoyun.com/dav/") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = username, onValueChange = { username = it }, label = { Text(zh("用户名")) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = password, onValueChange = { password = it }, label = { Text(zh("密码")) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(zh("启动时自动同步"), color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Switch(checked = autoSync, onCheckedChange = { autoSync = it })
                }
                if (SyncStore.lastSyncAt > 0) {
                    Text(
                        zhf("上次同步：%s", dateFormat.format(Date(SyncStore.lastSyncAt))),
                        color = colors.muted, fontSize = 12.sp, style = TextStyle(fontFeatureSettings = "tnum")
                    )
                }
                ComicHairline(isInner = true)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ComicTextAction(
                        text = if (testing) zh("测试中...") else zh("测试连接"),
                        enabled = !testing && !syncing && url.isNotBlank() && username.isNotBlank() && password.isNotBlank(),
                        accent = true,
                        onClick = {
                            SyncStore.url = url; SyncStore.username = username; SyncStore.password = password; SyncStore.autoSync = autoSync
                            testing = true; resultMsg = ""
                            scope.launch {
                                val result = WebDavSync.testConnection()
                                testing = false
                                when (result) {
                                    is WebDavSync.SyncResult.Success -> { resultMsg = zh("连接成功 ✓ 账号与路径可用，可以开始同步。"); resultIsError = false }
                                    is WebDavSync.SyncResult.Conflict -> { conflict = result; resultMsg = zh("双端都有修改，同步已停止，请选择保留本地或远程。") }
                                    is WebDavSync.SyncResult.Error -> { resultMsg = result.message; resultIsError = true }
                                }
                            }
                        }
                    )
                    ComicButton(
                        text = if (syncing) zh("同步中...") else zh("立即同步"),
                        enabled = !syncing && url.isNotBlank() && username.isNotBlank(),
                        cut = 5.5.dp,
                        onClick = {
                            SyncStore.url = url; SyncStore.username = username; SyncStore.password = password; SyncStore.autoSync = autoSync
                            syncing = true; resultMsg = ""
                            scope.launch {
                                val result = WebDavSync.syncNow(ReminderApp.instance)
                                syncing = false
                                when (result) {
                                    is WebDavSync.SyncResult.Success -> {
                                        resultMsg = if (result.conflict) zh("已用最新版本覆盖（检测到双端都有修改，未合并）") else zh("同步完成")
                                        resultIsError = false; onSyncResult(resultMsg)
                                    }
                                    is WebDavSync.SyncResult.Conflict -> { conflict = result; resultMsg = zh("双端都有修改，同步已停止，请选择保留本地或远程。") }
                                    is WebDavSync.SyncResult.Error -> { resultMsg = result.message; resultIsError = true }
                                }
                            }
                        }
                    )
                }
                if (resultMsg.isNotEmpty()) {
                    Text(resultMsg, color = if (resultIsError) colors.danger else colors.ok, fontSize = 13.sp)
                }
            }

            SyncConflictAndSnapshotsSection(
                activeConflict = activeConflict,
                syncing = syncing,
                snapshots = snapshots,
                dateFormat = dateFormat,
                pendingRestoreSnapshot = pendingRestoreSnapshot,
                onResolveConflict = { useLocal ->
                    val pending = activeConflict ?: return@SyncConflictAndSnapshotsSection
                    scope.launch {
                        syncing = true
                        when (val resolved = WebDavSync.syncNow(ReminderApp.instance, WebDavSync.Resolution(pending, useLocal))) {
                            is WebDavSync.SyncResult.Success -> {
                                conflict = null; WebDavSync.pendingConflict.value = null
                                resultMsg = zh("同步完成，本地快照已保留"); resultIsError = false; snapshotRefresh++
                            }
                            is WebDavSync.SyncResult.Error -> {
                                conflict = null; WebDavSync.pendingConflict.value = null
                                resultMsg = resolved.message; resultIsError = true
                            }
                            is WebDavSync.SyncResult.Conflict -> conflict = resolved
                        }
                        syncing = false
                    }
                },
                onCancelConflict = { conflict = null; WebDavSync.pendingConflict.value = null },
                onRequestRestoreSnapshot = { pendingRestoreSnapshot = it },
                onConfirmRestoreSnapshot = { file ->
                    pendingRestoreSnapshot = null
                    scope.launch {
                        val json = withContext(Dispatchers.IO) { runCatching { file.readText() }.getOrNull() }
                        val parsed = json?.let { BackupService.importFromJson(it) }
                        if (parsed == null) { resultMsg = zh("数据解析失败"); resultIsError = true; return@launch }
                        val imported = runCatching { ReminderActions(context, AppDatabase.getInstance(context)).importItems(parsed, json) }.getOrNull()
                        if (imported == null) { resultMsg = zh("恢复失败"); resultIsError = true }
                        else { resultMsg = zhf("导入完成：新增 %1\$s 条，跳过重复 %2\$s 条", imported.saved.size, imported.skipped); resultIsError = false }
                    }
                }
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
