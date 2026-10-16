package com.reminderapp.receiver

import android.content.*
import com.reminderapp.service.ReminderActions
import kotlinx.coroutines.*

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending=goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try { ReminderActions(context.applicationContext).deliver(intent.getLongExtra("reminder_id",0),
                intent.getLongExtra("expected_at",-1),intent.getLongExtra("revision",-1),intent.getIntExtra("advance_days",0)) }
            catch (e: Exception) { android.util.Log.e("ReminderAlarm", "Delivery failed", e) }
            finally { pending.finish() }
        }
    }
}
