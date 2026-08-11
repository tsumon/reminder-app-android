package com.reminderapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.compose.rememberNavController
import com.reminderapp.i18n.LocaleManager
import com.reminderapp.ui.navigation.NavGraph
import com.reminderapp.ui.theme.ReminderAppTheme

class MainActivity : ComponentActivity() {

    // v2.0.4: 手动语言切换 —— 在 Activity 构建前替换 Locale，整棵树资源跟随
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.wrap(newBase))
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* 用户授权结果无需额外处理；未授权时通知会静默不显 */ }

    // 批次2 功能1: 通知点击直达确认面板 —— 冷启动(onCreate)与热启动(onNewIntent)统一经由该状态下发
    private var deepLinkReminderId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // I7: 关键提醒全屏弹窗意图（锁屏时系统会丢弃未标记锁屏可见的 Activity）。
        // 仅当由此意图冷启动时才标记，避免普通启动也强制覆盖锁屏。
        if (intent?.getBooleanExtra(
                com.reminderapp.service.NotificationManager.EXTRA_CRITICAL_FULLSCREEN, false
            ) == true
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
            }
        }

        val app = application as ReminderApp

        // Android 13+ 必须显式申请通知权限，否则通知静默不显示
        requestNotificationPermissionIfNeeded()

        // 批次2 功能1: 冷启动（App 被杀后点通知）—— 从 intent 读取直达详情目标
        resolveDeepLink(intent)

        // 启动时自动同步（如果开启了且已配置）
        if (com.reminderapp.service.SyncStore.autoSync &&
            com.reminderapp.service.SyncStore.isConfigured
        ) {
            Thread {
                Thread.sleep(2000) // 等待界面就绪
                kotlinx.coroutines.runBlocking {
                    com.reminderapp.service.WebDavSync.syncNow(applicationContext)
                }
            }.start()
        }

        setContent {
            ReminderAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        database = app.database,
                        scheduler = app.scheduler,
                        notificationMgr = app.notificationManager,
                        aiService = app.aiService,
                        aiSettings = app.aiSettings,
                        deepLinkReminderId = deepLinkReminderId,
                        onDeepLinkConsumed = {
                            deepLinkReminderId = null
                            // v2.0.21 G6: 同时清掉 intent 里的 extra——否则配置变更（旋转屏幕、
                            // 深色模式切换）导致 Activity 重建时 onCreate 会重读同一个 intent，
                            // 又跳一次详情页
                            intent?.removeExtra(
                                com.reminderapp.service.NotificationManager.EXTRA_REMINDER_ID
                            )
                        }
                    )
                }
            }
        }
    }

    // 批次2 功能1: 热启动（App 已在后台，singleTop 复用实例）—— 更新直达目标并触发导航
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        resolveDeepLink(intent)
    }

    private fun resolveDeepLink(intent: Intent?) {
        val id = intent?.getLongExtra(
            com.reminderapp.service.NotificationManager.EXTRA_REMINDER_ID,
            0L
        ) ?: 0L
        deepLinkReminderId = id.takeIf { it > 0L }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val nm = NotificationManagerCompat.from(this)
            if (!nm.areNotificationsEnabled()) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
