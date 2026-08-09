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
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf

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
        val title = inputData.getString("title") ?: zh("提醒")
        val body = inputData.getString("body") ?: ""
        val kind = inputData.getString("kind") ?: "cycle"

        val notificationManager = NotificationManager(applicationContext)

        // 预告通知：直接发送，不做任何重排
        if (kind == "advance") {
            notificationManager.sendAdvanceNotification(reminderId, zhf("📅 即将到来：%s", title), body)
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

        // 周期 / 日期 / 规则提醒：到点后递增重试（对齐 iOS escalateRetry），
        // 未确认不推进周期 —— 1h → 4h → 12h → 24h → 24h → overdue
        runBlocking(Dispatchers.IO) {
            try {
                // v1.9.6 fix TOCTOU: 发通知后用户可能已点「确认/稍后」（写库+重排），
                // 必须重读最新状态，避免用过期快照覆盖用户操作
                val latest = dao.getById(reminderId) ?: return@runBlocking
                if (!latest.isActive ||
                    latest.status == ReminderStatus.CONFIRMED.name.lowercase() ||
                    latest.status == "overdue") return@runBlocking

                // v1.9.7: 接上之前是死代码的 escalate() —— 到点未确认先按递增间隔重试
                // （once 也走重试，与 iOS 一致；用户点「确认」才会归档/推进周期）。
                // 达到 5 次上限后标 overdue 停止轰炸，提醒留在列表等待用户手动处理。
                val escalated = ReminderEngine.escalate(latest)
                dao.update(escalated)
                if (escalated.status != "overdue") {
                    ReminderScheduler(applicationContext).schedule(escalated)
                }
                // v1.9.6 fix: Worker 是后台主路径——重试/逾期必须参与同步版本号，
                // 否则这些状态变化永远不上传（另一设备同步会把推进回滚）
                SyncStore.touchLocalChange()
                com.reminderapp.receiver.ReminderWidgetProvider.refresh(applicationContext)
            } catch (e: Exception) {
                // 通知已经发出，调度失败不影响本次提醒
            }
        }

        return Result.success()
    }
}
