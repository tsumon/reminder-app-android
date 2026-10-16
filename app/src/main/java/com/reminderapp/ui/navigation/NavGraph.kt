package com.reminderapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.dp
import com.reminderapp.ui.theme.SoftFab
import com.reminderapp.ui.theme.SoftTabDock
import com.reminderapp.ui.theme.softCanvas
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
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf

@Composable
fun NavGraph(
    navController: NavHostController,
    database: AppDatabase,
    scheduler: ReminderScheduler,
    notificationMgr: NotificationManager,
    aiService: AIService,
    aiSettings: AISettings,
    deepLinkReminderId: Long? = null,
    deepLinkHighlightConfirm: Boolean = false,
    onDeepLinkConsumed: () -> Unit = {}
) {
    val pendingSyncConflict by com.reminderapp.service.WebDavSync.pendingConflict.collectAsState()
    if(pendingSyncConflict!=null && navController.currentDestination?.route!="sync_settings") {
        AlertDialog(onDismissRequest={com.reminderapp.service.WebDavSync.pendingConflict.value=null},title={Text(zh("同步冲突"))},
            text={Text(zh("双端都有修改，同步已停止。请到同步设置选择要保留的数据。"))},
            confirmButton={TextButton(onClick={navController.navigate("sync_settings");com.reminderapp.service.WebDavSync.pendingConflict.value=null}) { Text(zh("选择数据")) }},
            dismissButton={TextButton(onClick={com.reminderapp.service.WebDavSync.pendingConflict.value=null}) { Text(zh("稍后处理")) }})
    }
    var createdUndo by remember { mutableStateOf<Pair<com.reminderapp.data.entity.ReminderEntity,Long>?>(null) }
    var draft by remember { mutableStateOf<com.reminderapp.data.entity.ReminderEntity?>(null) }
    var highlightConfirm by remember { mutableStateOf(false) }
    // 批次2 功能1: 通知点击直达确认面板 —— 冷启动(onCreate)与热启动(onNewIntent)都走这里。
    // 每次 deepLinkReminderId 变化（首次 set 或 onNewIntent 更新）导航一次，
    // 导航后回调清空，避免「同 id 再次点击不触发 / 重组重复导航」。
    LaunchedEffect(deepLinkReminderId) {
        val id = deepLinkReminderId ?: return@LaunchedEffect
        highlightConfirm = deepLinkHighlightConfirm
        navController.navigate("detail/$id")
        onDeepLinkConsumed()
    }

    // 安静确认：底部导航 4 Tab（首页 / 日历 / 统计 / 设置）；AI 从首页入口进入，不是 Tab。
    // chat / ai_settings / detail / create 必须全屏，否则 SoftTabDock 会盖住 AI 输入栏。
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = when (currentRoute) {
        "home", "calendar", "stats", "settings" -> true
        else -> false
    }

    fun goTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(Modifier.fillMaxSize()) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.fillMaxSize()
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
                        val result=runCatching { com.reminderapp.service.ReminderActions(context,database).importItems(reminders) }.getOrElse {
                            android.widget.Toast.makeText(context,zh("数据解析失败"),android.widget.Toast.LENGTH_LONG).show();return@launch
                        }
                        val imported=result.saved.size
                        val skipped=result.skipped
                        android.widget.Toast.makeText(
                            context,
                            com.reminderapp.i18n.zhf("导入完成：新增 %1\$s 条，跳过重复或失败 %2\$s 条", imported, skipped),
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
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
                            context, zh("检查更新失败，请检查网络后重试"), android.widget.Toast.LENGTH_SHORT
                        ).show()
                        com.reminderapp.service.UpdateService.isNewer(
                            info.latestVersion, com.reminderapp.service.UpdateService.currentVersion()
                        ) -> updateInfo = info
                        else -> android.widget.Toast.makeText(
                            context, zhf("当前已是最新版本 v%s", com.reminderapp.service.UpdateService.currentVersion()),
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
                onCreateReminder = { draft=null;navController.navigate("create") },
                onQuickCreate = { draft=it;navController.navigate("create") },
                canUndoCreate = createdUndo!=null,
                onUndoCreate = { scope.launch {
                    val token=createdUndo ?: return@launch
                    val ok=com.reminderapp.service.ReminderActions(context).undoCreation(token.first,token.second)
                    createdUndo=null
                    android.widget.Toast.makeText(context,if(ok) zh("已撤销创建") else zh("提醒已有新变更，无法撤销"),android.widget.Toast.LENGTH_LONG).show()
                } },
                onReminderClick = { id -> navController.navigate("detail/$id") },
                onAIChat = { navController.navigate("chat") },
                onDeleteReminder = { id ->
                    viewModel.deleteReminder(id)
                    com.reminderapp.service.TelemetryService.logEvent("reminder.delete")
                },
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
                        context.startActivity(android.content.Intent.createChooser(intent, zh("导出日历")))
                    }
                },
                onSyncNow = {
                    scope2.launch {
                        val result = com.reminderapp.service.WebDavSync.syncNow(context)
                        val msg = when (result) {
                            // v2.0.16: 双端都改过 → 提示已按版本覆盖
                            is com.reminderapp.service.WebDavSync.SyncResult.Success ->
                                if (result.conflict) zh("已用最新版本覆盖（检测到双端都有修改，未合并）") else zh("同步完成")
                            is com.reminderapp.service.WebDavSync.SyncResult.Conflict -> { navController.navigate("sync_settings"); zh("检测到同步冲突，请选择保留的数据") }
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
                    title = { Text(zhf("发现新版本 v%s", info.latestVersion)) },
                    text = {
                        Text(
                            if (downloading) zh("正在下载更新包…")
                            else zhf("当前版本 v%s，是否前往下载安装？", com.reminderapp.service.UpdateService.currentVersion())
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
                                            // v2.2.1: 多候选 + 镜像兜底下载
                                            val apk = com.reminderapp.service.UpdateService.downloadApk(context, info)
                                            com.reminderapp.service.UpdateService.install(context, apk)
                                        } else {
                                            com.reminderapp.service.UpdateService.openReleasePage(context, info.releaseUrl)
                                        }
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(
                                            context, zhf("下载失败: %s", e.message), android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        downloading = false
                                        updateInfo = null
                                    }
                                }
                            }
                        ) { Text(if (downloading) zh("下载中…") else zh("前往下载")) }
                    },
                    dismissButton = {
                        TextButton(enabled = !downloading, onClick = { updateInfo = null }) {
                            Text(zh("稍后再说"))
                        }
                    }
                )
            }
        }

        // v1.9.8: 独立日历 Tab（整页日历 + 当天任务，对齐设计图）
        composable("calendar") {
            val calendarReminders by database.reminderDao().getAllActive()
                .collectAsState(initial = emptyList())
            // v2.5.0: 打卡记录 → 连续天数 + 本周打卡天数（只读统计，喂给日历卡彩虹跑道）
            var calendarPlayful by remember {
                mutableStateOf<Pair<Int, Int>?>(null)
            }
            LaunchedEffect(calendarReminders) {
                runCatching {
                    val records = database.reminderRecordDao().getAll()
                    val streak = com.reminderapp.service.StatsService.summarize(records).currentStreak
                    calendarPlayful = streak to com.reminderapp.ui.theme.weekDoneDays(records)
                }
            }
            val calendarScope = rememberCoroutineScope()
            CalendarScreen(
                reminders = calendarReminders,
                onReminderClick = { id -> navController.navigate("detail/$id") },
                onConfirm = { reminder ->
                    calendarScope.launch {
                        com.reminderapp.service.ReminderActions(com.reminderapp.ReminderApp.instance).apply(reminder,com.reminderapp.service.ReminderActions.Action.COMPLETE)
                    }
                },
                streak = calendarPlayful?.first,
                weekDone = calendarPlayful?.second
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
                onBack = {},
                showBack = false,
                onOpenSyncSettings = { navController.navigate("sync_settings") },
                onOpenAISettings = { navController.navigate("ai_settings") },
                onOpenDiagnostics = { navController.navigate("diagnostics") }
            )
        }

        composable("sync_settings") {
            SyncSettingsScreen(
                onBack = { navController.popBackStack() },
                onSyncResult = {}
            )
        }

        composable("diagnostics") {
            // v2.1.1: 提醒可靠性诊断页（权限/排期/数据规模）
            com.reminderapp.ui.screen.DiagnosticsScreen(
                onBack = { navController.popBackStack() }
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
                initial = draft,
                onSave = { entity ->
                    scope.launch {
                        val actions=com.reminderapp.service.ReminderActions(context)
                        if(entity.id>0L) {
                            val expected=draft ?: return@launch
                            if(actions.edit(expected,entity)==null) android.widget.Toast.makeText(context,zh("提醒已有新变更，请重新打开编辑"),android.widget.Toast.LENGTH_LONG).show()
                        } else {
                            val created=actions.create(entity)
                            createdUndo=created.reminder to created.generation
                            com.reminderapp.service.TelemetryService.logEvent("reminder.create",mapOf("kind" to entity.kind))
                        }
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
                onBack = { navController.popBackStack() },
                highlightConfirm = highlightConfirm,
                onEdit = { draft=it;navController.navigate("create") }
            )
        }
    }

        if (showBottomBar) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.End
            ) {
                // Dock fill covers navigationBars; icon row pads once inside SoftTabDock.
                // Scaffold contentWindowInsets = statusBars only — do not add nav padding here.
                if (currentRoute == "home") {
                    SoftFab(onClick = { draft=null;navController.navigate("create") })
                }
                SoftTabDock(currentRoute = currentRoute, onSelect = { goTab(it) })
            }
        }
    }
}
