package com.reminderapp.service

import android.content.Context
import androidx.work.*
import com.reminderapp.data.entity.ReminderEntity
import java.util.concurrent.TimeUnit

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
     */
    fun schedule(reminder: ReminderEntity) {
        val delay = maxOf(0L, reminder.nextTriggerAt - System.currentTimeMillis())
        val requestId = reminder.id.toString()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setId(java.util.UUID.nameUUIDFromBytes(requestId.toByteArray()))
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

        WorkManager.getInstance(context).enqueue(request)
    }

    /**
     * 取消该提醒的所有待执行任务
     */
    fun cancel(reminderId: Long) {
        WorkManager.getInstance(context).cancelAllWorkByTag(reminderId.toString())
    }

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

    private fun buildNotificationBody(reminder: ReminderEntity): String {
        return reminder.note.ifEmpty { "该提醒事项需要你确认完成" }
    }
}
