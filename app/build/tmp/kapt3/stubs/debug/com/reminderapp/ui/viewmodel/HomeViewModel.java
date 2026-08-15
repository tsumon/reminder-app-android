package com.reminderapp.ui.viewmodel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\"\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\t\u0018\u00002\u00020\u0001:\u0001*B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0014\u0010\u0018\u001a\u00020\u00192\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u001c0\u001bJ\u0014\u0010\u001d\u001a\u00020\u00192\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u001c0\u001bJ\u0006\u0010\u001e\u001a\u00020\u0019J\u0018\u0010\u001f\u001a\u00020\u00192\u0006\u0010 \u001a\u00020\u00112\b\b\u0002\u0010!\u001a\u00020\"J\u0006\u0010#\u001a\u00020\u0019J\u000e\u0010$\u001a\u00020\u00192\u0006\u0010%\u001a\u00020\u001cJ\u0006\u0010&\u001a\u00020\u0019J\u0018\u0010\'\u001a\u00020\u00192\b\b\u0002\u0010!\u001a\u00020\"H\u0082@\u00a2\u0006\u0002\u0010(J\u000e\u0010)\u001a\u00020\u00192\u0006\u0010 \u001a\u00020\u0011R\u0016\u0010\t\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\r0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\u00100\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0019\u0010\u0014\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0013R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\r0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0013R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006+"}, d2 = {"Lcom/reminderapp/ui/viewmodel/HomeViewModel;", "Landroidx/lifecycle/ViewModel;", "dao", "Lcom/reminderapp/data/dao/ReminderDao;", "recordDao", "Lcom/reminderapp/data/dao/ReminderRecordDao;", "scheduler", "Lcom/reminderapp/service/ReminderScheduler;", "(Lcom/reminderapp/data/dao/ReminderDao;Lcom/reminderapp/data/dao/ReminderRecordDao;Lcom/reminderapp/service/ReminderScheduler;)V", "_checkInFeedback", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_groupedReminders", "Lcom/reminderapp/ui/viewmodel/HomeViewModel$GroupedReminders;", "allReminders", "Lkotlinx/coroutines/flow/StateFlow;", "", "Lcom/reminderapp/data/entity/ReminderEntity;", "getAllReminders", "()Lkotlinx/coroutines/flow/StateFlow;", "checkInFeedback", "getCheckInFeedback", "groupedReminders", "getGroupedReminders", "batchComplete", "", "ids", "", "", "batchDelete", "checkMissed", "confirmReminder", "reminder", "isMakeUp", "", "consumeCheckInFeedback", "deleteReminder", "id", "ensureSchedules", "publishCheckInFeedback", "(ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "reopenReminder", "GroupedReminders", "app_debug"})
public final class HomeViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.reminderapp.data.dao.ReminderDao dao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.reminderapp.data.dao.ReminderRecordDao recordDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.reminderapp.service.ReminderScheduler scheduler = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.reminderapp.data.entity.ReminderEntity>> allReminders = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.reminderapp.ui.viewmodel.HomeViewModel.GroupedReminders> _groupedReminders = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.reminderapp.ui.viewmodel.HomeViewModel.GroupedReminders> groupedReminders = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _checkInFeedback = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> checkInFeedback = null;
    
    public HomeViewModel(@org.jetbrains.annotations.NotNull()
    com.reminderapp.data.dao.ReminderDao dao, @org.jetbrains.annotations.NotNull()
    com.reminderapp.data.dao.ReminderRecordDao recordDao, @org.jetbrains.annotations.NotNull()
    com.reminderapp.service.ReminderScheduler scheduler) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.reminderapp.data.entity.ReminderEntity>> getAllReminders() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.reminderapp.ui.viewmodel.HomeViewModel.GroupedReminders> getGroupedReminders() {
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
     * 检查遗漏提醒
     *
     * 除了把状态标为「提醒中」，还必须重新排期：
     * 到点没响通常意味着 WorkManager 任务已经丢失（进程被杀 / 系统清理），
     * 只改状态的话这条提醒就永远不会再响了。
     *
     * 已经是 notifying 的说明上一轮已经补过一次，跳过重排，
     * 避免一次性提醒每次进入 App 都重复弹通知。
     */
    public final void checkMissed() {
    }
    
    /**
     * App 启动时确保所有活跃提醒都有 WorkManager 排期
     */
    public final void ensureSchedules() {
    }
    
    /**
     * 滑动完成：确认当前提醒（周期类自动前进到下一次；一次性提醒归档）
     *
     * @param isMakeUp 是否为「补打今天」（逾期后补打卡）。行为与普通确认一致
     *                （按今天完成 + 推进周期 + 计入统计），只区分反馈文案。
     */
    public final void confirmReminder(@org.jetbrains.annotations.NotNull()
    com.reminderapp.data.entity.ReminderEntity reminder, boolean isMakeUp) {
    }
    
    /**
     * 批次2 功能2: 计算当前连续打卡天数并发布正向反馈
     */
    private final java.lang.Object publishCheckInFeedback(boolean isMakeUp, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * 撤销完成：把提醒放回等待中
     */
    public final void reopenReminder(@org.jetbrains.annotations.NotNull()
    com.reminderapp.data.entity.ReminderEntity reminder) {
    }
    
    public final void deleteReminder(long id) {
    }
    
    /**
     * 批量确认完成（未完成的提醒逐条确认，一次同步版本）
     */
    public final void batchComplete(@org.jetbrains.annotations.NotNull()
    java.util.Set<java.lang.Long> ids) {
    }
    
    /**
     * 批量删除（软删 + 清通知 + 清记录）
     */
    public final void batchDelete(@org.jetbrains.annotations.NotNull()
    java.util.Set<java.lang.Long> ids) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B5\u0012\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\u0002\u0010\u0007J\u000f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J\u000f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J\u000f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J9\u0010\u000f\u001a\u00020\u00002\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0001J\u0013\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0013\u001a\u00020\u0014H\u00d6\u0001J\t\u0010\u0015\u001a\u00020\u0016H\u00d6\u0001R\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0017\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\tR\u0017\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\t\u00a8\u0006\u0017"}, d2 = {"Lcom/reminderapp/ui/viewmodel/HomeViewModel$GroupedReminders;", "", "reminding", "", "Lcom/reminderapp/data/entity/ReminderEntity;", "waiting", "completed", "(Ljava/util/List;Ljava/util/List;Ljava/util/List;)V", "getCompleted", "()Ljava/util/List;", "getReminding", "getWaiting", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "", "app_debug"})
    public static final class GroupedReminders {
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<com.reminderapp.data.entity.ReminderEntity> reminding = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<com.reminderapp.data.entity.ReminderEntity> waiting = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<com.reminderapp.data.entity.ReminderEntity> completed = null;
        
        public GroupedReminders(@org.jetbrains.annotations.NotNull()
        java.util.List<com.reminderapp.data.entity.ReminderEntity> reminding, @org.jetbrains.annotations.NotNull()
        java.util.List<com.reminderapp.data.entity.ReminderEntity> waiting, @org.jetbrains.annotations.NotNull()
        java.util.List<com.reminderapp.data.entity.ReminderEntity> completed) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.reminderapp.data.entity.ReminderEntity> getReminding() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.reminderapp.data.entity.ReminderEntity> getWaiting() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.reminderapp.data.entity.ReminderEntity> getCompleted() {
            return null;
        }
        
        public GroupedReminders() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.reminderapp.data.entity.ReminderEntity> component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.reminderapp.data.entity.ReminderEntity> component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.reminderapp.data.entity.ReminderEntity> component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.reminderapp.ui.viewmodel.HomeViewModel.GroupedReminders copy(@org.jetbrains.annotations.NotNull()
        java.util.List<com.reminderapp.data.entity.ReminderEntity> reminding, @org.jetbrains.annotations.NotNull()
        java.util.List<com.reminderapp.data.entity.ReminderEntity> waiting, @org.jetbrains.annotations.NotNull()
        java.util.List<com.reminderapp.data.entity.ReminderEntity> completed) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}