package com.reminderapp.service;

/**
 * 同步设置存储（WebDAV）
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\n\n\u0002\u0010\t\n\u0002\b\u001a\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0018\u00104\u001a\n 6*\u0004\u0018\u000105052\u0006\u00107\u001a\u000208H\u0002J\u0006\u00109\u001a\u00020:R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R$\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\u000e\u001a\u00020\u000f8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R$\u0010\u0015\u001a\u00020\u000f2\u0006\u0010\u000e\u001a\u00020\u000f8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u0016\u0010\u0012\"\u0004\b\u0017\u0010\u0014R\u0011\u0010\u0018\u001a\u00020\u000f8F\u00a2\u0006\u0006\u001a\u0004\b\u0018\u0010\u0012R\u0011\u0010\u0019\u001a\u00020\u000f8F\u00a2\u0006\u0006\u001a\u0004\b\u0019\u0010\u0012R$\u0010\u001b\u001a\u00020\u001a2\u0006\u0010\u000e\u001a\u00020\u001a8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u001c\u0010\u001d\"\u0004\b\u001e\u0010\u001fR$\u0010 \u001a\u00020\u001a2\u0006\u0010\u000e\u001a\u00020\u001a8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b!\u0010\u001d\"\u0004\b\"\u0010\u001fR$\u0010#\u001a\u00020\u001a2\u0006\u0010\u000e\u001a\u00020\u001a8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b$\u0010\u001d\"\u0004\b%\u0010\u001fR$\u0010&\u001a\u00020\u001a2\u0006\u0010\u000e\u001a\u00020\u001a8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\'\u0010\u001d\"\u0004\b(\u0010\u001fR$\u0010)\u001a\u00020\u00042\u0006\u0010\u000e\u001a\u00020\u00048F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b*\u0010+\"\u0004\b,\u0010-R$\u0010.\u001a\u00020\u00042\u0006\u0010\u000e\u001a\u00020\u00048F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b/\u0010+\"\u0004\b0\u0010-R$\u00101\u001a\u00020\u00042\u0006\u0010\u000e\u001a\u00020\u00048F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b2\u0010+\"\u0004\b3\u0010-\u00a8\u0006;"}, d2 = {"Lcom/reminderapp/service/SyncStore;", "", "()V", "KEY_AUTO_SYNC", "", "KEY_HAS_SYNCED_ONCE", "KEY_LAST_LOCAL_CHANGE", "KEY_LAST_SYNC", "KEY_LAST_SYNC_VERSION", "KEY_LOCAL_VERSION", "KEY_PASS", "KEY_URL", "KEY_USER", "PREFS", "value", "", "autoSync", "getAutoSync", "()Z", "setAutoSync", "(Z)V", "hasSyncedOnce", "getHasSyncedOnce", "setHasSyncedOnce", "isConfigured", "isFirstSync", "", "lastLocalChange", "getLastLocalChange", "()J", "setLastLocalChange", "(J)V", "lastSyncAt", "getLastSyncAt", "setLastSyncAt", "lastSyncVersion", "getLastSyncVersion", "setLastSyncVersion", "localVersion", "getLocalVersion", "setLocalVersion", "password", "getPassword", "()Ljava/lang/String;", "setPassword", "(Ljava/lang/String;)V", "url", "getUrl", "setUrl", "username", "getUsername", "setUsername", "prefs", "Landroid/content/SharedPreferences;", "kotlin.jvm.PlatformType", "context", "Landroid/content/Context;", "touchLocalChange", "", "app_debug"})
public final class SyncStore {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS = "sync_settings";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_URL = "webdav_url";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_USER = "webdav_user";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_PASS = "webdav_pass";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_AUTO_SYNC = "auto_sync";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_LAST_LOCAL_CHANGE = "last_local_change";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_LAST_SYNC = "last_sync";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_LOCAL_VERSION = "local_version";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_LAST_SYNC_VERSION = "last_sync_version";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_HAS_SYNCED_ONCE = "has_synced_once";
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.service.SyncStore INSTANCE = null;
    
    private SyncStore() {
        super();
    }
    
    private final android.content.SharedPreferences prefs(android.content.Context context) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getUrl() {
        return null;
    }
    
    public final void setUrl(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getUsername() {
        return null;
    }
    
    public final void setUsername(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPassword() {
        return null;
    }
    
    public final void setPassword(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final boolean getAutoSync() {
        return false;
    }
    
    public final void setAutoSync(boolean value) {
    }
    
    public final boolean isConfigured() {
        return false;
    }
    
    public final long getLastLocalChange() {
        return 0L;
    }
    
    public final void setLastLocalChange(long value) {
    }
    
    public final long getLocalVersion() {
        return 0L;
    }
    
    public final void setLocalVersion(long value) {
    }
    
    public final long getLastSyncVersion() {
        return 0L;
    }
    
    public final void setLastSyncVersion(long value) {
    }
    
    public final boolean getHasSyncedOnce() {
        return false;
    }
    
    public final void setHasSyncedOnce(boolean value) {
    }
    
    public final boolean isFirstSync() {
        return false;
    }
    
    /**
     * 标记本地数据发生了变更（墙钟时间戳 + 单调版本同时推进）
     */
    public final void touchLocalChange() {
    }
    
    public final long getLastSyncAt() {
        return 0L;
    }
    
    public final void setLastSyncAt(long value) {
    }
}