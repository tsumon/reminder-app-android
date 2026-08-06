package com.reminderapp.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.reminderapp.ReminderApp
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.model.ReminderStatus
import com.reminderapp.service.ReminderEngine
import com.reminderapp.service.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * WorkManager Worker — 到时间后发送通知
 *
 * - kind == "advance"：发送「预告通知」（一次性，不重排）
 * - 其它（cycle / date / rule）：发送正式通知后，若为可重复类型，则推进到下一次触发时间
 *   并重新调度，避免「只响一次就再也不响」。
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val reminderId = inputData.getLong("reminder_id", 0)
        val title = inputData.getString("title") ?: "提醒"
        val body = inputData.getString("body") ?: ""
        val kind = inputData.getString("kind") ?: "cycle"

        val notificationManager = NotificationManager(applicationContext)

        // 预告通知：直接发送，不做任何重排
        if (kind == "advance") {
            notificationManager.sendAdvanceNotification(reminderId, "📅 即将到来：$title", body)
            return Result.success()
        }

        notificationManager.sendReminderNotification(reminderId, title, body)

        // 周期 / 日期 / 规则提醒：到点后推进到下一次并重新调度
        runBlocking(Dispatchers.IO) {
            try {
                val dao = AppDatabase.getInstance(applicationContext).reminderDao()
                val reminder = dao.getById(reminderId) ?: return@runBlocking
                if (!reminder.isActive) return@runBlocking
                if (reminder.status == ReminderStatus.CONFIRMED.name.lowercase()) return@runBlocking

                val shouldReschedule = when (reminder.kind) {
                    "cycle" -> reminder.cycle != "once"
                    "date" -> true   // 生日 / 节假日每年重复
                    "rule" -> true
                    else -> false
                }
                if (shouldReschedule) {
                    val next = ReminderEngine.calculateNextTrigger(reminder)
                    val updated = reminder.copy(
                        status = ReminderStatus.PENDING.name.lowercase(),
                        nextTriggerAt = next,
                        retryCount = 0
                    )
                    dao.update(updated)
                    ReminderScheduler(applicationContext).schedule(updated)
                }
            } catch (e: Exception) {
                // 通知已经发出，调度失败不影响本次提醒
            }
        }

        return Result.success()
    }
}
