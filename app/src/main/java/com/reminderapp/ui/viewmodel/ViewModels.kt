package com.reminderapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.data.entity.ReminderRecordEntity
import com.reminderapp.service.ReminderEngine
import com.reminderapp.service.ReminderScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
                val reminding = reminders.filter { it.status in listOf("notifying", "pending") && it.nextTriggerAt <= System.currentTimeMillis() }
                val waiting = reminders.filter { it.status in listOf("notifying", "pending", "idle") && it.nextTriggerAt > System.currentTimeMillis() }
                val completed = reminders.filter { it.status == "confirmed" }
                _groupedReminders.value = GroupedReminders(reminding, waiting, completed)
            }
        }
    }

    fun checkMissed() {
        viewModelScope.launch {
            val all = dao.getDueReminders(System.currentTimeMillis())
            val missed = ReminderEngine.checkMissed(all, System.currentTimeMillis())
            missed.forEach { r ->
                dao.update(r.copy(status = "notifying"))
            }
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            scheduler.cancel(id)
            recordDao.deleteByReminderId(id)
            dao.softDelete(id)
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
            com.reminderapp.receiver.ReminderWidgetProvider.refresh(com.reminderapp.ReminderApp.instance)
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
                updated.note.ifEmpty { "该事项仍需要你确认" }
            )
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
            com.reminderapp.receiver.ReminderWidgetProvider.refresh(com.reminderapp.ReminderApp.instance)
        }
    }
}
