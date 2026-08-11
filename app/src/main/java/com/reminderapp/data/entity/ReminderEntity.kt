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

    // === 规则提醒字段（kind = "rule"：每月/每季度/每年 第N周周X）===
    @ColumnInfo(name = "rule_period") val rulePeriod: String? = null,   // monthly / quarterly / yearly
    @ColumnInfo(name = "rule_week") val ruleWeek: Int? = null,          // 第几周 1-5
    @ColumnInfo(name = "rule_weekday") val ruleWeekday: Int? = null,    // 周几 1=周一 ... 7=周日

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
    @ColumnInfo(name = "priority") val priority: String = "normal", // high / normal / low
    @ColumnInfo(name = "status") val status: String = "idle",
    @ColumnInfo(name = "first_trigger_at") val firstTriggerAt: Long,
    @ColumnInfo(name = "next_trigger_at") val nextTriggerAt: Long,
    @ColumnInfo(name = "last_confirmed_at") val lastConfirmedAt: Long? = null,
    @ColumnInfo(name = "retry_count") val retryCount: Int = 0,

    // === 节假日前移（Item 2）===
    // 非空表示该次触发因恰逢法定节假日被前移到假期前最近工作日，
    // 文案形如「因中秋节放假，已前移至假期前最近工作日（09-14）」。
    // 确认完成后清空；仅 kind∈{cycle,rule} 的提醒会参与前移，生日/节假日日期提醒不参与。
    @ColumnInfo(name = "holiday_adjust_note") val holidayAdjustNote: String? = null,

    // === 关键提醒（批次3 功能5）===
    // 关键提醒走更高优先级渠道 + 全屏弹窗（setFullScreenIntent），确保重要事项不被漏看。
    @ColumnInfo(name = "is_critical") val isCritical: Boolean = false,

    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_active") val isActive: Boolean = true
)
