package com.reminderapp.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.data.entity.*
import com.reminderapp.service.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderActionsTest {
    @Test fun completionPrechecksCachedHolidayAndExactUndoThroughSharedNonUiAction() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        val cache=context.getSharedPreferences("remote_holiday_cache_2026",0)
        val old=cache.all.toMap()
        var id=0L
        try {
            fun time(value: String)=java.time.LocalDateTime.parse(value).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val now=time("2026-09-24T09:00")
            cache.edit().clear().putString("2026-10-01","true|National Day").putString("2026-09-30","false|Workday").commit()
            val initial=ReminderEntity(title="Holiday completion",cycle="weekly",firstTriggerAt=now,nextTriggerAt=now,holidayAware=false)
            id=db.reminderDao().insert(initial)
            val before=initial.copy(id=id)
            val prior=db.reminderRecordDao().insert(ReminderRecordEntity(reminderId=id,action="confirmed",timestamp=now-86400000))
            val actions=ReminderActions(context,db,holidayRefresh={})
            val result=actions.apply(before,ReminderActions.Action.COMPLETE,now=now)!!
            assertEquals(time("2026-09-30T09:00"),result.reminder.nextTriggerAt)
            assertEquals(now,result.reminder.firstTriggerAt)
            assertTrue(result.reminder.holidayAdjustNote!!.contains("National Day"))
            assertEquals(result.reminder,db.reminderDao().getById(id))
            assertEquals(2,db.reminderRecordDao().getAll().count { it.action=="confirmed" })
            assertTrue(actions.undo(result.undo!!))
            assertEquals(before.copy(revision=result.reminder.revision+1),db.reminderDao().getById(id))
            assertEquals(listOf(prior),db.reminderRecordDao().getAll().map { it.id })
        } finally {
            cache.edit().clear().apply();old.forEach { (k,v) -> when(v) { is String -> cache.edit().putString(k,v).apply(); is Boolean -> cache.edit().putBoolean(k,v).apply() } }
            if(id>0) ReminderScheduler(context).cancel(id);db.close()
        }
    }
    @Test fun newerSnoozeOrEditDuringHolidayRefreshRejectsObsoleteCompletion() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        for(edit in listOf(false,true)) {
            val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
            val cache=context.getSharedPreferences("remote_holiday_cache_2026",0)
            val old=cache.all.toMap();var id=0L
            try {
                fun time(value: String)=java.time.LocalDateTime.parse(value).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                val now=time("2026-09-24T09:00")
                cache.edit().clear().commit()
                val initial=ReminderEntity(title="Refresh race",cycle="weekly",firstTriggerAt=now,nextTriggerAt=now)
                id=db.reminderDao().insert(initial);val before=initial.copy(id=id)
                val entered=CompletableDeferred<Unit>();val release=CompletableDeferred<Unit>()
                val completing=async { ReminderActions(context,db,holidayRefresh={
                    entered.complete(Unit);release.await()
                    cache.edit().putString("2026-10-01","true|National Day").putString("2026-09-30","false|Workday").commit()
                }).apply(before,ReminderActions.Action.COMPLETE,now=now) }
                entered.await()
                val actions=ReminderActions(context,db,holidayRefresh={})
                val newer=if(edit) actions.edit(before,before.copy(note="New edit"))!!
                    else actions.apply(before,ReminderActions.Action.SNOOZE,now=now)!!.reminder
                release.complete(Unit)
                assertNull(completing.await())
                assertEquals(newer,db.reminderDao().getById(id))
                assertTrue(db.reminderRecordDao().getAll().none { it.action=="confirmed" })
            } finally {
                cache.edit().clear().apply();old.forEach { (k,v) -> when(v) { is String -> cache.edit().putString(k,v).apply(); is Boolean -> cache.edit().putBoolean(k,v).apply() } }
                if(id>0) ReminderScheduler(context).cancel(id);db.close()
            }
        }
    }
    @Test fun earlyPrimaryCompletionAndSkipRejectDuplicateTokensAndPreserveRecords() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        var id=0L
        try {
            fun time(value: String)=java.time.LocalDateTime.parse(value).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val now=time("2026-10-19T12:00")
            val initial=ReminderEntity(title="Early primary",cycle="daily",firstTriggerAt=time("2026-10-20T09:00"),nextTriggerAt=time("2026-10-20T09:00"))
            id=db.reminderDao().insert(initial)
            val before=initial.copy(id=id)
            val prior=db.reminderRecordDao().insert(ReminderRecordEntity(reminderId=id,action="confirmed",timestamp=now-86400000))
            val actions=ReminderActions(context,db,holidayRefresh={})
            val completed=actions.apply(before,ReminderActions.Action.COMPLETE,now=now)!!
            assertEquals(time("2026-10-21T09:00"),completed.reminder.nextTriggerAt)
            assertEquals(now,completed.reminder.lastConfirmedAt)
            assertNull(actions.apply(before,ReminderActions.Action.COMPLETE,now=now))
            assertEquals(2,db.reminderRecordDao().getAll().count { it.action=="confirmed" })
            assertTrue(actions.undo(completed.undo!!))
            val restored=db.reminderDao().getById(id)!!
            val skipped=actions.apply(restored,ReminderActions.Action.SKIP,now=now)!!.reminder
            assertEquals(time("2026-10-21T09:00"),skipped.nextTriggerAt)
            assertNull(skipped.lastConfirmedAt)
            assertNull(actions.apply(restored,ReminderActions.Action.SKIP,now=now))
            assertEquals(listOf(prior),db.reminderRecordDao().getAll().filter { it.action=="confirmed" }.map { it.id })
            assertEquals(1,db.reminderRecordDao().getAll().count { it.action=="skipped" })
        } finally { if(id>0) ReminderScheduler(context).cancel(id);db.close() }
    }
    @Test fun productionActionsRecordSkipCompleteAndGuardedUndo() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        val actions=ReminderActions(context,db,holidayRefresh={})
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
    private fun grantNotifications() {
        if(android.os.Build.VERSION.SDK_INT>=33) {
            val context=InstrumentationRegistry.getInstrumentation().targetContext
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS").use { descriptor -> java.io.FileInputStream(descriptor.fileDescriptor).use { it.readBytes() } }
        }
    }
    @Test fun gentleDeliveryThenExplicitSnoozeHasOneMoreDeliveryOnly() = runBlocking {
        grantNotifications()
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        var id=0L
        try {
            val now=System.currentTimeMillis()
            val initial=ReminderEntity(title="Gentle snooze",cycle="once",reminderStyle="gentle",firstTriggerAt=now,nextTriggerAt=now)
            id=db.reminderDao().insert(initial)
            val actions=ReminderActions(context,db,notificationAllowed={true},holidayRefresh={})
            actions.deliver(id,now,0,now=now)
            val first=db.reminderDao().getById(id)!!
            val snoozed=actions.apply(first,ReminderActions.Action.SNOOZE,minutes=15,now=now+1)!!.reminder
            assertTrue(ReminderPolicy.shouldSchedule(snoozed))
            actions.deliver(id,snoozed.nextTriggerAt,snoozed.revision,now=snoozed.nextTriggerAt)
            val second=db.reminderDao().getById(id)!!
            assertFalse(ReminderPolicy.shouldSchedule(second))
            assertEquals(2,db.reminderRecordDao().getAll().count { it.action=="notified" })
            actions.deliver(id,second.nextTriggerAt,second.revision,now=second.nextTriggerAt)
            assertEquals(2,db.reminderRecordDao().getAll().count { it.action=="notified" })
            val tomorrow=actions.apply(second,ReminderActions.Action.TOMORROW,now=now+2)!!.reminder
            assertTrue(ReminderPolicy.shouldSchedule(tomorrow))
        } finally { if(id>0) { ReminderScheduler(context).cancel(id);NotificationManager(context).cancelReminderNotifications(id) };db.close() }
    }
    @Test fun queuedAiEditCannotOverwriteCompletionOrDelivery() = runBlocking {
        grantNotifications()
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        for(deliver in listOf(false,true)) {
            val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
            var id=0L
            try {
                val now=System.currentTimeMillis()
                val raw=ReminderEntity(title="Concurrent edit",cycle="daily",firstTriggerAt=now,nextTriggerAt=now)
                id=db.reminderDao().insert(raw)
                val before=raw.copy(id=id)
                val actions=ReminderActions(context,db,notificationAllowed={true},holidayRefresh={})
                val entered=CompletableDeferred<Unit>();val release=CompletableDeferred<Unit>()
                val holder=launch(Dispatchers.IO) { ReminderMutationGate.serialized { entered.complete(Unit);release.await() } }
                entered.await()
                val first=async(start=CoroutineStart.UNDISPATCHED) {
                    if(deliver) actions.deliver(id,now,0,now=now) else actions.apply(before,ReminderActions.Action.COMPLETE,now=now)
                }
                val staleEdit=async(start=CoroutineStart.UNDISPATCHED) { actions.edit(before,before.copy(note="AI edit")) }
                release.complete(Unit);holder.join();first.await()
                assertNull(staleEdit.await())
                val current=db.reminderDao().getById(id)!!
                assertEquals("",current.note)
                if(deliver) { assertEquals(1,current.retryCount);assertEquals(1,db.reminderRecordDao().getAll().count { it.action=="notified" }) }
                else { assertNotNull(current.lastConfirmedAt);assertEquals(1,db.reminderRecordDao().getAll().count { it.action=="confirmed" }) }
            } finally { if(id>0) { ReminderScheduler(context).cancel(id);NotificationManager(context).cancelReminderNotifications(id) };db.close() }
        }
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
            ReminderActions(context,db,notificationAllowed={true},holidayRefresh={}).deliver(id,r.nextTriggerAt,0,advanceDays=1,now=now)
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
            ReminderActions(context,db,notificationAllowed={false},holidayRefresh={}).deliver(id,now,0,now=now)
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
            val actions=ReminderActions(context,db,holidayRefresh={})
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
