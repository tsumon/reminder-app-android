package com.reminderapp.service

import com.reminderapp.data.entity.ReminderEntity
import org.junit.Assert.*
import org.junit.Test

class SyncMergePolicyTest {
    @Test fun remoteMergePreservesLocalOnlySchedulesAndStableIdsAndRetryState() {
        val local=ReminderEntity(id=42,title="Local only",syncId="local",firstTriggerAt=100,nextTriggerAt=500)
        val shared=local.copy(id=43,syncId="shared",title="Shared",revision=9)
        val remote=shared.copy(id=999,title="Remote edit",status="notifying",nextTriggerAt=12345,retryCount=3)
        val merged=SyncMergePolicy.merge(listOf(local,shared),listOf(remote))
        assertEquals(local,merged.first())
        val changed=merged.last()
        assertEquals(43L,changed.id);assertEquals(12345L,changed.nextTriggerAt);assertEquals(3,changed.retryCount)
        assertEquals("notifying",changed.status);assertEquals(10L,changed.revision)
        assertEquals(2,merged.filter { it.isActive }.size)
    }
    @Test fun remoteMissingRowsNeverDeleteLocalDataAndNewRowsReceiveLocalIds() {
        val local=ReminderEntity(id=5,title="Keep",syncId="keep",firstTriggerAt=100,nextTriggerAt=500)
        val remote=local.copy(id=88,title="New",syncId="new")
        val merged=SyncMergePolicy.merge(listOf(local),listOf(remote))
        assertEquals(local,merged.first());assertEquals(0L,merged.last().id);assertEquals("new",merged.last().syncId)
    }
}
