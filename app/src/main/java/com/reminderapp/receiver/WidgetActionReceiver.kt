package com.reminderapp.receiver

import android.content.*
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.service.*
import kotlinx.coroutines.*

class WidgetActionReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_COMPLETE = "com.reminderapp.widget.ACTION_COMPLETE"
        const val ACTION_SNOOZE = "com.reminderapp.widget.ACTION_SNOOZE"
        const val EXTRA_REMINDER_ID = "reminder_id"
    }
    override fun onReceive(context: Context,intent: Intent) {
        val action=when(intent.action) {
            ACTION_COMPLETE -> ReminderActions.Action.COMPLETE
            ACTION_SNOOZE -> ReminderActions.Action.SNOOZE
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
            } catch(e: Exception) { android.util.Log.e("WidgetActionReceiver","Action failed",e) }
            finally { pending.finish() }
        }
    }
}
