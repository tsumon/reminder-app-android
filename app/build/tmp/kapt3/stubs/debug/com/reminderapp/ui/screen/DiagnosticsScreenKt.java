package com.reminderapp.ui.screen;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u001e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u001a \u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00032\u0006\u0010\u0005\u001a\u00020\u0006H\u0003\u001a\u0016\u0010\u0007\u001a\u00020\u00012\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00010\tH\u0007\u00a8\u0006\n"}, d2 = {"DiagRow", "", "label", "", "value", "ok", "", "DiagnosticsScreen", "onBack", "Lkotlin/Function0;", "app_debug"})
public final class DiagnosticsScreenKt {
    
    /**
     * v2.1.1: 提醒可靠性诊断页——自签环境权限/排期状态经常异常，一键查看问题在哪。
     * 显示：通知权限、数据规模、WebDAV 配置、最近触发时间。
     */
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void DiagnosticsScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void DiagRow(java.lang.String label, java.lang.String value, boolean ok) {
    }
}