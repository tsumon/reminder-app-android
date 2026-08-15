package com.reminderapp.service;

/**
 * 提醒引擎 — 周期计算、确认处理、递增重试、遗漏检查
 * 镜像 iOS ReminderEngine.swift
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u001b\n\u0002\u0010\u000b\n\u0002\b\u0011\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J \u0010\b\u001a\u00020\u00072\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\u00072\u0006\u0010\f\u001a\u00020\u0007H\u0002J\u0018\u0010\r\u001a\u00020\u00072\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\f\u001a\u00020\u0007H\u0002J\u0018\u0010\u0010\u001a\u00020\u00072\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\f\u001a\u00020\u0007H\u0002J\u0018\u0010\u0011\u001a\u00020\u00072\u0006\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u0010\u0012\u001a\u00020\u0007J\u0018\u0010\u0013\u001a\u00020\u00072\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\f\u001a\u00020\u0007H\u0002J\"\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00062\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00062\u0006\u0010\f\u001a\u00020\u0007J\'\u0010\u0016\u001a\u0004\u0018\u00010\u00072\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0017\u001a\u00020\u00042\u0006\u0010\f\u001a\u00020\u0007H\u0002\u00a2\u0006\u0002\u0010\u0018J\u000e\u0010\u0019\u001a\u00020\u000f2\u0006\u0010\u000e\u001a\u00020\u000fJ8\u0010\u001a\u001a\u00020\u00042\u0006\u0010\u001b\u001a\u00020\u00042\u0006\u0010\u001c\u001a\u00020\u00042\u0006\u0010\u001d\u001a\u00020\u00042\u0006\u0010\u001e\u001a\u00020\u00042\u0006\u0010\u001f\u001a\u00020\u00042\u0006\u0010 \u001a\u00020\u0004H\u0002J \u0010!\u001a\u00020\u00042\u0006\u0010\u0017\u001a\u00020\u00042\u0006\u0010\"\u001a\u00020\u00042\u0006\u0010#\u001a\u00020\u0004H\u0002J\u000e\u0010$\u001a\u00020\u000f2\u0006\u0010\u000e\u001a\u00020\u000fJ\u001e\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u0010&\u001a\u00020\u0004J\u001c\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u00040\u00062\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\f\u001a\u00020\u0007J\u0018\u0010(\u001a\u00020\u00072\u0006\u0010\t\u001a\u00020\n2\u0006\u0010)\u001a\u00020\u0004H\u0002J\u001e\u0010*\u001a\u00020+2\u0006\u0010\u0017\u001a\u00020\u00042\u0006\u0010\"\u001a\u00020\u00042\u0006\u0010,\u001a\u00020\u0004J&\u0010-\u001a\u00020+2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0017\u001a\u00020\u00042\u0006\u0010\"\u001a\u00020\u00042\u0006\u0010,\u001a\u00020\u0004J(\u0010.\u001a\u00020+2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0017\u001a\u00020\u00042\u0006\u0010\"\u001a\u00020\u00042\u0006\u0010,\u001a\u00020\u0004H\u0002J(\u0010/\u001a\u00020+2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0017\u001a\u00020\u00042\u0006\u0010\"\u001a\u00020\u00042\u0006\u0010,\u001a\u00020\u0004H\u0002J(\u00100\u001a\u00020+2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0017\u001a\u00020\u00042\u0006\u0010\"\u001a\u00020\u00042\u0006\u0010,\u001a\u00020\u0004H\u0002J?\u00101\u001a\u0004\u0018\u00010\u00072\u0006\u0010\u0017\u001a\u00020\u00042\u0006\u0010\"\u001a\u00020\u00042\u0006\u00102\u001a\u00020\u00042\u0006\u00103\u001a\u00020\u00042\u0006\u00104\u001a\u00020\u00042\u0006\u00105\u001a\u00020\u0004H\u0002\u00a2\u0006\u0002\u00106J\u0018\u00107\u001a\u00020\u000f2\u0006\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u00108\u001a\u00020\u0007J\u000e\u00109\u001a\u00020\u000f2\u0006\u0010\u000e\u001a\u00020\u000fJ \u0010:\u001a\u00020\u00072\u0006\u0010\u0017\u001a\u00020\u00042\u0006\u0010\"\u001a\u00020\u00042\u0006\u0010,\u001a\u00020\u0004H\u0002J \u0010;\u001a\u00020\u00042\u0006\u0010\u0017\u001a\u00020\u00042\u0006\u0010\"\u001a\u00020\u00042\u0006\u0010,\u001a\u00020\u0004H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006<"}, d2 = {"Lcom/reminderapp/service/ReminderEngine;", "", "()V", "MAX_ESCALATION", "", "retryIntervals", "", "", "advanceByCalendarMonths", "cycle", "", "anchor", "now", "calculateCycleNextTrigger", "reminder", "Lcom/reminderapp/data/entity/ReminderEntity;", "calculateDateNextTrigger", "calculateNextTrigger", "from", "calculateRuleNextTrigger", "checkMissed", "reminders", "computeDateForYear", "year", "(Lcom/reminderapp/data/entity/ReminderEntity;IJ)Ljava/lang/Long;", "confirm", "daysBetween", "y1", "m1", "d1", "y2", "m2", "d2", "effectiveAnchorDay", "month", "anchorDay", "escalate", "futureTriggers", "count", "getAdvanceDaysToNotify", "getCycleIntervalMs", "customDays", "isValidSolarDate", "", "day", "occursOn", "occursOnCycle", "occursOnDate", "occursOnRule", "ruleDateInMonth", "week", "weekday", "hour", "minute", "(IIIIII)Ljava/lang/Long;", "snooze", "minutes", "snoozeTomorrow", "startOfDay", "weekdayMondayBased", "app_release"})
public final class ReminderEngine {
    
    /**
     * 递增重试上限 — 对齐 iOS：retryStage >= 5 即标 overdue（不再轰炸）
     */
    public static final int MAX_ESCALATION = 5;
    
    /**
     * 递增重试间隔（毫秒）— 对齐 iOS escalateRetry：1h / 4h / 12h / 24h / 24h
     * retryCount=0 → 1h、1 → 4h、2 → 12h、3 → 24h、4+ → 24h（与 iOS stage 映射一致）
     */
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<java.lang.Long> retryIntervals = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.service.ReminderEngine INSTANCE = null;
    
    private ReminderEngine() {
        super();
    }
    
    /**
     * 计算下一次触发时间；from 缺省为当前时刻（v2.1.1: 未来预览传入 cursor 推进）
     */
    public final long calculateNextTrigger(@org.jetbrains.annotations.NotNull()
    com.reminderapp.data.entity.ReminderEntity reminder, long from) {
        return 0L;
    }
    
    /**
     * v2.1.1: 未来 N 次触发时间预览（详情页展示；once 只有一次；已停用/已完成/已逾期为空）
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.Long> futureTriggers(@org.jetbrains.annotations.NotNull()
    com.reminderapp.data.entity.ReminderEntity reminder, int count) {
        return null;
    }
    
    /**
     * 规则提醒：每月/每季度/每年 第 N 周 周 X
     * 例如「每季度第二周周二」→ rulePeriod=quarterly, ruleWeek=2, ruleWeekday=2
     */
    private final long calculateRuleNextTrigger(com.reminderapp.data.entity.ReminderEntity reminder, long now) {
        return 0L;
    }
    
    /**
     * 计算某年某月「第 week 周的 weekday」的日期时间戳
     * 如果该月不存在第 N 个该星期（如第 5 周溢出），返回 null
     */
    private final java.lang.Long ruleDateInMonth(int year, int month, int week, int weekday, int hour, int minute) {
        return null;
    }
    
    /**
     * 周期提醒：锚点法防漂移
     *
     * 注意：月/季/年用「按日历月累加」而非固定天数，避免 30 天/90 天/365 天在大月小月、
     * 闰月、闰年下逐渐漂移（例如「每月 31 号」在 2 月不会错位成月初）。
     */
    private final long calculateCycleNextTrigger(com.reminderapp.data.entity.ReminderEntity reminder, long now) {
        return 0L;
    }
    
    /**
     * 按日历月累加推进（每月 / 每季度 / 每年）。
     * 注意「月末对齐」：锚点 31 号在 2 月落 28 号后，不能从 28 号继续加月
     * （会永久漂移到 28 号），必须每次累加后把日钳回「锚点日号」，
     * 目标月不足时取该月最后一天（1/31 → 2/28 → 3/31 → 4/30 → 5/31…）。
     */
    private final long advanceByCalendarMonths(java.lang.String cycle, long anchor, long now) {
        return 0L;
    }
    
    private final long getCycleIntervalMs(java.lang.String cycle, int customDays) {
        return 0L;
    }
    
    /**
     * 日期提醒：根据类型计算当年触发时间
     */
    private final long calculateDateNextTrigger(com.reminderapp.data.entity.ReminderEntity reminder, long now) {
        return 0L;
    }
    
    /**
     * 计算某年某日期提醒的触发时间戳（要求大于 now）；年内已过的返回 null
     */
    public final boolean isValidSolarDate(int year, int month, int day) {
        return false;
    }
    
    private final java.lang.Long computeDateForYear(com.reminderapp.data.entity.ReminderEntity reminder, int year, long now) {
        return null;
    }
    
    /**
     * 日期提醒：判断是否在提前预告期内，返回应发送预告的天数
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.Integer> getAdvanceDaysToNotify(@org.jetbrains.annotations.NotNull()
    com.reminderapp.data.entity.ReminderEntity reminder, long now) {
        return null;
    }
    
    /**
     * 确认完成 → 周期前进
     */
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.data.entity.ReminderEntity confirm(@org.jetbrains.annotations.NotNull()
    com.reminderapp.data.entity.ReminderEntity reminder) {
        return null;
    }
    
    /**
     * 稍后提醒 → N 分钟后重推（v1.9.7 对齐 iOS：不动 retryCount，
     * 避免用户反复点稍后导致 escalate 间隔被跳过；v2.1.0 支持任意分钟数）
     */
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.data.entity.ReminderEntity snooze(@org.jetbrains.annotations.NotNull()
    com.reminderapp.data.entity.ReminderEntity reminder, long minutes) {
        return null;
    }
    
    /**
     * 稍后到明天提醒时刻（reminderHour:reminderMinute；已过则顺延一天）
     */
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.data.entity.ReminderEntity snoozeTomorrow(@org.jetbrains.annotations.NotNull()
    com.reminderapp.data.entity.ReminderEntity reminder) {
        return null;
    }
    
    /**
     * 递增重试（到点未确认时自动调度）— 对齐 iOS escalateRetry
     *
     * 间隔序列：1h → 4h → 12h → 24h → 24h；
     * retryCount 达到 MAX_ESCALATION(5) 时标为 overdue（停止轰炸，等用户手动确认/重新打开）。
     * 注意：重试期间【不推进周期】，周期只在「确认完成 / 重新打开」时前进（与 iOS 一致）。
     */
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.data.entity.ReminderEntity escalate(@org.jetbrains.annotations.NotNull()
    com.reminderapp.data.entity.ReminderEntity reminder) {
        return null;
    }
    
    /**
     * 开机/重启后检查遗漏提醒
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.reminderapp.data.entity.ReminderEntity> checkMissed(@org.jetbrains.annotations.NotNull()
    java.util.List<com.reminderapp.data.entity.ReminderEntity> reminders, long now) {
        return null;
    }
    
    /**
     * 判断提醒是否在指定公历日期（year/month/day，month 1-based）触发。
     * 用于日历标记与「点击某天查看当日任务」。
     */
    public final boolean occursOn(@org.jetbrains.annotations.NotNull()
    com.reminderapp.data.entity.ReminderEntity reminder, int year, int month, int day) {
        return false;
    }
    
    private final long startOfDay(int year, int month, int day) {
        return 0L;
    }
    
    private final int daysBetween(int y1, int m1, int d1, int y2, int m2, int d2) {
        return 0;
    }
    
    private final int weekdayMondayBased(int year, int month, int day) {
        return 0;
    }
    
    private final boolean occursOnCycle(com.reminderapp.data.entity.ReminderEntity reminder, int year, int month, int day) {
        return false;
    }
    
    /**
     * 月末对齐用的「锚点日号」：目标月不足锚点日时取该月最后一天（如 31 号在 2 月 → 28/29）
     */
    private final int effectiveAnchorDay(int year, int month, int anchorDay) {
        return 0;
    }
    
    private final boolean occursOnDate(com.reminderapp.data.entity.ReminderEntity reminder, int year, int month, int day) {
        return false;
    }
    
    private final boolean occursOnRule(com.reminderapp.data.entity.ReminderEntity reminder, int year, int month, int day) {
        return false;
    }
}