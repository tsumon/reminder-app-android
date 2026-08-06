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
 * 桌面小组件「✓ 完成」按钮动作接收器（v1.8.7 小组件增强）
 *
 * 点击小组件上的完成按钮 → 广播到这里 → 确认该提醒并重排下一次，
 * 与通知栏「确认」逻辑一致，最后刷新小组件。
 */
class WidgetActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_COMPLETE = "com.reminderapp.widget.ACTION_COMPLETE"
        const val EXTRA_REMINDER_ID = "reminder_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_COMPLETE) return
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
                scheduler.schedule(updated)
                // v1.9.6 fix: 漏 touchLocalChange → 本地新数据被远程旧数据覆盖
                SyncStore.touchLocalChange()
                // 刷新小组件（含下次提醒与倒计时）
                ReminderWidgetProvider.refresh(appContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
