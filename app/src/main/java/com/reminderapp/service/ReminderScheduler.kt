package com.reminderapp.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.work.WorkManager
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.receiver.ReminderAlarmReceiver

/** User-visible timed reminders use AlarmManager, including Doze-aware inexact fallback. */
class ReminderScheduler(private val context: Context) {
    private val alarms = context.getSystemService(AlarmManager::class.java)
    fun canScheduleExact(): Boolean = Build.VERSION.SDK_INT < 31 || alarms.canScheduleExactAlarms()
    fun schedule(reminder: ReminderEntity) {
        cancel(reminder.id)
        if (!ReminderPolicy.shouldSchedule(reminder)) return
        register(reminder,0,ReminderPolicy.adjustQuiet(reminder,reminder.nextTriggerAt))
        if (reminder.kind == "date" && reminder.retryCount == 0) {
            for (day in 1..reminder.advanceDays.coerceIn(0,30)) {
                val at=reminder.nextTriggerAt-day*86_400_000L
                if (at > System.currentTimeMillis()) register(reminder,day,ReminderPolicy.adjustQuiet(reminder,at))
            }
        }
    }
    fun scheduleAdvance(r: ReminderEntity,day: Int,at: Long) { if(ReminderPolicy.shouldSchedule(r)) register(r,day,at) }
    private fun pending(id: Long, day: Int, reminder: ReminderEntity? = null): PendingIntent {
        val intent=Intent(context,ReminderAlarmReceiver::class.java).apply {
            data=Uri.parse("reminder://alarm/$id/$day")
            putExtra("reminder_id",id); putExtra("advance_days",day)
            reminder?.let { putExtra("expected_at",it.nextTriggerAt); putExtra("revision",it.revision) }
        }
        return PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
    private fun register(r: ReminderEntity,day: Int,at: Long) {
        val pi=pending(r.id,day,r)
        val due=maxOf(System.currentTimeMillis()+1000,at)
        try {
            if (canScheduleExact()) alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,due,pi)
            else alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,due,pi)
        } catch (_: SecurityException) { alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,due,pi) }
    }
    fun cancel(id: Long) {
        for (day in 0..30) alarms.cancel(pending(id,day))
        // Cancel jobs created by older versions during upgrade.
        WorkManager.getInstance(context).cancelAllWorkByTag(id.toString())
    }
    fun cancelAll() { WorkManager.getInstance(context).cancelAllWorkByTag("reminder_schedule") }
    suspend fun rescheduleAll(reminders: List<ReminderEntity>) { cancelAll(); reminders.forEach(::schedule) }
    fun ensureScheduled(reminders: List<ReminderEntity>) { reminders.forEach(::schedule) }
}
