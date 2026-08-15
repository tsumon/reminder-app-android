package com.reminderapp.service;

/**
 * v2.2.0: AI 调用日志——记录最近 20 次对话（模型/provider/轮数/token/耗时/成败），
 * 供诊断页查看，面试可讲「AI 可观测性」。内存 + SharedPreferences 双份（重启不丢）。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001\u0011B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rJ\u000e\u0010\u000e\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bJ\u0014\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\r0\u00102\u0006\u0010\n\u001a\u00020\u000bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/reminderapp/service/AILogStore;", "", "()V", "KEY", "", "MAX", "", "PREFS", "add", "", "context", "Landroid/content/Context;", "entry", "Lcom/reminderapp/service/AILogStore$Entry;", "clear", "recent", "", "Entry", "app_release"})
public final class AILogStore {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS = "ai_log_store";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY = "entries";
    private static final int MAX = 20;
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.service.AILogStore INSTANCE = null;
    
    private AILogStore() {
        super();
    }
    
    public final void add(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.reminderapp.service.AILogStore.Entry entry) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.reminderapp.service.AILogStore.Entry> recent(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    public final void clear(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\"\b\u0086\b\u0018\u00002\u00020\u0001Be\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\b\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\b\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\f\u001a\u00020\r\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u000fJ\t\u0010\u001f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010 \u001a\u00020\u0005H\u00c6\u0003J\t\u0010!\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\"\u001a\u00020\bH\u00c6\u0003J\u0010\u0010#\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0011J\u0010\u0010$\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0011J\t\u0010%\u001a\u00020\u0003H\u00c6\u0003J\t\u0010&\u001a\u00020\rH\u00c6\u0003J\u000b\u0010\'\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003Jn\u0010(\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\b2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\b2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\b2\b\b\u0002\u0010\u000b\u001a\u00020\u00032\b\b\u0002\u0010\f\u001a\u00020\r2\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u0005H\u00c6\u0001\u00a2\u0006\u0002\u0010)J\u0013\u0010*\u001a\u00020\r2\b\u0010+\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010,\u001a\u00020\bH\u00d6\u0001J\u0006\u0010-\u001a\u00020\u0005J\t\u0010.\u001a\u00020\u0005H\u00d6\u0001R\u0015\u0010\n\u001a\u0004\u0018\u00010\b\u00a2\u0006\n\n\u0002\u0010\u0012\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\u000b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0013\u0010\u000e\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0016R\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0015\u0010\t\u001a\u0004\u0018\u00010\b\u00a2\u0006\n\n\u0002\u0010\u0012\u001a\u0004\b\u001a\u0010\u0011R\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0016R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0014R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001e\u00a8\u0006/"}, d2 = {"Lcom/reminderapp/service/AILogStore$Entry;", "", "time", "", "model", "", "provider", "turns", "", "promptTokens", "completionTokens", "durationMs", "ok", "", "error", "(JLjava/lang/String;Ljava/lang/String;ILjava/lang/Integer;Ljava/lang/Integer;JZLjava/lang/String;)V", "getCompletionTokens", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getDurationMs", "()J", "getError", "()Ljava/lang/String;", "getModel", "getOk", "()Z", "getPromptTokens", "getProvider", "getTime", "getTurns", "()I", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(JLjava/lang/String;Ljava/lang/String;ILjava/lang/Integer;Ljava/lang/Integer;JZLjava/lang/String;)Lcom/reminderapp/service/AILogStore$Entry;", "equals", "other", "hashCode", "timeText", "toString", "app_release"})
    public static final class Entry {
        private final long time = 0L;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String model = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String provider = null;
        private final int turns = 0;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.Integer promptTokens = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.Integer completionTokens = null;
        private final long durationMs = 0L;
        private final boolean ok = false;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.String error = null;
        
        public Entry(long time, @org.jetbrains.annotations.NotNull()
        java.lang.String model, @org.jetbrains.annotations.NotNull()
        java.lang.String provider, int turns, @org.jetbrains.annotations.Nullable()
        java.lang.Integer promptTokens, @org.jetbrains.annotations.Nullable()
        java.lang.Integer completionTokens, long durationMs, boolean ok, @org.jetbrains.annotations.Nullable()
        java.lang.String error) {
            super();
        }
        
        public final long getTime() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getModel() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getProvider() {
            return null;
        }
        
        public final int getTurns() {
            return 0;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Integer getPromptTokens() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Integer getCompletionTokens() {
            return null;
        }
        
        public final long getDurationMs() {
            return 0L;
        }
        
        public final boolean getOk() {
            return false;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getError() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String timeText() {
            return null;
        }
        
        public Entry() {
            super();
        }
        
        public final long component1() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component3() {
            return null;
        }
        
        public final int component4() {
            return 0;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Integer component5() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Integer component6() {
            return null;
        }
        
        public final long component7() {
            return 0L;
        }
        
        public final boolean component8() {
            return false;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String component9() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.reminderapp.service.AILogStore.Entry copy(long time, @org.jetbrains.annotations.NotNull()
        java.lang.String model, @org.jetbrains.annotations.NotNull()
        java.lang.String provider, int turns, @org.jetbrains.annotations.Nullable()
        java.lang.Integer promptTokens, @org.jetbrains.annotations.Nullable()
        java.lang.Integer completionTokens, long durationMs, boolean ok, @org.jetbrains.annotations.Nullable()
        java.lang.String error) {
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