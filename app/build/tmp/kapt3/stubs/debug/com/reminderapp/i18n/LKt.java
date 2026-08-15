package com.reminderapp.i18n;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u001c\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0002\u0010\u0000\n\u0002\b\u0005\u001a\n\u0010\u0000\u001a\u0004\u0018\u00010\u0001H\u0002\u001a\u000e\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0003\u001a+\u0010\u0005\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00032\u0016\u0010\u0006\u001a\f\u0012\b\b\u0001\u0012\u0004\u0018\u00010\b0\u0007\"\u0004\u0018\u00010\b\u00a2\u0006\u0002\u0010\t\u001a\u0012\u0010\n\u001a\u00020\u0003*\u00020\u00012\u0006\u0010\u0004\u001a\u00020\u0003\u001a/\u0010\u000b\u001a\u00020\u0003*\u00020\u00012\u0006\u0010\u0004\u001a\u00020\u00032\u0016\u0010\u0006\u001a\f\u0012\b\b\u0001\u0012\u0004\u0018\u00010\b0\u0007\"\u0004\u0018\u00010\b\u00a2\u0006\u0002\u0010\f\u00a8\u0006\r"}, d2 = {"appContext", "Landroid/content/Context;", "zh", "", "src", "zhf", "args", "", "", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", "tr", "trf", "(Landroid/content/Context;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", "app_debug"})
public final class LKt {
    
    /**
     * 全局取多语言文案：默认返回简体中文原串（原串即 key）。
     * 依赖 ReminderApp.instance.applicationContext；未初始化时安全回退为原串，
     * 因此可在 Service / Receiver / 各类 helper 中任意处调用，无需 @Composable。
     */
    private static final android.content.Context appContext() {
        return null;
    }
    
    /**
     * 取多语言文案（无占位符）
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String zh(@org.jetbrains.annotations.NotNull()
    java.lang.String src) {
        return null;
    }
    
    /**
     * 取带占位符的多语言文案：src 须为格式串（如 "第%s周" / "%1\$s天%2\$s小时后"）
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String zhf(@org.jetbrains.annotations.NotNull()
    java.lang.String src, @org.jetbrains.annotations.NotNull()
    java.lang.Object... args) {
        return null;
    }
    
    /**
     * 已有 Context 时直接调用（内部复用全局实现）
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String tr(@org.jetbrains.annotations.NotNull()
    android.content.Context $this$tr, @org.jetbrains.annotations.NotNull()
    java.lang.String src) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String trf(@org.jetbrains.annotations.NotNull()
    android.content.Context $this$trf, @org.jetbrains.annotations.NotNull()
    java.lang.String src, @org.jetbrains.annotations.NotNull()
    java.lang.Object... args) {
        return null;
    }
}