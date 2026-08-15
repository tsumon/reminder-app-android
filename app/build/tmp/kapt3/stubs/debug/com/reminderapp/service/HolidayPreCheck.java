package com.reminderapp.service;

/**
 * 节假日前移预检（Item 2）
 *
 * 行为：对未完成的「周期 / 规则」提醒（不含生日、节假日等日期提醒），
 * 若其【下一次触发】落在约 31 天窗口内，提前联网检索该天是否为法定节假日：
 * - 命中节假日 → 把【这一次的触发】前移到「假期前最近工作日」，
 *  并在 holiday_adjust_note 写入说明文案；整体循环锚点 firstTriggerAt 不变，
 *  确认完成后该备注清空、下一周期照常推进。
 * - 未命中 → 不动。
 *
 * 触发时机：App 启动（刷新完远端节假日数据后）+ 每次确认完成后。
 * 设计要点：
 * - isOffDay 判定综合「远端节假日(isHoliday)」与「普通周末」：
 *  远端只列出法定节假日与调休上班日，普通周末 status 为 null，
 *  因此 null 时按星期判断周末，调休上班日(isHoliday=false)视为工作日。
 * - 仅在「已识别的节假日」上触发前移（周末不触发），避免对正常周末循环误移。
 * - 已前移过（holiday_adjust_note 非空）的本次不再重复前移。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0007\u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u0004H\u0002J\u0018\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0004H\u0002J\u001f\u0010\u0010\u001a\u0004\u0018\u00010\u00042\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0011\u001a\u00020\u0004H\u0002\u00a2\u0006\u0002\u0010\u0012J\u0016\u0010\u0013\u001a\u00020\u00142\u0006\u0010\r\u001a\u00020\u000eH\u0086@\u00a2\u0006\u0002\u0010\u0015R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/reminderapp/service/HolidayPreCheck;", "", "()V", "DAY_MS", "", "WINDOW_DAYS", "", "buildShiftedTs", "occCal", "Ljava/util/Calendar;", "shifted", "isOffDay", "", "context", "Landroid/content/Context;", "t", "lastWorkingDay", "occ", "(Landroid/content/Context;J)Ljava/lang/Long;", "run", "", "(Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class HolidayPreCheck {
    private static final int WINDOW_DAYS = 31;
    private static final long DAY_MS = 86400000L;
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.service.HolidayPreCheck INSTANCE = null;
    
    private HolidayPreCheck() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object run(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * 返回 occ 之前（不含）最近的一个工作日时间戳；找不到返回 null
     */
    private final java.lang.Long lastWorkingDay(android.content.Context context, long occ) {
        return null;
    }
    
    /**
     * 某天是否休息日：法定节假日(isHoliday) 或 普通周末（远端无数据时按星期判断）
     */
    private final boolean isOffDay(android.content.Context context, long t) {
        return false;
    }
    
    /**
     * 用 shifted 的日期 + occ 的「时分」拼出新的触发时间戳
     */
    private final long buildShiftedTs(java.util.Calendar occCal, long shifted) {
        return 0L;
    }
}