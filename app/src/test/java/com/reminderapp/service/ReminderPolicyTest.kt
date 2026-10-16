package com.reminderapp.service

import com.reminderapp.data.entity.ReminderEntity
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class ReminderPolicyTest {
    private fun time(value: String) = LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private fun daily() = ReminderEntity(id=7, title="Water", cycle="daily", firstTriggerAt=time("2026-10-16T09:00"), nextTriggerAt=time("2026-10-16T09:00"))
    @Test fun fixedKeepsAnchor() { assertEquals(time("2026-10-17T09:00"), ReminderEngine.confirm(daily(), time("2026-10-16T11:00")).nextTriggerAt) }
    @Test fun completionRelativeUsesCompletion() { assertEquals(time("2026-10-17T11:00"), ReminderEngine.confirm(daily().copy(recurrenceMode="afterCompletion"), time("2026-10-16T11:00")).nextTriggerAt) }
    @Test fun gentleStopsButCanComplete() {
        val delivered=ReminderEngine.escalate(daily().copy(reminderStyle="gentle"), time("2026-10-16T09:00"))
        assertFalse(ReminderPolicy.shouldSchedule(delivered)); assertEquals(1, delivered.retryCount)
        assertEquals("pending", ReminderEngine.confirm(delivered, time("2026-10-16T11:00")).status)
    }
    @Test fun retryRespectsQuietWindow() {
        val r=daily().copy(quietStartMinute=1320, quietEndMinute=480)
        assertEquals(time("2026-10-17T08:00"), ReminderEngine.escalate(r, time("2026-10-16T22:00")).nextTriggerAt)
        assertEquals(time("2026-10-17T08:00"), ReminderPolicy.adjustQuiet(r,time("2026-10-17T08:00")))
    }
    @Test fun skipDoesNotConfirmAndOnceCannotSkip() {
        val r=ReminderEngine.skip(daily(),time("2026-10-16T11:00"))!!
        assertNull(r.lastConfirmedAt); assertEquals(time("2026-10-17T09:00"),r.nextTriggerAt)
        assertNull(ReminderEngine.skip(daily().copy(cycle="once"),time("2026-10-16T11:00")))
    }
    @Test fun monthlyCompletionAndFixedUseCalendar() {
        val now=time("2026-01-31T11:00")
        val r=daily().copy(cycle="monthly", firstTriggerAt=time("2026-01-31T09:00"))
        assertEquals(time("2026-02-28T09:00"),ReminderEngine.confirm(r,now).nextTriggerAt)
        assertEquals(time("2026-02-28T11:00"),ReminderEngine.confirm(r.copy(recurrenceMode="afterCompletion"),now).nextTriggerAt)
    }
    @Test fun legacyAndV3RoundTrip() {
        val old=BackupService.importFromJson("""{"reminders":[{"title":"Old","firstTriggerAt":1,"nextTriggerAt":12345}]}""")!!.single()
        assertEquals("fixed",old.recurrenceMode); assertEquals("persistent",old.reminderStyle); assertEquals(5,old.maxReminderAttempts)
        assertEquals(listOf(60,240,720,1440),ReminderPolicy.intervals(old))
        val r=daily().copy(recurrenceMode="afterCompletion",reminderStyle="important",retryIntervalsMinutes="5,15",maxReminderAttempts=3,quietStartMinute=1320,quietEndMinute=480,nextTriggerAt=12345,retryCount=2)
        val json=BackupService.exportToJson(listOf(r),0)
        assertEquals(3,BackupService.schemaVersionOf(json)); val imported=BackupService.importFromJson(json)!!.single()
        assertEquals(r,imported)
    }
    @Test fun sharedV3FixtureRetainsSnoozeAndPolicy() {
        val fixture=javaClass.classLoader!!.getResource("fixtures/protocol_v3.json")!!.readText()
        val r=BackupService.importFromJson(fixture)!!.single()
        assertEquals(1792123200000L,r.nextTriggerAt);assertEquals(2,r.retryCount)
        assertEquals("afterCompletion",r.recurrenceMode);assertEquals("important",r.reminderStyle)
        assertEquals(listOf(15,60,240),ReminderPolicy.intervals(r))
        val imported=BackupService.importFromJson(BackupService.exportToJson(listOf(r),10))!!.single()
        assertEquals(r,imported)
    }
    @Test fun quietSameDayAndBoundaryAndInvalidPair() {
        val r=daily().copy(quietStartMinute=600,quietEndMinute=720)
        assertEquals(time("2026-10-16T12:00"),ReminderPolicy.adjustQuiet(r,time("2026-10-16T10:00")))
        assertEquals(time("2026-10-16T12:00"),ReminderPolicy.adjustQuiet(r,time("2026-10-16T12:00")))
        val json="""{"reminders":[{"title":"test","quietStartMinute":1320}]}"""
        assertNull(BackupService.importFromJson(json)!!.single().quietStartMinute)
    }
    @Test fun aiPolicyArgumentsUseSameValidatedContract() {
        val changed=ReminderPolicy.withArguments(daily(),mapOf("recurrenceMode" to "afterCompletion","reminderStyle" to "gentle","maxReminderAttempts" to 3.0,"retryIntervalsMinutes" to listOf(5.0,15.0)))
        assertEquals("afterCompletion",changed.recurrenceMode);assertEquals("gentle",changed.reminderStyle)
        assertEquals(listOf(5,15),ReminderPolicy.intervals(changed));assertEquals(3,changed.maxReminderAttempts)
        assertEquals(ReminderPolicy.legacyIntervals,ReminderPolicy.intervals(ReminderPolicy.withArguments(daily(),mapOf("retryIntervalsMinutes" to listOf(1.5)))))
    }
    @Test fun malformedPolicyFallsBack() { assertEquals(listOf(60,240,720,1440),ReminderPolicy.intervals(daily().copy(retryIntervalsMinutes="-3,0"))) }
    @Test fun undoGuardRejectsAnyNewerState() {
        val before=daily(); val after=ReminderEngine.confirm(before,time("2026-10-16T11:00"))
        assertTrue(ReminderPolicy.canUndo(after,after,4,4)); assertFalse(ReminderPolicy.canUndo(after.copy(note="edit"),after,4,4)); assertFalse(ReminderPolicy.canUndo(after,after,5,4))
    }
}
