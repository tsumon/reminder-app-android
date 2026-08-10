package com.reminderapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.data.entity.ReminderRecordEntity
import com.reminderapp.service.ReminderEngine
import com.reminderapp.service.ReminderScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.reminderapp.i18n.zh

class HomeViewModel(
    private val dao: com.reminderapp.data.dao.ReminderDao,
    private val recordDao: com.reminderapp.data.dao.ReminderRecordDao,
    private val scheduler: ReminderScheduler
) : ViewModel() {

    val allReminders: StateFlow<List<ReminderEntity>> = dao.getAllActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _groupedReminders = MutableStateFlow(GroupedReminders())
    val groupedReminders: StateFlow<GroupedReminders> = _groupedReminders.asStateFlow()

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

    /** 滑动完成：确认当前提醒（周期类自动前进到下一次；一次性提醒归档） */
    fun confirmReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            val updated = ReminderEngine.confirm(reminder)
            dao.update(updated)
            recordDao.insert(ReminderRecordEntity(reminderId = reminder.id, action = "confirmed"))
            // v1.9.6 fix: 确认后取消已显示的通知，避免通知栏残留
            com.reminderapp.service.NotificationManager(com.reminderapp.ReminderApp.instance).cancelReminderNotifications(reminder.id)
            scheduler.schedule(updated)
            com.reminderapp.service.SyncStore.touchLocalChange()
            com.reminderapp.receiver.ReminderWidgetProvider.refresh(com.reminderapp.ReminderApp.instance)
            // Item 2: 确认后预检下一次触发是否遇节假日
            viewModelScope.launch {
                com.reminderapp.service.HolidayPreCheck.run(com.reminderapp.ReminderApp.instance)
            }
            // v1.8.7 任务⑥: 埋点
            com.reminderapp.service.TelemetryService.logEvent(
                "confirm",
                mapOf("kind" to reminder.kind, "cycle" to reminder.cycle)
            )
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

    init {
        viewModelScope.launch {
            _reminder.value = dao.getById(reminderId)
            _records.value = recordDao.getByReminderId(reminderId)
        }
    }

    fun confirm() {
        viewModelScope.launch {
            val r = _reminder.value ?: return@launch
            val updated = ReminderEngine.confirm(r)
            dao.update(updated)
            recordDao.insert(ReminderRecordEntity(reminderId = r.id, action = "confirmed"))
            scheduler.schedule(updated)
            notificationMgr.cancelReminderNotifications(r.id)
            _reminder.value = updated
            com.reminderapp.service.SyncStore.touchLocalChange()
            com.reminderapp.receiver.ReminderWidgetProvider.refresh(com.reminderapp.ReminderApp.instance)
            // Item 2: 确认后预检下一次触发是否遇节假日
            viewModelScope.launch {
                com.reminderapp.service.HolidayPreCheck.run(com.reminderapp.ReminderApp.instance)
            }
        }
    }

    fun snooze() {
        viewModelScope.launch {
            val r = _reminder.value ?: return@launch
            val updated = ReminderEngine.snooze(r)
            dao.update(updated)
            recordDao.insert(ReminderRecordEntity(reminderId = r.id, action = "snoozed"))
            scheduler.schedule(updated)
            notificationMgr.cancelReminderNotifications(r.id)
            _reminder.value = updated
        }
    }

    fun escalate() {
        viewModelScope.launch {
            val r = _reminder.value ?: return@launch
            val updated = ReminderEngine.escalate(r)
            dao.update(updated)
            recordDao.insert(ReminderRecordEntity(reminderId = r.id, action = "notified"))
            notificationMgr.sendReminderNotification(
                updated.id,
                "⏰ ${updated.title}",
                updated.note.ifEmpty { zh("该事项仍需要你确认") }
            )
            // v1.9.7: 对齐 iOS —— 重试必须重新排期，否则下一次到点永远不会响；
            // 已逾期（达到重试上限）不再排期
            if (updated.status != "overdue") scheduler.schedule(updated)
            _reminder.value = updated
        }
    }

    /** 删除提醒（取消调度 + 删除记录 + 软删除） */
    fun delete() {
        viewModelScope.launch {
            val r = _reminder.value ?: return@launch
            scheduler.cancel(r.id)
            recordDao.deleteByReminderId(r.id)
            dao.softDelete(r.id)
            com.reminderapp.service.SyncStore.touchLocalChange()
            com.reminderapp.receiver.ReminderWidgetProvider.refresh(com.reminderapp.ReminderApp.instance)
        }
    }
}
