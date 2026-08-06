package com.reminderapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.service.NotificationManager
import com.reminderapp.service.ReminderEngine
import com.reminderapp.service.ReminderScheduler
import com.reminderapp.ui.screen.CalendarScreen
import com.reminderapp.ui.screen.CreateReminderScreen
import com.reminderapp.ui.screen.HomeScreen
import com.reminderapp.ui.screen.ReminderDetailScreen
import com.reminderapp.ui.screen.SyncSettingsScreen
import com.reminderapp.ui.screen.AIChatScreen
import com.reminderapp.ui.screen.AISettingsScreen
import com.reminderapp.ui.screen.StatsScreen
import com.reminderapp.ui.screen.SettingsScreen
import com.reminderapp.ui.screen.NearbyShareScreen
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
    // v1.9.8 UI 对齐设计图：底部导航 4 Tab（首页 / 日历 / 统计 / AI）
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val tabRoutes = listOf("home", "calendar", "stats", "chat")
    val showBottomBar = currentRoute in tabRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ) {
                    // 首页
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                        label = { Text("首页") }
                    )
                    // 日历
                    NavigationBarItem(
                        selected = currentRoute == "calendar",
                        onClick = {
                            navController.navigate("calendar") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                        label = { Text("日历") }
                    )
                    // 统计
                    NavigationBarItem(
                        selected = currentRoute == "stats",
                        onClick = {
                            navController.navigate("stats") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Filled.BarChart, contentDescription = null) },
                        label = { Text("统计") }
                    )
                    // AI
                    NavigationBarItem(
                        selected = currentRoute == "chat",
                        onClick = {
                            navController.navigate("chat") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Filled.SmartToy, contentDescription = null) },
                        label = { Text("AI") }
                    )
                }
            }
        }
    ) { innerPadding ->
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(innerPadding)
    ) {

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
                            // 强制 id=0 让 Room 重新自增：备份 JSON 保留了原 id，
                            // 直接插入会与现有行（含软删行）主键冲突 → SQLiteConstraintException 崩溃
                            val newId = try {
                                database.reminderDao().insert(r.copy(id = 0))
                            } catch (e: Exception) {
                                android.util.Log.e("NavGraph", "导入单条失败: ${e.message}")
                                return@forEach
                            }
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
                onNearbyShare = { navController.navigate("nearby_share") },
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

        // v1.9.8: 独立日历 Tab（整页日历 + 当天任务，对齐设计图）
        composable("calendar") {
            val calendarReminders by database.reminderDao().getAllActive()
                .collectAsState(initial = emptyList())
            CalendarScreen(
                reminders = calendarReminders,
                onReminderClick = { id -> navController.navigate("detail/$id") }
            )
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

        // 近场传输: 同一局域网互传提醒
        composable("nearby_share") {
            NearbyShareScreen(
                database = database,
                scheduler = scheduler,
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

            // 必须 remember：每次重组 new 会让 VM 初始状态变 null → 详情页闪空白反复加载
            val viewModel = remember(reminderId) {
                ReminderDetailViewModel(
                    reminderId = reminderId,
                    dao = database.reminderDao(),
                    recordDao = database.reminderRecordDao(),
                    scheduler = scheduler,
                    notificationMgr = notificationMgr
                )
            }

            ReminderDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
    }
}
