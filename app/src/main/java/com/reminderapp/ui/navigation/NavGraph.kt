package com.reminderapp.ui.navigation

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.reminderapp.ReminderApp
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.data.entity.ReminderRecordEntity
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.service.AIService
import com.reminderapp.service.AISettings
import com.reminderapp.service.BackupService
import com.reminderapp.service.IcsExporter
import com.reminderapp.service.NotificationManager
import com.reminderapp.service.ReminderActions
import com.reminderapp.service.ReminderScheduler
import com.reminderapp.service.StatsService
import com.reminderapp.service.TelemetryService
import com.reminderapp.service.UpdateService
import com.reminderapp.service.WebDavSync
import com.reminderapp.ui.comic.ComicTabDock
import com.reminderapp.ui.screen.AIChatScreen
import com.reminderapp.ui.screen.AISettingsScreen
import com.reminderapp.ui.screen.CalendarScreen
import com.reminderapp.ui.screen.CreateReminderScreen
import com.reminderapp.ui.screen.DiagnosticsScreen
import com.reminderapp.ui.screen.HomeScreen
import com.reminderapp.ui.screen.NearbyShareScreen
import com.reminderapp.ui.screen.ReminderDetailScreen
import com.reminderapp.ui.screen.SettingsScreen
import com.reminderapp.ui.screen.StatsScreen
import com.reminderapp.ui.screen.SyncSettingsScreen
import com.reminderapp.ui.viewmodel.HomeViewModel
import com.reminderapp.ui.viewmodel.ReminderDetailViewModel
import java.io.File
import java.util.Calendar
import kotlinx.coroutines.launch

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
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val pendingSyncConflict by WebDavSync.pendingConflict.collectAsState()

    if (pendingSyncConflict != null && currentRoute != "sync_settings") {
        AlertDialog(
            onDismissRequest = { WebDavSync.pendingConflict.value = null },
            title = { Text(zh("同步冲突")) },
            text = { Text(zh("双端都有修改，同步已停止。请到同步设置选择要保留的数据。")) },
            confirmButton = {
                TextButton(onClick = { navController.navigate("sync_settings") }) {
                    Text(zh("选择数据"))
                }
            },
            dismissButton = {
                TextButton(onClick = { WebDavSync.pendingConflict.value = null }) {
                    Text(zh("稍后处理"))
                }
            }
        )
    }

    var createdUndo by remember { mutableStateOf<Pair<ReminderEntity, Long>?>(null) }
    var draft by remember { mutableStateOf<ReminderEntity?>(null) }
    var highlightConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(deepLinkReminderId) {
        val id = deepLinkReminderId ?: return@LaunchedEffect
        highlightConfirm = deepLinkHighlightConfirm
        navController.navigate("detail/$id")
        onDeepLinkConsumed()
    }

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
                val viewModel = remember {
                    HomeViewModel(
                        dao = database.reminderDao(),
                        recordDao = database.reminderRecordDao(),
                        scheduler = scheduler
                    )
                }
                val context = LocalContext.current
                val scope = rememberCoroutineScope()

                val exportLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.CreateDocument("application/json")
                ) { uri ->
                    if (uri != null) {
                        scope.launch {
                            val json = BackupService.exportDatabase(database)
                            BackupService.writeToUri(context, uri, json)
                        }
                    }
                }

                val importLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.OpenDocument()
                ) { uri ->
                    if (uri != null) {
                        scope.launch {
                            val json = BackupService.readFromUri(context, uri) ?: return@launch
                            val reminders = BackupService.importFromJson(json) ?: return@launch
                            val result = runCatching {
                                ReminderActions(context, database).importItems(reminders, json)
                            }.getOrElse {
                                Toast.makeText(context, zh("数据解析失败"), Toast.LENGTH_LONG).show()
                                return@launch
                            }
                            Toast.makeText(
                                context,
                                zhf("导入完成：新增 %1\$s 条，跳过重复或失败 %2\$s 条", result.saved.size, result.skipped),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                var updateInfo by remember { mutableStateOf<UpdateService.UpdateInfo?>(null) }
                var downloading by remember { mutableStateOf(false) }
                val manualCheck: () -> Unit = {
                    scope.launch {
                        val info = UpdateService.checkLatest()
                        when {
                            info == null -> Toast.makeText(
                                context,
                                zh("检查更新失败，请检查网络后重试"),
                                Toast.LENGTH_SHORT
                            ).show()
                            UpdateService.isNewer(info.latestVersion, UpdateService.currentVersion()) ->
                                updateInfo = info
                            else -> Toast.makeText(
                                context,
                                zhf("当前已是最新版本 v%s", UpdateService.currentVersion()),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                LaunchedEffect(Unit) {
                    val info = UpdateService.checkLatest()
                    if (info != null && UpdateService.isNewer(info.latestVersion, UpdateService.currentVersion())) {
                        updateInfo = info
                    }
                }

                HomeScreen(
                    viewModel = viewModel,
                    onCreateReminder = { draft = null; navController.navigate("create") },
                    onQuickCreate = { draft = it; navController.navigate("create") },
                    canUndoCreate = createdUndo != null,
                    onUndoCreate = {
                        scope.launch {
                            val token = createdUndo ?: return@launch
                            val ok = ReminderActions(context).undoCreation(token.first, token.second)
                            createdUndo = null
                            Toast.makeText(
                                context,
                                if (ok) zh("已撤销创建") else zh("提醒已有新变更，无法撤销"),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    onReminderClick = { id -> navController.navigate("detail/$id") },
                    onAIChat = { navController.navigate("chat") },
                    onDeleteReminder = { id ->
                        viewModel.deleteReminder(id)
                        TelemetryService.logEvent("reminder.delete")
                    },
                    onExport = { exportLauncher.launch("reminder_backup_${System.currentTimeMillis() / 1000}.json") },
                    onImport = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                    onOpenSyncSettings = { navController.navigate("sync_settings") },
                    onOpenDiagnostics = { navController.navigate("diagnostics") },
                    onOpenStats = { navController.navigate("stats") },
                    onOpenSettings = { navController.navigate("settings") },
                    onCheckUpdate = manualCheck,
                    onNearbyShare = { navController.navigate("nearby_share") },
                    onExportICS = {
                        scope.launch {
                            val reminders = database.reminderDao().getAllSync()
                            val ics = IcsExporter.generateIcs(reminders)
                            val file = File(context.cacheDir, "reminders.ics")
                            file.writeText(ics)
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/calendar"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, zh("导出日历")))
                        }
                    },
                    onSyncNow = {
                        scope.launch {
                            val result = WebDavSync.syncNow(context)
                            val msg = when (result) {
                                is WebDavSync.SyncResult.Success ->
                                    if (result.conflict) zh("已用最新版本覆盖（检测到双端都有修改，未合并）")
                                    else zh("同步完成")
                                is WebDavSync.SyncResult.Conflict -> {
                                    WebDavSync.pendingConflict.value = result
                                    navController.navigate("sync_settings")
                                    zh("检测到同步冲突，请选择保留的数据")
                                }
                                is WebDavSync.SyncResult.Error -> result.message
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                updateInfo?.let { info ->
                    AlertDialog(
                        onDismissRequest = { if (!downloading) updateInfo = null },
                        title = { Text(zhf("发现新版本 v%s", info.latestVersion)) },
                        text = {
                            Text(
                                if (downloading) zh("正在下载更新包…")
                                else zhf("当前版本 v%s，是否前往下载安装？", UpdateService.currentVersion())
                            )
                        },
                        confirmButton = {
                            TextButton(
                                enabled = !downloading,
                                onClick = {
                                    scope.launch {
                                        downloading = true
                                        try {
                                            if (info.apkUrl != null) {
                                                val apk = UpdateService.downloadApk(context, info)
                                                UpdateService.install(context, apk)
                                            } else {
                                                UpdateService.openReleasePage(context, info.releaseUrl)
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                zhf("下载失败: %s", e.message),
                                                Toast.LENGTH_LONG
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

            composable("calendar") {
                val calendarReminders by database.reminderDao().getAllActive()
                    .collectAsState(initial = emptyList())
                var streakDays by remember { mutableStateOf<Int?>(null) }
                LaunchedEffect(calendarReminders) {
                    runCatching {
                        val records = database.reminderRecordDao().getAll()
                        streakDays = StatsService.summarize(records).currentStreak
                    }
                }
                val calendarScope = rememberCoroutineScope()
                CalendarScreen(
                    reminders = calendarReminders,
                    onReminderClick = { id -> navController.navigate("detail/$id") },
                    onConfirm = { reminder ->
                        calendarScope.launch {
                            ReminderActions(ReminderApp.instance)
                                .apply(reminder, ReminderActions.Action.COMPLETE)
                        }
                    },
                    onCreateOnDay = { date ->
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = date
                            set(Calendar.HOUR_OF_DAY, 9)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        draft = ReminderEntity(
                            title = "",
                            firstTriggerAt = calendar.timeInMillis,
                            nextTriggerAt = calendar.timeInMillis
                        )
                        navController.navigate("create")
                    },
                    streak = streakDays,
                    weekDone = null
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
                DiagnosticsScreen(onBack = { navController.popBackStack() })
            }

            composable("stats") {
                var records by remember { mutableStateOf<List<ReminderRecordEntity>>(emptyList()) }
                LaunchedEffect(Unit) {
                    records = database.reminderRecordDao().getAll()
                }
                StatsScreen(
                    allRecords = records,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("nearby_share") {
                NearbyShareScreen(
                    database = database,
                    scheduler = scheduler,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("create") {
                val scope = rememberCoroutineScope()
                val context = LocalContext.current
                CreateReminderScreen(
                    initial = draft,
                    onSave = { entity ->
                        scope.launch {
                            val actions = ReminderActions(context)
                            if (entity.id > 0L) {
                                val expected = draft ?: return@launch
                                if (actions.edit(expected, entity) == null) {
                                    Toast.makeText(
                                        context,
                                        zh("提醒已有新变更，请重新打开编辑"),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                val created = actions.create(entity)
                                createdUndo = created.reminder to created.generation
                                TelemetryService.logEvent("reminder.create", mapOf("kind" to entity.kind))
                            }
                            navController.popBackStack()
                        }
                    },
                    onSaveAll = { entities ->
                        scope.launch {
                            val actions = ReminderActions(context)
                            entities.forEach { entity ->
                                if (entity.id > 0L) {
                                    val expected = draft ?: return@forEach
                                    if (actions.edit(expected, entity) == null) {
                                        Toast.makeText(
                                            context,
                                            zh("提醒已有新变更，请重新打开编辑"),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } else {
                                    val created = actions.create(entity)
                                    createdUndo = created.reminder to created.generation
                                    TelemetryService.logEvent("reminder.create", mapOf("kind" to entity.kind))
                                }
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
            ) { entry ->
                val reminderId = entry.arguments?.getLong("reminderId") ?: return@composable
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
                    onEdit = { draft = it; navController.navigate("create") }
                )
            }
        }

        // No floating SoftFab circle — Home's bare top header carries the single crisp `+ 新建` ComicButton.
        if (showBottomBar) {
            ComicTabDock(
                currentRoute = currentRoute,
                onSelect = { goTab(it) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
