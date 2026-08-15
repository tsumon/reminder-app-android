package com.reminderapp.service;

/**
 * 通知管理器 — 创建通知渠道、发送本地通知（确认/稍后按钮 + 预告通知）
 * 镜像 iOS NotificationManager.swift
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0006\u0018\u0000 \u00132\u00020\u0001:\u0001\u0013B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\b\u0010\u000b\u001a\u00020\bH\u0002J\u001e\u0010\f\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u000eJ\u001e\u0010\u0010\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u000eJ\u001e\u0010\u0011\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u000eJ\u0016\u0010\u0012\u001a\u00020\b2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u000eR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/reminderapp/service/NotificationManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "notificationManager", "Landroidx/core/app/NotificationManagerCompat;", "cancelReminderNotifications", "", "reminderId", "", "createChannels", "sendAdvanceNotification", "title", "", "body", "sendCriticalReminderNotification", "sendReminderNotification", "sendWeeklyReportNotification", "Companion", "app_debug"})
public final class NotificationManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHANNEL_REMINDER = "reminder_channel";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHANNEL_ADVANCE = "advance_channel";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHANNEL_WEEKLY = "weekly_channel";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHANNEL_CRITICAL = "critical_channel";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_CONFIRM = "com.reminderapp.CONFIRM";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_SNOOZE = "com.reminderapp.SNOOZE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_REMINDER_ID = "reminder_id";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_NOTIFICATION_ID = "notification_id";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_CRITICAL_FULLSCREEN = "critical_fullscreen";
    @org.jetbrains.annotations.NotNull()
    private final androidx.core.app.NotificationManagerCompat notificationManager = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.service.NotificationManager.Companion Companion = null;
    
    public NotificationManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    private final void createChannels() {
    }
    
    /**
     * 发送正式提醒通知（带确认/稍后按钮 + 点击通知体直达确认面板）
     */
    public final void sendReminderNotification(long reminderId, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String body) {
    }
    
    /**
     * 批次3 功能5: 发送「关键提醒」通知。
     * 走最高优先级渠道 + 全屏弹窗（设备锁屏时直接覆盖屏幕），确保重要事项不被漏看。
     * 全屏弹窗需 Manifest 中声明 USE_FULL_SCREEN_INTENT 权限（普通权限，默认授予）。
     */
    public final void sendCriticalReminderNotification(long reminderId, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String body) {
    }
    
    /**
     * 发送预告通知（纯信息，无操作按钮）
     */
    public final void sendAdvanceNotification(long reminderId, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String body) {
    }
    
    /**
     * 取消某个提醒的所有通知
     */
    public final void cancelReminderNotifications(long reminderId) {
    }
    
    /**
     * 批次2 功能3: 发送统计周报通知（低打扰渠道；点击打开 App）
     */
    public final void sendWeeklyReportNotification(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String body) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\t\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/reminderapp/service/NotificationManager$Companion;", "", "()V", "ACTION_CONFIRM", "", "ACTION_SNOOZE", "CHANNEL_ADVANCE", "CHANNEL_CRITICAL", "CHANNEL_REMINDER", "CHANNEL_WEEKLY", "EXTRA_CRITICAL_FULLSCREEN", "EXTRA_NOTIFICATION_ID", "EXTRA_REMINDER_ID", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}