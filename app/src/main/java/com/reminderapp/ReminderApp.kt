package com.reminderapp

import android.app.Application
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.service.AIService
import com.reminderapp.service.AISettings
import com.reminderapp.service.HolidayRemoteService
import com.reminderapp.service.NotificationManager
import com.reminderapp.service.ReminderScheduler
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
        }
    }

    companion object {
        lateinit var instance: ReminderApp
            private set
    }
}
