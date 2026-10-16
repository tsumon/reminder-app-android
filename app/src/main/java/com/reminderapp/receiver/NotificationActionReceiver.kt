package com.reminderapp.receiver

import android.content.*
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.service.*
import kotlinx.coroutines.*

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context,intent: Intent) {
        val action=when(intent.action) {
            NotificationManager.ACTION_CONFIRM -> ReminderActions.Action.COMPLETE
            NotificationManager.ACTION_SNOOZE -> ReminderActions.Action.SNOOZE
            else -> return
        }
        val pending=goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val r=AppDatabase.getInstance(context).reminderDao().getById(intent.getLongExtra("reminder_id",0)) ?: return@launch
                // Missing tokens are legacy notifications: require opening the app after upgrade.
                if (r.nextTriggerAt != intent.getLongExtra("expected_at",-1) || r.revision != intent.getLongExtra("revision",-1)) return@launch
                if (r.toString() != intent.getStringExtra("expected_state")) return@launch
                ReminderActions(context).apply(r,action)
            } catch(e: Exception) { android.util.Log.e("NotificationActionReceiver","Action failed",e) }
            finally { pending.finish() }
        }
    }
}
