package com.reminderapp.data.dao

import androidx.room.*
import com.reminderapp.data.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE is_active = 1 ORDER BY next_trigger_at ASC")
    fun getAllActive(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE is_active = 1 AND next_trigger_at <= :now")
    suspend fun getDueReminders(now: Long): List<ReminderEntity>

    @Insert
    suspend fun insert(reminder: ReminderEntity): Long

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE reminders SET is_active = 0 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Query("SELECT * FROM reminders WHERE is_active = 1 ORDER BY next_trigger_at ASC")
    suspend fun getAllSync(): List<ReminderEntity>

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun delete(reminder: ReminderEntity) {
        deleteById(reminder.id)
    }
}
