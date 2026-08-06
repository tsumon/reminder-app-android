package com.reminderapp.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.reminderapp.ReminderApp
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.data.entity.ReminderRecordEntity
import com.reminderapp.model.ReminderStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * WorkManager Worker — 到时间后发送通知
 *
 * - kind == "advance"：发送「预告通知」（一次性，不重排）
 * - 其它（cycle / date / rule）：发送正式通知后，若为可重复类型，则推进到下一次触发时间
 *   并重新调度，避免「只响一次就再也不响」。
 *
 * v1.9.6 fix:
 * - 先查库校验再发通知：已确认/已软删/已停用的提醒不再弹（幽灵通知）
 * - 发通知写 notified 操作记录（统计「漏掉/忘记时段」数据源）
 * - once 提醒触发后置为 confirmed 且不重排（避免 delay=0 立即再弹）
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

        val db = AppDatabase.getInstance(applicationContext)
        val dao = db.reminderDao()
        val recordDao = db.reminderRecordDao()

        // v1.9.6 fix: 先查库校验再发通知
        val reminder = runBlocking(Dispatchers.IO) { dao.getById(reminderId) }
        if (reminder == null || !reminder.isActive ||
            reminder.status == ReminderStatus.CONFIRMED.name.lowercase() ||
            reminder.status == "overdue"
        ) {
            // 已删除/停用/已完成/已逾期：静默返回，不弹通知
            return Result.success()
        }

        notificationManager.sendReminderNotification(reminderId, title, body)

        // 写 notified 操作记录（统计「漏掉/最常忘记时段」数据源）
        runBlocking(Dispatchers.IO) {
            try {
                recordDao.insert(ReminderRecordEntity(reminderId = reminderId, action = "notified"))
            } catch (_: Exception) {
            }
        }

        // 周期 / 日期 / 规则提醒：到点后推进到下一次并重新调度
        runBlocking(Dispatchers.IO) {
            try {
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
                } else {
                    // once：触发即归档，不重排（nextTriggerAt 是过去值，schedule 会立即再弹）
                    dao.update(reminder.copy(status = ReminderStatus.CONFIRMED.name.lowercase()))
                }
            } catch (e: Exception) {
                // 通知已经发出，调度失败不影响本次提醒
            }
        }

        return Result.success()
    }
}
