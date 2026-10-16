package com.reminderapp.service

import com.reminderapp.data.entity.ReminderEntity

/** No tombstones exist in legacy backups: preserve local-only rows and every imported retry/snooze. */
object SyncMergePolicy {
    fun matchIndex(local: List<ReminderEntity>,remote: ReminderEntity): Int {
        val explicit=remote.syncId?.takeIf { it.isNotBlank() }
        val stable=if(explicit!=null) local.indexOfFirst { it.syncId==explicit } else -1
        if(stable>=0) return stable
        return local.indexOfFirst { existing ->
            (explicit==null || existing.syncId.isNullOrBlank()) && BackupService.fingerprint(remote)==BackupService.fingerprint(existing)
        }
    }
    fun merge(local: List<ReminderEntity>, remote: List<ReminderEntity>): List<ReminderEntity> {
        val merged=local.toMutableList()
        remote.forEach { r ->
            val index=matchIndex(merged,r)
            if(index>=0) {
                val old=merged[index]
                merged[index]=r.copy(id=old.id,syncId=old.syncId ?: r.syncId,revision=old.revision+1)
            } else merged.add(BackupService.ensureSyncId(r).copy(id=0))
        }
        return merged
    }
}
