package com.reminderapp;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0004\u0018\u0000 \u001c2\u00020\u0001:\u0001\u001cB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0018\u001a\u00020\u0019H\u0016J\b\u0010\u001a\u001a\u00020\u0019H\u0002J\b\u0010\u001b\u001a\u00020\u0019H\u0002R\u001e\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0003\u001a\u00020\u0004@BX\u0086.\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u001e\u0010\t\u001a\u00020\b2\u0006\u0010\u0003\u001a\u00020\b@BX\u0086.\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u001e\u0010\r\u001a\u00020\f2\u0006\u0010\u0003\u001a\u00020\f@BX\u0086.\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u001e\u0010\u0011\u001a\u00020\u00102\u0006\u0010\u0003\u001a\u00020\u0010@BX\u0086.\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u001e\u0010\u0015\u001a\u00020\u00142\u0006\u0010\u0003\u001a\u00020\u0014@BX\u0086.\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017\u00a8\u0006\u001d"}, d2 = {"Lcom/reminderapp/ReminderApp;", "Landroid/app/Application;", "()V", "<set-?>", "Lcom/reminderapp/service/AIService;", "aiService", "getAiService", "()Lcom/reminderapp/service/AIService;", "Lcom/reminderapp/service/AISettings;", "aiSettings", "getAiSettings", "()Lcom/reminderapp/service/AISettings;", "Lcom/reminderapp/data/database/AppDatabase;", "database", "getDatabase", "()Lcom/reminderapp/data/database/AppDatabase;", "Lcom/reminderapp/service/NotificationManager;", "notificationManager", "getNotificationManager", "()Lcom/reminderapp/service/NotificationManager;", "Lcom/reminderapp/service/ReminderScheduler;", "scheduler", "getScheduler", "()Lcom/reminderapp/service/ReminderScheduler;", "onCreate", "", "refreshRemoteHolidays", "scheduleWeeklyReport", "Companion", "app_release"})
public final class ReminderApp extends android.app.Application {
    private com.reminderapp.data.database.AppDatabase database;
    private com.reminderapp.service.ReminderScheduler scheduler;
    private com.reminderapp.service.NotificationManager notificationManager;
    private com.reminderapp.service.AIService aiService;
    private com.reminderapp.service.AISettings aiSettings;
    private static com.reminderapp.ReminderApp instance;
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.ReminderApp.Companion Companion = null;
    
    public ReminderApp() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.data.database.AppDatabase getDatabase() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.service.ReminderScheduler getScheduler() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.service.NotificationManager getNotificationManager() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.service.AIService getAiService() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.service.AISettings getAiSettings() {
        return null;
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    /**
     * 批次2 功能3: 统计周报 —— 每天跑一次的周期任务，首次对齐下一个 20:00。
     *
     * v2.0.21 修 G1：原为 7 天周期，一旦周日 20:00 的那次被 Doze/省电推迟到周一，
     * Worker 里「非周日直接 return」就让本周周报永久漏发。改为每天触发，
     * 由 WeeklyReportWorker 自行判定「该发哪一周」（周日发本周、周一~周三补发上一周）。
     */
    private final void scheduleWeeklyReport() {
    }
    
    private final void refreshRemoteHolidays() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u001e\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0003\u001a\u00020\u0004@BX\u0086.\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\b"}, d2 = {"Lcom/reminderapp/ReminderApp$Companion;", "", "()V", "<set-?>", "Lcom/reminderapp/ReminderApp;", "instance", "getInstance", "()Lcom/reminderapp/ReminderApp;", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.reminderapp.ReminderApp getInstance() {
            return null;
        }
    }
}