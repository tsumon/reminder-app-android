package com.reminderapp.service

import com.reminderapp.data.entity.ReminderEntity
import org.junit.Assert.*
import org.junit.Test

class SharedGenerationTest {
    private fun android(generation: Long = 100) = ReminderSyncCoordinator.Snapshot(
        listOf(ReminderEntity(title="Android 100",firstTriggerAt=1,nextTriggerAt=2)),
        generation,1000,false,100,1000)
    @Test fun freshIosPullThenEditMustDownloadOnCleanAndroidAndConflictOnEditedAndroid() {
        // Real iOS production export is also passed to this decision path by the verification harness.
        assertEquals(ReminderSyncCoordinator.Decision.DOWNLOAD,ReminderSyncCoordinator.decide(android(),101,2000,true))
        assertEquals(ReminderSyncCoordinator.Decision.CONFLICT,ReminderSyncCoordinator.decide(android(101),101,2000,true))
    }
    @Test fun selectedWireRevisionAlwaysExceedsBothCounters() {
        assertEquals(201,ReminderSyncCoordinator.resolutionGeneration(200,100))
        assertEquals(301,ReminderSyncCoordinator.resolutionGeneration(200,300))
    }
}
