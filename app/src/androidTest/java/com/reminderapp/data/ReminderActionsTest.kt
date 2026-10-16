package com.reminderapp.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.data.entity.*
import com.reminderapp.service.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderActionsTest {
    @Test fun productionActionsRecordSkipCompleteAndGuardedUndo() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        val actions=ReminderActions(context,db)
        var id=0L
        try {
            val now=System.currentTimeMillis()
            val initial=ReminderEntity(title="Action test",cycle="daily",firstTriggerAt=now,nextTriggerAt=now)
            id=db.reminderDao().insert(initial)
            val before=initial.copy(id=id)
            val prior=db.reminderRecordDao().insert(ReminderRecordEntity(reminderId=id,action="confirmed",timestamp=now-86400000))
            val skipped=actions.apply(before,ReminderActions.Action.SKIP,now=now)!!.reminder
            assertNull(skipped.lastConfirmedAt)
            assertEquals(1,db.reminderRecordDao().getAll().count { it.action=="confirmed" })
            val completed=actions.apply(skipped,ReminderActions.Action.COMPLETE,now=now+1000)!!
            assertEquals(2,db.reminderRecordDao().getAll().count { it.action=="confirmed" })
            assertNull(actions.apply(skipped,ReminderActions.Action.COMPLETE,now=now+1000)) // duplicate stale callback
            assertTrue(actions.undo(completed.undo!!))
            assertEquals(listOf(prior),db.reminderRecordDao().getAll().filter { it.action=="confirmed" }.map { it.id })
            val restored=db.reminderDao().getById(id)!!
            val again=actions.apply(restored,ReminderActions.Action.COMPLETE,now=now+2000)!!
            db.reminderDao().update(again.reminder.copy(note="new edit"))
            assertFalse(actions.undo(again.undo!!))
            val fresh=db.reminderDao().getById(id)!!
            val third=actions.apply(fresh,ReminderActions.Action.COMPLETE,now=now+3000)!!
            SyncStore.touchLocalChange() // synchronization or a newer unrelated edit invalidates undo
            assertFalse(actions.undo(third.undo!!))
        } finally { if(id>0) ReminderScheduler(context).cancel(id);db.close() }
    }
    @Test fun advanceDeliveryAlsoRespectsQuietHours() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        var id=0L
        try {
            val now=System.currentTimeMillis()
            val clock=java.util.Calendar.getInstance().apply { timeInMillis=now }
            val minute=clock.get(java.util.Calendar.HOUR_OF_DAY)*60+clock.get(java.util.Calendar.MINUTE)
            val r=ReminderEntity(title="Quiet advance",kind="date",dateType="solar_birthday",firstTriggerAt=now,nextTriggerAt=now+86400000,
                quietStartMinute=minute,quietEndMinute=(minute+30)%1440)
            id=db.reminderDao().insert(r)
            NotificationManager(context).cancelReminderNotifications(id)
            ReminderActions(context,db,notificationAllowed={true}).deliver(id,r.nextTriggerAt,0,advanceDays=1,now=now)
            val system=context.getSystemService(android.app.NotificationManager::class.java)
            assertFalse(system.activeNotifications.any { it.id==id.toInt()+2000 })
            assertEquals(r.copy(id=id),db.reminderDao().getById(id))
            assertTrue(db.reminderRecordDao().getAll().isEmpty())
        } finally { if(id>0) ReminderScheduler(context).cancel(id);db.close() }
    }
    @Test fun deniedNotificationsDoNotConsumeAttemptsOrStatistics() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        var id=0L
        try {
            val now=System.currentTimeMillis()
            val r=ReminderEntity(title="Permission test",cycle="once",firstTriggerAt=now,nextTriggerAt=now)
            id=db.reminderDao().insert(r)
            ReminderActions(context,db,notificationAllowed={false}).deliver(id,now,0,now=now)
            val deferred=db.reminderDao().getById(id)!!
            assertEquals(0,deferred.retryCount);assertTrue(deferred.nextTriggerAt>now)
            assertTrue(db.reminderRecordDao().getAll().isEmpty())
            assertTrue(ReminderPolicy.shouldSchedule(deferred))
        } finally { if(id>0) ReminderScheduler(context).cancel(id);db.close() }
    }
    @Test fun deliveryClaimIsIdempotentAndGentleStillCompletes() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        if(android.os.Build.VERSION.SDK_INT>=33) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS").use { descriptor ->
                java.io.FileInputStream(descriptor.fileDescriptor).use { it.readBytes() }
            }
        }
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        var id=0L
        try {
            val now=System.currentTimeMillis()
            val r=ReminderEntity(title="Gentle test",cycle="once",reminderStyle="gentle",firstTriggerAt=now,nextTriggerAt=now)
            id=db.reminderDao().insert(r)
            val actions=ReminderActions(context,db)
            actions.deliver(id,now,0,now=now)
            actions.deliver(id,now,0,now=now)
            assertEquals(1,db.reminderRecordDao().getAll().count { it.action=="notified" })
            val delivered=db.reminderDao().getById(id)!!
            assertFalse(ReminderPolicy.shouldSchedule(delivered))
            val done=actions.apply(delivered,ReminderActions.Action.COMPLETE,now=now+1000)!!
            assertEquals("confirmed",done.reminder.status)
            assertEquals(1,db.reminderRecordDao().getAll().count { it.action=="confirmed" })
        } finally { if(id>0) { ReminderScheduler(context).cancel(id);NotificationManager(context).cancelReminderNotifications(id) };db.close() }
    }
}
