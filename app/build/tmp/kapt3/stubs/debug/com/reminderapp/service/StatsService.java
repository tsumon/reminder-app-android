package com.reminderapp.service;

/**
 * 统计洞察聚合（v1.8.7 任务③）— 镜像 iOS StatsService.swift
 *
 * 数据完全本地：复用 ReminderRecordEntity（操作记录）按日聚合。
 * 口径（双端一致，v2.0.17 落文档防漂移）：
 * - 完成率 = 确认天数 / (确认天数 + 漏掉天数)
 * - 「漏掉」= 到点未确认进入重试才算（Worker escalate 写 notified / iOS escalateRetry 写 trigger）；
 *  按时确认**不**记漏 —— 否则每次发通知都记一条，完成率恒 ≈50%
 * - 连续打卡 = confirmed 记录按天去重后的连续天数（当前/最长）
 * - 最常忘记时段 = notified(未确认) 记录按小时分布 Top3
 * - 月历热力图 = 每月每天 confirmed 次数
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001\u000fB\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\"\u0010\u0003\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050\u00042\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007H\u0002J\u0010\u0010\t\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\bH\u0002J\u0014\u0010\u000b\u001a\u00020\f2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0007\u00a8\u0006\u0010"}, d2 = {"Lcom/reminderapp/service/StatsService;", "", "()V", "calcStreak", "Lkotlin/Pair;", "", "sortedDays", "", "", "startOfDay", "ts", "summarize", "Lcom/reminderapp/service/StatsService$Summary;", "records", "Lcom/reminderapp/data/entity/ReminderRecordEntity;", "Summary", "app_debug"})
public final class StatsService {
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.service.StatsService INSTANCE = null;
    
    private StatsService() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.service.StatsService.Summary summarize(@org.jetbrains.annotations.NotNull()
    java.util.List<com.reminderapp.data.entity.ReminderRecordEntity> records) {
        return null;
    }
    
    /**
     * 返回 (当前连续, 最长连续)
     */
    private final kotlin.Pair<java.lang.Integer, java.lang.Integer> calcStreak(java.util.List<java.lang.Long> sortedDays) {
        return null;
    }
    
    private final long startOfDay(long ts) {
        return 0L;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\b\u0017\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B]\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u0012\u0006\u0010\u0007\u001a\u00020\u0003\u0012\u0006\u0010\b\u001a\u00020\u0003\u0012\u0018\u0010\t\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00030\u000b0\n\u0012\u0012\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00030\r\u00a2\u0006\u0002\u0010\u000fJ\t\u0010\u001c\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001d\u001a\u00020\u0003H\u00c6\u0003J\u0010\u0010\u001e\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0011J\t\u0010\u001f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010 \u001a\u00020\u0003H\u00c6\u0003J\u001b\u0010!\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00030\u000b0\nH\u00c6\u0003J\u0015\u0010\"\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00030\rH\u00c6\u0003Jt\u0010#\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\u00032\u001a\b\u0002\u0010\t\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00030\u000b0\n2\u0014\b\u0002\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00030\rH\u00c6\u0001\u00a2\u0006\u0002\u0010$J\u0013\u0010%\u001a\u00020&2\b\u0010\'\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010(\u001a\u00020\u0003H\u00d6\u0001J\t\u0010)\u001a\u00020\u000eH\u00d6\u0001R\u0015\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\n\n\u0002\u0010\u0012\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0014R#\u0010\t\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00030\u000b0\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u001d\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00030\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0014R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0014\u00a8\u0006*"}, d2 = {"Lcom/reminderapp/service/StatsService$Summary;", "", "confirmCount", "", "missedCount", "completionRate", "", "currentStreak", "longestStreak", "forgetHours", "", "Lkotlin/Pair;", "heatmap", "", "", "(IILjava/lang/Double;IILjava/util/List;Ljava/util/Map;)V", "getCompletionRate", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getConfirmCount", "()I", "getCurrentStreak", "getForgetHours", "()Ljava/util/List;", "getHeatmap", "()Ljava/util/Map;", "getLongestStreak", "getMissedCount", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "(IILjava/lang/Double;IILjava/util/List;Ljava/util/Map;)Lcom/reminderapp/service/StatsService$Summary;", "equals", "", "other", "hashCode", "toString", "app_debug"})
    public static final class Summary {
        private final int confirmCount = 0;
        private final int missedCount = 0;
        
        /**
         * 完成率 0-1；无数据时为 null
         */
        @org.jetbrains.annotations.Nullable()
        private final java.lang.Double completionRate = null;
        private final int currentStreak = 0;
        private final int longestStreak = 0;
        
        /**
         * 最常忘记的时段（hour 0-23 降序，最多 3 条）
         */
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<kotlin.Pair<java.lang.Integer, java.lang.Integer>> forgetHours = null;
        
        /**
         * 月历热力图："yyyy-MM-dd" -> confirmed 次数
         */
        @org.jetbrains.annotations.NotNull()
        private final java.util.Map<java.lang.String, java.lang.Integer> heatmap = null;
        
        public Summary(int confirmCount, int missedCount, @org.jetbrains.annotations.Nullable()
        java.lang.Double completionRate, int currentStreak, int longestStreak, @org.jetbrains.annotations.NotNull()
        java.util.List<kotlin.Pair<java.lang.Integer, java.lang.Integer>> forgetHours, @org.jetbrains.annotations.NotNull()
        java.util.Map<java.lang.String, java.lang.Integer> heatmap) {
            super();
        }
        
        public final int getConfirmCount() {
            return 0;
        }
        
        public final int getMissedCount() {
            return 0;
        }
        
        /**
         * 完成率 0-1；无数据时为 null
         */
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Double getCompletionRate() {
            return null;
        }
        
        public final int getCurrentStreak() {
            return 0;
        }
        
        public final int getLongestStreak() {
            return 0;
        }
        
        /**
         * 最常忘记的时段（hour 0-23 降序，最多 3 条）
         */
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<kotlin.Pair<java.lang.Integer, java.lang.Integer>> getForgetHours() {
            return null;
        }
        
        /**
         * 月历热力图："yyyy-MM-dd" -> confirmed 次数
         */
        @org.jetbrains.annotations.NotNull()
        public final java.util.Map<java.lang.String, java.lang.Integer> getHeatmap() {
            return null;
        }
        
        public final int component1() {
            return 0;
        }
        
        public final int component2() {
            return 0;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Double component3() {
            return null;
        }
        
        public final int component4() {
            return 0;
        }
        
        public final int component5() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<kotlin.Pair<java.lang.Integer, java.lang.Integer>> component6() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.Map<java.lang.String, java.lang.Integer> component7() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.reminderapp.service.StatsService.Summary copy(int confirmCount, int missedCount, @org.jetbrains.annotations.Nullable()
        java.lang.Double completionRate, int currentStreak, int longestStreak, @org.jetbrains.annotations.NotNull()
        java.util.List<kotlin.Pair<java.lang.Integer, java.lang.Integer>> forgetHours, @org.jetbrains.annotations.NotNull()
        java.util.Map<java.lang.String, java.lang.Integer> heatmap) {
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