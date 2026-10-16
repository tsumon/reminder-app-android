package com.reminderapp.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.data.entity.ReminderRecordEntity
import com.reminderapp.service.*
import kotlinx.coroutines.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncGenerationTest {
    @Test fun explicitSelectionsPublishMonotonicWireGenerationAndRetainLocalOnlyRows() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        try {
            val now=System.currentTimeMillis()
            val shared=ReminderEntity(title="Shared Android",syncId="aaaaaaaa-bbbb-4ccc-8ddd-eeeeeeeeeeee",firstTriggerAt=now,nextTriggerAt=now+86400000)
            val id=db.reminderDao().insert(shared)
            val onlyId=db.reminderDao().insert(shared.copy(syncId="bbbbbbbb-bbbb-4ccc-8ddd-eeeeeeeeeeee",title="Local only"))
            val coordinator=ReminderSyncCoordinator(context,db)
            SyncStore.localVersion=200;SyncStore.lastSyncVersion=199;SyncStore.hasSyncedOnce=true
            val local=coordinator.capture()
            val chosenLocal=coordinator.prepareLocalResolution(local,100)
            val localJSON=JSONObject(coordinator.uploadJson(chosenLocal))
            assertEquals(201,localJSON.getLong("dataVersion"))
            assertEquals("Shared Android",localJSON.getJSONArray("reminders").getJSONObject(0).getString("title"))
            coordinator.acknowledgeUpload(chosenLocal,now)
            assertEquals(201,SyncStore.lastSyncVersion)
            val before=coordinator.capture()
            coordinator.merge(before,listOf(shared.copy(id=999,title="Selected remote")),100,now,publishSelected=true)
            val selected=coordinator.capture()
            assertEquals(202,selected.generation)
            assertTrue(selected.generation>selected.lastSyncGeneration)
            assertEquals("Selected remote",db.reminderDao().getById(id)!!.title)
            assertNotNull(db.reminderDao().getById(onlyId))
            val selectedJSON=JSONObject(coordinator.uploadJson(selected))
            assertEquals(202,selectedJSON.getLong("dataVersion"))
            assertEquals(2,selectedJSON.getJSONArray("reminders").length())
            // Immutable uploaded selection cannot acknowledge a newer local edit.
            SyncStore.touchLocalChange()
            coordinator.acknowledgeUpload(selected,now)
            assertEquals(202,SyncStore.lastSyncVersion)
            assertTrue(SyncStore.localVersion>SyncStore.lastSyncVersion)
            val cleanOther=ReminderSyncCoordinator.Snapshot(listOf(shared),200,now,false,200,now)
            assertEquals(ReminderSyncCoordinator.Decision.DOWNLOAD,ReminderSyncCoordinator.decide(cleanOther,202,now+1,true))
            assertEquals(ReminderSyncCoordinator.Decision.CONFLICT,ReminderSyncCoordinator.decide(cleanOther.copy(generation=201),202,now+1,true))
        } finally { db.close() }
    }
    @Test fun uuidCaseMergeAndImportKeepLocalRecordReferencesAndStoredIdentity() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        var id=0L
        try {
            val now=System.currentTimeMillis()
            val local=ReminderEntity(title="UUID identity",syncId="8f14e45f-ceea-4b5f-8d1a-9c3f2b7e5d01",
                cycle="daily",firstTriggerAt=now,nextTriggerAt=now+86400000,revision=4)
            id=db.reminderDao().insert(local)
            val recordId=db.reminderRecordDao().insert(ReminderRecordEntity(reminderId=id,action="confirmed",timestamp=now-1000))
            val coordinator=ReminderSyncCoordinator(context,db)
            val remote=local.copy(id=999,syncId=local.syncId!!.uppercase(),note="Remote edit",status="notifying",retryCount=2)
            coordinator.merge(coordinator.capture(),listOf(remote),300,now)
            val merged=db.reminderDao().getAllIncludingInactive().single()
            assertEquals(remote.copy(id=id,syncId=local.syncId,revision=5),merged)
            val imported=ReminderActions(context,db,holidayRefresh={}).importItems(listOf(remote.copy(note="File import")))
            assertEquals(1,imported.saved.size)
            assertEquals(remote.copy(id=id,syncId=local.syncId,note="File import",revision=6),db.reminderDao().getAllIncludingInactive().single())
            val record=db.reminderRecordDao().getAll().single()
            assertEquals(recordId,record.id)
            assertEquals(id,record.reminderId)
        } finally { if(id>0) ReminderScheduler(context).cancel(id);db.close() }
    }
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
            ReminderActions(context,db,holidayRefresh={}).apply(raw.copy(id=id),ReminderActions.Action.COMPLETE,now=now+1000)!!
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
            val actions=ReminderActions(context,db,holidayRefresh={})
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
            ReminderActions(context,db,holidayRefresh={}).apply(merged,ReminderActions.Action.COMPLETE,now=now+1000)!!
            coordinator.restoreSchedules()
            assertEquals(mergedGeneration,SyncStore.lastSyncVersion)
            assertTrue(SyncStore.localVersion>SyncStore.lastSyncVersion)
            val current=db.reminderDao().getById(id)!!
            assertEquals("remote",current.note);assertNotNull(current.lastConfirmedAt)
            assertEquals(1,db.reminderRecordDao().getAll().count { it.action=="confirmed" })
        } finally { if(id>0) ReminderScheduler(context).cancel(id);db.close() }
    }
}
