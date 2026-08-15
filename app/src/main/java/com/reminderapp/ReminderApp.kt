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

        // 批次2 功能3: 注册统计周报周期任务（首次对齐下一个周日 20:00，之后每 7 天）
        scheduleWeeklyReport()

        // v1.8.7 任务②: 后台刷新联网节假日数据（当年 + 下一年，跨年预取）
        refreshRemoteHolidays()

        // v2.1.1: 本地自动备份（自签无 iCloud 的本地兜底；当日已备份则跳过）
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            com.reminderapp.service.LocalBackupService.backupOnLaunch(this@ReminderApp)
        }
    }

    /**
     * 批次2 功能3: 统计周报 —— 每天跑一次的周期任务，首次对齐下一个 20:00。
     *
     * v2.0.21 修 G1：原为 7 天周期，一旦周日 20:00 的那次被 Doze/省电推迟到周一，
     * Worker 里「非周日直接 return」就让本周周报永久漏发。改为每天触发，
     * 由 WeeklyReportWorker 自行判定「该发哪一周」（周日发本周、周一~周三补发上一周）。
     */
    private fun scheduleWeeklyReport() {
        val now = System.currentTimeMillis()
        // 对齐下一个 20:00（今天 20:00 已过则顺延到明天）
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= now) cal.add(Calendar.DAY_OF_MONTH, 1)
        val initialDelay = maxOf(0L, cal.timeInMillis - now)

        val request = androidx.work.PeriodicWorkRequestBuilder<com.reminderapp.service.WeeklyReportWorker>(
            1, java.util.concurrent.TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()

        // UPDATE：从 v2.0.20 升级上来的用户已排了 7 天周期的旧任务，
        // 用 KEEP 会一直保留旧周期（新逻辑永不生效）；UPDATE 就地换参数且不丢执行历史。
        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            com.reminderapp.service.WeeklyReportWorker.UNIQUE_NAME,
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
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
