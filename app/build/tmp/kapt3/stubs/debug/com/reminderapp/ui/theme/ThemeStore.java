package com.reminderapp.ui.theme;

/**
 * 手动主题（v2.1.1）：0=跟随系统 1=浅色 2=深色
 * 与 iOS ThemeStore 对齐；自签环境系统外观可能不可控，手动切换是最稳的兜底。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tJ\u0016\u0010\n\u001a\u00020\u000b2\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u0006\u001a\u00020\u0007R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/reminderapp/ui/theme/ThemeStore;", "", "()V", "KEY_MODE", "", "PREFS", "mode", "", "context", "Landroid/content/Context;", "setMode", "", "app_debug"})
public final class ThemeStore {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS = "theme_settings";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_MODE = "theme_mode";
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.ui.theme.ThemeStore INSTANCE = null;
    
    private ThemeStore() {
        super();
    }
    
    public final int mode(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return 0;
    }
    
    public final void setMode(@org.jetbrains.annotations.NotNull()
    android.content.Context context, int mode) {
    }
}