package com.reminderapp.data.entity;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\b\u0087\b\u0018\u0000 \u001a2\u00020\u0001:\u0001\u001aB)\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\bJ\t\u0010\u000f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0010\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0012\u001a\u00020\u0003H\u00c6\u0003J1\u0010\u0013\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0017\u001a\u00020\u0018H\u00d6\u0001J\t\u0010\u0019\u001a\u00020\u0006H\u00d6\u0001R\u0016\u0010\u0005\u001a\u00020\u00068\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0016\u0010\u0004\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\fR\u0016\u0010\u0007\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\f\u00a8\u0006\u001b"}, d2 = {"Lcom/reminderapp/data/entity/ReminderRecordEntity;", "", "id", "", "reminderId", "action", "", "timestamp", "(JJLjava/lang/String;J)V", "getAction", "()Ljava/lang/String;", "getId", "()J", "getReminderId", "getTimestamp", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "", "toString", "Companion", "app_release"})
@androidx.room.Entity(tableName = "reminder_records", foreignKeys = {@androidx.room.ForeignKey(entity = com.reminderapp.data.entity.ReminderEntity.class, parentColumns = {"id"}, childColumns = {"reminder_id"}, onDelete = 5)})
public final class ReminderRecordEntity {
    @androidx.room.PrimaryKey(autoGenerate = true)
    private final long id = 0L;
    @androidx.room.ColumnInfo(name = "reminder_id")
    private final long reminderId = 0L;
    @androidx.room.ColumnInfo(name = "action")
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String action = null;
    @androidx.room.ColumnInfo(name = "timestamp")
    private final long timestamp = 0L;
    
    /**
     * 操作记录 action 常量（v2.0.16 枚举化，防 "notifying" 式拼写漂移；值与历史存储一致）
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_CONFIRMED = "confirmed";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_SNOOZED = "snoozed";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_NOTIFIED = "notified";
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.data.entity.ReminderRecordEntity.Companion Companion = null;
    
    public ReminderRecordEntity(long id, long reminderId, @org.jetbrains.annotations.NotNull()
    java.lang.String action, long timestamp) {
        super();
    }
    
    public final long getId() {
        return 0L;
    }
    
    public final long getReminderId() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAction() {
        return null;
    }
    
    public final long getTimestamp() {
        return 0L;
    }
    
    public final long component1() {
        return 0L;
    }
    
    public final long component2() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    public final long component4() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.reminderapp.data.entity.ReminderRecordEntity copy(long id, long reminderId, @org.jetbrains.annotations.NotNull()
    java.lang.String action, long timestamp) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/reminderapp/data/entity/ReminderRecordEntity$Companion;", "", "()V", "ACTION_CONFIRMED", "", "ACTION_NOTIFIED", "ACTION_SNOOZED", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}