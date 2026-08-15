package com.reminderapp.i18n;

/**
 * 手动语言切换（v2.0.4 新增）
 *
 * 偏好存 SharedPreferences：code = system/zh/en/zh-rTW/ja/ko（system = 跟随系统）。
 * 资源目录对应：默认 values(简中) / values-en / values-ja / values-ko / values-zh-rTW(繁中)。
 *
 * 生效机制：
 * - Application.onCreate 调 [apply]：Locale.setDefault + 预热文案 Context；
 * - Activity.attachBaseContext 调 [wrap]：整棵 Activity 资源换语言；
 * - L.kt 的 zh()/zhf() 经 [wrap] 取文案 Context，保证非 Composable 全局生效。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\bJ\u000e\u0010\u0010\u001a\u00020\u00042\u0006\u0010\u0011\u001a\u00020\bJ\u000e\u0010\u0012\u001a\u00020\u00042\u0006\u0010\u0013\u001a\u00020\u0004J\u000e\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0011\u001a\u00020\bJ\u0016\u0010\u0016\u001a\u00020\u000e2\u0006\u0010\u0011\u001a\u00020\b2\u0006\u0010\u0013\u001a\u00020\u0004J\u000e\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0013\u001a\u00020\u0004J\u000e\u0010\u0019\u001a\u00020\b2\u0006\u0010\u001a\u001a\u00020\bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00040\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\f\u00a8\u0006\u001b"}, d2 = {"Lcom/reminderapp/i18n/LocaleManager;", "", "()V", "KEY_LANGUAGE", "", "PREFS", "cachedCode", "cachedContext", "Landroid/content/Context;", "options", "", "getOptions", "()Ljava/util/List;", "apply", "", "app", "currentCode", "context", "displayName", "code", "isOverride", "", "setLanguage", "toLocale", "Ljava/util/Locale;", "wrap", "base", "app_release"})
public final class LocaleManager {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS = "locale_prefs";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_LANGUAGE = "app_language";
    
    /**
     * 可选项：跟随系统/简体中文/English/繁體中文/日本語/한국어
     */
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<java.lang.String> options = null;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile java.lang.String cachedCode;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile android.content.Context cachedContext;
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.i18n.LocaleManager INSTANCE = null;
    
    private LocaleManager() {
        super();
    }
    
    /**
     * 可选项：跟随系统/简体中文/English/繁體中文/日本語/한국어
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getOptions() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String currentCode(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    public final boolean isOverride(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    /**
     * 偏好代码 → Locale；简中用 Locale("zh")（匹配默认 values），繁中用 zh-rTW
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.Locale toLocale(@org.jetbrains.annotations.NotNull()
    java.lang.String code) {
        return null;
    }
    
    /**
     * 语言选项显示名：语言名用各自语言自称，不参与本地化
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String displayName(@org.jetbrains.annotations.NotNull()
    java.lang.String code) {
        return null;
    }
    
    /**
     * 取带目标 Locale 的 Context；跟随系统时原样返回
     */
    @org.jetbrains.annotations.NotNull()
    public final android.content.Context wrap(@org.jetbrains.annotations.NotNull()
    android.content.Context base) {
        return null;
    }
    
    /**
     * Application.onCreate 调用：默认 Locale + 预热文案 Context
     */
    public final void apply(@org.jetbrains.annotations.NotNull()
    android.content.Context app) {
    }
    
    /**
     * 设置语言：保存偏好并重建当前 Activity（attachBaseContext 会重新 wrap）
     */
    public final void setLanguage(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String code) {
    }
}