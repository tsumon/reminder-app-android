package com.reminderapp.data.entity;

/**
 * 提醒实体 — 镜像 iOS Reminder.swift
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0016\n\u0002\u0010\u000b\n\u0002\bH\b\u0087\b\u0018\u00002\u00020\u0001B\u00ad\u0002\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\b\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\b\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\b\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\b\u0012\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u0011\u001a\u00020\b\u0012\b\b\u0002\u0010\u0012\u001a\u00020\b\u0012\b\b\u0002\u0010\u0013\u001a\u00020\b\u0012\u0006\u0010\u0014\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0016\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0017\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u0005\u0012\u0006\u0010\u0019\u001a\u00020\u0003\u0012\u0006\u0010\u001a\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u001c\u001a\u00020\b\u0012\n\b\u0002\u0010\u001d\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u001e\u001a\u00020\u001f\u0012\b\b\u0002\u0010 \u001a\u00020\u0003\u0012\b\b\u0002\u0010!\u001a\u00020\u001f\u00a2\u0006\u0002\u0010\"J\t\u0010E\u001a\u00020\u0003H\u00c6\u0003J\u0010\u0010F\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u0010=J\u000b\u0010G\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010H\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010I\u001a\u00020\bH\u00c6\u0003J\t\u0010J\u001a\u00020\bH\u00c6\u0003J\t\u0010K\u001a\u00020\bH\u00c6\u0003J\t\u0010L\u001a\u00020\u0005H\u00c6\u0003J\t\u0010M\u001a\u00020\u0005H\u00c6\u0003J\t\u0010N\u001a\u00020\u0005H\u00c6\u0003J\t\u0010O\u001a\u00020\u0005H\u00c6\u0003J\t\u0010P\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010Q\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010R\u001a\u00020\u0003H\u00c6\u0003J\t\u0010S\u001a\u00020\u0003H\u00c6\u0003J\u0010\u0010T\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u00103J\t\u0010U\u001a\u00020\bH\u00c6\u0003J\u000b\u0010V\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010W\u001a\u00020\u001fH\u00c6\u0003J\t\u0010X\u001a\u00020\u0003H\u00c6\u0003J\t\u0010Y\u001a\u00020\u001fH\u00c6\u0003J\t\u0010Z\u001a\u00020\u0005H\u00c6\u0003J\t\u0010[\u001a\u00020\bH\u00c6\u0003J\u000b\u0010\\\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u0010\u0010]\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u0010=J\u0010\u0010^\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u0010=J\u000b\u0010_\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u0010\u0010`\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u0010=J\u00bc\u0002\u0010a\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\b2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\b2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\b2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\b2\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\b2\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u0011\u001a\u00020\b2\b\b\u0002\u0010\u0012\u001a\u00020\b2\b\b\u0002\u0010\u0013\u001a\u00020\b2\b\b\u0002\u0010\u0014\u001a\u00020\u00052\b\b\u0002\u0010\u0015\u001a\u00020\u00052\b\b\u0002\u0010\u0016\u001a\u00020\u00052\b\b\u0002\u0010\u0017\u001a\u00020\u00052\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u0019\u001a\u00020\u00032\b\b\u0002\u0010\u001a\u001a\u00020\u00032\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u001c\u001a\u00020\b2\n\b\u0002\u0010\u001d\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u001e\u001a\u00020\u001f2\b\b\u0002\u0010 \u001a\u00020\u00032\b\b\u0002\u0010!\u001a\u00020\u001fH\u00c6\u0001\u00a2\u0006\u0002\u0010bJ\u0013\u0010c\u001a\u00020\u001f2\b\u0010d\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010e\u001a\u00020\bH\u00d6\u0001J\t\u0010f\u001a\u00020\u0005H\u00d6\u0001R\u0016\u0010\u0011\u001a\u00020\b8\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010$R\u0016\u0010 \u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010&R\u0016\u0010\u0007\u001a\u00020\b8\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010$R\u0016\u0010\u0006\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010)R\u0018\u0010\f\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010)R\u0016\u0010\u0019\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010&R\u0018\u0010\u001d\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010)R\u0018\u0010\u0010\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010)R\u0018\u0010\u000f\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010)R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010&R\u0016\u0010!\u001a\u00020\u001f8\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u00100R\u0016\u0010\u001e\u001a\u00020\u001f8\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u00100R\u0016\u0010\u0004\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u0010)R\u001a\u0010\u001b\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004\u00a2\u0006\n\n\u0002\u00104\u001a\u0004\b2\u00103R\u0016\u0010\u001a\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b5\u0010&R\u0016\u0010\u0015\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b6\u0010)R\u0016\u0010\u0016\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b7\u0010)R\u0016\u0010\u0012\u001a\u00020\b8\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u0010$R\u0016\u0010\u0013\u001a\u00020\b8\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b9\u0010$R\u0016\u0010\u001c\u001a\u00020\b8\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b:\u0010$R\u0018\u0010\t\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b;\u0010)R\u001a\u0010\n\u001a\u0004\u0018\u00010\b8\u0006X\u0087\u0004\u00a2\u0006\n\n\u0002\u0010>\u001a\u0004\b<\u0010=R\u001a\u0010\u000b\u001a\u0004\u0018\u00010\b8\u0006X\u0087\u0004\u00a2\u0006\n\n\u0002\u0010>\u001a\u0004\b?\u0010=R\u0016\u0010\u0017\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b@\u0010)R\u0018\u0010\u0018\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\bA\u0010)R\u001a\u0010\u000e\u001a\u0004\u0018\u00010\b8\u0006X\u0087\u0004\u00a2\u0006\n\n\u0002\u0010>\u001a\u0004\bB\u0010=R\u001a\u0010\r\u001a\u0004\u0018\u00010\b8\u0006X\u0087\u0004\u00a2\u0006\n\n\u0002\u0010>\u001a\u0004\bC\u0010=R\u0016\u0010\u0014\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\bD\u0010)\u00a8\u0006g"}, d2 = {"Lcom/reminderapp/data/entity/ReminderEntity;", "", "id", "", "kind", "", "cycle", "customDays", "", "rulePeriod", "ruleWeek", "ruleWeekday", "dateType", "targetMonth", "targetDay", "holidayName", "holidayId", "advanceDays", "reminderHour", "reminderMinute", "title", "note", "priority", "status", "syncId", "firstTriggerAt", "nextTriggerAt", "lastConfirmedAt", "retryCount", "holidayAdjustNote", "isCritical", "", "createdAt", "isActive", "(JLjava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;IIILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JJLjava/lang/Long;ILjava/lang/String;ZJZ)V", "getAdvanceDays", "()I", "getCreatedAt", "()J", "getCustomDays", "getCycle", "()Ljava/lang/String;", "getDateType", "getFirstTriggerAt", "getHolidayAdjustNote", "getHolidayId", "getHolidayName", "getId", "()Z", "getKind", "getLastConfirmedAt", "()Ljava/lang/Long;", "Ljava/lang/Long;", "getNextTriggerAt", "getNote", "getPriority", "getReminderHour", "getReminderMinute", "getRetryCount", "getRulePeriod", "getRuleWeek", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getRuleWeekday", "getStatus", "getSyncId", "getTargetDay", "getTargetMonth", "getTitle", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component20", "component21", "component22", "component23", "component24", "component25", "component26", "component27", "component28", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(JLjava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;IIILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JJLjava/lang/Long;ILjava/lang/String;ZJZ)Lcom/reminderapp/data/entity/ReminderEntity;", "equals", "other", "hashCode", "toString", "app_release"})
@androidx.room.Entity(tableName = "reminders")
public final class ReminderEntity {
    @androidx.room.PrimaryKey(autoGenerate = true)
    private final long id = 0L;
    @androidx.room.ColumnInfo(name = "kind")
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String kind = null;
    @androidx.room.ColumnInfo(name = "cycle")
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String cycle = null;
    @androidx.room.ColumnInfo(name = "custom_days")
    private final int customDays = 0;
    @androidx.room.ColumnInfo(name = "rule_period")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String rulePeriod = null;
    @androidx.room.ColumnInfo(name = "rule_week")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer ruleWeek = null;
    @androidx.room.ColumnInfo(name = "rule_weekday")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer ruleWeekday = null;
    @androidx.room.ColumnInfo(name = "date_type")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String dateType = null;
    @androidx.room.ColumnInfo(name = "target_month")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer targetMonth = null;
    @androidx.room.ColumnInfo(name = "target_day")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer targetDay = null;
    @androidx.room.ColumnInfo(name = "holiday_name")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String holidayName = null;
    @androidx.room.ColumnInfo(name = "holiday_id")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String holidayId = null;
    @androidx.room.ColumnInfo(name = "advance_days")
    private final int advanceDays = 0;
    @androidx.room.ColumnInfo(name = "reminder_hour")
    private final int reminderHour = 0;
    @androidx.room.ColumnInfo(name = "reminder_minute")
    private final int reminderMinute = 0;
    @androidx.room.ColumnInfo(name = "title")
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String title = null;
    @androidx.room.ColumnInfo(name = "note")
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String note = null;
    @androidx.room.ColumnInfo(name = "priority")
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String priority = null;
    @androidx.room.ColumnInfo(name = "status")
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String status = null;
    @androidx.room.ColumnInfo(name = "sync_id")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String syncId = null;
    @androidx.room.ColumnInfo(name = "first_trigger_at")
    private final long firstTriggerAt = 0L;
    @androidx.room.ColumnInfo(name = "next_trigger_at")
    private final long nextTriggerAt = 0L;
    @androidx.room.ColumnInfo(name = "last_confirmed_at")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Long lastConfirmedAt = null;
    @androidx.room.ColumnInfo(name = "retry_count")
    private final int retryCount = 0;
    @androidx.room.ColumnInfo(name = "holiday_adjust_note")
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String holidayAdjustNote = null;
    @androidx.room.ColumnInfo(name = "is_critical")
    private final boolean isCritical = false;
    @androidx.room.ColumnInfo(name = "created_at")
    private final long createdAt = 0L;
    @androidx.room.ColumnInfo(name = "is_active")
    private final boolean isActive = false;
    
    public ReminderEntity(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String kind, @org.jetbrains.annotations.NotNull()
    java.lang.String cycle, int customDays, @org.jetbrains.annotations.Nullable()
    java.lang.String rulePeriod, @org.jetbrains.annotations.Nullable()
    java.lang.Integer ruleWeek, @org.jetbrains.annotations.Nullable()
    java.lang.Integer ruleWeekday, @org.jetbrains.annotations.Nullable()
    java.lang.String dateType, @org.jetbrains.annotations.Nullable()
    java.lang.Integer targetMonth, @org.jetbrains.annotations.Nullable()
    java.lang.Integer targetDay, @org.jetbrains.annotations.Nullable()
    java.lang.String holidayName, @org.jetbrains.annotations.Nullable()
    java.lang.String holidayId, int advanceDays, int reminderHour, int reminderMinute, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String note, @org.jetbrains.annotations.NotNull()
    java.lang.String priority, @org.jetbrains.annotations.NotNull()
    java.lang.String status, @org.jetbrains.annotations.Nullable()
    java.lang.String syncId, long firstTriggerAt, long nextTriggerAt, @org.jetbrains.annotations.Nullable()
    java.lang.Long lastConfirmedAt, int retryCount, @org.jetbrains.annotations.Nullable()
    java.lang.String holidayAdjustNote, boolean isCritical, long createdAt, boolean isActive) {
        super();
    }
    
    public final long getId() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getKind() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCycle() {
        return null;
    }
    
    public final int getCustomDays() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getRulePeriod() {
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
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getHolidayName() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getHolidayId() {
        return null;
    }
    
    public final int getAdvanceDays() {
        return 0;
    }
    
    public final int getReminderHour() {
        return 0;
    }
    
    public final int getReminderMinute() {
        return 0;
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
    public final java.lang.String getPriority() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getSyncId() {
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
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getHolidayAdjustNote() {
        return null;
    }
    
    public final boolean isCritical() {
        return false;
    }
    
    public final long getCreatedAt() {
        return 0L;
    }
    
    public final boolean isActive() {
        return false;
    }
    
    public final long component1() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component10() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component11() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component12() {
        return null;
    }
    
    public final int component13() {
        return 0;
    }
    
    public final int component14() {
        return 0;
    }
    
    public final int component15() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component16() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component17() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component18() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component19() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component20() {
        return null;
    }
    
    public final long component21() {
        return 0L;
    }
    
    public final long component22() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long component23() {
        return null;
    }
    
    public final int component24() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component25() {
        return null;
    }
    
    public final boolean component26() {
        return false;
    }
    
    public final long component27() {
        return 0L;
    }
    
    public final boolean component28() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    public final int component4() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component5() {
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
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.data.entity.ReminderEntity copy(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String kind, @org.jetbrains.annotations.NotNull()
    java.lang.String cycle, int customDays, @org.jetbrains.annotations.Nullable()
    java.lang.String rulePeriod, @org.jetbrains.annotations.Nullable()
    java.lang.Integer ruleWeek, @org.jetbrains.annotations.Nullable()
    java.lang.Integer ruleWeekday, @org.jetbrains.annotations.Nullable()
    java.lang.String dateType, @org.jetbrains.annotations.Nullable()
    java.lang.Integer targetMonth, @org.jetbrains.annotations.Nullable()
    java.lang.Integer targetDay, @org.jetbrains.annotations.Nullable()
    java.lang.String holidayName, @org.jetbrains.annotations.Nullable()
    java.lang.String holidayId, int advanceDays, int reminderHour, int reminderMinute, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String note, @org.jetbrains.annotations.NotNull()
    java.lang.String priority, @org.jetbrains.annotations.NotNull()
    java.lang.String status, @org.jetbrains.annotations.Nullable()
    java.lang.String syncId, long firstTriggerAt, long nextTriggerAt, @org.jetbrains.annotations.Nullable()
    java.lang.Long lastConfirmedAt, int retryCount, @org.jetbrains.annotations.Nullable()
    java.lang.String holidayAdjustNote, boolean isCritical, long createdAt, boolean isActive) {
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