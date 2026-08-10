package com.reminderapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.service.ReminderEngine
import com.reminderapp.service.NotificationManager
import com.reminderapp.service.ReminderScheduler
import com.reminderapp.service.SyncStore
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

        // ⚠️ 广播回调必须 goAsync() 保活，否则协程里的 Room 操作常来不及完成，
        // 用户点了「确认/稍后」却什么都没发生（进程被杀）
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminder = db.reminderDao().getById(reminderId) ?: return@launch

                when (intent.action) {
                    NotificationManager.ACTION_CONFIRM -> {
                        val updated = ReminderEngine.confirm(reminder)
                        db.reminderDao().update(updated)
                        // v1.9.6 fix: once 提醒确认后归档，不重排——
                        // nextTriggerAt 是过去值，schedule 会 delay=0 立即再弹一条「已完成」
                        if (updated.cycle != "once") {
                            scheduler.schedule(updated)
                        }
                        // v1.9.6 fix: 写操作记录（通知栏确认是常用路径，统计依赖它）
                        try {
                            db.reminderRecordDao().insert(
                                com.reminderapp.data.entity.ReminderRecordEntity(reminderId = reminderId, action = com.reminderapp.data.entity.ReminderRecordEntity.ACTION_CONFIRMED)
                            )
                        } catch (_: Exception) {
                        }
                        // v1.9.6 fix: 漏 touchLocalChange → 本地新数据被远程旧数据覆盖 / AI 新建不同步
                        SyncStore.touchLocalChange()
                        ReminderWidgetProvider.refresh(context)
                    }
                    NotificationManager.ACTION_SNOOZE -> {
                        val updated = ReminderEngine.snooze(reminder)
                        db.reminderDao().update(updated)
                        // snooze 已把 nextTriggerAt 设成 15 分钟后，交给 scheduler 定时重推。
                        scheduler.schedule(updated)
                        try {
                            db.reminderRecordDao().insert(
                                com.reminderapp.data.entity.ReminderRecordEntity(reminderId = reminderId, action = com.reminderapp.data.entity.ReminderRecordEntity.ACTION_SNOOZED)
                            )
                        } catch (_: Exception) {
                        }
                        SyncStore.touchLocalChange()
                        ReminderWidgetProvider.refresh(context)
                    }
                }

                // 取消已显示的通知
                notificationMgr.cancelReminderNotifications(reminderId)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
