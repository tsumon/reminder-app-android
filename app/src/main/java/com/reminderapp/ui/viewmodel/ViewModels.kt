package com.reminderapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.data.entity.ReminderRecordEntity
import com.reminderapp.service.ReminderEngine
import com.reminderapp.service.ReminderScheduler
import com.reminderapp.service.StatsService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf

class HomeViewModel(
    private val dao: com.reminderapp.data.dao.ReminderDao,
    private val recordDao: com.reminderapp.data.dao.ReminderRecordDao,
    private val scheduler: ReminderScheduler
) : ViewModel() {

    val allReminders: StateFlow<List<ReminderEntity>> = dao.getAllActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _groupedReminders = MutableStateFlow(GroupedReminders())
    val groupedReminders: StateFlow<GroupedReminders> = _groupedReminders.asStateFlow()

    // 批次2 功能2: 正向反馈 —— 打卡成功后展示「打卡成功 · 连续 N 天」卡片
    private val _checkInFeedback = MutableStateFlow<String?>(null)
    val checkInFeedback: StateFlow<String?> = _checkInFeedback.asStateFlow()

    /** UI 消费打卡反馈（展示后清除，避免重复弹出） */
    fun consumeCheckInFeedback() {
        _checkInFeedback.value = null
    }

    init {
        viewModelScope.launch {
            allReminders.collect { reminders ->
                // v1.9.7: overdue（递增重试到上限）始终显示在「需要确认」区，
                // 用户可对其点确认/重新打开恢复周期
                val reminding = reminders.filter {
                    it.status == "overdue" ||
                        (it.status in listOf("notifying", "pending") && it.nextTriggerAt <= System.currentTimeMillis())
                }
                val waiting = reminders.filter { it.status in listOf("notifying", "pending", "idle") && it.nextTriggerAt > System.currentTimeMillis() }
                val completed = reminders.filter { it.status == "confirmed" }
                _groupedReminders.value = GroupedReminders(reminding, waiting, completed)
            }
        }
    }

    /**
     * 检查遗漏提醒
     *
     * 除了把状态标为「提醒中」，还必须重新排期：
     * 到点没响通常意味着 WorkManager 任务已经丢失（进程被杀 / 系统清理），
     * 只改状态的话这条提醒就永远不会再响了。
     *
     * 已经是 notifying 的说明上一轮已经补过一次，跳过重排，
     * 避免一次性提醒每次进入 App 都重复弹通知。
     */
    fun checkMissed() {
        viewModelScope.launch {
            val all = dao.getDueReminders(System.currentTimeMillis())
            val missed = ReminderEngine.checkMissed(all, System.currentTimeMillis())
            missed.forEach { r ->
                val alreadyNotifying = r.status == "notifying"
                val updated = r.copy(status = "notifying")
                dao.update(updated)
                if (!alreadyNotifying) scheduler.schedule(updated)
            }
        }
    }

    /** App 启动时确保所有活跃提醒都有 WorkManager 排期 */
    fun ensureSchedules() {
        viewModelScope.launch {
            val active = dao.getAllSync()
            if (active.isNotEmpty()) scheduler.ensureScheduled(active)
        }
    }

    /**
     * 滑动完成：确认当前提醒（周期类自动前进到下一次；一次性提醒归档）
     *
     * @param isMakeUp 是否为「补打今天」（逾期后补打卡）。行为与普通确认一致
     *                 （按今天完成 + 推进周期 + 计入统计），只区分反馈文案。
     */
    fun confirmReminder(reminder: ReminderEntity, isMakeUp: Boolean = false) {
        viewModelScope.launch {
            val updated = ReminderEngine.confirm(reminder)
            dao.update(updated)
            recordDao.insert(ReminderRecordEntity(reminderId = reminder.id, action = ReminderRecordEntity.ACTION_CONFIRMED))
            // v1.9.6 fix: 确认后取消已显示的通知，避免通知栏残留
            com.reminderapp.service.NotificationManager(com.reminderapp.ReminderApp.instance).cancelReminderNotifications(reminder.id)
            // v2.0.22: 一次性提醒确认后归档，不重排（对齐 NotificationActionReceiver，
            // 避免 delay=0 的 WorkManager 立即再弹一条「已完成」）
            if (!(updated.kind == "cycle" && updated.cycle == "once")) {
                scheduler.schedule(updated)
            }
            com.reminderapp.service.SyncStore.touchLocalChange()
            com.reminderapp.receiver.ReminderWidgetProvider.refresh(com.reminderapp.ReminderApp.instance)
            // 批次2 功能2: 打卡成功 → 正向反馈卡片（含连续打卡天数）
            publishCheckInFeedback(isMakeUp)
            // Item 2: 确认后预检下一次触发是否遇节假日
            viewModelScope.launch {
                com.reminderapp.service.HolidayPreCheck.run(com.reminderapp.ReminderApp.instance)
            }
            // v1.8.7 任务⑥: 埋点
            com.reminderapp.service.TelemetryService.logEvent(
                if (isMakeUp) "confirm_makeup" else "confirm",
                mapOf("kind" to reminder.kind, "cycle" to reminder.cycle)
            )
        }
    }

    /** 批次2 功能2: 计算当前连续打卡天数并发布正向反馈 */
    private suspend fun publishCheckInFeedback(isMakeUp: Boolean = false) {
        val okText = if (isMakeUp) zh("补打卡成功 🎉") else zh("打卡成功 🎉")
        try {
            val records = recordDao.getAll()
            val streak = StatsService.summarize(records).currentStreak
            _checkInFeedback.value =
                if (streak > 1) {
                    if (isMakeUp) zhf("补打卡成功，已连续 %1\$d 天 🎉", streak)
                    else zhf("打卡成功，已连续 %1\$d 天 🎉", streak)
                } else okText
        } catch (_: Exception) {
            _checkInFeedback.value = okText
        }
    }

    /** 撤销完成：把提醒放回等待中 */
    fun reopenReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            val next = if (reminder.nextTriggerAt <= System.currentTimeMillis())
                ReminderEngine.calculateNextTrigger(reminder) else reminder.nextTriggerAt
            val updated = reminder.copy(status = "pending", nextTriggerAt = next, retryCount = 0, holidayAdjustNote = null)
            dao.update(updated)
            scheduler.schedule(updated)
            com.reminderapp.service.SyncStore.touchLocalChange()
            com.reminderapp.receiver.ReminderWidgetProvider.refresh(com.reminderapp.ReminderApp.instance)
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            scheduler.cancel(id)
            recordDao.deleteByReminderId(id)
            dao.softDelete(id)
            // v2.0.22: 删除时同时取消已显示的通知，避免旧通知操作已删除的提醒
            com.reminderapp.service.NotificationManager(com.reminderapp.ReminderApp.instance).cancelReminderNotifications(id)
            com.reminderapp.service.SyncStore.touchLocalChange()
            com.reminderapp.receiver.ReminderWidgetProvider.refresh(com.reminderapp.ReminderApp.instance)
        }
    }

    data class GroupedReminders(
        val reminding: List<ReminderEntity> = emptyList(),
        val waiting: List<ReminderEntity> = emptyList(),
        val completed: List<ReminderEntity> = emptyList()
    )
}

class ReminderDetailViewModel(
    reminderId: Long,
    private val dao: com.reminderapp.data.dao.ReminderDao,
    private val recordDao: com.reminderapp.data.dao.ReminderRecordDao,
    private val scheduler: ReminderScheduler,
    private val notificationMgr: com.reminderapp.service.NotificationManager
) : ViewModel() {

    private val _reminder = MutableStateFlow<ReminderEntity?>(null)
    val reminder: StateFlow<ReminderEntity?> = _reminder.asStateFlow()

    private val _records = MutableStateFlow<List<ReminderRecordEntity>>(emptyList())
    val records: StateFlow<List<ReminderRecordEntity>> = _records.asStateFlow()

    // 批次2 功能2: 正向反馈 —— 确认后展示「打卡成功 · 连续 N 天」卡片
    private val _checkInFeedback = MutableStateFlow<String?>(null)
    val checkInFeedback: StateFlow<String?> = _checkInFeedback.asStateFlow()

    /** UI 消费打卡反馈（展示后清除，避免重复弹出） */
    fun consumeCheckInFeedback() {
        _checkInFeedback.value = null
    }

    init {
        viewModelScope.launch {
            _reminder.value = dao.getById(reminderId)
            _records.value = recordDao.getByReminderId(reminderId)
        }
    }

    /**
     * 确认完成。
     *
     * @param isMakeUp 是否为「补打今天」（逾期后补打卡）。行为与普通确认完全一致
     *                 （按今天完成 + 推进周期 + 计入统计），只是反馈文案区分，
     *                 不改 Room schema，避免为一句文案做迁移。
     */
    fun confirm(isMakeUp: Boolean = false) {
        viewModelScope.launch {
            val r = _reminder.value ?: return@launch
            val updated = ReminderEngine.confirm(r)
            dao.update(updated)
            recordDao.insert(ReminderRecordEntity(reminderId = r.id, action = ReminderRecordEntity.ACTION_CONFIRMED))
            // v2.0.22: 一次性提醒确认后归档，不重排（对齐 NotificationActionReceiver）
            if (!(updated.kind == "cycle" && updated.cycle == "once")) {
                scheduler.schedule(updated)
            }
            notificationMgr.cancelReminderNotifications(r.id)
            _reminder.value = updated
            com.reminderapp.service.SyncStore.touchLocalChange()
            com.reminderapp.receiver.ReminderWidgetProvider.refresh(com.reminderapp.ReminderApp.instance)
            // 批次2 功能2: 打卡成功 → 正向反馈卡片
            val okText = if (isMakeUp) zh("补打卡成功 🎉") else zh("打卡成功 🎉")
            try {
                val streak = StatsService.summarize(recordDao.getAll()).currentStreak
                _checkInFeedback.value =
                    if (streak > 1) {
                        if (isMakeUp) zhf("补打卡成功，已连续 %1\$d 天 🎉", streak)
                        else zhf("打卡成功，已连续 %1\$d 天 🎉", streak)
                    } else okText
            } catch (_: Exception) {
                _checkInFeedback.value = okText
            }
            // Item 2: 确认后预检下一次触发是否遇节假日
            viewModelScope.launch {
                com.reminderapp.service.HolidayPreCheck.run(com.reminderapp.ReminderApp.instance)
            }
        }
    }

    fun snooze(minutes: Long = 15) {
        viewModelScope.launch {
            val r = _reminder.value ?: return@launch
            val updated = ReminderEngine.snooze(r, minutes)
            dao.update(updated)
            recordDao.insert(ReminderRecordEntity(reminderId = r.id, action = ReminderRecordEntity.ACTION_SNOOZED))
            scheduler.schedule(updated)
            notificationMgr.cancelReminderNotifications(r.id)
            _reminder.value = updated
            // v2.0.22: 稍后同样参与同步版本与小组件刷新（对齐通知栏/小组件路径）
            com.reminderapp.service.SyncStore.touchLocalChange()
            com.reminderapp.receiver.ReminderWidgetProvider.refresh(com.reminderapp.ReminderApp.instance)
        }
    }

    /** v2.1.0: 稍后到明天提醒时刻 */
    fun snoozeTomorrow() {
        viewModelScope.launch {
            val r = _reminder.value ?: return@launch
            val updated = ReminderEngine.snoozeTomorrow(r)
            dao.update(updated)
            recordDao.insert(ReminderRecordEntity(reminderId = r.id, action = ReminderRecordEntity.ACTION_SNOOZED))
            scheduler.schedule(updated)
            notificationMgr.cancelReminderNotifications(r.id)
            _reminder.value = updated
            com.reminderapp.service.SyncStore.touchLocalChange()
            com.reminderapp.receiver.ReminderWidgetProvider.refresh(com.reminderapp.ReminderApp.instance)
        }
    }

    /** 批次3 功能5: 切换关键提醒标记（落库 + 按对应渠道重排通知） */
    fun setCritical(value: Boolean) {
        viewModelScope.launch {
            val r = _reminder.value ?: return@launch
            val updated = r.copy(isCritical = value)
            dao.update(updated)
            notificationMgr.cancelReminderNotifications(r.id)
            // 仅当提醒仍生效时重排，使其走对应的通知渠道
            if (updated.isActive &&
                updated.status != "confirmed" &&
                updated.status != "overdue"
            ) {
                scheduler.schedule(updated)
            }
            _reminder.value = updated
            com.reminderapp.service.SyncStore.touchLocalChange()
        }
    }

    fun escalate() {
        viewModelScope.launch {
            val r = _reminder.value ?: return@launch
            val updated = ReminderEngine.escalate(r)
            dao.update(updated)
            recordDao.insert(ReminderRecordEntity(reminderId = r.id, action = ReminderRecordEntity.ACTION_NOTIFIED))
            // 批次3 功能5: 关键提醒走最高优先级渠道 + 全屏弹窗
            if (updated.isCritical) {
                notificationMgr.sendCriticalReminderNotification(
                    updated.id,
                    "⏰ ${updated.title}",
                    updated.note.ifEmpty { zh("该事项仍需要你确认") }
                )
            } else {
                notificationMgr.sendReminderNotification(
                    updated.id,
                    "⏰ ${updated.title}",
                    updated.note.ifEmpty { zh("该事项仍需要你确认") }
                )
            }
            // v1.9.7: 对齐 iOS —— 重试必须重新排期，否则下一次到点永远不会响；
            // 已逾期（达到重试上限）不再排期
            if (updated.status != "overdue") scheduler.schedule(updated)
            _reminder.value = updated
        }
    }

    /** 删除提醒（取消调度 + 取消已显示通知 + 删除记录 + 软删除） */
    fun delete() {
        viewModelScope.launch {
            val r = _reminder.value ?: return@launch
            scheduler.cancel(r.id)
            notificationMgr.cancelReminderNotifications(r.id)
            recordDao.deleteByReminderId(r.id)
            dao.softDelete(r.id)
            com.reminderapp.service.SyncStore.touchLocalChange()
            com.reminderapp.receiver.ReminderWidgetProvider.refresh(com.reminderapp.ReminderApp.instance)
        }
    }
}
