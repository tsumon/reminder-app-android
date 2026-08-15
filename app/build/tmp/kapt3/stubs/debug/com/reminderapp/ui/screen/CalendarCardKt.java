package com.reminderapp.ui.screen;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000H\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\u001a6\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\u0014\b\u0002\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u00010\bH\u0007\u001aP\u0010\n\u001a\u00020\u00012\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\f2\u0006\u0010\u0013\u001a\u00020\u000e2\b\u0010\u0014\u001a\u0004\u0018\u00010\u00152\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00010\u0017H\u0003\u001a \u0010\u0018\u001a\u00020\u00112\u0006\u0010\u0019\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\f2\u0006\u0010\u000b\u001a\u00020\fH\u0002\u001a \u0010\u001b\u001a\u00020\u00112\u0006\u0010\u0019\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\f2\u0006\u0010\u000b\u001a\u00020\fH\u0002\u00a8\u0006\u001c"}, d2 = {"CalendarCard", "", "reminders", "", "Lcom/reminderapp/data/entity/ReminderEntity;", "modifier", "Landroidx/compose/ui/Modifier;", "onDateClick", "Lkotlin/Function1;", "", "DayCell", "day", "", "isToday", "", "isSelected", "lunarText", "", "taskCount", "isFutureMonth", "holidayStatus", "Lcom/reminderapp/service/HolidayRemoteService$DayStatus;", "onClick", "Lkotlin/Function0;", "dateKey", "year", "month", "lunarTextFor", "app_debug"})
public final class CalendarCardKt {
    
    /**
     * 主页日历卡片：公历 + 农历 + 星期几 + 任务缩略标记
     */
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void CalendarCard(@org.jetbrains.annotations.NotNull()
    java.util.List<com.reminderapp.data.entity.ReminderEntity> reminders, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Long, kotlin.Unit> onDateClick) {
    }
    
    /**
     * 单个日期格子：公历数字 + 农历 + 「休/班」角标 + 任务角标（数字右上）
     */
    @androidx.compose.runtime.Composable()
    private static final void DayCell(int day, boolean isToday, boolean isSelected, java.lang.String lunarText, int taskCount, boolean isFutureMonth, com.reminderapp.service.HolidayRemoteService.DayStatus holidayStatus, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    /**
     * 获取某公历日期的农历显示文本
     */
    private static final java.lang.String lunarTextFor(int year, int month, int day) {
        return null;
    }
    
    /**
     * 生成 yyyy-MM-dd 日期键
     */
    private static final java.lang.String dateKey(int year, int month, int day) {
        return null;
    }
}