package com.reminderapp.ui.theme;

/**
 * 设计令牌（v1.8.7 任务⑤）— 双端统一（Android 本文件 / iOS ThemeTokens.swift）
 *
 * 一份令牌，两端各映射一份；新 UI 一律引用令牌，不写硬编码颜色/圆角/字号。
 * 主色统一为 Material Design 3 紫色 #6750A4。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0018\u0002\n\u0002\b\u001c\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0019\u0010\u0003\u001a\u00020\u0004\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\u0007\u001a\u0004\b\u0005\u0010\u0006R\u0019\u0010\b\u001a\u00020\u0004\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\u0007\u001a\u0004\b\t\u0010\u0006R\u0019\u0010\n\u001a\u00020\u0004\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\u0007\u001a\u0004\b\u000b\u0010\u0006R\u0019\u0010\f\u001a\u00020\r\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\u0007\u001a\u0004\b\u000e\u0010\u0006R\u0019\u0010\u000f\u001a\u00020\r\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\u0007\u001a\u0004\b\u0010\u0010\u0006R\u0019\u0010\u0011\u001a\u00020\u0004\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\u0007\u001a\u0004\b\u0012\u0010\u0006R\u0019\u0010\u0013\u001a\u00020\u0004\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\u0007\u001a\u0004\b\u0014\u0010\u0006R\u0019\u0010\u0015\u001a\u00020\u0004\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\u0007\u001a\u0004\b\u0016\u0010\u0006R\u0019\u0010\u0017\u001a\u00020\u0004\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\u0007\u001a\u0004\b\u0018\u0010\u0006R\u0019\u0010\u0019\u001a\u00020\u0004\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\u0007\u001a\u0004\b\u001a\u0010\u0006R\u0019\u0010\u001b\u001a\u00020\u0004\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\u0007\u001a\u0004\b\u001c\u0010\u0006R\u0019\u0010\u001d\u001a\u00020\u001e\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010!\u001a\u0004\b\u001f\u0010 R\u0019\u0010\"\u001a\u00020\u001e\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010!\u001a\u0004\b#\u0010 R\u0019\u0010$\u001a\u00020\u001e\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010!\u001a\u0004\b%\u0010 R\u0019\u0010&\u001a\u00020\u001e\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010!\u001a\u0004\b\'\u0010 R\u0019\u0010(\u001a\u00020\u001e\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010!\u001a\u0004\b)\u0010 R\u0019\u0010*\u001a\u00020\u001e\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010!\u001a\u0004\b+\u0010 R\u0019\u0010,\u001a\u00020\u001e\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010!\u001a\u0004\b-\u0010 R\u0019\u0010.\u001a\u00020\u001e\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010!\u001a\u0004\b/\u0010 R\u0019\u00100\u001a\u00020\u0004\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\u0007\u001a\u0004\b1\u0010\u0006R\u0019\u00102\u001a\u00020\u0004\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\u0007\u001a\u0004\b3\u0010\u0006R\u0019\u00104\u001a\u00020\u0004\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\u0007\u001a\u0004\b5\u0010\u0006R\u0019\u00106\u001a\u00020\u0004\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\u0007\u001a\u0004\b7\u0010\u0006R\u0019\u00108\u001a\u00020\u0004\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\u0007\u001a\u0004\b9\u0010\u0006\u0082\u0002\u000b\n\u0005\b\u00a1\u001e0\u0001\n\u0002\b!\u00a8\u0006:"}, d2 = {"Lcom/reminderapp/ui/theme/Tokens;", "", "()V", "BrandPrimary", "Landroidx/compose/ui/graphics/Color;", "getBrandPrimary-0d7_KjU", "()J", "J", "BrandPrimaryContainer", "getBrandPrimaryContainer-0d7_KjU", "BrandPrimaryDark", "getBrandPrimaryDark-0d7_KjU", "FontMicro", "Landroidx/compose/ui/unit/TextUnit;", "getFontMicro-XSAIIZE", "FontTiny", "getFontTiny-XSAIIZE", "Heatmap0", "getHeatmap0-0d7_KjU", "Heatmap1", "getHeatmap1-0d7_KjU", "Heatmap2", "getHeatmap2-0d7_KjU", "Heatmap3", "getHeatmap3-0d7_KjU", "HolidayRest", "getHolidayRest-0d7_KjU", "HolidayWork", "getHolidayWork-0d7_KjU", "RadiusCard", "Landroidx/compose/ui/unit/Dp;", "getRadiusCard-D9Ej5fM", "()F", "F", "RadiusCell", "getRadiusCell-D9Ej5fM", "RadiusLarge", "getRadiusLarge-D9Ej5fM", "SpaceL", "getSpaceL-D9Ej5fM", "SpaceM", "getSpaceM-D9Ej5fM", "SpaceS", "getSpaceS-D9Ej5fM", "SpaceXL", "getSpaceXL-D9Ej5fM", "SpaceXS", "getSpaceXS-D9Ej5fM", "StatusCompleted", "getStatusCompleted-0d7_KjU", "StatusOverdue", "getStatusOverdue-0d7_KjU", "StatusReminding", "getStatusReminding-0d7_KjU", "StatusSnoozed", "getStatusSnoozed-0d7_KjU", "StatusWaiting", "getStatusWaiting-0d7_KjU", "app_release"})
public final class Tokens {
    private static final long BrandPrimary = 0L;
    private static final long BrandPrimaryDark = 0L;
    private static final long BrandPrimaryContainer = 0L;
    private static final long StatusReminding = 0L;
    private static final long StatusWaiting = 0L;
    private static final long StatusCompleted = 0L;
    private static final long StatusOverdue = 0L;
    private static final long StatusSnoozed = 0L;
    private static final long HolidayRest = 0L;
    private static final long HolidayWork = 0L;
    private static final long Heatmap0 = 0L;
    private static final long Heatmap1 = 0L;
    private static final long Heatmap2 = 0L;
    private static final long Heatmap3 = 0L;
    private static final float RadiusCard = 0.0F;
    private static final float RadiusCell = 0.0F;
    private static final float RadiusLarge = 0.0F;
    private static final long FontTiny = 0L;
    private static final long FontMicro = 0L;
    private static final float SpaceXS = 0.0F;
    private static final float SpaceS = 0.0F;
    private static final float SpaceM = 0.0F;
    private static final float SpaceL = 0.0F;
    private static final float SpaceXL = 0.0F;
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.ui.theme.Tokens INSTANCE = null;
    
    private Tokens() {
        super();
    }
}