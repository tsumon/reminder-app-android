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
    @Test fun stableIdWinsBeforeEarlierFingerprintCollision() {
        val a=ReminderEntity(id=1,title="Same",syncId="a",firstTriggerAt=100,nextTriggerAt=500)
        val b=a.copy(id=2,title="Previously B",syncId="b")
        val remote=a.copy(id=99,syncId="b",reminderStyle="gentle")
        val merged=SyncMergePolicy.merge(listOf(a,b),listOf(remote))
        assertEquals(a,merged.first());assertEquals("Same",merged.last().title)
        assertEquals("gentle",merged.last().reminderStyle);assertEquals("b",merged.last().syncId)
    }
    @Test fun distinctExplicitIdNeverCollapsesByFingerprint() {
        val a=ReminderEntity(id=1,title="Same",syncId="a",firstTriggerAt=100,nextTriggerAt=500)
        val remote=a.copy(id=99,syncId="b")
        val merged=SyncMergePolicy.merge(listOf(a),listOf(remote))
        assertEquals(2,merged.size);assertEquals(a,merged.first());assertEquals("b",merged.last().syncId)
    }
    @Test fun legacyMissingIdCanStillMatchFingerprint() {
        val a=ReminderEntity(id=1,title="Same",syncId="a",firstTriggerAt=100,nextTriggerAt=500)
        val merged=SyncMergePolicy.merge(listOf(a),listOf(a.copy(id=99,syncId=null,priority="high")))
        assertEquals(1,merged.size);assertEquals(1L,merged.single().id);assertEquals("a",merged.single().syncId)
    }
    @Test fun dirtyCapturedGenerationStillConflictsAfterOlderUploadAcknowledgementTime() {
        val local=ReminderEntity(id=1,title="Dirty",firstTriggerAt=100,nextTriggerAt=500)
        val snapshot=ReminderSyncCoordinator.Snapshot(listOf(local),101,1000,false,100,2000)
        assertTrue(ReminderSyncCoordinator.conflicts(snapshot,0,3000,true))
        assertFalse(ReminderSyncCoordinator.conflicts(snapshot,0,1500,true))
    }
    @Test fun remoteMissingRowsNeverDeleteLocalDataAndNewRowsReceiveLocalIds() {
        val local=ReminderEntity(id=5,title="Keep",syncId="keep",firstTriggerAt=100,nextTriggerAt=500)
        val remote=local.copy(id=88,title="New",syncId="new")
        val merged=SyncMergePolicy.merge(listOf(local),listOf(remote))
        assertEquals(local,merged.first());assertEquals(0L,merged.last().id);assertEquals("new",merged.last().syncId)
    }
}
