package com.reminderapp.service;

/**
 * v2.1.1: 本地自动备份——自签环境没有 iCloud，把数据写到「下载」目录兜底（用户可见、可导出）。
 * 策略：启动时写一份当日备份；手动可随时备份；保留最近 5 份。
 * Android 10+ 用 MediaStore.Downloads（无需权限）；低版本回落应用专属外部目录。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0006\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0007\u001a\u0004\u0018\u00010\u00062\u0006\u0010\b\u001a\u00020\tJ\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\b\u001a\u00020\tJ\u0018\u0010\f\u001a\u00020\r2\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u000e\u001a\u00020\u0006H\u0002J\u0010\u0010\u000f\u001a\u00020\u000b2\u0006\u0010\b\u001a\u00020\tH\u0002J \u0010\u0010\u001a\u00020\r2\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u000e\u001a\u00020\u00062\u0006\u0010\u0011\u001a\u00020\u0006H\u0002J \u0010\u0012\u001a\u00020\r2\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u000e\u001a\u00020\u00062\u0006\u0010\u0011\u001a\u00020\u0006H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/reminderapp/service/LocalBackupService;", "", "()V", "MAX_KEEP", "", "PREFIX", "", "backupNow", "context", "Landroid/content/Context;", "backupOnLaunch", "", "hasBackupNamed", "", "name", "trim", "writeToDownloads", "json", "writeToExternalFiles", "app_debug"})
public final class LocalBackupService {
    private static final int MAX_KEEP = 5;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFIX = "reminder_backup_";
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.service.LocalBackupService INSTANCE = null;
    
    private LocalBackupService() {
        super();
    }
    
    /**
     * 启动时调用：当日同名文件已存在则跳过
     */
    public final void backupOnLaunch(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    /**
     * 立即备份，返回文件名（失败返回 null）
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String backupNow(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    private final boolean writeToDownloads(android.content.Context context, java.lang.String name, java.lang.String json) {
        return false;
    }
    
    private final boolean writeToExternalFiles(android.content.Context context, java.lang.String name, java.lang.String json) {
        return false;
    }
    
    private final boolean hasBackupNamed(android.content.Context context, java.lang.String name) {
        return false;
    }
    
    /**
     * 只保留最近 MAX_KEEP 份
     */
    private final void trim(android.content.Context context) {
    }
}