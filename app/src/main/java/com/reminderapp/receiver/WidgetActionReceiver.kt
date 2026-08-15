package com.reminderapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.service.ReminderEngine
import com.reminderapp.service.ReminderScheduler
import com.reminderapp.service.SyncStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 桌面小组件快捷按钮动作接收器（v1.8.7 小组件增强）
 *
 * 点击小组件上的完成/稍后按钮 → 广播到这里 → 确认该提醒并重排下一次，
 * 与通知栏「确认」逻辑一致，最后刷新小组件。
 */
class WidgetActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_COMPLETE = "com.reminderapp.widget.ACTION_COMPLETE"
        // v2.0.16: 小组件「稍后提醒」按钮
        const val ACTION_SNOOZE = "com.reminderapp.widget.ACTION_SNOOZE"
        const val EXTRA_REMINDER_ID = "reminder_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_COMPLETE -> handleComplete(context, intent)
            ACTION_SNOOZE -> handleSnooze(context, intent)
        }
    }

    private fun handleComplete(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (reminderId <= 0) return

        val appContext = context.applicationContext
        val db = AppDatabase.getInstance(appContext)
        val scheduler = ReminderScheduler(appContext)

        // ⚠️ goAsync() 保活：小组件广播后台执行，协程未完成进程可能被杀
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminder = db.reminderDao().getById(reminderId) ?: return@launch
                val updated = ReminderEngine.confirm(reminder)
                db.reminderDao().update(updated)
                // v1.9.6 fix: 完成确认后取消已显示的通知
                com.reminderapp.service.NotificationManager(appContext).cancelReminderNotifications(reminderId)
                // v2.0.22: 一次性提醒确认后归档，不重排（对齐 NotificationActionReceiver）
                if (!(updated.kind == "cycle" && updated.cycle == "once")) {
                    scheduler.schedule(updated)
                }
                // v1.9.6 fix: 漏 touchLocalChange → 本地新数据被远程旧数据覆盖
                SyncStore.touchLocalChange()
                // 刷新小组件（含下次提醒与倒计时）
                ReminderWidgetProvider.refresh(appContext)
            } finally {
                pendingResult.finish()
            }
        }
    }

    // v2.0.16: 小组件「稍后提醒」——推迟 15 分钟并重排（对齐通知栏 ACTION_SNOOZE）
    private fun handleSnooze(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (reminderId <= 0) return

        val appContext = context.applicationContext
        val db = AppDatabase.getInstance(appContext)
        val scheduler = ReminderScheduler(appContext)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminder = db.reminderDao().getById(reminderId) ?: return@launch
                val updated = ReminderEngine.snooze(reminder)
                db.reminderDao().update(updated)
                // snooze 已把 nextTriggerAt 设成 15 分钟后，交给 scheduler 定时重推
                scheduler.schedule(updated)
                // 写操作记录（统计「稍后提醒」频率；口径与通知栏 snooze 一致）
                try {
                    db.reminderRecordDao().insert(
                        com.reminderapp.data.entity.ReminderRecordEntity(
                            reminderId = reminderId,
                            action = com.reminderapp.data.entity.ReminderRecordEntity.ACTION_SNOOZED
                        )
                    )
                } catch (_: Exception) {
                }
                com.reminderapp.service.NotificationManager(appContext).cancelReminderNotifications(reminderId)
                SyncStore.touchLocalChange()
                ReminderWidgetProvider.refresh(appContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
