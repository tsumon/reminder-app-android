package com.reminderapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.reminderapp.ui.navigation.NavGraph
import com.reminderapp.ui.theme.ReminderAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ReminderApp

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
                        aiSettings = app.aiSettings
                    )
                }
            }
        }
    }
}
