package com.reminderapp.ui.screen;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000>\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a&\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0007\u001a^\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0012\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u00010\u00102\u0018\u0010\u0012\u001a\u0014\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u00010\u0013H\u0002\u00a8\u0006\u0014"}, d2 = {"NearbyShareScreen", "", "database", "Lcom/reminderapp/data/database/AppDatabase;", "scheduler", "Lcom/reminderapp/service/ReminderScheduler;", "onBack", "Lkotlin/Function0;", "receive", "hostInput", "", "context", "Landroid/content/Context;", "scope", "Lkotlinx/coroutines/CoroutineScope;", "setReceiving", "Lkotlin/Function1;", "", "setResult", "Lkotlin/Function2;", "app_release"})
public final class NearbyShareScreenKt {
    
    /**
     * 近场传输：同一局域网内互传提醒
     * - 发送：显示二维码 + 启动本地服务，对方扫码或输入地址即可收到全部提醒
     * - 接收：扫码（或输入 IP）→ 下载 → 导入（去重/重新调度）
     */
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void NearbyShareScreen(@org.jetbrains.annotations.NotNull()
    com.reminderapp.data.database.AppDatabase database, @org.jetbrains.annotations.NotNull()
    com.reminderapp.service.ReminderScheduler scheduler, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack) {
    }
    
    /**
     * 接收：下载 → 解析 → 导入（扫码与手动输入共用）
     */
    private static final void receive(java.lang.String hostInput, com.reminderapp.data.database.AppDatabase database, com.reminderapp.service.ReminderScheduler scheduler, android.content.Context context, kotlinx.coroutines.CoroutineScope scope, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> setReceiving, kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.Boolean, kotlin.Unit> setResult) {
    }
}