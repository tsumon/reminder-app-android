package com.reminderapp.data.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\u0007\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u0097@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0014\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\r0\fH\'J\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00050\rH\u00a7@\u00a2\u0006\u0002\u0010\u000fJ\u000e\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00050\rH\'J\u0018\u0010\u0011\u001a\u0004\u0018\u00010\u00052\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0018\u0010\u0012\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u0013\u001a\u00020\u0014H\u00a7@\u00a2\u0006\u0002\u0010\u0015J\u001c\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00050\r2\u0006\u0010\u0017\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\u0018\u001a\u00020\t2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0019\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\u001a\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u001b"}, d2 = {"Lcom/reminderapp/data/dao/ReminderDao;", "", "delete", "", "reminder", "Lcom/reminderapp/data/entity/ReminderEntity;", "(Lcom/reminderapp/data/entity/ReminderEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteById", "id", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllActive", "Lkotlinx/coroutines/flow/Flow;", "", "getAllSync", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllSyncBlocking", "getById", "getBySyncId", "syncId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getDueReminders", "now", "insert", "softDelete", "update", "app_debug"})
@androidx.room.Dao()
public abstract interface ReminderDao {
    
    @androidx.room.Query(value = "SELECT * FROM reminders WHERE is_active = 1 ORDER BY CASE priority WHEN \'high\' THEN 0 WHEN \'normal\' THEN 1 ELSE 2 END, next_trigger_at ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.reminderapp.data.entity.ReminderEntity>> getAllActive();
    
    @androidx.room.Query(value = "SELECT * FROM reminders WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getById(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.reminderapp.data.entity.ReminderEntity> $completion);
    
    /**
     * 阶段2: 按跨端稳定 ID 查（WebDAV upsert / 导入匹配用）
     */
    @androidx.room.Query(value = "SELECT * FROM reminders WHERE sync_id = :syncId LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getBySyncId(@org.jetbrains.annotations.NotNull()
    java.lang.String syncId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.reminderapp.data.entity.ReminderEntity> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM reminders WHERE is_active = 1 AND next_trigger_at <= :now")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getDueReminders(long now, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.reminderapp.data.entity.ReminderEntity>> $completion);
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.reminderapp.data.entity.ReminderEntity reminder, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object update(@org.jetbrains.annotations.NotNull()
    com.reminderapp.data.entity.ReminderEntity reminder, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM reminders WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteById(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE reminders SET is_active = 0 WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object softDelete(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM reminders WHERE is_active = 1 ORDER BY CASE priority WHEN \'high\' THEN 0 WHEN \'normal\' THEN 1 ELSE 2 END, next_trigger_at ASC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAllSync(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.reminderapp.data.entity.ReminderEntity>> $completion);
    
    /**
     * 同步查询（小组件/后台线程使用，不挂起）
     */
    @androidx.room.Query(value = "SELECT * FROM reminders WHERE is_active = 1 ORDER BY CASE priority WHEN \'high\' THEN 0 WHEN \'normal\' THEN 1 ELSE 2 END, next_trigger_at ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract java.util.List<com.reminderapp.data.entity.ReminderEntity> getAllSyncBlocking();
    
    @androidx.room.Query(value = "DELETE FROM reminders WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object delete(@org.jetbrains.annotations.NotNull()
    com.reminderapp.data.entity.ReminderEntity reminder, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
        
        @androidx.room.Query(value = "DELETE FROM reminders WHERE id = :id")
        @org.jetbrains.annotations.Nullable()
        public static java.lang.Object delete(@org.jetbrains.annotations.NotNull()
        com.reminderapp.data.dao.ReminderDao $this, @org.jetbrains.annotations.NotNull()
        com.reminderapp.data.entity.ReminderEntity reminder, @org.jetbrains.annotations.NotNull()
        kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
            return null;
        }
    }
}