package com.reminderapp.ui.screen;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000N\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\b\u001a\u0010\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003H\u0003\u001a\u0010\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003H\u0003\u001a4\u0010\u0005\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u00072\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00010\t2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00010\tH\u0003\u001a*\u0010\u000b\u001a\u00020\u00012\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0012\u0010\u0013\u001a4\u0010\u0014\u001a\u00020\u00012\u0006\u0010\u0015\u001a\u00020\r2\u0006\u0010\u0016\u001a\u00020\r2\u0006\u0010\u0010\u001a\u00020\u00112\b\b\u0002\u0010\u0017\u001a\u00020\u0018H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0019\u0010\u001a\u001a$\u0010\u001b\u001a\u00020\u00012\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u001e0\u001d2\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00010\tH\u0007\u001a<\u0010 \u001a\u00020\u00012\u0006\u0010\u0015\u001a\u00020\r2\u0006\u0010\u0016\u001a\u00020!2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00112\b\b\u0002\u0010\u0017\u001a\u00020\u0018H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\"\u0010#\u001a\u0015\u0010$\u001a\u00020\u00112\u0006\u0010%\u001a\u00020!H\u0002\u00a2\u0006\u0002\u0010&\u001a\u0010\u0010\'\u001a\u00020!2\u0006\u0010(\u001a\u00020!H\u0002\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006)"}, d2 = {"CompletionCard", "", "summary", "Lcom/reminderapp/service/StatsService$Summary;", "ForgetHoursCard", "HeatmapCard", "displayMonth", "Ljava/util/Calendar;", "onPrevMonth", "Lkotlin/Function0;", "onNextMonth", "LabelChip", "text", "", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "color", "Landroidx/compose/ui/graphics/Color;", "LabelChip-mxwnekA", "(Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;J)V", "StatMiniCard", "title", "value", "modifier", "Landroidx/compose/ui/Modifier;", "StatMiniCard-9LQNqLg", "(Ljava/lang/String;Ljava/lang/String;JLandroidx/compose/ui/Modifier;)V", "StatsScreen", "records", "", "Lcom/reminderapp/data/entity/ReminderRecordEntity;", "onBack", "StreakCard", "", "StreakCard-42QJj7c", "(Ljava/lang/String;ILandroidx/compose/ui/graphics/vector/ImageVector;JLandroidx/compose/ui/Modifier;)V", "heatColor", "level", "(I)J", "heatLevel", "count", "app_release"})
public final class StatsScreenKt {
    
    /**
     * 统计洞察页（v1.8.7 任务③）：完成率 / 连续打卡 / 最常忘记时段 / 月历热力图
     * 镜像 iOS StatsView.swift
     */
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void StatsScreen(@org.jetbrains.annotations.NotNull()
    java.util.List<com.reminderapp.data.entity.ReminderRecordEntity> records, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void CompletionCard(com.reminderapp.service.StatsService.Summary summary) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void ForgetHoursCard(com.reminderapp.service.StatsService.Summary summary) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void HeatmapCard(com.reminderapp.service.StatsService.Summary summary, java.util.Calendar displayMonth, kotlin.jvm.functions.Function0<kotlin.Unit> onPrevMonth, kotlin.jvm.functions.Function0<kotlin.Unit> onNextMonth) {
    }
    
    /**
     * 0=无，1=1次，2=2-3次，3=4+次（色阶令牌化，与 iOS 一致）
     */
    private static final int heatLevel(int count) {
        return 0;
    }
    
    private static final long heatColor(int level) {
        return 0L;
    }
}