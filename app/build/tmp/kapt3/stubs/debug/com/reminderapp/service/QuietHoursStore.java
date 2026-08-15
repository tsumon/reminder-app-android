package com.reminderapp.service;

/**
 * 勿扰时段（v2.1.1）：单条提醒的每日静默窗口。
 * 存 SharedPreferences（按提醒本地 id 索引）——与 iOS QuietHoursStore 语义一致，
 * 避免改 DB schema 引发 migration 风险。
 * 语义：start 到 end（分钟，0..1439）之间不弹通知；end < start 表示跨天窗口（如 22:00–08:00）。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0005\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u00062\u0006\u0010\n\u001a\u00020\u0006J\u001d\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\rJ\u0016\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0006J\u0018\u0010\u0010\u001a\u00020\u00042\u0006\u0010\t\u001a\u00020\u00062\u0006\u0010\u0011\u001a\u00020\u0004H\u0002J/\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u00062\b\u0010\u0014\u001a\u0004\u0018\u00010\f2\b\u0010\u0015\u001a\u0004\u0018\u00010\f\u00a2\u0006\u0002\u0010\u0016J\u001d\u0010\u0017\u001a\u0004\u0018\u00010\f2\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\rR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0018"}, d2 = {"Lcom/reminderapp/service/QuietHoursStore;", "", "()V", "PREFS", "", "adjust", "", "context", "Landroid/content/Context;", "id", "timestamp", "endMinute", "", "(Landroid/content/Context;J)Ljava/lang/Integer;", "isEnabled", "", "key", "suffix", "set", "", "start", "end", "(Landroid/content/Context;JLjava/lang/Integer;Ljava/lang/Integer;)V", "startMinute", "app_debug"})
public final class QuietHoursStore {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS = "quiet_hours_settings";
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.service.QuietHoursStore INSTANCE = null;
    
    private QuietHoursStore() {
        super();
    }
    
    private final java.lang.String key(long id, java.lang.String suffix) {
        return null;
    }
    
    public final boolean isEnabled(@org.jetbrains.annotations.NotNull()
    android.content.Context context, long id) {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer startMinute(@org.jetbrains.annotations.NotNull()
    android.content.Context context, long id) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer endMinute(@org.jetbrains.annotations.NotNull()
    android.content.Context context, long id) {
        return null;
    }
    
    /**
     * 设置勿扰窗口；传 null 关闭
     */
    public final void set(@org.jetbrains.annotations.NotNull()
    android.content.Context context, long id, @org.jetbrains.annotations.Nullable()
    java.lang.Integer start, @org.jetbrains.annotations.Nullable()
    java.lang.Integer end) {
    }
    
    /**
     * 时间落在窗口内时顺延到窗口结束（未启用/不在窗口内原样返回）。
     * 供 ReminderWorker 到点执行时检查：在窗口内则不弹、重排到窗口结束。
     */
    public final long adjust(@org.jetbrains.annotations.NotNull()
    android.content.Context context, long id, long timestamp) {
        return 0L;
    }
}