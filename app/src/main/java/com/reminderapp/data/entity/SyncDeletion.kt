package com.reminderapp.data.entity
import androidx.room.*
@Entity(tableName="sync_deletions", primaryKeys=["kind", "syncId"])
data class SyncDeletion(val kind: String, val syncId: String)
@Dao interface SyncDeletionDao {
    @Query("SELECT * FROM sync_deletions") suspend fun all(): List<SyncDeletion>
    @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun insert(value: SyncDeletion)
}
