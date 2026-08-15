package com.reminderapp.service;

/**
 * 历法引擎回归测试（v1.8.5 换系统历法后必跑）
 *
 * ⚠️ 重要说明：产品代码 [LunarCalendar] 使用 android.icu.util.ChineseCalendar
 * （Android framework API，JVM 单测无法加载），本测试用同源的
 * com.ibm.icu.util.ChineseCalendar（ICU4J）**复制**与产品代码完全相同的逻辑
 * （EXTENDED_YEAR-2637、IS_LEAP_MONTH、往返校验、官方口径修正表）来断言
 * 权威农历事实。android.icu 与 com.ibm.icu 由同一 ICU 项目派生，行为一致。
 *
 * 覆盖（用户要求的回归集）：
 * - 近 10 年春节（2017-2026）
 * - 2020 闰四月、2023 闰二月
 * - 除夕/正月初一边界（2026 无年三十 → 2027 春节 02-06，官方口径修正）
 * - 农历生日跨年（今年已过 → 明年）
 * - 不存在的日期被往返校验拦截
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\"\n\u0002\u0010\b\n\u0000\n\u0002\u0010$\n\u0000\n\u0002\u0010\u000e\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\r\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\t\u0018\u00002\u00020\u0001:\u0001\'B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\f\u001a\u00020\rH\u0007J\b\u0010\u000e\u001a\u00020\rH\u0007J\b\u0010\u000f\u001a\u00020\rH\u0007J\b\u0010\u0010\u001a\u00020\rH\u0007J\b\u0010\u0011\u001a\u00020\rH\u0007J \u0010\u0012\u001a\u00020\n2\u0006\u0010\u0013\u001a\u00020\u00052\u0006\u0010\u0014\u001a\u00020\u00052\u0006\u0010\u0015\u001a\u00020\u0005H\u0002J1\u0010\u0016\u001a\u0004\u0018\u00010\n2\u0006\u0010\u0017\u001a\u00020\u00052\u0006\u0010\u0018\u001a\u00020\u00052\u0006\u0010\u0019\u001a\u00020\u00052\b\b\u0002\u0010\u001a\u001a\u00020\u001bH\u0002\u00a2\u0006\u0002\u0010\u001cJ1\u0010\u001d\u001a\u0004\u0018\u00010\n2\u0006\u0010\u0017\u001a\u00020\u00052\u0006\u0010\u0018\u001a\u00020\u00052\u0006\u0010\u0019\u001a\u00020\u00052\b\b\u0002\u0010\u001a\u001a\u00020\u001bH\u0002\u00a2\u0006\u0002\u0010\u001cJ\u0010\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\nH\u0002J\u0010\u0010!\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\nH\u0002J\b\u0010\"\u001a\u00020\rH\u0007J\b\u0010#\u001a\u00020\rH\u0007J\b\u0010$\u001a\u00020\rH\u0007J\b\u0010%\u001a\u00020\rH\u0007J\b\u0010&\u001a\u00020\rH\u0007R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010\b\u001a\u0010\u0012\u0004\u0012\u00020\t\u0012\u0006\u0012\u0004\u0018\u00010\n0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000b\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\t0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006("}, d2 = {"Lcom/reminderapp/service/LunarCalendarRegressionTest;", "", "()V", "NO_NIAN_SHA_YEARS", "", "", "YEAR_DAY_DELTA", "", "lunarOverrides", "", "", "solarOverrides", "2020\u95f0\u56db\u6708", "", "2023\u95f0\u4e8c\u6708", "2025-2030\u6625\u8282\u5b98\u65b9\u53e3\u5f84\u951a\u70b9", "2026\u5e74\u516d\u6708\u4e09\u5341_\u516c\u53868\u670812\u65e5", "2027\u6625\u8282\u5b98\u65b9\u53e3\u5f84\u4fee\u6b63_ICU\u5dee1\u5929", "date", "y", "m", "d", "lunarToSolar", "year", "month", "day", "isLeapMonth", "", "(IIIZ)Ljava/lang/Long;", "rawLunarToSolar", "rawSolarToLunar", "Lcom/reminderapp/service/LunarCalendarRegressionTest$Lunar;", "millis", "solarToLunar", "\u4e0d\u5b58\u5728\u7684\u65e5\u671f\u88ab\u5f80\u8fd4\u6821\u9a8c\u62e6\u622a", "\u519c\u5386\u751f\u65e5\u8de8\u5e74", "\u5e73\u79fb\u5e74\u4efd\u519c\u5386\u65e5\u8fde\u7eed\u4e14\u53cd\u5411\u4e00\u81f4", "\u65e0\u5e74\u4e09\u5341\u5e74\u4efd\u814a\u6708\u4e09\u5341\u5b98\u65b9\u4e0d\u5b58\u5728", "\u8fd110\u5e74\u6625\u8282\u6b63\u6708\u521d\u4e00\u516c\u5386\u5bf9\u7167", "Lunar", "app_debugUnitTest"})
public final class LunarCalendarRegressionTest {
    @org.jetbrains.annotations.NotNull()
    private final java.util.Set<java.lang.Integer> NO_NIAN_SHA_YEARS = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.Integer, java.lang.Integer> YEAR_DAY_DELTA = null;
    
    /**
     * 农历→公历修正表（镜像产品代码，生成式）
     */
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, java.lang.Long> lunarOverrides = null;
    
    /**
     * 公历→农历修正表（由 lunarOverrides 反推）
     */
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, java.lang.String> solarOverrides = null;
    
    public LunarCalendarRegressionTest() {
        super();
    }
    
    private final com.reminderapp.service.LunarCalendarRegressionTest.Lunar rawSolarToLunar(long millis) {
        return null;
    }
    
    private final java.lang.Long rawLunarToSolar(int year, int month, int day, boolean isLeapMonth) {
        return null;
    }
    
    private final com.reminderapp.service.LunarCalendarRegressionTest.Lunar solarToLunar(long millis) {
        return null;
    }
    
    private final java.lang.Long lunarToSolar(int year, int month, int day, boolean isLeapMonth) {
        return null;
    }
    
    private final long date(int y, int m, int d) {
        return 0L;
    }
    
    @org.junit.Test()
    public final void 近10年春节正月初一公历对照() {
    }
    
    @org.junit.Test()
    public final void 农历生日跨年() {
    }
    
    @org.junit.Test()
    public final void 不存在的日期被往返校验拦截() {
    }
    
    @org.junit.Test()
    public final void 无年三十年份腊月三十官方不存在() {
    }
    
    @org.junit.Test()
    public final void 平移年份农历日连续且反向一致() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0010\n\u0002\u0010\u000e\n\u0000\b\u0082\b\u0018\u00002\u00020\u0001B%\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\t\u0010\u000f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0010\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0012\u001a\u00020\u0007H\u00c6\u0003J1\u0010\u0013\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u00c6\u0001J\u0013\u0010\u0014\u001a\u00020\u00072\b\u0010\u0015\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0016\u001a\u00020\u0003H\u00d6\u0001J\t\u0010\u0017\u001a\u00020\u0018H\u00d6\u0001R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\nR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\n\u00a8\u0006\u0019"}, d2 = {"Lcom/reminderapp/service/LunarCalendarRegressionTest$Lunar;", "", "year", "", "month", "day", "leap", "", "(IIIZ)V", "getDay", "()I", "getLeap", "()Z", "getMonth", "getYear", "component1", "component2", "component3", "component4", "copy", "equals", "other", "hashCode", "toString", "", "app_debugUnitTest"})
    static final class Lunar {
        private final int year = 0;
        private final int month = 0;
        private final int day = 0;
        private final boolean leap = false;
        
        public Lunar(int year, int month, int day, boolean leap) {
            super();
        }
        
        public final int getYear() {
            return 0;
        }
        
        public final int getMonth() {
            return 0;
        }
        
        public final int getDay() {
            return 0;
        }
        
        public final boolean getLeap() {
            return false;
        }
        
        public final int component1() {
            return 0;
        }
        
        public final int component2() {
            return 0;
        }
        
        public final int component3() {
            return 0;
        }
        
        public final boolean component4() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.reminderapp.service.LunarCalendarRegressionTest.Lunar copy(int year, int month, int day, boolean leap) {
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