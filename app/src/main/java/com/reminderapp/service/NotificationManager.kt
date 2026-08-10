package com.reminderapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.reminderapp.R
import com.reminderapp.receiver.NotificationActionReceiver

/**
 * 通知管理器 — 创建通知渠道、发送本地通知（确认/稍后按钮 + 预告通知）
 * 镜像 iOS NotificationManager.swift
 */
class NotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_REMINDER = "reminder_channel"
        const val CHANNEL_ADVANCE = "advance_channel"

        const val ACTION_CONFIRM = "com.reminderapp.CONFIRM"
        const val ACTION_SNOOZE = "com.reminderapp.SNOOZE"

        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createChannels()
    }

    private fun createChannels() {
        // 正式提醒渠道
        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDER,
            context.getString(R.string.notification_channel_name),
            android.app.NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_desc)
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(reminderChannel)

        // 预告通知渠道
        val advanceChannel = NotificationChannel(
            CHANNEL_ADVANCE,
            context.getString(R.string.advance_channel_name),
            android.app.NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.advance_channel_desc)
        }
        notificationManager.createNotificationChannel(advanceChannel)
    }

    /**
     * 发送正式提醒通知（带确认/稍后按钮）
     */
    fun sendReminderNotification(reminderId: Long, title: String, body: String) {
        val notificationId = reminderId.toInt() + 1000

        val confirmIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_CONFIRM
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val confirmPending = PendingIntent.getBroadcast(
            context, notificationId * 2, confirmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val snoozePending = PendingIntent.getBroadcast(
            context, notificationId * 2 + 1, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDER)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .addAction(0, context.getString(R.string.confirm_action), confirmPending)
            .addAction(0, context.getString(R.string.snooze_action), snoozePending)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    /**
     * 发送预告通知（纯信息，无操作按钮）
     */
    fun sendAdvanceNotification(reminderId: Long, title: String, body: String) {
        val notificationId = reminderId.toInt() + 2000

        val notification = NotificationCompat.Builder(context, CHANNEL_ADVANCE)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    /**
     * 取消某个提醒的所有通知
     */
    fun cancelReminderNotifications(reminderId: Long) {
        notificationManager.cancel(reminderId.toInt() + 1000)
        notificationManager.cancel(reminderId.toInt() + 2000)
    }

}
