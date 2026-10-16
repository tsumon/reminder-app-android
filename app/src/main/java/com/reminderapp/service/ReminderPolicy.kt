package com.reminderapp.service

import com.reminderapp.data.entity.ReminderEntity
import java.time.*

/** Pure scheduling and stale-action policy shared by preview, delivery and actions. */
object ReminderPolicy {
    val legacyIntervals = listOf(60, 240, 720, 1440)
    fun intervals(r: ReminderEntity): List<Int> {
        val parsed = r.retryIntervalsMinutes.split(",").map { it.trim().toIntOrNull() }
        return if (parsed.size in 1..10 && parsed.all { it != null && it in 1..10080 }) parsed.filterNotNull() else legacyIntervals
    }
    fun shouldSchedule(r: ReminderEntity) = r.isActive && r.status !in setOf("confirmed", "overdue") && !(r.reminderStyle == "gentle" && r.retryCount > 0)
    fun adjustQuiet(r: ReminderEntity, at: Long, zone: ZoneId = ZoneId.systemDefault()): Long {
        val start = r.quietStartMinute ?: return at
        val end = r.quietEndMinute ?: return at
        if (start !in 0..1439 || end !in 0..1439 || start == end) return at
        val date = Instant.ofEpochMilli(at).atZone(zone)
        val minute = date.hour * 60 + date.minute
        val inside = if (start < end) minute >= start && minute < end else minute >= start || minute < end
        if (!inside) return at
        var next = date.toLocalDate().atTime(end / 60, end % 60).atZone(zone)
        if (next.toInstant().toEpochMilli() <= at) next = next.plusDays(1)
        return next.toInstant().toEpochMilli()
    }
    fun matches(r: ReminderEntity, expected: ReminderEntity) = r == expected && r.isActive
    fun canUndo(current: ReminderEntity, expected: ReminderEntity, localVersion: Long, expectedVersion: Long) = current == expected && localVersion == expectedVersion
}
