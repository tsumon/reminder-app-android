package com.reminderapp.data.dao

import androidx.room.*
import com.reminderapp.data.entity.ReminderRecordEntity

@Dao
interface ReminderRecordDao {
    @Query("SELECT * FROM reminder_records WHERE reminder_id = :reminderId ORDER BY timestamp DESC")
    suspend fun getByReminderId(reminderId: Long): List<ReminderRecordEntity>

    // v1.8.7 任务③: 统计洞察需要全量记录（按时间升序便于连续打卡计算）
    @Query("SELECT * FROM reminder_records ORDER BY timestamp ASC")
    suspend fun getAll(): List<ReminderRecordEntity>

    @Insert
    suspend fun insert(record: ReminderRecordEntity): Long

    @Query("DELETE FROM reminder_records WHERE reminder_id = :reminderId")
    suspend fun deleteByReminderId(reminderId: Long)
}
