package com.reminderapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.service.NotificationManager
import com.reminderapp.service.ReminderEngine
import com.reminderapp.service.ReminderScheduler
import com.reminderapp.ui.screen.CreateReminderScreen
import com.reminderapp.ui.screen.HomeScreen
import com.reminderapp.ui.screen.ReminderDetailScreen
import com.reminderapp.ui.screen.SyncSettingsScreen
import com.reminderapp.ui.screen.AIChatScreen
import com.reminderapp.ui.screen.AISettingsScreen
import com.reminderapp.ui.screen.StatsScreen
import com.reminderapp.ui.screen.SettingsScreen
import com.reminderapp.ui.viewmodel.HomeViewModel
import com.reminderapp.ui.viewmodel.ReminderDetailViewModel
import com.reminderapp.service.AISettings
import com.reminderapp.service.AIService
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    navController: NavHostController,
    database: AppDatabase,
    scheduler: ReminderScheduler,
    notificationMgr: NotificationManager,
    aiService: AIService,
    aiSettings: AISettings
) {
    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            // 必须 remember：否则每次重组都会新建 ViewModel，
            // StateFlow 被重建、订阅重启，首页会反复闪烁并重复触发副作用
            val viewModel = androidx.compose.runtime.remember {
                HomeViewModel(
                    dao = database.reminderDao(),
                    recordDao = database.reminderRecordDao(),
                    scheduler = scheduler
                )
            }
            val context = androidx.compose.ui.platform.LocalContext.current
            val scope = rememberCoroutineScope()

            // 导出：创建 JSON 文件
            val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
            ) { uri ->
                if (uri != null) {
                    scope.launch {
                        val reminders = database.reminderDao().getAllSync()
                        val json = com.reminderapp.service.BackupService.exportToJson(reminders)
                        com.reminderapp.service.BackupService.writeToUri(context, uri, json)
                    }
                }
            }

            // 导入：打开 JSON 文件
            val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
            ) { uri ->
                if (uri != null) {
                    scope.launch {
                        val json = com.reminderapp.service.BackupService.readFromUri(context, uri)
                            ?: return@launch
                        val reminders = com.reminderapp.service.BackupService.importFromJson(json)
                            ?: return@launch
                        reminders.forEach { r ->
                            val newId = database.reminderDao().insert(r)
                            scheduler.schedule(r.copy(id = newId))
                        }
                        com.reminderapp.service.SyncStore.touchLocalChange()
                        com.reminderapp.receiver.ReminderWidgetProvider.refresh(context)
                    }
                }
            }

            // 立即同步
            val scope2 = rememberCoroutineScope()

            // v1.9.0 在线升级: 启动静默检查 + 手动检查（无新版/失败 toast）
            var updateInfo by remember { mutableStateOf<com.reminderapp.service.UpdateService.UpdateInfo?>(null) }
            var downloading by remember { mutableStateOf(false) }
            val manualCheck: () -> Unit = {
                scope.launch {
                    val info = com.reminderapp.service.UpdateService.checkLatest()
                    when {
                        info == null -> android.widget.Toast.makeText(
                            context, "检查更新失败，请检查网络后重试", android.widget.Toast.LENGTH_SHORT
                        ).show()
                        com.reminderapp.service.UpdateService.isNewer(
                            info.latestVersion, com.reminderapp.service.UpdateService.currentVersion()
                        ) -> updateInfo = info
                        else -> android.widget.Toast.makeText(
                            context, "当前已是最新版本 v${com.reminderapp.service.UpdateService.currentVersion()}",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            LaunchedEffect(Unit) {
                val info = com.reminderapp.service.UpdateService.checkLatest()
                if (info != null &&
                    com.reminderapp.service.UpdateService.isNewer(info.latestVersion, com.reminderapp.service.UpdateService.currentVersion())) {
                    updateInfo = info
                }
            }

            HomeScreen(
                viewModel = viewModel,
                onCreateReminder = { navController.navigate("create") },
                onReminderClick = { id -> navController.navigate("detail/$id") },
                onAIChat = { navController.navigate("chat") },
                onDeleteReminder = { id -> viewModel.deleteReminder(id) },
                onExport = { exportLauncher.launch("reminder_backup_${System.currentTimeMillis() / 1000}.json") },
                onImport = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                onOpenSyncSettings = { navController.navigate("sync_settings") },
                onOpenStats = { navController.navigate("stats") },
                onOpenSettings = { navController.navigate("settings") },
                onCheckUpdate = manualCheck,
                // v1.8.7 任务④: .ics 导出 → 写 cache + FileProvider 分享
                onExportICS = {
                    scope.launch {
                        val reminders = database.reminderDao().getAllSync()
                        val ics = com.reminderapp.service.IcsExporter.generateIcs(reminders)
                        val file = java.io.File(context.cacheDir, "reminders.ics")
                        file.writeText(ics)
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context, "${context.packageName}.fileprovider", file
                        )
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/calendar"
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "导出日历"))
                    }
                },
                onSyncNow = {
                    scope2.launch {
                        val result = com.reminderapp.service.WebDavSync.syncNow(context)
                        val msg = when (result) {
                            is com.reminderapp.service.WebDavSync.SyncResult.Success -> "同步完成"
                            is com.reminderapp.service.WebDavSync.SyncResult.Error -> result.message
                        }
                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // v1.9.0 在线升级弹窗（updateInfo/downloading/manualCheck 定义在 HomeScreen 之前）
            updateInfo?.let { info ->
                AlertDialog(
                    onDismissRequest = { if (!downloading) updateInfo = null },
                    title = { Text("发现新版本 v${info.latestVersion}") },
                    text = {
                        Text(
                            if (downloading) "正在下载更新包…"
                            else "当前版本 v${com.reminderapp.service.UpdateService.currentVersion()}，是否前往下载安装？"
                        )
                    },
                    confirmButton = {
                        TextButton(
                            enabled = !downloading,
                            onClick = {
                                scope.launch {
                                    downloading = true
                                    try {
                                        val apkUrl = info.apkUrl
                                        if (apkUrl != null) {
                                            val apk = com.reminderapp.service.UpdateService.downloadApk(context, apkUrl)
                                            com.reminderapp.service.UpdateService.install(context, apk)
                                        } else {
                                            com.reminderapp.service.UpdateService.openReleasePage(context, info.releaseUrl)
                                        }
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(
                                            context, "下载失败: ${e.message}", android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        downloading = false
                                        updateInfo = null
                                    }
                                }
                            }
                        ) { Text(if (downloading) "下载中…" else "前往下载") }
                    },
                    dismissButton = {
                        TextButton(enabled = !downloading, onClick = { updateInfo = null }) {
                            Text("稍后再说")
                        }
                    }
                )
            }
        }

        composable("chat") {
            AIChatScreen(
                settings = aiSettings,
                aiService = aiService,
                database = database,
                scheduler = scheduler,
                notificationMgr = notificationMgr,
                onBack = { navController.popBackStack() },
                onNavigateSettings = { navController.navigate("ai_settings") }
            )
        }

        composable("ai_settings") {
            AISettingsScreen(
                settings = aiSettings,
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            // v1.9.2: 设置页（版本号/检查更新/更新日志/同步/AI）
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenSyncSettings = { navController.navigate("sync_settings") },
                onOpenAISettings = { navController.navigate("ai_settings") }
            )
        }

        composable("sync_settings") {
            SyncSettingsScreen(
                onBack = { navController.popBackStack() },
                onSyncResult = {}
            )
        }

        composable("stats") {
            // v1.8.7 任务③: 统计洞察（全本地，加载全部操作记录后聚合）
            var records by remember {
                mutableStateOf<List<com.reminderapp.data.entity.ReminderRecordEntity>>(emptyList())
            }
            LaunchedEffect(Unit) {
                records = database.reminderRecordDao().getAll()
            }
            StatsScreen(
                records = records,
                onBack = { navController.popBackStack() }
            )
        }

        composable("create") {
            val scope = rememberCoroutineScope()
            val context = androidx.compose.ui.platform.LocalContext.current
            CreateReminderScreen(
                onSave = { entity ->
                    scope.launch {
                        // P0 修复：手动创建路径之前直接 insert + schedule，
                        // date 类 nextTriggerAt 落到「提醒时间」默认的今天，
                        // 导致手动建的生日/节假日今天 9:00 就误触发。
                        // 与 AI 创建路径（AIChatScreen）一致，date/rule 类
                        // 统一走引擎按目标月日重算；cycle 类引擎会按周期锚点算。
                        val finalEntity = if (entity.kind == "cycle") {
                            entity
                        } else {
                            entity.copy(nextTriggerAt = ReminderEngine.calculateNextTrigger(entity))
                        }
                        val id = database.reminderDao().insert(finalEntity)
                        val saved = finalEntity.copy(id = id)
                        scheduler.schedule(saved)
                        com.reminderapp.service.SyncStore.touchLocalChange()
                        com.reminderapp.receiver.ReminderWidgetProvider.refresh(context)
                        navController.popBackStack()
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "detail/{reminderId}",
            arguments = listOf(navArgument("reminderId") { type = NavType.LongType })
        ) { backStackEntry ->
            val reminderId = backStackEntry.arguments?.getLong("reminderId") ?: return@composable

            val viewModel = ReminderDetailViewModel(
                reminderId = reminderId,
                dao = database.reminderDao(),
                recordDao = database.reminderRecordDao(),
                scheduler = scheduler,
                notificationMgr = notificationMgr
            )

            ReminderDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
