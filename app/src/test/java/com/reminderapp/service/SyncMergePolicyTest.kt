package com.reminderapp.service

import com.reminderapp.data.entity.ReminderEntity
import org.junit.Assert.*
import org.junit.Test

class SyncMergePolicyTest {
    @Test fun uuidCaseAliasesPreserveLocalIdentityAndRevisionInBothDirections() {
        val lower="8f14e45f-ceea-4b5f-8d1a-9c3f2b7e5d01"
        for (stored in listOf(lower, lower.uppercase())) {
            val local=ReminderEntity(id=42,title="Shared",syncId=stored,firstTriggerAt=100,nextTriggerAt=500,revision=9)
            val remote=local.copy(id=999,syncId=if(stored==lower) lower.uppercase() else lower,note="Remote edit",status="notifying",retryCount=3)
            val merged=SyncMergePolicy.merge(listOf(local),listOf(remote))
            assertEquals(1,merged.size)
            assertEquals(remote.copy(id=42,syncId=stored,revision=10),merged.single())
        }
    }
    @Test fun uuidCaseMatchWinsBeforeEarlierLegacyFingerprintCollision() {
        val remote=ReminderEntity(id=999,title="Shared",syncId="8F14E45F-CEEA-4B5F-8D1A-9C3F2B7E5D01",firstTriggerAt=100,nextTriggerAt=500)
        val legacy=remote.copy(id=1,syncId=null)
        val stable=remote.copy(id=2,syncId=remote.syncId!!.lowercase(),note="Old note",revision=4)
        val merged=SyncMergePolicy.merge(listOf(legacy,stable),listOf(remote))
        assertEquals(2,merged.size)
        assertEquals(legacy,merged.first())
        assertEquals(remote.copy(id=2,syncId=stable.syncId,revision=5),merged.last())
    }
    @Test fun opaqueAndNoncanonicalIdsRemainCaseSensitive() {
        for ((stored,incoming) in listOf("opaque-A" to "opaque-a", "a-b-c-d-e" to "A-B-C-D-E",
            "8f14e45f-ceea-4b5f-8d1a-9c3f2b7e5d01" to "8f14e45f-ceea-4b5f-8d1a-9c3f2b7e5d02")) {
            val local=ReminderEntity(id=1,title="Same",syncId=stored,firstTriggerAt=100,nextTriggerAt=500)
            val merged=SyncMergePolicy.merge(listOf(local),listOf(local.copy(id=99,syncId=incoming)))
            assertEquals(2,merged.size)
            assertEquals(local,merged.first())
            assertEquals(incoming,merged.last().syncId)
        }
    }
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
