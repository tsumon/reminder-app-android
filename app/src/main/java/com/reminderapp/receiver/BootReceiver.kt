package com.reminderapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 开机重注册所有提醒
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_MY_PACKAGE_REPLACED, android.app.AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED)) return

        // v1.9.6 fix: goAsync() 保活——开机广播返回后进程可被立即回收，
        // 不加保活重排常来不及完成（用户不开 App 时开机恢复只靠这里）
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                com.reminderapp.service.QuietHoursStore.migrateLegacy(context)
                com.reminderapp.service.ReminderSyncCoordinator(context).restoreSchedules()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
