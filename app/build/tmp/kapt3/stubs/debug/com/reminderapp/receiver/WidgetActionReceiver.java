package com.reminderapp.receiver;

/**
 * 桌面小组件快捷按钮动作接收器（v1.8.7 小组件增强）
 *
 * 点击小组件上的完成/稍后按钮 → 广播到这里 → 确认该提醒并重排下一次，
 * 与通知栏「确认」逻辑一致，最后刷新小组件。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u0000 \u000b2\u00020\u0001:\u0001\u000bB\u0005\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0002J\u0018\u0010\t\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0002J\u0018\u0010\n\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0016\u00a8\u0006\f"}, d2 = {"Lcom/reminderapp/receiver/WidgetActionReceiver;", "Landroid/content/BroadcastReceiver;", "()V", "handleComplete", "", "context", "Landroid/content/Context;", "intent", "Landroid/content/Intent;", "handleSnooze", "onReceive", "Companion", "app_debug"})
public final class WidgetActionReceiver extends android.content.BroadcastReceiver {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_COMPLETE = "com.reminderapp.widget.ACTION_COMPLETE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_SNOOZE = "com.reminderapp.widget.ACTION_SNOOZE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_REMINDER_ID = "reminder_id";
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.receiver.WidgetActionReceiver.Companion Companion = null;
    
    public WidgetActionReceiver() {
        super();
    }
    
    @java.lang.Override()
    public void onReceive(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.content.Intent intent) {
    }
    
    private final void handleComplete(android.content.Context context, android.content.Intent intent) {
    }
    
    private final void handleSnooze(android.content.Context context, android.content.Intent intent) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/reminderapp/receiver/WidgetActionReceiver$Companion;", "", "()V", "ACTION_COMPLETE", "", "ACTION_SNOOZE", "EXTRA_REMINDER_ID", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}