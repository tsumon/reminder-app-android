package com.reminderapp.service;

/**
 * 提醒调度器 — 用 WorkManager 管理后台定时任务
 * 负责周期/日期提醒和递增重试的精确调度
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0006\u0018\u0000 \u00182\u00020\u0001:\u0001\u0018B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0018\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0002J\u0010\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\rH\u0002J\u000e\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0007\u001a\u00020\bJ\u0006\u0010\u0010\u001a\u00020\u000fJ\u0014\u0010\u0011\u001a\u00020\u000f2\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\r0\u0013J\u001c\u0010\u0014\u001a\u00020\u000f2\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\r0\u0013H\u0086@\u00a2\u0006\u0002\u0010\u0015J\u000e\u0010\u0016\u001a\u00020\u000f2\u0006\u0010\f\u001a\u00020\rJ\u0010\u0010\u0017\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"Lcom/reminderapp/service/ReminderScheduler;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "advanceUniqueName", "", "reminderId", "", "days", "", "buildNotificationBody", "reminder", "Lcom/reminderapp/data/entity/ReminderEntity;", "cancel", "", "cancelAll", "ensureScheduled", "reminders", "", "rescheduleAll", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "schedule", "uniqueName", "Companion", "app_release"})
public final class ReminderScheduler {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG_REMINDER = "reminder_schedule";
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.service.ReminderScheduler.Companion Companion = null;
    
    public ReminderScheduler(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    /**
     * 为提醒注册 WorkManager 定时任务
     *
     * - 主提醒（D-day）始终排一次
     * - 日期类提醒（生日 / 节假日）：额外排「提前 N 天」的预告通知
     */
    public final void schedule(@org.jetbrains.annotations.NotNull()
    com.reminderapp.data.entity.ReminderEntity reminder) {
    }
    
    /**
     * 取消该提醒的所有待执行任务（主提醒 + 预告）
     */
    public final void cancel(long reminderId) {
    }
    
    private final java.lang.String uniqueName(long reminderId) {
        return null;
    }
    
    private final java.lang.String advanceUniqueName(long reminderId, int days) {
        return null;
    }
    
    /**
     * 取消所有提醒任务（谨慎使用）
     */
    public final void cancelAll() {
    }
    
    /**
     * 重新调度所有活跃提醒（开机后调用）
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object rescheduleAll(@org.jetbrains.annotations.NotNull()
    java.util.List<com.reminderapp.data.entity.ReminderEntity> reminders, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * 确保所有活跃提醒都有排期（App 每次启动调用）
     *
     * 与 [rescheduleAll] 的区别：不做全量取消，仅逐条 REPLACE 入队，
     * 因此不会打断正在等待的任务，可以安全地在每次冷启动时调用，
     * 用于修复「系统清理了 WorkManager 任务后提醒再也不响」。
     */
    public final void ensureScheduled(@org.jetbrains.annotations.NotNull()
    java.util.List<com.reminderapp.data.entity.ReminderEntity> reminders) {
    }
    
    private final java.lang.String buildNotificationBody(com.reminderapp.data.entity.ReminderEntity reminder) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/reminderapp/service/ReminderScheduler$Companion;", "", "()V", "TAG_REMINDER", "", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}