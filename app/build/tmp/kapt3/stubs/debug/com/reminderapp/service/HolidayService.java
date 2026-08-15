package com.reminderapp.service;

/**
 * 节假日服务 — 内置 13 个中国节假日
 * 镜像 iOS HolidayService.swift
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\b\u001a\u0004\u0018\u00010\u00052\u0006\u0010\t\u001a\u00020\nJ\u001d\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0010R\u0017\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0011"}, d2 = {"Lcom/reminderapp/service/HolidayService;", "", "()V", "allHolidays", "", "Lcom/reminderapp/model/Holiday;", "getAllHolidays", "()Ljava/util/List;", "findById", "id", "", "getHolidaySolarDate", "", "year", "", "holiday", "(ILcom/reminderapp/model/Holiday;)Ljava/lang/Long;", "app_debug"})
public final class HolidayService {
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<com.reminderapp.model.Holiday> allHolidays = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.service.HolidayService INSTANCE = null;
    
    private HolidayService() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.reminderapp.model.Holiday> getAllHolidays() {
        return null;
    }
    
    /**
     * 按稳定 ID 查节假日（协议字段 holidayId 用；找不到返回 null）
     */
    @org.jetbrains.annotations.Nullable()
    public final com.reminderapp.model.Holiday findById(@org.jetbrains.annotations.NotNull()
    java.lang.String id) {
        return null;
    }
    
    /**
     * 获取指定年份中节日的公历日期
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long getHolidaySolarDate(int year, @org.jetbrains.annotations.NotNull()
    com.reminderapp.model.Holiday holiday) {
        return null;
    }
}