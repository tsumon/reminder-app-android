package com.reminderapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 提醒实体 — 镜像 iOS Reminder.swift
 */
@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    // 分类：cycle（周期提醒）/ date（日期提醒）
    @ColumnInfo(name = "kind") val kind: String = "cycle",

    // === 周期提醒字段 ===
    @ColumnInfo(name = "cycle") val cycle: String = "weekly",
    @ColumnInfo(name = "custom_days") val customDays: Int = 0,

    // === 日期提醒字段 ===
    @ColumnInfo(name = "date_type") val dateType: String? = null,
    @ColumnInfo(name = "target_month") val targetMonth: Int? = null,
    @ColumnInfo(name = "target_day") val targetDay: Int? = null,
    @ColumnInfo(name = "holiday_name") val holidayName: String? = null,
    @ColumnInfo(name = "advance_days") val advanceDays: Int = 3,
    @ColumnInfo(name = "reminder_hour") val reminderHour: Int = 9,
    @ColumnInfo(name = "reminder_minute") val reminderMinute: Int = 0,

    // === 通用字段 ===
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "note") val note: String = "",
    @ColumnInfo(name = "status") val status: String = "idle",
    @ColumnInfo(name = "first_trigger_at") val firstTriggerAt: Long,
    @ColumnInfo(name = "next_trigger_at") val nextTriggerAt: Long,
    @ColumnInfo(name = "last_confirmed_at") val lastConfirmedAt: Long? = null,
    @ColumnInfo(name = "retry_count") val retryCount: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_active") val isActive: Boolean = true
)
