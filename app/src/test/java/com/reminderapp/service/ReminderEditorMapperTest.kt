package com.reminderapp.service

import com.reminderapp.data.entity.ReminderEntity
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class ReminderEditorMapperTest {
    private fun t(s:String)=LocalDateTime.parse(s).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private fun monthly()=ReminderEntity(id=42,title="Filter",cycle="monthly",firstTriggerAt=t("2026-01-31T09:00"),nextTriggerAt=t("2026-11-30T09:00"),status="pending",revision=4)
    private fun rename(r:ReminderEntity)=ReminderEditorMapper.map(r,r.copy(title="Renamed",firstTriggerAt=t("2026-11-01T11:00"),nextTriggerAt=t("2026-11-01T11:00"),status="idle",retryCount=0,lastConfirmedAt=null),false,t("2026-10-16T11:00"))
    @Test fun monthlyTitleOnlyPreservesAnchorAndRuntime() { val old=monthly();assertEquals(old.copy(title="Renamed",revision=5),rename(old)) }
    @Test fun retryTitleOnlyPreservesRetryAndSnoozedOccurrence() { val old=monthly().copy(status="notifying",retryCount=3,nextTriggerAt=t("2026-10-16T11:00"));assertEquals(old.copy(title="Renamed",revision=5),rename(old)) }
    @Test fun completionRelativeEditKeepsActualCompletionAnchor() { val old=monthly().copy(recurrenceMode="afterCompletion",lastConfirmedAt=t("2026-10-15T14:00"));assertEquals(old.copy(title="Renamed",revision=5),rename(old)) }
    @Test fun completedOnceDoesNotReopenWhenRenamed() { val old=monthly().copy(cycle="once",status="confirmed",lastConfirmedAt=t("2026-10-15T14:00"));assertEquals(old.copy(title="Renamed",revision=5),rename(old)) }
    @Test fun formSeedUsesAnchorDayAndClockInsteadOfRetry() {
        val seeded=ReminderEditorMapper.seed(monthly().copy(nextTriggerAt=t("2026-11-01T11:00")))
        assertEquals(31,seeded.targetDay);assertEquals(9,seeded.reminderHour);assertEquals(0,seeded.reminderMinute)
    }
    @Test fun intentionalScheduleEditResetsPendingRetryAndMovesOnlySchedule() {
        val old=monthly().copy(status="notifying",retryCount=3)
        val next=t("2026-11-15T10:00")
        val edited=ReminderEditorMapper.map(old,old.copy(firstTriggerAt=next,nextTriggerAt=next,reminderHour=10),true,t("2026-10-16T11:00"))
        assertEquals(next,edited.nextTriggerAt);assertEquals("pending",edited.status);assertEquals(0,edited.retryCount);assertEquals(old.syncId,edited.syncId)
    }
    @Test fun visibleInvalidPolicyTextMustBlockSave() {
        assertFalse(ReminderPolicy.validEditorInput("persistent","60,0","5","",""))
        assertFalse(ReminderPolicy.validEditorInput("persistent","60,240","","",""))
        assertFalse(ReminderPolicy.validEditorInput("gentle","60,240","5","22:00",""))
        assertTrue(ReminderPolicy.validEditorInput("persistent","60,240","5","22:00","08:00"))
    }
}
