package com.reminderapp.service

import android.content.Context
import androidx.room.withTransaction
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.data.entity.ReminderEntity
import org.json.JSONObject

/** Captures and acknowledges only the state actually transferred; network IO never holds the mutation gate. */
class ReminderSyncCoordinator(private val context: Context,private val db: AppDatabase=AppDatabase.getInstance(context)) {
    data class Snapshot(val items: List<ReminderEntity>,val generation: Long,val lastChange: Long,val firstSync: Boolean,val lastSyncGeneration: Long,val lastSyncedAt: Long)
    companion object {
        fun conflicts(local: Snapshot,remoteGeneration: Long,remoteTimestamp: Long,remoteHasData: Boolean): Boolean {
            if(local.firstSync) return local.items.isNotEmpty() && remoteHasData
            val localDirty=local.generation>local.lastSyncGeneration || local.lastChange>local.lastSyncedAt
            val remoteDirty=if(remoteGeneration>0) remoteGeneration>local.lastSyncGeneration else remoteTimestamp>local.lastSyncedAt
            return localDirty && remoteDirty
        }
    }
    suspend fun capture(): Snapshot = ReminderMutationGate.serialized {
        db.withTransaction {
            Snapshot(db.reminderDao().getAllSync(),SyncStore.localVersion,SyncStore.lastLocalChange,SyncStore.isFirstSync,SyncStore.lastSyncVersion,SyncStore.lastSyncAt)
        }
    }
    fun uploadJson(snapshot: Snapshot, now: Long=System.currentTimeMillis()): String {
        val root=JSONObject(BackupService.exportToJson(snapshot.items,snapshot.generation))
        root.put("exportedAt",maxOf(now,snapshot.lastChange+1))
        return root.toString(2)
    }
    suspend fun acknowledgeUpload(snapshot: Snapshot, uploadedAt: Long) = ReminderMutationGate.serialized {
        SyncStore.acknowledgeCaptured(snapshot.generation,uploadedAt)
    }
    /** Persist the merge and its generation before any suspendable schedule restoration can admit a newer action. */
    suspend fun merge(snapshot: Snapshot,remote: List<ReminderEntity>,remoteGeneration: Long,remoteTimestamp: Long): Long = ReminderMutationGate.serialized {
        check(SyncStore.localVersion==snapshot.generation) { "本地数据已变更，请重试" }
        db.withTransaction {
            val local=db.reminderDao().getAllIncludingInactive()
            val merged=SyncMergePolicy.merge(local,remote)
            merged.forEach { r -> if(r.id==0L) db.reminderDao().insert(r) else if(local.find { it.id==r.id }!=r) db.reminderDao().update(r) }
        }
        SyncStore.acceptRemote(snapshot.generation,remoteGeneration,remoteTimestamp)
    }
    suspend fun restoreSchedules() = ReminderMutationGate.serialized {
        ReminderScheduler(context).rescheduleAll(db.reminderDao().getAllSync())
        com.reminderapp.receiver.ReminderWidgetProvider.refresh(context)
    }
}
