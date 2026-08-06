package com.reminderapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.reminderapp.BuildConfig
import com.reminderapp.service.UpdateService
import com.reminderapp.ui.theme.Tokens
import kotlinx.coroutines.launch

/**
 * 设置页：同步/AI/关于（版本号 + 检查更新 + 更新日志）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenSyncSettings: () -> Unit,
    onOpenAISettings: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<UpdateService.UpdateInfo?>(null) }
    var showChangelog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
            // === 同步 ===
            item {
                SettingsCard {
                    SettingRow(
                        icon = Icons.Filled.CloudSync,
                        title = "WebDAV 同步",
                        subtitle = "坚果云/自建服务器备份与多端同步",
                        onClick = onOpenSyncSettings
                    )
                }
            }

            // === AI ===
            item {
                SettingsCard {
                    SettingRow(
                        icon = Icons.Filled.SmartToy,
                        title = "AI 助手设置",
                        subtitle = "API 地址、模型与本地模式",
                        onClick = onOpenAISettings
                    )
                }
            }

            // === 关于 ===
            item {
                SettingsCard {
                    // 版本号
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Info, contentDescription = null,
                            tint = Tokens.BrandPrimary, modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("版本", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider()

                    // 检查更新
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickableItem {
                                checking = true
                                scope.launch {
                                    val info = UpdateService.checkLatest()
                                    checking = false
                                    when {
                                        info == null -> android.widget.Toast.makeText(
                                            context, "检查更新失败，请检查网络后重试",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                        UpdateService.isNewer(info.latestVersion, UpdateService.currentVersion()) ->
                                            updateInfo = info
                                        else -> android.widget.Toast.makeText(
                                            context, "当前已是最新版本 v${UpdateService.currentVersion()}",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Refresh, contentDescription = null,
                            tint = Tokens.BrandPrimary, modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("检查更新", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.weight(1f))
                        if (checking) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        }
                    }
                    HorizontalDivider()

                    // 更新日志
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickableItem { showChangelog = true }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Build, contentDescription = null,
                            tint = Tokens.BrandPrimary, modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("更新日志", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    "循环提醒 · 支持多端同步与在线升级",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
        }
    }

    // 更新日志弹窗
    if (showChangelog) {
        AlertDialog(
            onDismissRequest = { showChangelog = false },
            title = { Text("更新日志") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    changelog.forEach { (version, items) ->
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
                TextButton(onClick = { showChangelog = false }) { Text("好") }
            }
        )
    }

    // 发现新版本弹窗（下载/跳转）
    updateInfo?.let { info ->
        AlertDialog(
            onDismissRequest = { updateInfo = null },
            title = { Text("发现新版本 v${info.latestVersion}") },
            text = { Text("当前版本 v${UpdateService.currentVersion()}，是否下载安装？") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val apkUrl = info.apkUrl
                        if (apkUrl != null) {
                            try {
                                val apk = UpdateService.downloadApk(context, apkUrl)
                                UpdateService.install(context, apk)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(
                                    context, "下载失败: ${e.message}", android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            UpdateService.openReleasePage(context, info.releaseUrl)
                        }
                        updateInfo = null
                    }
                }) { Text("前往下载") }
            },
            dismissButton = {
                TextButton(onClick = { updateInfo = null }) { Text("稍后再说") }
            }
        )
    }
}

/** 更新日志数据（与 iOS 一致） */
private val changelog: List<Pair<String, List<String>>> = listOf(
    "v1.9.2" to listOf(
        "修复收不到更新提示（GitHub API 国内超时）",
        "WebDAV 坚果云友好错误提示 + 测试连接按钮"
    ),
    "v1.9.1" to listOf(
        "AI 支持创建规则提醒（每季度/每月/每年 第N周周X）",
        "首页菜单新增检查更新入口"
    ),
    "v1.9.0" to listOf(
        "UI 优化：滴答清单风格（渐变概览卡、已完成划线、卡片圆角阴影）",
        "日历：滑动切月 + 月份选择器 + 回到今天",
        "在线升级：自动检查 GitHub 最新版本并下载安装",
        "新增 App 图标（品牌紫 + 铃铛）"
    ),
    "v1.8.7" to listOf(
        "小组件增强：农历日期格、倒计时、完成按钮、大尺寸",
        "节假日联网补全：日历显示休/班",
        "统计洞察：完成率、连续打卡、忘记时段、月历热力图",
        ".ics 日历导出",
        "设计令牌统一（主色 M3 紫 #6750A4）",
        "崩溃监控 + 埋点基础设施"
    )
)

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            content = content
        )
    }
}

@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableItem(onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Tokens.BrandPrimary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun Modifier.clickableItem(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
