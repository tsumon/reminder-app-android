package com.reminderapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.service.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 开机重注册所有提醒
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val db = AppDatabase.getInstance(context)
        val scheduler = ReminderScheduler(context)

        // v1.9.6 fix: goAsync() 保活——开机广播返回后进程可被立即回收，
        // 不加保活重排常来不及完成（用户不开 App 时开机恢复只靠这里）
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val allActive = db.reminderDao().getAllActive().first()
                scheduler.rescheduleAll(allActive)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
