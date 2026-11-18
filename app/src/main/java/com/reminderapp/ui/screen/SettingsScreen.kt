package com.reminderapp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.BuildConfig
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.service.LocalBackupService
import com.reminderapp.service.UpdateService
import com.reminderapp.service.WebDavSync
import com.reminderapp.ui.comic.ComicCard
import com.reminderapp.ui.comic.ComicHairline
import com.reminderapp.ui.comic.ComicTopBar
import com.reminderapp.ui.comic.comicColors
import com.reminderapp.ui.screen.settings.SettingsAppearanceCard
import com.reminderapp.ui.screen.settings.SettingsDialogs
import com.reminderapp.ui.theme.ThemeStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    showBack: Boolean = true,
    onOpenSyncSettings: () -> Unit,
    onOpenAISettings: () -> Unit,
    onOpenDiagnostics: () -> Unit
) {
    val context = LocalContext.current
    val colors = comicColors()
    val scope = rememberCoroutineScope()

    var checking by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<UpdateService.UpdateInfo?>(null) }
    var showChangelog by remember { mutableStateOf(false) }
    var showLocalBackupDialog by remember { mutableStateOf(false) }
    var localBackups by remember { mutableStateOf<List<LocalBackupService.BackupEntry>>(emptyList()) }
    var icsBusy by remember { mutableStateOf(false) }
    var icsResult by remember { mutableStateOf<WebDavSync.IcsUploadResult?>(null) }
    var themeMode by remember { mutableIntStateOf(ThemeStore.mode(context)) }
    val activeColorIdx = ThemeStore.colorIndex(context)

    Column(modifier = Modifier.fillMaxSize().background(colors.canvas)) {
        ComicTopBar(
            title = zh("设置"),
            onBack = if (showBack) onBack else null,
            showShard = !showBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "settings-appearance") {
                SettingsAppearanceCard(
                    themeMode = themeMode,
                    activeColorIdx = activeColorIdx,
                    onThemeModeChange = { themeMode = it }
                )
            }

            item(key = "settings-data-ai") {
                SettingsCard {
                    ComicSettingRow(
                        title = zh("WebDAV 同步"),
                        subtitle = zh("坚果云/自建服务器备份与多端同步"),
                        onClick = onOpenSyncSettings
                    )
                    ComicSettingRow(
                        title = if (icsBusy) zh("正在上传日历…") else zh("日历订阅链接"),
                        subtitle = zh("上传 .ics 到 WebDAV，供系统日历订阅"),
                        onClick = {
                            if (!icsBusy) {
                                scope.launch {
                                    icsBusy = true
                                    val list = runCatching { AppDatabase.getInstance(context).reminderDao().getAllSync() }
                                        .getOrDefault(emptyList())
                                    icsResult = WebDavSync.uploadIcs(list)
                                    icsBusy = false
                                }
                            }
                        }
                    )
                    ComicSettingRow(
                        title = zh("AI 助手设置"),
                        subtitle = zh("API 地址、模型与本地模式"),
                        onClick = onOpenAISettings
                    )
                    ComicHairline(isInner = true)
                    ComicSettingRow(
                        title = zh("提醒诊断"),
                        subtitle = zh("通知权限 / 排期 / 数据规模检查"),
                        onClick = onOpenDiagnostics
                    )
                    ComicSettingRow(
                        title = zh("立即备份"),
                        subtitle = zh("备份到下载目录（保留最近 5 份）"),
                        onClick = {
                            val name = LocalBackupService.backupNow(context)
                            Toast.makeText(
                                context,
                                if (name != null) zhf("已备份到下载目录：%s", name) else zh("备份失败"),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                    ComicSettingRow(
                        title = zh("恢复本地备份"),
                        subtitle = zh("从下载目录导入已备份的提醒"),
                        onClick = {
                            showLocalBackupDialog = true
                            scope.launch {
                                localBackups = withContext(Dispatchers.IO) { LocalBackupService.listBackups(context) }
                            }
                        }
                    )
                }
            }

            item(key = "settings-about") {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(zh("版本"), color = colors.ink, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(
                            "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                            color = colors.muted,
                            fontSize = 13.sp,
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                checking = true
                                scope.launch {
                                    val info = UpdateService.checkLatest()
                                    checking = false
                                    when {
                                        info == null -> Toast.makeText(context, zh("检查更新失败，请检查网络后重试"), Toast.LENGTH_SHORT).show()
                                        UpdateService.isNewer(info.latestVersion, UpdateService.currentVersion()) -> updateInfo = info
                                        else -> Toast.makeText(context, zhf("当前已是最新版本 v%s", UpdateService.currentVersion()), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(zh("检查更新"), color = colors.ink, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        if (checking) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = colors.accent)
                        else Text("›", color = colors.muted, fontSize = 15.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showChangelog = true }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(zh("更新日志"), color = colors.ink, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("›", color = colors.muted, fontSize = 15.sp)
                    }
                }
            }

            item(key = "settings-footer") {
                Text(
                    zh("循环提醒 · 支持多端同步与在线升级"),
                    color = colors.muted,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp).wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
        }
    }

    SettingsDialogs(
        icsResult = icsResult,
        onDismissIcs = { icsResult = null },
        showChangelog = showChangelog,
        onDismissChangelog = { showChangelog = false },
        updateInfo = updateInfo,
        onDismissUpdate = { updateInfo = null },
        showLocalBackupDialog = showLocalBackupDialog,
        localBackups = localBackups,
        onDismissLocalBackup = { showLocalBackupDialog = false }
    )
}

@Composable
internal fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    ComicCard(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun ComicSettingRow(title: String, subtitle: String, onClick: () -> Unit) {
    val colors = comicColors()
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = colors.ink, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = colors.muted, fontSize = 12.sp)
        }
        Spacer(Modifier.width(8.dp))
        Text("›", color = colors.muted, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
