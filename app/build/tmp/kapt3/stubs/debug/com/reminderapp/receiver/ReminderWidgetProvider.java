package com.reminderapp.receiver;

/**
 * 桌面小组件：显示未处理提醒数 + 距离最近的提醒
 * 数据由 App 在提醒变化时通过 [refresh] 主动刷新
 *
 * 注意：onUpdate 跑在广播的主线程上，绝对不能 runBlocking 查 Room，
 * 否则数据量一大 / 磁盘一慢就直接 ANR。这里统一走 IO 线程 + goAsync()。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0015\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\u0018\u0000 \u00132\u00020\u0001:\u0002\u0013\u0014B\u0005\u00a2\u0006\u0002\u0010\u0002J(\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0002J\u0018\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\fH\u0002J\u0010\u0010\u000f\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0016J \u0010\u0010\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0011\u001a\u00020\b2\u0006\u0010\u0012\u001a\u00020\nH\u0016\u00a8\u0006\u0015"}, d2 = {"Lcom/reminderapp/receiver/ReminderWidgetProvider;", "Landroid/appwidget/AppWidgetProvider;", "()V", "applyData", "", "context", "Landroid/content/Context;", "manager", "Landroid/appwidget/AppWidgetManager;", "ids", "", "data", "Lcom/reminderapp/receiver/ReminderWidgetProvider$WidgetData;", "buildViews", "Landroid/widget/RemoteViews;", "onEnabled", "onUpdate", "appWidgetManager", "appWidgetIds", "Companion", "WidgetData", "app_debug"})
public final class ReminderWidgetProvider extends android.appwidget.AppWidgetProvider {
    
    /**
     * 小组件是进程级单例式的，用一个共享 scope 承载后台查询
     */
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.receiver.ReminderWidgetProvider.Companion Companion = null;
    
    public ReminderWidgetProvider() {
        super();
    }
    
    @java.lang.Override()
    public void onUpdate(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.appwidget.AppWidgetManager appWidgetManager, @org.jetbrains.annotations.NotNull()
    int[] appWidgetIds) {
    }
    
    @java.lang.Override()
    public void onEnabled(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    private final void applyData(android.content.Context context, android.appwidget.AppWidgetManager manager, int[] ids, com.reminderapp.receiver.ReminderWidgetProvider.WidgetData data) {
    }
    
    /**
     * 纯渲染，不碰数据库，可以安全跑在主线程
     */
    private final android.widget.RemoteViews buildViews(android.content.Context context, com.reminderapp.receiver.ReminderWidgetProvider.WidgetData data) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0002J\u0016\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0082@\u00a2\u0006\u0002\u0010\rJ\u000e\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u000b\u001a\u00020\fR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Lcom/reminderapp/receiver/ReminderWidgetProvider$Companion;", "", "()V", "scope", "Lkotlinx/coroutines/CoroutineScope;", "countdownText", "", "diffMillis", "", "loadData", "Lcom/reminderapp/receiver/ReminderWidgetProvider$WidgetData;", "context", "Landroid/content/Context;", "(Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "refresh", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        private final java.lang.Object loadData(android.content.Context context, kotlin.coroutines.Continuation<? super com.reminderapp.receiver.ReminderWidgetProvider.WidgetData> $completion) {
            return null;
        }
        
        /**
         * 生成倒计时文案：2小时15分后 / 3天2小时后 / 45天后
         */
        private final java.lang.String countdownText(long diffMillis) {
            return null;
        }
        
        /**
         * App 数据变化后调用，刷新所有已添加的小组件（异步，绝不阻塞调用方）
         */
        public final void refresh(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
        }
    }
    
    /**
     * 小组件要展示的最小数据集，在 IO 线程算好再回主线程渲染
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0012\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0082\b\u0018\u00002\u00020\u0001B3\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\b\u001a\u0004\u0018\u00010\t\u00a2\u0006\u0002\u0010\nJ\t\u0010\u0014\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010\u0016\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010\u0017\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u0010\u0010\u0018\u001a\u0004\u0018\u00010\tH\u00c6\u0003\u00a2\u0006\u0002\u0010\fJF\u0010\u0019\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\tH\u00c6\u0001\u00a2\u0006\u0002\u0010\u001aJ\u0013\u0010\u001b\u001a\u00020\u001c2\b\u0010\u001d\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001e\u001a\u00020\u0003H\u00d6\u0001J\t\u0010\u001f\u001a\u00020\u0005H\u00d6\u0001R\u0015\u0010\b\u001a\u0004\u0018\u00010\t\u00a2\u0006\n\n\u0002\u0010\r\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0013\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000fR\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000fR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013\u00a8\u0006 "}, d2 = {"Lcom/reminderapp/receiver/ReminderWidgetProvider$WidgetData;", "", "unhandledCount", "", "lunarText", "", "nextTitle", "nextTimeText", "completeReminderId", "", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Long;)V", "getCompleteReminderId", "()Ljava/lang/Long;", "Ljava/lang/Long;", "getLunarText", "()Ljava/lang/String;", "getNextTimeText", "getNextTitle", "getUnhandledCount", "()I", "component1", "component2", "component3", "component4", "component5", "copy", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Long;)Lcom/reminderapp/receiver/ReminderWidgetProvider$WidgetData;", "equals", "", "other", "hashCode", "toString", "app_debug"})
    static final class WidgetData {
        private final int unhandledCount = 0;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String lunarText = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.String nextTitle = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.String nextTimeText = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.Long completeReminderId = null;
        
        public WidgetData(int unhandledCount, @org.jetbrains.annotations.NotNull()
        java.lang.String lunarText, @org.jetbrains.annotations.Nullable()
        java.lang.String nextTitle, @org.jetbrains.annotations.Nullable()
        java.lang.String nextTimeText, @org.jetbrains.annotations.Nullable()
        java.lang.Long completeReminderId) {
            super();
        }
        
        public final int getUnhandledCount() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getLunarText() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getNextTitle() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getNextTimeText() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Long getCompleteReminderId() {
            return null;
        }
        
        public final int component1() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String component3() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String component4() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Long component5() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.reminderapp.receiver.ReminderWidgetProvider.WidgetData copy(int unhandledCount, @org.jetbrains.annotations.NotNull()
        java.lang.String lunarText, @org.jetbrains.annotations.Nullable()
        java.lang.String nextTitle, @org.jetbrains.annotations.Nullable()
        java.lang.String nextTimeText, @org.jetbrains.annotations.Nullable()
        java.lang.Long completeReminderId) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}