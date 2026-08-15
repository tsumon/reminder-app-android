package com.reminderapp.service;

/**
 * 提醒数据导入/导出（JSON 格式，双端统一）
 *
 * 导出文件结构：
 * {
 *  "version": 1,          // 历史格式版本（旧版读取兼容）
 *  "schemaVersion": 2,    // 阶段2: 协议版本（2 = syncId/holidayId/isCritical；缺省按 1 解析）
 *  "exportedAt": <timestamp>,
 *  "dataVersion": <本地自增版本, v2.0.17>,
 *  "reminders": [ { ... } ]
 * }
 *
 * 协议 v2 变更（阶段2）：
 * - 每条新增 syncId（跨平台稳定 UUID，iOS 端直接复用 Reminder.id）
 * - 每条新增 holidayId（节假日稳定 ID，与 iOS Holiday.id 对齐；holidayName 保留兼容旧文件）
 * - isCritical 改为 camelCase（is_critical 继续导出供旧版读取）
 * - 导入旧文件（无 schemaVersion / 无 syncId）时：字段回落默认值，syncId 由调用方补 UUID
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\"\n\u0002\b\f\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tJ(\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000b2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\f0\u000b2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\t0\u000fJ\u000e\u0010\u0010\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\fJ\u000e\u0010\u0012\u001a\u00020\t2\u0006\u0010\u0013\u001a\u00020\fJ\u001e\u0010\u0014\u001a\u00020\t2\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\f0\u000b2\b\b\u0002\u0010\u0016\u001a\u00020\u0007J\u000e\u0010\u0017\u001a\u00020\t2\u0006\u0010\u0011\u001a\u00020\fJ\u0016\u0010\u0018\u001a\n\u0012\u0004\u0012\u00020\f\u0018\u00010\u000b2\u0006\u0010\b\u001a\u00020\tJ\u0010\u0010\u0019\u001a\u0004\u0018\u00010\f2\u0006\u0010\b\u001a\u00020\tJ\u0018\u0010\u001a\u001a\u0004\u0018\u00010\t2\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u001eJ\u000e\u0010\u001f\u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\tJ\u000e\u0010 \u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\tJ\u001e\u0010!\u001a\u00020\"2\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\b\u001a\u00020\tR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006#"}, d2 = {"Lcom/reminderapp/service/BackupService;", "", "()V", "BACKUP_VERSION", "", "SCHEMA_VERSION", "dataVersionOf", "", "json", "", "dedupeByFingerprint", "", "Lcom/reminderapp/data/entity/ReminderEntity;", "entities", "existingFingerprints", "", "ensureSyncId", "r", "exportSingle", "reminder", "exportToJson", "reminders", "dataVersion", "fingerprint", "importFromJson", "importSingle", "readFromUri", "context", "Landroid/content/Context;", "uri", "Landroid/net/Uri;", "remindersCountOf", "schemaVersionOf", "writeToUri", "", "app_release"})
public final class BackupService {
    private static final int BACKUP_VERSION = 1;
    private static final int SCHEMA_VERSION = 2;
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.service.BackupService INSTANCE = null;
    
    private BackupService() {
        super();
    }
    
    /**
     * 将提醒列表导出为 JSON 字符串（dataVersion 取当前本地单调版本，判新主依据）
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String exportToJson(@org.jetbrains.annotations.NotNull()
    java.util.List<com.reminderapp.data.entity.ReminderEntity> reminders, long dataVersion) {
        return null;
    }
    
    /**
     * 从 JSON 字符串解析提醒列表（失败返回 null）
     */
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<com.reminderapp.data.entity.ReminderEntity> importFromJson(@org.jetbrains.annotations.NotNull()
    java.lang.String json) {
        return null;
    }
    
    /**
     * 导入指纹（与近场传输共用，防误判）：title|nextTriggerAt|kind|cycle|note
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String fingerprint(@org.jetbrains.annotations.NotNull()
    com.reminderapp.data.entity.ReminderEntity r) {
        return null;
    }
    
    /**
     * 阶段2: syncId 缺失时补 UUID（旧文件/手写数据），保证落库的提醒都有跨端稳定 ID
     */
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.data.entity.ReminderEntity ensureSyncId(@org.jetbrains.annotations.NotNull()
    com.reminderapp.data.entity.ReminderEntity r) {
        return null;
    }
    
    /**
     * 统一导入去重：按指纹过滤掉与现有提醒重复的条目。
     * 文件导入与近场传输共用同一规则，避免「文件导入重复新增、近场传输跳过重复」的跨入口不一致。
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.reminderapp.data.entity.ReminderEntity> dedupeByFingerprint(@org.jetbrains.annotations.NotNull()
    java.util.List<com.reminderapp.data.entity.ReminderEntity> entities, @org.jetbrains.annotations.NotNull()
    java.util.Set<java.lang.String> existingFingerprints) {
        return null;
    }
    
    /**
     * 批次3 功能6: 单条提醒分享卡片 —— 复用备份信封格式导出单条
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String exportSingle(@org.jetbrains.annotations.NotNull()
    com.reminderapp.data.entity.ReminderEntity reminder) {
        return null;
    }
    
    /**
     * 批次3 功能6: 从分享卡片 JSON 解析出单条（失败返回 null）
     */
    @org.jetbrains.annotations.Nullable()
    public final com.reminderapp.data.entity.ReminderEntity importSingle(@org.jetbrains.annotations.NotNull()
    java.lang.String json) {
        return null;
    }
    
    /**
     * 读取 JSON 中的 dataVersion（v2.0.17 单调版本；旧文件缺字段返回 0）
     */
    public final long dataVersionOf(@org.jetbrains.annotations.NotNull()
    java.lang.String json) {
        return 0L;
    }
    
    /**
     * 阶段2: 读取协议 schemaVersion（缺省视为 1——旧协议无 syncId/holidayId）
     */
    public final int schemaVersionOf(@org.jetbrains.annotations.NotNull()
    java.lang.String json) {
        return 0;
    }
    
    /**
     * 轻量读取 JSON 内提醒条数（v2.0.21 F1：首次同步冲突判定用，不做完整解析）
     */
    public final int remindersCountOf(@org.jetbrains.annotations.NotNull()
    java.lang.String json) {
        return 0;
    }
    
    /**
     * 将 JSON 字符串写入 Uri（SAF）
     */
    public final boolean writeToUri(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri, @org.jetbrains.annotations.NotNull()
    java.lang.String json) {
        return false;
    }
    
    /**
     * 从 Uri 读取全部文本
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String readFromUri(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
        return null;
    }
}