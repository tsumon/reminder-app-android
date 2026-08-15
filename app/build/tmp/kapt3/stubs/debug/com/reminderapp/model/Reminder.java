package com.reminderapp.model;

/**
 * 前端展示用的提醒模型（从 Entity 映射）
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b=\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B\u00bb\u0001\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\b\u0010\n\u001a\u0004\u0018\u00010\u000b\u0012\b\u0010\f\u001a\u0004\u0018\u00010\t\u0012\b\u0010\r\u001a\u0004\u0018\u00010\t\u0012\b\u0010\u000e\u001a\u0004\u0018\u00010\u000f\u0012\u0006\u0010\u0010\u001a\u00020\t\u0012\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0012\u0012\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\t\u0012\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\t\u0012\u0006\u0010\u0015\u001a\u00020\u000f\u0012\u0006\u0010\u0016\u001a\u00020\u000f\u0012\u0006\u0010\u0017\u001a\u00020\u0018\u0012\u0006\u0010\u0019\u001a\u00020\u0003\u0012\u0006\u0010\u001a\u001a\u00020\u0003\u0012\b\u0010\u001b\u001a\u0004\u0018\u00010\u0003\u0012\u0006\u0010\u001c\u001a\u00020\t\u0012\u0006\u0010\u001d\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u001eJ\t\u0010?\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010@\u001a\u0004\u0018\u00010\u0012H\u00c6\u0003J\u0010\u0010A\u001a\u0004\u0018\u00010\tH\u00c6\u0003\u00a2\u0006\u0002\u00107J\u0010\u0010B\u001a\u0004\u0018\u00010\tH\u00c6\u0003\u00a2\u0006\u0002\u00107J\t\u0010C\u001a\u00020\u000fH\u00c6\u0003J\t\u0010D\u001a\u00020\u000fH\u00c6\u0003J\t\u0010E\u001a\u00020\u0018H\u00c6\u0003J\t\u0010F\u001a\u00020\u0003H\u00c6\u0003J\t\u0010G\u001a\u00020\u0003H\u00c6\u0003J\u0010\u0010H\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010/J\t\u0010I\u001a\u00020\tH\u00c6\u0003J\t\u0010J\u001a\u00020\u0005H\u00c6\u0003J\t\u0010K\u001a\u00020\u0003H\u00c6\u0003J\t\u0010L\u001a\u00020\u0007H\u00c6\u0003J\t\u0010M\u001a\u00020\tH\u00c6\u0003J\u000b\u0010N\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003J\u0010\u0010O\u001a\u0004\u0018\u00010\tH\u00c6\u0003\u00a2\u0006\u0002\u00107J\u0010\u0010P\u001a\u0004\u0018\u00010\tH\u00c6\u0003\u00a2\u0006\u0002\u00107J\u000b\u0010Q\u001a\u0004\u0018\u00010\u000fH\u00c6\u0003J\t\u0010R\u001a\u00020\tH\u00c6\u0003J\u00e6\u0001\u0010S\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\t2\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\t2\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\t2\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u00122\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\t2\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\t2\b\b\u0002\u0010\u0015\u001a\u00020\u000f2\b\b\u0002\u0010\u0016\u001a\u00020\u000f2\b\b\u0002\u0010\u0017\u001a\u00020\u00182\b\b\u0002\u0010\u0019\u001a\u00020\u00032\b\b\u0002\u0010\u001a\u001a\u00020\u00032\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u001c\u001a\u00020\t2\b\b\u0002\u0010\u001d\u001a\u00020\u0003H\u00c6\u0001\u00a2\u0006\u0002\u0010TJ\u0013\u0010U\u001a\u00020V2\b\u0010W\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010X\u001a\u00020\tH\u00d6\u0001J\t\u0010Y\u001a\u00020\u000fH\u00d6\u0001R\u0011\u0010\u0010\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010 R\u0011\u0010\u001d\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\"R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010 R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010%R\u0013\u0010\n\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\'R\u0011\u0010\u0019\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010\"R\u0013\u0010\u000e\u001a\u0004\u0018\u00010\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010*R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010\"R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010-R\u0015\u0010\u001b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u00100\u001a\u0004\b.\u0010/R\u0011\u0010\u001a\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u0010\"R\u0011\u0010\u0016\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u0010*R\u0011\u0010\u001c\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b3\u0010 R\u0013\u0010\u0011\u001a\u0004\u0018\u00010\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u00105R\u0015\u0010\u0013\u001a\u0004\u0018\u00010\t\u00a2\u0006\n\n\u0002\u00108\u001a\u0004\b6\u00107R\u0015\u0010\u0014\u001a\u0004\u0018\u00010\t\u00a2\u0006\n\n\u0002\u00108\u001a\u0004\b9\u00107R\u0011\u0010\u0017\u001a\u00020\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b:\u0010;R\u0015\u0010\r\u001a\u0004\u0018\u00010\t\u00a2\u0006\n\n\u0002\u00108\u001a\u0004\b<\u00107R\u0015\u0010\f\u001a\u0004\u0018\u00010\t\u00a2\u0006\n\n\u0002\u00108\u001a\u0004\b=\u00107R\u0011\u0010\u0015\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b>\u0010*\u00a8\u0006Z"}, d2 = {"Lcom/reminderapp/model/Reminder;", "", "id", "", "kind", "Lcom/reminderapp/model/ReminderKind;", "cycle", "Lcom/reminderapp/model/Cycle;", "customDays", "", "dateType", "Lcom/reminderapp/model/DateReminderType;", "targetMonth", "targetDay", "holidayName", "", "advanceDays", "rulePeriod", "Lcom/reminderapp/model/RulePeriod;", "ruleWeek", "ruleWeekday", "title", "note", "status", "Lcom/reminderapp/model/ReminderStatus;", "firstTriggerAt", "nextTriggerAt", "lastConfirmedAt", "retryCount", "createdAt", "(JLcom/reminderapp/model/ReminderKind;Lcom/reminderapp/model/Cycle;ILcom/reminderapp/model/DateReminderType;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;ILcom/reminderapp/model/RulePeriod;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Lcom/reminderapp/model/ReminderStatus;JJLjava/lang/Long;IJ)V", "getAdvanceDays", "()I", "getCreatedAt", "()J", "getCustomDays", "getCycle", "()Lcom/reminderapp/model/Cycle;", "getDateType", "()Lcom/reminderapp/model/DateReminderType;", "getFirstTriggerAt", "getHolidayName", "()Ljava/lang/String;", "getId", "getKind", "()Lcom/reminderapp/model/ReminderKind;", "getLastConfirmedAt", "()Ljava/lang/Long;", "Ljava/lang/Long;", "getNextTriggerAt", "getNote", "getRetryCount", "getRulePeriod", "()Lcom/reminderapp/model/RulePeriod;", "getRuleWeek", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getRuleWeekday", "getStatus", "()Lcom/reminderapp/model/ReminderStatus;", "getTargetDay", "getTargetMonth", "getTitle", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component20", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(JLcom/reminderapp/model/ReminderKind;Lcom/reminderapp/model/Cycle;ILcom/reminderapp/model/DateReminderType;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;ILcom/reminderapp/model/RulePeriod;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Lcom/reminderapp/model/ReminderStatus;JJLjava/lang/Long;IJ)Lcom/reminderapp/model/Reminder;", "equals", "", "other", "hashCode", "toString", "app_debug"})
public final class Reminder {
    private final long id = 0L;
    @org.jetbrains.annotations.NotNull()
    private final com.reminderapp.model.ReminderKind kind = null;
    @org.jetbrains.annotations.NotNull()
    private final com.reminderapp.model.Cycle cycle = null;
    private final int customDays = 0;
    @org.jetbrains.annotations.Nullable()
    private final com.reminderapp.model.DateReminderType dateType = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer targetMonth = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer targetDay = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String holidayName = null;
    private final int advanceDays = 0;
    @org.jetbrains.annotations.Nullable()
    private final com.reminderapp.model.RulePeriod rulePeriod = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer ruleWeek = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer ruleWeekday = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String title = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String note = null;
    @org.jetbrains.annotations.NotNull()
    private final com.reminderapp.model.ReminderStatus status = null;
    private final long firstTriggerAt = 0L;
    private final long nextTriggerAt = 0L;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Long lastConfirmedAt = null;
    private final int retryCount = 0;
    private final long createdAt = 0L;
    
    public Reminder(long id, @org.jetbrains.annotations.NotNull()
    com.reminderapp.model.ReminderKind kind, @org.jetbrains.annotations.NotNull()
    com.reminderapp.model.Cycle cycle, int customDays, @org.jetbrains.annotations.Nullable()
    com.reminderapp.model.DateReminderType dateType, @org.jetbrains.annotations.Nullable()
    java.lang.Integer targetMonth, @org.jetbrains.annotations.Nullable()
    java.lang.Integer targetDay, @org.jetbrains.annotations.Nullable()
    java.lang.String holidayName, int advanceDays, @org.jetbrains.annotations.Nullable()
    com.reminderapp.model.RulePeriod rulePeriod, @org.jetbrains.annotations.Nullable()
    java.lang.Integer ruleWeek, @org.jetbrains.annotations.Nullable()
    java.lang.Integer ruleWeekday, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String note, @org.jetbrains.annotations.NotNull()
    com.reminderapp.model.ReminderStatus status, long firstTriggerAt, long nextTriggerAt, @org.jetbrains.annotations.Nullable()
    java.lang.Long lastConfirmedAt, int retryCount, long createdAt) {
        super();
    }
    
    public final long getId() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.model.ReminderKind getKind() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.model.Cycle getCycle() {
        return null;
    }
    
    public final int getCustomDays() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.reminderapp.model.DateReminderType getDateType() {
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
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getHolidayName() {
        return null;
    }
    
    public final int getAdvanceDays() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.reminderapp.model.RulePeriod getRulePeriod() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getRuleWeek() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getRuleWeekday() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTitle() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getNote() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.model.ReminderStatus getStatus() {
        return null;
    }
    
    public final long getFirstTriggerAt() {
        return 0L;
    }
    
    public final long getNextTriggerAt() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long getLastConfirmedAt() {
        return null;
    }
    
    public final int getRetryCount() {
        return 0;
    }
    
    public final long getCreatedAt() {
        return 0L;
    }
    
    public final long component1() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.reminderapp.model.RulePeriod component10() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component11() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component12() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component13() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component14() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.model.ReminderStatus component15() {
        return null;
    }
    
    public final long component16() {
        return 0L;
    }
    
    public final long component17() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long component18() {
        return null;
    }
    
    public final int component19() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.model.ReminderKind component2() {
        return null;
    }
    
    public final long component20() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.model.Cycle component3() {
        return null;
    }
    
    public final int component4() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.reminderapp.model.DateReminderType component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component8() {
        return null;
    }
    
    public final int component9() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.model.Reminder copy(long id, @org.jetbrains.annotations.NotNull()
    com.reminderapp.model.ReminderKind kind, @org.jetbrains.annotations.NotNull()
    com.reminderapp.model.Cycle cycle, int customDays, @org.jetbrains.annotations.Nullable()
    com.reminderapp.model.DateReminderType dateType, @org.jetbrains.annotations.Nullable()
    java.lang.Integer targetMonth, @org.jetbrains.annotations.Nullable()
    java.lang.Integer targetDay, @org.jetbrains.annotations.Nullable()
    java.lang.String holidayName, int advanceDays, @org.jetbrains.annotations.Nullable()
    com.reminderapp.model.RulePeriod rulePeriod, @org.jetbrains.annotations.Nullable()
    java.lang.Integer ruleWeek, @org.jetbrains.annotations.Nullable()
    java.lang.Integer ruleWeekday, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String note, @org.jetbrains.annotations.NotNull()
    com.reminderapp.model.ReminderStatus status, long firstTriggerAt, long nextTriggerAt, @org.jetbrains.annotations.Nullable()
    java.lang.Long lastConfirmedAt, int retryCount, long createdAt) {
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