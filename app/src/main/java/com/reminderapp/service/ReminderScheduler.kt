package com.reminderapp.service

import android.content.Context
import androidx.work.*
import com.reminderapp.data.entity.ReminderEntity
import java.util.concurrent.TimeUnit
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf

/**
 * 提醒调度器 — 用 WorkManager 管理后台定时任务
 * 负责周期/日期提醒和递增重试的精确调度
 */
class ReminderScheduler(private val context: Context) {

    companion object {
        private const val TAG_REMINDER = "reminder_schedule"
    }

    /**
     * 为提醒注册 WorkManager 定时任务
     *
     * - 主提醒（D-day）始终排一次
     * - 日期类提醒（生日 / 节假日）：额外排「提前 N 天」的预告通知
     */
    fun schedule(reminder: ReminderEntity) {
        val delay = maxOf(0L, reminder.nextTriggerAt - System.currentTimeMillis())
        val requestId = reminder.id.toString()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "reminder_id" to reminder.id,
                    "title" to reminder.title,
                    "body" to buildNotificationBody(reminder),
                    "kind" to reminder.kind
                )
            )
            .addTag(TAG_REMINDER)
            .addTag(requestId)
            .build()

        // 必须用 UniqueWork + REPLACE：
        // WorkManager 的 enqueue() 对已存在的 WorkSpec 是 IGNORE 策略，
        // 用固定 id 直接 enqueue 会导致「Worker 内部排下一次」被静默丢弃 —— 提醒只响一次。
        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueName(reminder.id),
            ExistingWorkPolicy.REPLACE,
            request
        )

        // 日期类提醒：排提前预告通知（N 天前）
        if (reminder.kind == "date" && reminder.advanceDays > 0) {
            for (d in 1..reminder.advanceDays) {
                val advanceAt = reminder.nextTriggerAt - d * 86_400_000L
                if (advanceAt <= System.currentTimeMillis()) continue
                val advanceDelay = advanceAt - System.currentTimeMillis()
                val advanceRequestId = "${reminder.id}-advance-$d"
                val advanceRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(advanceDelay, TimeUnit.MILLISECONDS)
                    .setInputData(
                        workDataOf(
                            "reminder_id" to reminder.id,
                            "title" to reminder.title,
                            "body" to zhf("还有 %1\$s 天就是「%2\$s」了，提前做好准备", d, reminder.title),
                            "kind" to "advance"
                        )
                    )
                    .addTag(TAG_REMINDER)
                    .addTag(requestId) // 用主 id 作 tag，删除时可一并取消
                    .addTag(advanceRequestId)
                    .build()
                WorkManager.getInstance(context).enqueueUniqueWork(
                    advanceUniqueName(reminder.id, d),
                    ExistingWorkPolicy.REPLACE,
                    advanceRequest
                )
            }
        }
    }

    /**
     * 取消该提醒的所有待执行任务（主提醒 + 预告）
     */
    fun cancel(reminderId: Long) {
        val wm = WorkManager.getInstance(context)
        wm.cancelUniqueWork(uniqueName(reminderId))
        wm.cancelAllWorkByTag(reminderId.toString())
    }

    private fun uniqueName(reminderId: Long) = "reminder_$reminderId"

    private fun advanceUniqueName(reminderId: Long, days: Int) = "reminder_${reminderId}_advance_$days"

    /**
     * 取消所有提醒任务（谨慎使用）
     */
    fun cancelAll() {
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG_REMINDER)
    }

    /**
     * 重新调度所有活跃提醒（开机后调用）
     */
    suspend fun rescheduleAll(reminders: List<ReminderEntity>) {
        cancelAll()
        reminders.forEach { schedule(it) }
    }

    /**
     * 确保所有活跃提醒都有排期（App 每次启动调用）
     *
     * 与 [rescheduleAll] 的区别：不做全量取消，仅逐条 REPLACE 入队，
     * 因此不会打断正在等待的任务，可以安全地在每次冷启动时调用，
     * 用于修复「系统清理了 WorkManager 任务后提醒再也不响」。
     */
    fun ensureScheduled(reminders: List<ReminderEntity>) {
        reminders.forEach { schedule(it) }
    }

    private fun buildNotificationBody(reminder: ReminderEntity): String {
        return reminder.note.ifEmpty { zh("该提醒事项需要你确认完成") }
    }
}
