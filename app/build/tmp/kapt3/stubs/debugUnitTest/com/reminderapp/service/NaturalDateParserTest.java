package com.reminderapp.service;

/**
 * 自然语言日期解析回归测试 — 固定 now，断言「上周/本周/下周」跨端一致性（对齐 iOS NaturalDateParser）
 *
 * 基座：2026-08-17（周一）12:00。
 * - 「上周一」→ 2026-08-10（上周一，iOS 同样减 7 天）
 * - 「周一」（本周已过的同一天）→ 下周一 2026-08-24
 * - 「下周一」→ 2026-08-24
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J*\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u00072\u0006\u0010\t\u001a\u00020\u00072\b\b\u0002\u0010\n\u001a\u00020\u0007H\u0002J\b\u0010\u000b\u001a\u00020\fH\u0007J\b\u0010\r\u001a\u00020\fH\u0007J\b\u0010\u000e\u001a\u00020\fH\u0007J\b\u0010\u000f\u001a\u00020\fH\u0007R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Lcom/reminderapp/service/NaturalDateParserTest;", "", "()V", "now", "", "date", "y", "", "m", "d", "h", "\u4e0a\u5468\u4e00 \u89e3\u6790\u4e3a\u4e0a\u5468\u4e00", "", "\u4e0a\u5468\u65e5 \u89e3\u6790\u4e3a\u4e0a\u5468\u65e5", "\u4e0b\u5468\u4e00 \u89e3\u6790\u4e3a\u4e0b\u5468\u4e00", "\u5468\u4e00 \u5728\u672c\u5468\u5df2\u8fc7\u65f6\u987a\u5ef6\u5230\u4e0b\u5468\u4e00", "app_debugUnitTest"})
public final class NaturalDateParserTest {
    private final long now = 0L;
    
    public NaturalDateParserTest() {
        super();
    }
    
    private final long date(int y, int m, int d, int h) {
        return 0L;
    }
}