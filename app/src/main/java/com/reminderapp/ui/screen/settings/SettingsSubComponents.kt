package com.reminderapp.ui.screen.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.i18n.LocaleManager
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.service.BackupService
import com.reminderapp.service.NearbyShareService
import com.reminderapp.service.QrScanner
import com.reminderapp.service.ReminderActions
import com.reminderapp.service.WebDavSync
import com.reminderapp.ui.comic.ComicCard
import com.reminderapp.ui.comic.ComicHairline
import com.reminderapp.ui.comic.ComicPalettes
import com.reminderapp.ui.comic.ComicTextAction
import com.reminderapp.ui.comic.comicColors
import com.reminderapp.ui.theme.ThemeStore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsAppearanceCard(
    themeMode: Int,
    activeColorIdx: Int,
    onThemeModeChange: (Int) -> Unit
) {
    val context = LocalContext.current
    val colors = comicColors()

    ComicCard(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = zh("外观") + " · " + zh(colors.paletteName),
            color = colors.ink,
            fontSize = 15.5.sp,
            fontWeight = FontWeight.ExtraBold
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ComicPalettes.All.forEachIndexed { idx, spec ->
                val selected = activeColorIdx == idx
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { ThemeStore.setColorIndex(context, idx) }
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(spec.accent),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Box(Modifier.size(6.dp).clip(CircleShape).background(spec.onAccent))
                        }
                    }
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = zh(spec.name),
                        color = if (selected) colors.ink else colors.muted,
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }

        ComicHairline(isInner = true)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(zh("显示"), color = colors.muted, fontSize = 13.5.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                listOf(0 to zh("跟随系统"), 1 to zh("浅色"), 2 to zh("深色")).forEach { (m, label) ->
                    val active = themeMode == m
                    Text(
                        text = label,
                        color = if (active) colors.accent else colors.muted,
                        fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium,
                        fontSize = 13.5.sp,
                        modifier = Modifier.clickable {
                            ThemeStore.setMode(context, m)
                            onThemeModeChange(m)
                        }
                    )
                }
            }
        }

        ComicHairline(isInner = true)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(zh("语言"), color = colors.muted, fontSize = 13.5.sp)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LocaleManager.options.forEach { code ->
                    val active = LocaleManager.currentCode(context) == code
                    Text(
                        text = LocaleManager.displayName(code),
                        color = if (active) colors.accent else colors.ink,
                        fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Normal,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { LocaleManager.setLanguage(context, code) }
                    )
                }
            }
        }
    }
}

@Composable
fun SyncConflictAndSnapshotsSection(
    activeConflict: WebDavSync.SyncResult.Conflict?,
    syncing: Boolean,
    snapshots: List<File>,
    dateFormat: SimpleDateFormat,
    pendingRestoreSnapshot: File?,
    onResolveConflict: (Boolean) -> Unit,
    onCancelConflict: () -> Unit,
    onRequestRestoreSnapshot: (File?) -> Unit,
    onConfirmRestoreSnapshot: (File) -> Unit
) {
    val colors = comicColors()

    activeConflict?.let {
        ComicCard(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(zh("同步冲突"), color = colors.danger, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                zh("同步冲突：选择后会先保存本地快照。远程选择合并同 ID 内容，本地独有提醒仍保留。"),
                color = colors.ink,
                fontSize = 13.sp
            )
            listOf(true to "保留本地并上传", false to "使用远程并合并").forEach { (useLocal, label) ->
                ComicTextAction(
                    text = "› " + zh(label),
                    enabled = !syncing,
                    accent = true,
                    fontSize = 14.sp,
                    onClick = { onResolveConflict(useLocal) }
                )
            }
            ComicTextAction(text = zh("取消，保留双方数据"), onClick = onCancelConflict)
        }
    }

    ComicCard(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(zh("同步前本地快照"), color = colors.ink, fontSize = 15.5.sp, fontWeight = FontWeight.ExtraBold)
        if (snapshots.isEmpty()) {
            Text(zh("发生同步冲突覆盖前会自动保存快照到这里"), color = colors.muted, fontSize = 12.5.sp)
        } else {
            snapshots.forEach { file ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(file.name, color = colors.ink, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                        Text(
                            dateFormat.format(Date(file.lastModified())),
                            color = colors.muted,
                            fontSize = 11.5.sp,
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )
                    }
                    ComicTextAction(
                        text = zh("恢复"),
                        enabled = !syncing,
                        accent = true,
                        onClick = { onRequestRestoreSnapshot(file) }
                    )
                }
            }
        }
    }

    pendingRestoreSnapshot?.let { target ->
        AlertDialog(
            onDismissRequest = { onRequestRestoreSnapshot(null) },
            title = { Text(zh("恢复同步快照")) },
            text = { Text(target.name) },
            confirmButton = {
                TextButton(onClick = { onConfirmRestoreSnapshot(target) }) { Text(zh("恢复")) }
            },
            dismissButton = {
                TextButton(onClick = { onRequestRestoreSnapshot(null) }) { Text(zh("取消")) }
            }
        )
    }
}

@Composable
fun NearbyQrScannerDialog(
    visible: Boolean,
    onScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            QrScanner(onQrScanned = onScanned, modifier = Modifier.fillMaxSize())
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(220.dp))
                Text(zh("对准对方的二维码"), color = Color.White, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onDismiss) { Text(zh("取消"), color = Color.White) }
            }
        }
    }
}

fun receiveNearbyReminders(
    hostInput: String,
    database: AppDatabase,
    context: Context,
    scope: CoroutineScope,
    setReceiving: (Boolean) -> Unit,
    setResult: (String, Boolean) -> Unit
) {
    val host = hostInput.trim()
    if (host.isEmpty()) return
    setReceiving(true)
    setResult("", false)
    scope.launch {
        val json = withContext(Dispatchers.IO) { NearbyShareService.fetchFrom(host) }
        setReceiving(false)
        if (json == null) {
            setResult(
                zh("下载失败：请确认两台设备在同一 Wi-Fi、发送方已点「开始共享」、地址正确；如仍失败请检查路由器是否开启客户端隔离"),
                true
            )
            return@launch
        }
        val entities = BackupService.importFromJson(json)
        if (entities == null) {
            setResult(zh("数据解析失败"), true)
            return@launch
        }
        val result = runCatching {
            ReminderActions(context, database).importItems(entities, json)
        }.getOrElse {
            setResult(zh("数据解析失败"), true)
            return@launch
        }
        setResult(zhf("导入完成：新增 %1\$s 条，跳过重复 %2\$s 条", result.saved.size, result.skipped), false)
    }
}
