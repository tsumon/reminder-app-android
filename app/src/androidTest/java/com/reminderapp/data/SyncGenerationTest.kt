package com.reminderapp.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.service.*
import kotlinx.coroutines.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncGenerationTest {
    @Test fun completionDuringInitialUploadRemainsDirtyAndPayloadUsesCapturedGeneration() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        var id=0L
        try {
            SyncStore.localVersion=100;SyncStore.lastSyncVersion=100
            val now=System.currentTimeMillis()
            val raw=ReminderEntity(title="Upload interleave",cycle="daily",firstTriggerAt=now,nextTriggerAt=now)
            id=db.reminderDao().insert(raw)
            val coordinator=ReminderSyncCoordinator(context,db)
            val captured=coordinator.capture()
            val inFlight=CompletableDeferred<Unit>();val finishUpload=CompletableDeferred<Unit>()
            val upload=async(Dispatchers.IO) {
                val payload=coordinator.uploadJson(captured,now)
                inFlight.complete(Unit);finishUpload.await()
                coordinator.acknowledgeUpload(captured,now)
                payload
            }
            inFlight.await()
            ReminderActions(context,db).apply(raw.copy(id=id),ReminderActions.Action.COMPLETE,now=now+1000)!!
            finishUpload.complete(Unit)
            val payload=JSONObject(upload.await())
            assertEquals(captured.generation,payload.getLong("dataVersion"))
            assertTrue(payload.getJSONArray("reminders").getJSONObject(0).isNull("lastConfirmedAt"))
            assertEquals(captured.generation,SyncStore.lastSyncVersion)
            assertTrue(SyncStore.localVersion>SyncStore.lastSyncVersion)
            assertNotNull(db.reminderDao().getById(id)!!.lastConfirmedAt)
        } finally { if(id>0) ReminderScheduler(context).cancel(id);db.close() }
    }
    @Test fun createImportAndDeleteAreCapturedWithTheirExactGeneration() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        val ids=mutableListOf<Long>()
        try {
            SyncStore.localVersion=400;SyncStore.lastSyncVersion=400
            val actions=ReminderActions(context,db)
            val coordinator=ReminderSyncCoordinator(context,db)
            val now=System.currentTimeMillis()
            val initial=ReminderEntity(title="Creation gate",cycle="daily",firstTriggerAt=now,nextTriggerAt=now)
            val entered=CompletableDeferred<Unit>();val release=CompletableDeferred<Unit>()
            val holder=launch(Dispatchers.IO) { ReminderMutationGate.serialized { entered.complete(Unit);release.await() } }
            entered.await()
            val creation=async(start=CoroutineStart.UNDISPATCHED) { actions.create(initial) }
            val capture=async(start=CoroutineStart.UNDISPATCHED) { coordinator.capture() }
            release.complete(Unit);holder.join()
            val created=creation.await();ids.add(created.reminder.id)
            val captured=capture.await()
            assertEquals(created.generation,captured.generation)
            assertEquals(listOf(created.reminder),captured.items)
            val batch=actions.importItems(listOf(initial.copy(syncId="separate-a"),initial.copy(syncId="separate-b")))
            ids.addAll(batch.saved.map { it.id })
            assertEquals(2,batch.saved.size) // explicit identities sharing a fingerprint stay distinct
            val imported=coordinator.capture()
            assertEquals(3,imported.items.size)
            assertTrue(actions.delete(created.reminder))
            coordinator.acknowledgeUpload(imported,now)
            assertEquals(imported.generation,SyncStore.lastSyncVersion)
            assertTrue(SyncStore.localVersion>SyncStore.lastSyncVersion)
            assertFalse(db.reminderDao().getById(created.reminder.id)!!.isActive)
        } finally { ids.forEach { ReminderScheduler(context).cancel(it) };db.close() }
    }
    @Test fun completionAfterMergeCommitBeforeScheduleRestoreIsNotAcknowledged() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        var id=0L
        try {
            SyncStore.localVersion=200;SyncStore.lastSyncVersion=200
            val now=System.currentTimeMillis()
            val raw=ReminderEntity(title="Merge interleave",cycle="daily",firstTriggerAt=now,nextTriggerAt=now)
            id=db.reminderDao().insert(raw)
            val coordinator=ReminderSyncCoordinator(context,db)
            val captured=coordinator.capture()
            val mergedGeneration=coordinator.merge(captured,listOf(raw.copy(id=999,note="remote")),300,now)
            val merged=db.reminderDao().getById(id)!!
            ReminderActions(context,db).apply(merged,ReminderActions.Action.COMPLETE,now=now+1000)!!
            coordinator.restoreSchedules()
            assertEquals(mergedGeneration,SyncStore.lastSyncVersion)
            assertTrue(SyncStore.localVersion>SyncStore.lastSyncVersion)
            val current=db.reminderDao().getById(id)!!
            assertEquals("remote",current.note);assertNotNull(current.lastConfirmedAt)
            assertEquals(1,db.reminderRecordDao().getAll().count { it.action=="confirmed" })
        } finally { if(id>0) ReminderScheduler(context).cancel(id);db.close() }
    }
}
