package com.reminderapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminder_records",
    foreignKeys = [
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = ["id"],
            childColumns = ["reminder_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReminderRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    @ColumnInfo(name = "reminder_id") val reminderId: Long,

    // action 取值见 companion 常量（统计口径用；存储值勿改——已有历史数据落库）
    @ColumnInfo(name = "action") val action: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        /** 操作记录 action 常量（v2.0.16 枚举化，防 "notifying" 式拼写漂移；值与历史存储一致） */
        const val ACTION_CONFIRMED = "confirmed"
        const val ACTION_SNOOZED = "snoozed"
        const val ACTION_NOTIFIED = "notified"
    }
}
