package com.reminderapp.service;

/**
 * 联网节假日数据服务（v1.8.7 任务②）— 镜像 iOS HolidayRemoteService.swift
 *
 * 拉取官方口径的法定节假日安排（放假 + 调休上班日），供首页/日历卡显示「休/班」。
 * - 数据源：holiday-cn（NateScarlet/holiday-cn，GitHub 官方口径 JSON，免 key 无反爬）
 *  GET https://raw.githubusercontent.com/NateScarlet/holiday-cn/master/{year}.json
 *  结构：{ "days": [ { "name": "元旦", "date": "2026-01-01", "isOffDay": true }, ... ] }
 * - 缓存：按年存 SharedPreferences（key=yyyy-MM-dd，value="isOffDay|name"）
 * - 离线/失败：保留旧缓存，UI 静默降级；提醒功能继续走内置 HolidayService，互不影响
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001\u0017B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001e\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0086@\u00a2\u0006\u0002\u0010\u0012J(\u0010\u0013\u001a\u0004\u0018\u00010\u00142\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0015\u001a\u00020\u00112\u0006\u0010\u0016\u001a\u00020\u0011R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0006\u001a\u00020\u00078BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\n\u0010\u000b\u001a\u0004\b\b\u0010\t\u00a8\u0006\u0018"}, d2 = {"Lcom/reminderapp/service/HolidayRemoteService;", "", "()V", "BASE_URL", "", "CACHE_KEY_PREFIX", "client", "Lokhttp3/OkHttpClient;", "getClient", "()Lokhttp3/OkHttpClient;", "client$delegate", "Lkotlin/Lazy;", "refresh", "", "context", "Landroid/content/Context;", "year", "", "(Landroid/content/Context;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "status", "Lcom/reminderapp/service/HolidayRemoteService$DayStatus;", "month", "day", "DayStatus", "app_debug"})
public final class HolidayRemoteService {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String BASE_URL = "https://raw.githubusercontent.com/NateScarlet/holiday-cn/master";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String CACHE_KEY_PREFIX = "remote_holiday_cache_";
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.Lazy client$delegate = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.service.HolidayRemoteService INSTANCE = null;
    
    private HolidayRemoteService() {
        super();
    }
    
    private final okhttp3.OkHttpClient getClient() {
        return null;
    }
    
    /**
     * 查询某公历日期（yyyy-MM-dd）的节假日状态；无数据（普通工作日/未拉到）返回 null
     */
    @org.jetbrains.annotations.Nullable()
    public final com.reminderapp.service.HolidayRemoteService.DayStatus status(@org.jetbrains.annotations.NotNull()
    android.content.Context context, int year, int month, int day) {
        return null;
    }
    
    /**
     * 拉取指定年份的节假日安排并缓存到本地；失败静默（保留旧缓存，UI 降级）
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object refresh(@org.jetbrains.annotations.NotNull()
    android.content.Context context, int year, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\t\u0010\n\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000b\u001a\u00020\u0005H\u00c6\u0003J\u001d\u0010\f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u00c6\u0001J\u0013\u0010\r\u001a\u00020\u00032\b\u0010\u000e\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001J\t\u0010\u0011\u001a\u00020\u0005H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u0007R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\t\u00a8\u0006\u0012"}, d2 = {"Lcom/reminderapp/service/HolidayRemoteService$DayStatus;", "", "isHoliday", "", "name", "", "(ZLjava/lang/String;)V", "()Z", "getName", "()Ljava/lang/String;", "component1", "component2", "copy", "equals", "other", "hashCode", "", "toString", "app_debug"})
    public static final class DayStatus {
        private final boolean isHoliday = false;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String name = null;
        
        public DayStatus(boolean isHoliday, @org.jetbrains.annotations.NotNull()
        java.lang.String name) {
            super();
        }
        
        public final boolean isHoliday() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getName() {
            return null;
        }
        
        public final boolean component1() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.reminderapp.service.HolidayRemoteService.DayStatus copy(boolean isHoliday, @org.jetbrains.annotations.NotNull()
        java.lang.String name) {
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