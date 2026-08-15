package com.reminderapp.service;

/**
 * 崩溃监控 + 埋点基础设施（v1.8.7 任务⑥）
 *
 * 先做本地基础设施，后续填 Bugly/AppCenter 的 AppID 即可启用真实上报：
 * - 崩溃捕获：Thread.setDefaultUncaughtExceptionHandler → 写崩溃日志文件
 * - 埋点事件：事件日志 JSON Lines 写本地文件（filesDir/telemetry/events.jsonl）
 * - 可插拔上报接口：CrashReporter / EventReporter，默认本地文件实现，
 *  未来替换为 Bugly/AppCenter 实现即可（无需改业务代码）
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010$\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0003\u001f !B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0012H\u0002J\u0006\u0010\u0014\u001a\u00020\u0015J$\u0010\u0016\u001a\u00020\u00152\u0006\u0010\u0017\u001a\u00020\u00122\u0014\b\u0002\u0010\u0018\u001a\u000e\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020\u00120\u0019J\b\u0010\u001a\u001a\u00020\u0012H\u0002J\u0018\u0010\u001b\u001a\u00020\u00152\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u0012H\u0002R\u001a\u0010\u0003\u001a\u00020\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001a\u0010\t\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\""}, d2 = {"Lcom/reminderapp/service/TelemetryService;", "", "()V", "crashReporter", "Lcom/reminderapp/service/TelemetryService$CrashReporter;", "getCrashReporter", "()Lcom/reminderapp/service/TelemetryService$CrashReporter;", "setCrashReporter", "(Lcom/reminderapp/service/TelemetryService$CrashReporter;)V", "eventReporter", "Lcom/reminderapp/service/TelemetryService$EventReporter;", "getEventReporter", "()Lcom/reminderapp/service/TelemetryService$EventReporter;", "setEventReporter", "(Lcom/reminderapp/service/TelemetryService$EventReporter;)V", "installed", "", "escape", "", "s", "install", "", "logEvent", "name", "params", "", "now", "writeLine", "file", "Ljava/io/File;", "line", "CrashReporter", "EventReporter", "LocalFileReporter", "app_release"})
public final class TelemetryService {
    
    /**
     * 上报器（默认本地文件；接入 Bugly/AppCenter 时替换为云实现）
     */
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.NotNull()
    private static volatile com.reminderapp.service.TelemetryService.CrashReporter crashReporter;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.NotNull()
    private static volatile com.reminderapp.service.TelemetryService.EventReporter eventReporter;
    private static boolean installed = false;
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.service.TelemetryService INSTANCE = null;
    
    private TelemetryService() {
        super();
    }
    
    /**
     * 上报器（默认本地文件；接入 Bugly/AppCenter 时替换为云实现）
     */
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.service.TelemetryService.CrashReporter getCrashReporter() {
        return null;
    }
    
    /**
     * 上报器（默认本地文件；接入 Bugly/AppCenter 时替换为云实现）
     */
    public final void setCrashReporter(@org.jetbrains.annotations.NotNull()
    com.reminderapp.service.TelemetryService.CrashReporter p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.service.TelemetryService.EventReporter getEventReporter() {
        return null;
    }
    
    public final void setEventReporter(@org.jetbrains.annotations.NotNull()
    com.reminderapp.service.TelemetryService.EventReporter p0) {
    }
    
    /**
     * 在 Application.onCreate 调用一次
     */
    @kotlin.jvm.Synchronized()
    public final synchronized void install() {
    }
    
    /**
     * 记录一条埋点事件（业务代码调用，如 confirm / snooze / reminder_created）
     */
    public final void logEvent(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> params) {
    }
    
    private final java.lang.String now() {
        return null;
    }
    
    private final void writeLine(java.io.File file, java.lang.String line) {
    }
    
    /**
     * JSON 字符串转义：除 \ " 外还要处理 \t \b \f 及全部控制字符，否则 crash 日志是非法 JSON
     */
    private final java.lang.String escape(java.lang.String s) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0003\n\u0000\bf\u0018\u00002\u00020\u0001J\u0018\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H&\u00a8\u0006\b"}, d2 = {"Lcom/reminderapp/service/TelemetryService$CrashReporter;", "", "reportCrash", "", "thread", "Ljava/lang/Thread;", "throwable", "", "app_release"})
    public static abstract interface CrashReporter {
        
        /**
         * 上报一次崩溃；返回 false 表示未消费（由默认处理器兜底崩溃流程）
         */
        public abstract boolean reportCrash(@org.jetbrains.annotations.NotNull()
        java.lang.Thread thread, @org.jetbrains.annotations.NotNull()
        java.lang.Throwable throwable);
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010$\n\u0000\bf\u0018\u00002\u00020\u0001J$\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0012\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050\u0007H&\u00a8\u0006\b"}, d2 = {"Lcom/reminderapp/service/TelemetryService$EventReporter;", "", "reportEvent", "", "name", "", "params", "", "app_release"})
    public static abstract interface EventReporter {
        
        /**
         * 上报一条埋点事件
         */
        public abstract void reportEvent(@org.jetbrains.annotations.NotNull()
        java.lang.String name, @org.jetbrains.annotations.NotNull()
        java.util.Map<java.lang.String, java.lang.String> params);
    }
    
    /**
     * 默认实现：写本地日志文件（不上传）
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0003\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010$\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u00012\u00020\u0002B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\b\u0010\u0004\u001a\u00020\u0005H\u0002J\b\u0010\u0006\u001a\u00020\u0005H\u0002J\u0018\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0016J$\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0012\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00100\u0012H\u0016\u00a8\u0006\u0013"}, d2 = {"Lcom/reminderapp/service/TelemetryService$LocalFileReporter;", "Lcom/reminderapp/service/TelemetryService$CrashReporter;", "Lcom/reminderapp/service/TelemetryService$EventReporter;", "()V", "crashFile", "Ljava/io/File;", "eventFile", "reportCrash", "", "thread", "Ljava/lang/Thread;", "throwable", "", "reportEvent", "", "name", "", "params", "", "app_release"})
    public static final class LocalFileReporter implements com.reminderapp.service.TelemetryService.CrashReporter, com.reminderapp.service.TelemetryService.EventReporter {
        @org.jetbrains.annotations.NotNull()
        public static final com.reminderapp.service.TelemetryService.LocalFileReporter INSTANCE = null;
        
        private LocalFileReporter() {
            super();
        }
        
        @java.lang.Override()
        public boolean reportCrash(@org.jetbrains.annotations.NotNull()
        java.lang.Thread thread, @org.jetbrains.annotations.NotNull()
        java.lang.Throwable throwable) {
            return false;
        }
        
        @java.lang.Override()
        public void reportEvent(@org.jetbrains.annotations.NotNull()
        java.lang.String name, @org.jetbrains.annotations.NotNull()
        java.util.Map<java.lang.String, java.lang.String> params) {
        }
        
        private final java.io.File crashFile() {
            return null;
        }
        
        private final java.io.File eventFile() {
            return null;
        }
    }
}