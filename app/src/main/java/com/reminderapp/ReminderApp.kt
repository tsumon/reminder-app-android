package com.reminderapp

import android.app.Application
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.i18n.LocaleManager
import com.reminderapp.service.AIService
import com.reminderapp.service.AISettings
import com.reminderapp.service.HolidayPreCheck
import com.reminderapp.service.HolidayRemoteService
import com.reminderapp.service.NotificationManager
import com.reminderapp.service.ReminderScheduler
import com.reminderapp.service.TelemetryService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar

class ReminderApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var scheduler: ReminderScheduler
        private set

    lateinit var notificationManager: NotificationManager
        private set

    lateinit var aiService: AIService
        private set

    lateinit var aiSettings: AISettings
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // v2.0.4: 手动语言切换 —— 先于一切业务初始化应用目标 Locale
        LocaleManager.apply(this)

        // v1.8.7 任务⑥: 崩溃监控 + 埋点基础设施（必须先于业务初始化）
        TelemetryService.install()

        database = AppDatabase.getInstance(this)
        scheduler = ReminderScheduler(this)
        notificationManager = NotificationManager(this)
        aiService = AIService()
        aiSettings = AISettings(this)

        // v1.8.7 任务②: 后台刷新联网节假日数据（当年 + 下一年，跨年预取）
        refreshRemoteHolidays()
    }

    private fun refreshRemoteHolidays() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val year = Calendar.getInstance().get(Calendar.YEAR)
        scope.launch {
            HolidayRemoteService.refresh(this@ReminderApp, year)
            HolidayRemoteService.refresh(this@ReminderApp, year + 1)
            // Item 2: 启动后预检——把落在 ~1 个月内、恰逢法定节假日的循环/规则提醒前移
            HolidayPreCheck.run(this@ReminderApp)
        }
    }

    companion object {
        lateinit var instance: ReminderApp
            private set
    }
}
