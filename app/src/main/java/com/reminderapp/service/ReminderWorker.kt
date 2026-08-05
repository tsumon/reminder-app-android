package com.reminderapp.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * WorkManager Worker — 到时间后发送通知
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val reminderId = inputData.getLong("reminder_id", 0)
        val title = inputData.getString("title") ?: "提醒"
        val body = inputData.getString("body") ?: ""
        val kind = inputData.getString("kind") ?: "cycle"

        val notificationManager = NotificationManager(applicationContext)

        if (kind == "date") {
            // 判断是预告通知还是正式通知
            notificationManager.sendReminderNotification(reminderId, "📅 $title", body)
        } else {
            notificationManager.sendReminderNotification(reminderId, title, body)
        }

        return Result.success()
    }
}
