package com.reminderapp.service;

/**
 * 自然语言日期/时间解析（本地、无需 API）
 *
 * 支持示例：
 * - 今天 / 明天 / 后天 / 大后天
 * - 周一 / 下周一 / 下周日（周X/星期X/礼拜X）
 * - 每月15号 / 每月5日
 * - 9月5号 / 1月14日（公历生日）
 * - 农历八月初五 / 旧历8月11
 * - 每天 / 每周 / 每月 / 每年
 * - 明早9点 / 下午3点 / 晚上8点半 / 09:30
 * - 3天后 / 2小时后 / 30分钟后 / 2周后
 *
 * 返回结构化的下次触发时间与重复模式。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001\u0011B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0007\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\tH\u0002J\u0010\u0010\n\u001a\u00020\u00052\u0006\u0010\u000b\u001a\u00020\u0005H\u0002J\u001a\u0010\f\u001a\u0004\u0018\u00010\r2\u0006\u0010\u000e\u001a\u00020\u00052\b\b\u0002\u0010\u000f\u001a\u00020\u0010R\u001a\u0010\u0003\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00060\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/reminderapp/service/NaturalDateParser;", "", "()V", "weekdayMap", "", "", "", "dowFromCalendar", "cal", "Ljava/util/Calendar;", "extractTitle", "text", "parse", "Lcom/reminderapp/service/NaturalDateParser$ParsedSchedule;", "input", "now", "", "ParsedSchedule", "app_release"})
public final class NaturalDateParser {
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Map<java.lang.String, java.lang.Integer> weekdayMap = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.service.NaturalDateParser INSTANCE = null;
    
    private NaturalDateParser() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.reminderapp.service.NaturalDateParser.ParsedSchedule parse(@org.jetbrains.annotations.NotNull()
    java.lang.String input, long now) {
        return null;
    }
    
    private final int dowFromCalendar(java.util.Calendar cal) {
        return 0;
    }
    
    private final java.lang.String extractTitle(java.lang.String text) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0019\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001BC\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\b\u0012\b\u0010\t\u001a\u0004\u0018\u00010\b\u0012\u0006\u0010\n\u001a\u00020\u0005\u0012\u0006\u0010\u000b\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\fJ\t\u0010\u0018\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010\u001a\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u0010\u0010\u001b\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0014J\u0010\u0010\u001c\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0014J\t\u0010\u001d\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\u0005H\u00c6\u0003JZ\u0010\u001f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\b2\b\b\u0002\u0010\n\u001a\u00020\u00052\b\b\u0002\u0010\u000b\u001a\u00020\u0005H\u00c6\u0001\u00a2\u0006\u0002\u0010 J\u0013\u0010!\u001a\u00020\"2\b\u0010#\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010$\u001a\u00020\bH\u00d6\u0001J\t\u0010%\u001a\u00020\u0005H\u00d6\u0001R\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u000b\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u000eR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u000eR\u0015\u0010\t\u001a\u0004\u0018\u00010\b\u00a2\u0006\n\n\u0002\u0010\u0015\u001a\u0004\b\u0013\u0010\u0014R\u0015\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\n\n\u0002\u0010\u0015\u001a\u0004\b\u0016\u0010\u0014R\u0011\u0010\n\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u000e\u00a8\u0006&"}, d2 = {"Lcom/reminderapp/service/NaturalDateParser$ParsedSchedule;", "", "nextTriggerAt", "", "repeatMode", "", "dateType", "targetMonth", "", "targetDay", "title", "label", "(JLjava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;)V", "getDateType", "()Ljava/lang/String;", "getLabel", "getNextTriggerAt", "()J", "getRepeatMode", "getTargetDay", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getTargetMonth", "getTitle", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "(JLjava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;)Lcom/reminderapp/service/NaturalDateParser$ParsedSchedule;", "equals", "", "other", "hashCode", "toString", "app_release"})
    public static final class ParsedSchedule {
        private final long nextTriggerAt = 0L;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String repeatMode = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.String dateType = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.Integer targetMonth = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.Integer targetDay = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String title = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String label = null;
        
        public ParsedSchedule(long nextTriggerAt, @org.jetbrains.annotations.NotNull()
        java.lang.String repeatMode, @org.jetbrains.annotations.Nullable()
        java.lang.String dateType, @org.jetbrains.annotations.Nullable()
        java.lang.Integer targetMonth, @org.jetbrains.annotations.Nullable()
        java.lang.Integer targetDay, @org.jetbrains.annotations.NotNull()
        java.lang.String title, @org.jetbrains.annotations.NotNull()
        java.lang.String label) {
            super();
        }
        
        public final long getNextTriggerAt() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getRepeatMode() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getDateType() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Integer getTargetMonth() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Integer getTargetDay() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getTitle() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getLabel() {
            return null;
        }
        
        public final long component1() {
            return 0L;
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
        public final java.lang.Integer component4() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Integer component5() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component6() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component7() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.reminderapp.service.NaturalDateParser.ParsedSchedule copy(long nextTriggerAt, @org.jetbrains.annotations.NotNull()
        java.lang.String repeatMode, @org.jetbrains.annotations.Nullable()
        java.lang.String dateType, @org.jetbrains.annotations.Nullable()
        java.lang.Integer targetMonth, @org.jetbrains.annotations.Nullable()
        java.lang.Integer targetDay, @org.jetbrains.annotations.NotNull()
        java.lang.String title, @org.jetbrains.annotations.NotNull()
        java.lang.String label) {
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