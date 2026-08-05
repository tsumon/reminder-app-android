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

    // action: "confirmed" / "snoozed" / "notified"
    @ColumnInfo(name = "action") val action: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)
