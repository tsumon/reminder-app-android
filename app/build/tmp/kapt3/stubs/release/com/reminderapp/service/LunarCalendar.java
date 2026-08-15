package com.reminderapp.service;

/**
 * 农历日历转换 — 基于 Android 系统内置 ICU 历法（android.icu.util.ChineseCalendar）
 *
 * 「系统时间为主」：不再维护手写 1900-2100 查表数据，直接用系统历法引擎计算。
 * 好处：覆盖范围由系统保证、闰月/大小月与系统日历一致、纯本地计算离线可用。
 *
 * ⚠️ 中国官方口径修正：ICU/CLDR 农历数据在个别年份与中国大陆官方口径
 * （紫金山天文台，新华社发布口径）有 1 天差异。例如农历 2026 年腊月，
 * ICU 认为有三十（→ 2027 春节 02-07），官方口径无年三十（2027 春节 02-06）。
 * 提醒 App 面向中国用户，春节等关键日期必须与官方一致，故用 [officialOverrides]
 * 做定向修正。新差异由回归测试（LunarCalendarRegressionTest）持续发现并补充。
 *
 * 对外接口与旧实现完全一致（lunarToSolar / solarToLunar / LunarDate），调用点零改动。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0010\u0015\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001\u0018B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\n\u001a\u0010\u0012\u0004\u0012\u00020\u0007\u0012\u0006\u0012\u0004\u0018\u00010\b0\u0006H\u0002J/\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\r\u001a\u00020\u00042\u0006\u0010\u000e\u001a\u00020\u00042\u0006\u0010\u000f\u001a\u00020\u00042\b\b\u0002\u0010\u0010\u001a\u00020\u0011\u00a2\u0006\u0002\u0010\u0012J1\u0010\u0013\u001a\u0004\u0018\u00010\f2\u0006\u0010\r\u001a\u00020\u00042\u0006\u0010\u000e\u001a\u00020\u00042\u0006\u0010\u000f\u001a\u00020\u00042\b\b\u0002\u0010\u0010\u001a\u00020\u0011H\u0002\u00a2\u0006\u0002\u0010\u0012J\u0012\u0010\u0014\u001a\u0004\u0018\u00010\u00152\u0006\u0010\u0016\u001a\u00020\fH\u0002J\u0010\u0010\u0017\u001a\u0004\u0018\u00010\u00152\u0006\u0010\u0016\u001a\u00020\fR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0005\u001a\u0010\u0012\u0004\u0012\u00020\u0007\u0012\u0006\u0012\u0004\u0018\u00010\b0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"Lcom/reminderapp/service/LunarCalendar;", "", "()V", "EXTENDED_YEAR_OFFSET", "", "lunarOverrides", "", "", "", "solarOverrides", "buildCorrectionTable", "lunarToSolar", "", "year", "month", "day", "isLeapMonth", "", "(IIIZ)Ljava/lang/Long;", "rawLunarToSolar", "rawSolarToLunar", "Lcom/reminderapp/service/LunarCalendar$LunarDate;", "timestamp", "solarToLunar", "LunarDate", "app_release"})
public final class LunarCalendar {
    private static final int EXTENDED_YEAR_OFFSET = 2637;
    
    /**
     * 官方口径修正表（农历 → 公历）
     * key = "农历年:月:日"（普通月），value = 官方公历 (y, m, d)；value=null 表示官方不存在该日。
     */
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Map<java.lang.String, int[]> lunarOverrides = null;
    
    /**
     * 官方口径修正表（公历 → 农历），由 [lunarOverrides] 反推。
     * key = "yyyyMMdd"，value = "农历年:月:日"。
     */
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Map<java.lang.String, java.lang.String> solarOverrides = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.service.LunarCalendar INSTANCE = null;
    
    private LunarCalendar() {
        super();
    }
    
    /**
     * 官方口径修正（显式权威表，直接写死官方公历日，不依赖生成式）：
     * 春节官方口径：2025→01-29、2026→02-17、2027→02-06、2028→01-26、2029→02-13、2030→02-03。
     * 其中 ICU(CLDR) 裸算仅 2027 偏晚 1 天（裸 02-07）、2030 偏早 1 天（裸 02-02）；
     * 2028/2029 ICU 裸算已与官方一致，无需平移。故此处直接写官方值，避免「整年 -1」误伤 2028/2029。
     * 无「年三十」农历年：2025/2026/2027/2028/2030（2029 有，公历 2030-02-02）。
     * 数据来源：寿星天文历（紫金山天文台官方口径，borax + zhdate 双库校验）。
     * 2025/2026 与 2031+ 与系统一致，走系统兜底。
     */
    private final java.lang.Long rawLunarToSolar(int year, int month, int day, boolean isLeapMonth) {
        return null;
    }
    
    private final com.reminderapp.service.LunarCalendar.LunarDate rawSolarToLunar(long timestamp) {
        return null;
    }
    
    private final java.util.Map<java.lang.String, int[]> buildCorrectionTable() {
        return null;
    }
    
    /**
     * 将农历月日转换为当年公历日期（毫秒时间戳，当天 00:00）
     * [isLeapMonth] 指定闰月；若该农历日期不存在（如该年无此闰月/无此普通月）返回 null
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long lunarToSolar(int year, int month, int day, boolean isLeapMonth) {
        return null;
    }
    
    /**
     * 将公历时间戳转换为农历日期
     */
    @org.jetbrains.annotations.Nullable()
    public final com.reminderapp.service.LunarCalendar.LunarDate solarToLunar(long timestamp) {
        return null;
    }
    
    /**
     * 农历日期结构
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u000f\b\u0086\b\u0018\u00002\u00020\u0001B%\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\t\u0010\u0012\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\u0007H\u00c6\u0003J1\u0010\u0016\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u00c6\u0001J\u0013\u0010\u0017\u001a\u00020\u00072\b\u0010\u0018\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0019\u001a\u00020\u0003H\u00d6\u0001J\t\u0010\u001a\u001a\u00020\fH\u00d6\u0001R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0011\u0010\u000b\u001a\u00020\f8F\u00a2\u0006\u0006\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u000fR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\nR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\n\u00a8\u0006\u001b"}, d2 = {"Lcom/reminderapp/service/LunarCalendar$LunarDate;", "", "year", "", "month", "day", "isLeapMonth", "", "(IIIZ)V", "getDay", "()I", "description", "", "getDescription", "()Ljava/lang/String;", "()Z", "getMonth", "getYear", "component1", "component2", "component3", "component4", "copy", "equals", "other", "hashCode", "toString", "app_release"})
    public static final class LunarDate {
        private final int year = 0;
        private final int month = 0;
        private final int day = 0;
        private final boolean isLeapMonth = false;
        
        public LunarDate(int year, int month, int day, boolean isLeapMonth) {
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
        
        public final boolean isLeapMonth() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getDescription() {
            return null;
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
        public final com.reminderapp.service.LunarCalendar.LunarDate copy(int year, int month, int day, boolean isLeapMonth) {
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