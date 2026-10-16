package com.reminderapp.service

import com.reminderapp.data.entity.ReminderEntity

/** No tombstones exist in legacy backups: preserve local-only rows and every imported retry/snooze. */
object SyncMergePolicy {
    fun merge(local: List<ReminderEntity>, remote: List<ReminderEntity>): List<ReminderEntity> {
        val merged=local.toMutableList()
        remote.forEach { r ->
            val index=merged.indexOfFirst { existing ->
                (!r.syncId.isNullOrBlank() && r.syncId==existing.syncId) || BackupService.fingerprint(r)==BackupService.fingerprint(existing)
            }
            if(index>=0) {
                val old=merged[index]
                merged[index]=r.copy(id=old.id,syncId=old.syncId ?: r.syncId,revision=old.revision+1)
            } else merged.add(BackupService.ensureSyncId(r).copy(id=0))
        }
        return merged
    }
}
