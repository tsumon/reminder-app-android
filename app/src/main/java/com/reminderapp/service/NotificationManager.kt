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
        const val CHANNEL_WEEKLY = "weekly_channel"
        // 批次3 功能5: 关键提醒专用高优先级渠道
        const val CHANNEL_CRITICAL = "critical_channel"

        const val ACTION_CONFIRM = "com.reminderapp.CONFIRM"
        const val ACTION_SNOOZE = "com.reminderapp.SNOOZE"

        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        // I7: 关键提醒全屏弹窗意图专属标记——MainActivity 据此标记锁屏可见，避免锁屏下全屏 Intent 被系统丢弃
        const val EXTRA_CRITICAL_FULLSCREEN = "critical_fullscreen"
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

        // 批次3 功能5: 关键提醒渠道（最高优先级 + 穿透勿扰 + 锁屏公开显示）
        val criticalChannel = NotificationChannel(
            CHANNEL_CRITICAL,
            context.getString(R.string.critical_channel_name),
            android.app.NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.critical_channel_desc)
            enableVibration(true)
            enableLights(true)
            setBypassDnd(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(criticalChannel)

        // 预告通知渠道
        val advanceChannel = NotificationChannel(
            CHANNEL_ADVANCE,
            context.getString(R.string.advance_channel_name),
            android.app.NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.advance_channel_desc)
        }
        notificationManager.createNotificationChannel(advanceChannel)

        // 批次2 功能3: 统计周报渠道（低打扰）
        val weeklyChannel = NotificationChannel(
            CHANNEL_WEEKLY,
            context.getString(R.string.weekly_channel_name),
            android.app.NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.weekly_channel_desc)
        }
        notificationManager.createNotificationChannel(weeklyChannel)
    }

    /**
     * 发送正式提醒通知（带确认/稍后按钮 + 点击通知体直达确认面板）
     */
    fun sendReminderNotification(reminderId: Long, title: String, body: String) {
        val notificationId = reminderId.toInt() + 1000

        // 批次2 功能1: 点击通知体 → 打开确认面板（详情页）而非仅打开首页
        val contentIntent = Intent(context, com.reminderapp.MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra(EXTRA_REMINDER_ID, reminderId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPending = PendingIntent.getActivity(
            context, notificationId * 2 + 2, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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
            .setContentIntent(contentPending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .addAction(0, context.getString(R.string.confirm_action), confirmPending)
            .addAction(0, context.getString(R.string.snooze_action), snoozePending)
            // 功能7 轻量手表伴侣：Wear OS 卡片主点击直接「确认」，无需打开手机
            .extend(NotificationCompat.WearableExtender().setContentAction(0))
            .build()

        notificationManager.notify(notificationId, notification)
    }

    /**
     * 批次3 功能5: 发送「关键提醒」通知。
     * 走最高优先级渠道 + 全屏弹窗（设备锁屏时直接覆盖屏幕），确保重要事项不被漏看。
     * 全屏弹窗需 Manifest 中声明 USE_FULL_SCREEN_INTENT 权限（普通权限，默认授予）。
     */
    fun sendCriticalReminderNotification(reminderId: Long, title: String, body: String) {
        val notificationId = reminderId.toInt() + 1000

        val contentIntent = Intent(context, com.reminderapp.MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra(EXTRA_REMINDER_ID, reminderId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPending = PendingIntent.getActivity(
            context, notificationId * 2 + 2, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 全屏弹窗意图：锁屏时直接覆盖屏幕，点击进入确认面板
        val fullScreenIntent = Intent(context, com.reminderapp.MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra(EXTRA_REMINDER_ID, reminderId)
            // I7: 标记此为关键提醒全屏意图，MainActivity 据此调用 setShowWhenLocked 锁屏可见
            putExtra(EXTRA_CRITICAL_FULLSCREEN, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPending = PendingIntent.getActivity(
            context, notificationId * 3 + 5, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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

        val notification = NotificationCompat.Builder(context, CHANNEL_CRITICAL)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(contentPending)
            .setFullScreenIntent(fullScreenPending, true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 400, 200, 400, 200, 400))
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .addAction(0, context.getString(R.string.confirm_action), confirmPending)
            .addAction(0, context.getString(R.string.snooze_action), snoozePending)
            // 功能7 轻量手表伴侣：Wear OS 卡片主点击直接「确认」（关键提醒同样适用）
            .extend(NotificationCompat.WearableExtender().setContentAction(0))
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

    /**
     * 批次2 功能3: 发送统计周报通知（低打扰渠道；点击打开 App）
     */
    fun sendWeeklyReportNotification(title: String, body: String) {
        val contentIntent = Intent(context, com.reminderapp.MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPending = PendingIntent.getActivity(
            context, 0x5A17, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_WEEKLY)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(contentPending)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0x5A17, notification)
    }

}
