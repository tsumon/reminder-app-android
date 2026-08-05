package com.reminderapp.data.dao

import androidx.room.*
import com.reminderapp.data.entity.ReminderRecordEntity

@Dao
interface ReminderRecordDao {
    @Query("SELECT * FROM reminder_records WHERE reminder_id = :reminderId ORDER BY timestamp DESC")
    suspend fun getByReminderId(reminderId: Long): List<ReminderRecordEntity>

    @Insert
    suspend fun insert(record: ReminderRecordEntity): Long

    @Query("DELETE FROM reminder_records WHERE reminder_id = :reminderId")
    suspend fun deleteByReminderId(reminderId: Long)
}
