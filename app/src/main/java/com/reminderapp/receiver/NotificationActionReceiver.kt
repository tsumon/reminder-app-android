package com.reminderapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.service.ReminderEngine
import com.reminderapp.service.NotificationManager
import com.reminderapp.service.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 通知操作按钮接收器 — 处理「确认完成」和「稍后提醒」
 */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(NotificationManager.EXTRA_REMINDER_ID, 0)
        val notificationId = intent.getIntExtra(NotificationManager.EXTRA_NOTIFICATION_ID, 0)
        if (reminderId == 0L) return

        val db = AppDatabase.getInstance(context)
        val notificationMgr = NotificationManager(context)
        val scheduler = ReminderScheduler(context)

        CoroutineScope(Dispatchers.IO).launch {
            val reminder = db.reminderDao().getById(reminderId) ?: return@launch

            when (intent.action) {
                NotificationManager.ACTION_CONFIRM -> {
                    val updated = ReminderEngine.confirm(reminder)
                    db.reminderDao().update(updated)
                    // 重新调度下一次触发，避免从通知栏确认后周期断链
                    scheduler.schedule(updated)

                    // 日期提醒：也生成预告通知
                    if (updated.kind == "date") {
                        scheduleAdvanceNotifications(context, updated)
                    }
                }
                NotificationManager.ACTION_SNOOZE -> {
                    val updated = ReminderEngine.snooze(reminder)
                    db.reminderDao().update(updated)
                    // snooze 已把 nextTriggerAt 设成 15 分钟后，交给 scheduler 定时重推。
                    // 这里不要再直接 sendReminderNotification：那是「立刻」弹一条，
                    // 且会被本方法末尾的 cancelReminderNotifications 立即撤掉，
                    // 结果就是点了「稍后」等于什么都没发生。
                    scheduler.schedule(updated)
                }
            }

            // 取消已显示的通知
            notificationMgr.cancelReminderNotifications(reminderId)
        }
    }

    private fun scheduleAdvanceNotifications(context: Context, reminder: com.reminderapp.data.entity.ReminderEntity) {
        val now = System.currentTimeMillis()
        val advanceDays = ReminderEngine.getAdvanceDaysToNotify(reminder, now)
        val notificationMgr = NotificationManager(context)

        advanceDays.forEach { days ->
            notificationMgr.sendAdvanceNotification(
                reminder.id,
                "📅 即将到来：${reminder.title}",
                "还有 $days 天，提前做好准备"
            )
        }
    }
}
