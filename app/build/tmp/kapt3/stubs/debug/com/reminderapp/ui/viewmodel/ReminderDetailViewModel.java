package com.reminderapp.ui.viewmodel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\n\u0018\u00002\u00020\u0001B-\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fJ\u0010\u0010\u001d\u001a\u00020\u001e2\b\b\u0002\u0010\u001f\u001a\u00020 J\u0006\u0010!\u001a\u00020\u001eJ\u0006\u0010\"\u001a\u00020\u001eJ\u0006\u0010#\u001a\u00020\u001eJ\u0006\u0010$\u001a\u00020\u001eJ\u000e\u0010%\u001a\u00020\u001e2\u0006\u0010&\u001a\u00020 J\u0010\u0010\'\u001a\u00020\u001e2\b\b\u0002\u0010(\u001a\u00020\u0003J\u0006\u0010)\u001a\u00020\u001eR\u0016\u0010\r\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00110\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0013\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00140\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0015\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000f0\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00110\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0018R\u0019\u0010\u001b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00140\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0018R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006*"}, d2 = {"Lcom/reminderapp/ui/viewmodel/ReminderDetailViewModel;", "Landroidx/lifecycle/ViewModel;", "reminderId", "", "dao", "Lcom/reminderapp/data/dao/ReminderDao;", "recordDao", "Lcom/reminderapp/data/dao/ReminderRecordDao;", "scheduler", "Lcom/reminderapp/service/ReminderScheduler;", "notificationMgr", "Lcom/reminderapp/service/NotificationManager;", "(JLcom/reminderapp/data/dao/ReminderDao;Lcom/reminderapp/data/dao/ReminderRecordDao;Lcom/reminderapp/service/ReminderScheduler;Lcom/reminderapp/service/NotificationManager;)V", "_checkInFeedback", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_records", "", "Lcom/reminderapp/data/entity/ReminderRecordEntity;", "_reminder", "Lcom/reminderapp/data/entity/ReminderEntity;", "checkInFeedback", "Lkotlinx/coroutines/flow/StateFlow;", "getCheckInFeedback", "()Lkotlinx/coroutines/flow/StateFlow;", "records", "getRecords", "reminder", "getReminder", "confirm", "", "isMakeUp", "", "consumeCheckInFeedback", "delete", "escalate", "rescheduleQuietHours", "setCritical", "value", "snooze", "minutes", "snoozeTomorrow", "app_debug"})
public final class ReminderDetailViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.reminderapp.data.dao.ReminderDao dao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.reminderapp.data.dao.ReminderRecordDao recordDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.reminderapp.service.ReminderScheduler scheduler = null;
    @org.jetbrains.annotations.NotNull()
    private final com.reminderapp.service.NotificationManager notificationMgr = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.reminderapp.data.entity.ReminderEntity> _reminder = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.reminderapp.data.entity.ReminderEntity> reminder = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.reminderapp.data.entity.ReminderRecordEntity>> _records = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.reminderapp.data.entity.ReminderRecordEntity>> records = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _checkInFeedback = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> checkInFeedback = null;
    
    public ReminderDetailViewModel(long reminderId, @org.jetbrains.annotations.NotNull()
    com.reminderapp.data.dao.ReminderDao dao, @org.jetbrains.annotations.NotNull()
    com.reminderapp.data.dao.ReminderRecordDao recordDao, @org.jetbrains.annotations.NotNull()
    com.reminderapp.service.ReminderScheduler scheduler, @org.jetbrains.annotations.NotNull()
    com.reminderapp.service.NotificationManager notificationMgr) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.reminderapp.data.entity.ReminderEntity> getReminder() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.reminderapp.data.entity.ReminderRecordEntity>> getRecords() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getCheckInFeedback() {
        return null;
    }
    
    /**
     * UI 消费打卡反馈（展示后清除，避免重复弹出）
     */
    public final void consumeCheckInFeedback() {
    }
    
    /**
     * 确认完成。
     *
     * @param isMakeUp 是否为「补打今天」（逾期后补打卡）。行为与普通确认完全一致
     *                （按今天完成 + 推进周期 + 计入统计），只是反馈文案区分，
     *                不改 Room schema，避免为一句文案做迁移。
     */
    public final void confirm(boolean isMakeUp) {
    }
    
    public final void snooze(long minutes) {
    }
    
    /**
     * v2.1.0: 稍后到明天提醒时刻
     */
    public final void snoozeTomorrow() {
    }
    
    /**
     * v2.1.1: 勿扰时段变更后重排通知（数据不变，仅按新窗口重排）
     */
    public final void rescheduleQuietHours() {
    }
    
    /**
     * 批次3 功能5: 切换关键提醒标记（落库 + 按对应渠道重排通知）
     */
    public final void setCritical(boolean value) {
    }
    
    public final void escalate() {
    }
    
    /**
     * 删除提醒（取消调度 + 取消已显示通知 + 删除记录 + 软删除）
     */
    public final void delete() {
    }
}