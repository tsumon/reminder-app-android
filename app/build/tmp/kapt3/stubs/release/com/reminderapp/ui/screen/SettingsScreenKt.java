package com.reminderapp.ui.screen;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000@\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\u001a.\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u00032\u0006\u0010\t\u001a\u00020\u00032\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00050\u000bH\u0003\u001a&\u0010\f\u001a\u00020\u00052\u001c\u0010\r\u001a\u0018\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u00050\u000e\u00a2\u0006\u0002\b\u0010\u00a2\u0006\u0002\b\u0011H\u0001\u001a@\u0010\u0012\u001a\u00020\u00052\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b2\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b2\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b2\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00050\u000bH\u0007\u001a\u001a\u0010\u0017\u001a\u00020\u0018*\u00020\u00182\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00050\u000bH\u0002\"&\u0010\u0000\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0004\u0012\u00020\u0003\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00030\u00010\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"changelog", "", "Lkotlin/Pair;", "", "SettingRow", "", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "title", "subtitle", "onClick", "Lkotlin/Function0;", "SettingsCard", "content", "Lkotlin/Function1;", "Landroidx/compose/foundation/layout/ColumnScope;", "Landroidx/compose/runtime/Composable;", "Lkotlin/ExtensionFunctionType;", "SettingsScreen", "onBack", "onOpenSyncSettings", "onOpenAISettings", "onOpenDiagnostics", "clickableItem", "Landroidx/compose/ui/Modifier;", "app_release"})
public final class SettingsScreenKt {
    
    /**
     * 更新日志数据（与 iOS 一致）
     */
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<kotlin.Pair<java.lang.String, java.util.List<java.lang.String>>> changelog = null;
    
    /**
     * 设置页：同步/AI/关于（版本号 + 检查更新 + 更新日志）
     */
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void SettingsScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onOpenSyncSettings, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onOpenAISettings, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onOpenDiagnostics) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void SettingsCard(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super androidx.compose.foundation.layout.ColumnScope, kotlin.Unit> content) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void SettingRow(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String title, java.lang.String subtitle, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    private static final androidx.compose.ui.Modifier clickableItem(androidx.compose.ui.Modifier $this$clickableItem, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
        return null;
    }
}