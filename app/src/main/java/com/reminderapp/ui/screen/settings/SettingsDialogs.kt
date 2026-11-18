package com.reminderapp.ui.screen.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.service.BackupService
import com.reminderapp.service.LocalBackupService
import com.reminderapp.service.ReminderActions
import com.reminderapp.service.UpdateService
import com.reminderapp.service.WebDavSync
import com.reminderapp.ui.comic.comicColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val appChangelog: List<Pair<String, List<String>>> = listOf(
    "v2.11.0" to listOf(
        "全新极简奶油漫风（Minimalist Cream Comic）前端架构，92% 暖奶油留白与零嵌套碎框",
        "支持奶油橙、抹茶奶纸、海盐汽水、莓果奶昔、黄油柠檬、复古电紫六套纸感色板与暗夜暖琥珀模式",
        "重构首页焦点大字卡、清爽无分割线列表、统计热力点阵与桌面小组件"
    ),
    "v2.10.0" to listOf(
        "修复日期农历跨年与闰日触发、调休补班顺延、规则周期日历标点与统计漏掉口径",
        "新增卡片快捷跳过与本地备份/同步快照恢复",
        "WebDAV 增量内容指纹去重并完善桌面小组件适配"
    ),
    "v2.9.0" to listOf(
        "双端对齐增量同步代际、删除墓碑与并发动作防护",
        "支持自定义重试间隔与勿扰时段"
    ),
    "v2.5.0" to listOf(
        "升级多套主题色板、统计热力图、连续打卡追踪与智能频率建议"
    )
)

@Composable
fun SettingsDialogs(
    icsResult: WebDavSync.IcsUploadResult?,
    onDismissIcs: () -> Unit,
    showChangelog: Boolean,
    onDismissChangelog: () -> Unit,
    updateInfo: UpdateService.UpdateInfo?,
    onDismissUpdate: () -> Unit,
    showLocalBackupDialog: Boolean,
    localBackups: List<LocalBackupService.BackupEntry>,
    onDismissLocalBackup: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = comicColors()

    icsResult?.let { result ->
        when (result) {
            is WebDavSync.IcsUploadResult.Success -> AlertDialog(
                onDismissRequest = onDismissIcs,
                title = { Text(zh("日历已上传")) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            zhf("已把 %s 条提醒导出为 reminders.ics 并上传到你的 WebDAV 目录。", result.count),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            result.url,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            zh(
                                "这个地址需要账号密码，系统日历无法直接订阅。请在网盘网页端找到该文件 → 创建分享链接 →" +
                                    "把分享直链填进「日历 → 添加订阅日历」。之后每次点这里重新上传，订阅端刷新即可看到最新日程。"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.muted
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("ics", result.url))
                        Toast.makeText(context, zh("链接已复制"), Toast.LENGTH_SHORT).show()
                        onDismissIcs()
                    }) { Text(zh("复制链接")) }
                },
                dismissButton = {
                    TextButton(onClick = onDismissIcs) { Text(zh("关闭")) }
                }
            )
            is WebDavSync.IcsUploadResult.Error -> AlertDialog(
                onDismissRequest = onDismissIcs,
                title = { Text(zh("上传失败")) },
                text = { Text(result.message) },
                confirmButton = {
                    TextButton(onClick = onDismissIcs) { Text(zh("好")) }
                }
            )
        }
    }

    if (showChangelog) {
        AlertDialog(
            onDismissRequest = onDismissChangelog,
            title = { Text(zh("更新日志")) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    appChangelog.forEach { (version, items) ->
                        item {
                            Column {
                                Text(version, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                items.forEach { item ->
                                    Text("· $item", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissChangelog) { Text(zh("好")) }
            }
        )
    }

    updateInfo?.let { info ->
        AlertDialog(
            onDismissRequest = onDismissUpdate,
            title = { Text(zhf("发现新版本 v%s", info.latestVersion)) },
            text = { Text(zhf("当前版本 v%s，是否下载安装？", UpdateService.currentVersion())) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        if (info.apkUrl != null) {
                            try {
                                val apk = UpdateService.downloadApk(context, info)
                                UpdateService.install(context, apk)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    zhf("下载失败: %s", e.message),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            UpdateService.openReleasePage(context, info.releaseUrl)
                        }
                        onDismissUpdate()
                    }
                }) { Text(zh("前往下载")) }
            },
            dismissButton = {
                TextButton(onClick = onDismissUpdate) { Text(zh("稍后再说")) }
            }
        )
    }

    if (showLocalBackupDialog) {
        AlertDialog(
            onDismissRequest = onDismissLocalBackup,
            title = { Text(zh("恢复本地备份")) },
            text = {
                if (localBackups.isEmpty()) {
                    Text(zh("未找到本地备份文件"), style = MaterialTheme.typography.bodyMedium)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        localBackups.forEach { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onDismissLocalBackup()
                                        scope.launch {
                                            val json = withContext(Dispatchers.IO) { entry.readJson() }
                                            val parsed = json?.let { BackupService.importFromJson(it) }
                                            if (parsed == null) {
                                                Toast.makeText(context, zh("数据解析失败"), Toast.LENGTH_SHORT).show()
                                                return@launch
                                            }
                                            val imported = runCatching {
                                                ReminderActions(context, AppDatabase.getInstance(context)).importItems(parsed, json)
                                            }.getOrNull()
                                            Toast.makeText(
                                                context,
                                                if (imported != null) {
                                                    zhf("导入完成：新增 %1\$s 条，跳过重复 %2\$s 条", imported.saved.size, imported.skipped)
                                                } else zh("恢复失败"),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entry.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text(
                                        "${(entry.sizeBytes / 1024L).coerceAtLeast(1L)} KB",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.muted
                                    )
                                }
                                Text(zh("恢复"), color = colors.accent, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissLocalBackup) { Text(zh("关闭")) }
            }
        )
    }
}
