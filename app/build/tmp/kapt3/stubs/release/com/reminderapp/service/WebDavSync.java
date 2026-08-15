package com.reminderapp.service;

/**
 * WebDAV 同步：将全部提醒导出为 JSON 上传/下载，以 exportedAt 时间戳判断新旧
 *
 * 同步策略（简单可靠）：
 * - 本地数据版本 = 最后一次实际修改时间（SyncStore.lastLocalChange）
 * - 远程数据版本 = 远程 JSON 内的 exportedAt
 * - 远程新 → 下载并覆盖本地；本地新 → 上传覆盖远程；相同 → 跳过
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000v\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\"\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\u0010\u0003\n\u0000\n\u0002\u0010\u000b\n\u0002\b\f\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\b\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0002EFB\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J&\u0010\f\u001a\u0004\u0018\u00010\u00042\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e2\u0006\u0010\u0010\u001a\u00020\u0011H\u0082@\u00a2\u0006\u0002\u0010\u0012J\u0012\u0010\u0013\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0014\u001a\u00020\u0004H\u0002J\u0014\u0010\u0015\u001a\u00020\u00042\n\u0010\u0016\u001a\u00060\u0017j\u0002`\u0018H\u0002J\u0010\u0010\u0015\u001a\u00020\u00042\u0006\u0010\u0019\u001a\u00020\u001aH\u0002J\u0010\u0010\u0015\u001a\u00020\u00042\u0006\u0010\u0016\u001a\u00020\u001bH\u0002J8\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001d2\u0006\u0010\u001f\u001a\u00020\u001d2\u0006\u0010 \u001a\u00020\u001d2\u0006\u0010!\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u00112\u0006\u0010#\u001a\u00020\u0011H\u0002J0\u0010$\u001a\u00020\u001d2\u0006\u0010%\u001a\u00020\u00112\u0006\u0010&\u001a\u00020\u00112\u0006\u0010\'\u001a\u00020\u00112\u0006\u0010(\u001a\u00020\u00112\u0006\u0010\u001e\u001a\u00020\u001dH\u0002J$\u0010)\u001a\u00020*2\u0006\u0010+\u001a\u00020,2\f\u0010-\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eH\u0082@\u00a2\u0006\u0002\u0010.J\b\u0010/\u001a\u00020*H\u0002J0\u00100\u001a\u00020\u001d2\u0006\u0010%\u001a\u00020\u00112\u0006\u0010&\u001a\u00020\u00112\u0006\u0010\'\u001a\u00020\u00112\u0006\u0010(\u001a\u00020\u00112\u0006\u0010\u001e\u001a\u00020\u001dH\u0002J\b\u00101\u001a\u00020\u0004H\u0002J\u0010\u00101\u001a\u00020\u00042\u0006\u00102\u001a\u00020\u0004H\u0002J\u0016\u00103\u001a\u00020*2\f\u00104\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eH\u0002J\u0016\u00105\u001a\u0002062\u0006\u00107\u001a\u000208H\u0086@\u00a2\u0006\u0002\u00109J\u000e\u0010:\u001a\u000206H\u0086@\u00a2\u0006\u0002\u0010;J\u0010\u0010<\u001a\u00020*2\u0006\u0010=\u001a\u00020\u0004H\u0002J\u001c\u0010>\u001a\u00020?2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eH\u0086@\u00a2\u0006\u0002\u0010@J \u0010A\u001a\u00020*2\u0006\u0010B\u001a\u00020\u00042\u0006\u00102\u001a\u00020\u00042\u0006\u0010C\u001a\u00020\u0004H\u0002J\u0010\u0010D\u001a\u00020\u00042\u0006\u0010\u0019\u001a\u00020\u001aH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00040\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006G"}, d2 = {"Lcom/reminderapp/service/WebDavSync;", "", "()V", "CONTENT_TYPE", "", "ICS_CONTENT_TYPE", "KEPT_STATUS", "", "REMOTE_FILE", "REMOTE_ICS_FILE", "client", "Lokhttp3/OkHttpClient;", "buildUploadJson", "reminders", "", "Lcom/reminderapp/data/entity/ReminderEntity;", "localVersion", "", "(Ljava/util/List;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "download", "url", "friendlyMessage", "e", "Ljava/lang/Exception;", "Lkotlin/Exception;", "code", "", "", "isConflict", "", "firstSync", "localHasData", "remoteHasData", "localVer", "remoteDataVersion", "lastSyncVer", "localNewer", "rdv", "rts", "lv", "lts", "mergeRemote", "", "db", "Lcom/reminderapp/data/database/AppDatabase;", "items", "(Lcom/reminderapp/data/database/AppDatabase;Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "mkcolIfNeeded", "remoteNewer", "remoteUrl", "fileName", "snapshotLocal", "localItems", "syncNow", "Lcom/reminderapp/service/WebDavSync$SyncResult;", "context", "Landroid/content/Context;", "(Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "testConnection", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "upload", "json", "uploadIcs", "Lcom/reminderapp/service/WebDavSync$IcsUploadResult;", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadRaw", "content", "contentType", "writeFailureHint", "IcsUploadResult", "SyncResult", "app_release"})
public final class WebDavSync {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String REMOTE_FILE = "reminder_backup.json";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String CONTENT_TYPE = "application/json; charset=utf-8";
    
    /**
     * 阶段2: 下载合并时随同步保留的状态（未知值回落 pending）
     */
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Set<java.lang.String> KEPT_STATUS = null;
    
    /**
     * 批次3 功能4: 日历订阅用的 .ics 文件名（与备份 JSON 同目录）
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String REMOTE_ICS_FILE = "reminders.ics";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String ICS_CONTENT_TYPE = "text/calendar; charset=utf-8";
    @org.jetbrains.annotations.NotNull()
    private static final okhttp3.OkHttpClient client = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.service.WebDavSync INSTANCE = null;
    
    private WebDavSync() {
        super();
    }
    
    /**
     * 执行同步。result 返回上传/下载/无变更的描述
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object syncNow(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.reminderapp.service.WebDavSync.SyncResult> $completion) {
        return null;
    }
    
    /**
     * 远程是否更新
     */
    private final boolean remoteNewer(long rdv, long rts, long lv, long lts, boolean firstSync) {
        return false;
    }
    
    /**
     * 本地是否更新
     */
    private final boolean localNewer(long rdv, long rts, long lv, long lts, boolean firstSync) {
        return false;
    }
    
    /**
     * 冲突判定（是否需要提示「已按版本覆盖」）。
     *
     * - 首次同步：无 lastSyncVersion 基线可比，只要两边都有数据就意味着一方内容会被整体覆盖 → 提示
     * - 后续同步：自上次同步后双端都推进过版本 → 提示
     */
    private final boolean isConflict(boolean firstSync, boolean localHasData, boolean remoteHasData, long localVer, long remoteDataVersion, long lastSyncVer) {
        return false;
    }
    
    /**
     * 远程文件完整 URL（WebDAV 目录 + 文件名）
     */
    private final java.lang.String remoteUrl() {
        return null;
    }
    
    private final java.lang.String remoteUrl(java.lang.String fileName) {
        return null;
    }
    
    /**
     * 把当前全部提醒导出为 .ics 并上传到 WebDAV 目录，返回该文件的 WebDAV URL。
     *
     * 这个 URL 本身**需要账号密码**，不能直接丢给系统日历订阅——坚果云等网盘的正确姿势是：
     * 上传后在网页端对该文件「创建分享链接」，再把分享直链填进日历订阅。
     * 因此 UI 侧会把这个 URL 连同操作指引一起给用户（见 SettingsScreen 的订阅入口）。
     *
     * 每次调用都覆盖同名文件，订阅端下次刷新即可拿到最新日程。
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object uploadIcs(@org.jetbrains.annotations.NotNull()
    java.util.List<com.reminderapp.data.entity.ReminderEntity> reminders, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.reminderapp.service.WebDavSync.IcsUploadResult> $completion) {
        return null;
    }
    
    /**
     * 通用 PUT：404 时先 MKCOL 建目录再重试一次（与 upload 同策略）
     */
    private final void uploadRaw(java.lang.String content, java.lang.String fileName, java.lang.String contentType) {
    }
    
    /**
     * 构建上传 JSON，exportedAt 保证比远程版本新
     */
    private final java.lang.Object buildUploadJson(java.util.List<com.reminderapp.data.entity.ReminderEntity> reminders, long localVersion, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    /**
     * 用远程数据合并本地（阶段2：按 syncId upsert，不再整库 delete/reinsert）
     */
    private final java.lang.Object mergeRemote(com.reminderapp.data.database.AppDatabase db, java.util.List<com.reminderapp.data.entity.ReminderEntity> items, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * 阶段2: 合并前把当前本地全量导出为 JSON 快照（可恢复；保留最近 5 份）
     */
    private final void snapshotLocal(java.util.List<com.reminderapp.data.entity.ReminderEntity> localItems) {
    }
    
    private final java.lang.String download(java.lang.String url) {
        return null;
    }
    
    private final void upload(java.lang.String json) {
    }
    
    /**
     * 确保 WebDAV 目录存在（MKCOL；已存在时返回 405/301 属正常，忽略）
     */
    private final void mkcolIfNeeded() {
    }
    
    /**
     * 测试连接：完整读写测试。
     * 仅 PROPFIND 通过不算成功——很多账号只读不开写（如坚果云第三方登录受限），
     * 必须实际能写才算配置成功。
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object testConnection(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.reminderapp.service.WebDavSync.SyncResult> $completion) {
        return null;
    }
    
    /**
     * 「可读但不可写」的精准提示（testConnection 第二步失败时使用）
     */
    private final java.lang.String writeFailureHint(int code) {
        return null;
    }
    
    /**
     * 把 HTTP 状态码/异常转成可操作的中文提示（尤其坚果云 401 应用密码）
     */
    private final java.lang.String friendlyMessage(int code) {
        return null;
    }
    
    private final java.lang.String friendlyMessage(java.lang.Throwable e) {
        return null;
    }
    
    private final java.lang.String friendlyMessage(java.lang.Exception e) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0002\u0003\u0004B\u0007\b\u0004\u00a2\u0006\u0002\u0010\u0002\u0082\u0001\u0002\u0005\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/reminderapp/service/WebDavSync$IcsUploadResult;", "", "()V", "Error", "Success", "Lcom/reminderapp/service/WebDavSync$IcsUploadResult$Error;", "Lcom/reminderapp/service/WebDavSync$IcsUploadResult$Success;", "app_release"})
    public static abstract class IcsUploadResult {
        
        private IcsUploadResult() {
            super();
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0010"}, d2 = {"Lcom/reminderapp/service/WebDavSync$IcsUploadResult$Error;", "Lcom/reminderapp/service/WebDavSync$IcsUploadResult;", "message", "", "(Ljava/lang/String;)V", "getMessage", "()Ljava/lang/String;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "app_release"})
        public static final class Error extends com.reminderapp.service.WebDavSync.IcsUploadResult {
            @org.jetbrains.annotations.NotNull()
            private final java.lang.String message = null;
            
            public Error(@org.jetbrains.annotations.NotNull()
            java.lang.String message) {
            }
            
            @org.jetbrains.annotations.NotNull()
            public final java.lang.String getMessage() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final java.lang.String component1() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final com.reminderapp.service.WebDavSync.IcsUploadResult.Error copy(@org.jetbrains.annotations.NotNull()
            java.lang.String message) {
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
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\t\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0003\b\u0086\b\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\t\u0010\u000b\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\f\u001a\u00020\u0005H\u00c6\u0003J\u001d\u0010\r\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u00c6\u0001J\u0013\u0010\u000e\u001a\u00020\u000f2\b\u0010\u0010\u001a\u0004\u0018\u00010\u0011H\u00d6\u0003J\t\u0010\u0012\u001a\u00020\u0005H\u00d6\u0001J\t\u0010\u0013\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\n\u00a8\u0006\u0014"}, d2 = {"Lcom/reminderapp/service/WebDavSync$IcsUploadResult$Success;", "Lcom/reminderapp/service/WebDavSync$IcsUploadResult;", "url", "", "count", "", "(Ljava/lang/String;I)V", "getCount", "()I", "getUrl", "()Ljava/lang/String;", "component1", "component2", "copy", "equals", "", "other", "", "hashCode", "toString", "app_release"})
        public static final class Success extends com.reminderapp.service.WebDavSync.IcsUploadResult {
            @org.jetbrains.annotations.NotNull()
            private final java.lang.String url = null;
            private final int count = 0;
            
            public Success(@org.jetbrains.annotations.NotNull()
            java.lang.String url, int count) {
            }
            
            @org.jetbrains.annotations.NotNull()
            public final java.lang.String getUrl() {
                return null;
            }
            
            public final int getCount() {
                return 0;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final java.lang.String component1() {
                return null;
            }
            
            public final int component2() {
                return 0;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final com.reminderapp.service.WebDavSync.IcsUploadResult.Success copy(@org.jetbrains.annotations.NotNull()
            java.lang.String url, int count) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0002\u0003\u0004B\u0007\b\u0004\u00a2\u0006\u0002\u0010\u0002\u0082\u0001\u0002\u0005\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/reminderapp/service/WebDavSync$SyncResult;", "", "()V", "Error", "Success", "Lcom/reminderapp/service/WebDavSync$SyncResult$Error;", "Lcom/reminderapp/service/WebDavSync$SyncResult$Success;", "app_release"})
    public static abstract class SyncResult {
        
        private SyncResult() {
            super();
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0010"}, d2 = {"Lcom/reminderapp/service/WebDavSync$SyncResult$Error;", "Lcom/reminderapp/service/WebDavSync$SyncResult;", "message", "", "(Ljava/lang/String;)V", "getMessage", "()Ljava/lang/String;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "app_release"})
        public static final class Error extends com.reminderapp.service.WebDavSync.SyncResult {
            @org.jetbrains.annotations.NotNull()
            private final java.lang.String message = null;
            
            public Error(@org.jetbrains.annotations.NotNull()
            java.lang.String message) {
            }
            
            @org.jetbrains.annotations.NotNull()
            public final java.lang.String getMessage() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final java.lang.String component1() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final com.reminderapp.service.WebDavSync.SyncResult.Error copy(@org.jetbrains.annotations.NotNull()
            java.lang.String message) {
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
        
        /**
         * conflict=true：检测到上次同步后双端都有修改，已按版本覆盖（UI 提示用户）
         */
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u000f\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\u00032\b\u0010\n\u001a\u0004\u0018\u00010\u000bH\u00d6\u0003J\t\u0010\f\u001a\u00020\rH\u00d6\u0001J\t\u0010\u000e\u001a\u00020\u000fH\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0010"}, d2 = {"Lcom/reminderapp/service/WebDavSync$SyncResult$Success;", "Lcom/reminderapp/service/WebDavSync$SyncResult;", "conflict", "", "(Z)V", "getConflict", "()Z", "component1", "copy", "equals", "other", "", "hashCode", "", "toString", "", "app_release"})
        public static final class Success extends com.reminderapp.service.WebDavSync.SyncResult {
            private final boolean conflict = false;
            
            public Success(boolean conflict) {
            }
            
            public final boolean getConflict() {
                return false;
            }
            
            public Success() {
            }
            
            public final boolean component1() {
                return false;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final com.reminderapp.service.WebDavSync.SyncResult.Success copy(boolean conflict) {
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
}