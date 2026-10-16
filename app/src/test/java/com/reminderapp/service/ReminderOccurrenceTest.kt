package com.reminderapp.service

import com.reminderapp.data.entity.ReminderEntity
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class ReminderOccurrenceTest {
    private fun time(value: String) = LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private val now = time("2026-10-19T12:00")
    private fun daily() = ReminderEntity(id=7,title="Water",cycle="daily",
        firstTriggerAt=time("2026-10-01T09:00"),nextTriggerAt=time("2026-10-20T09:00"),revision=4)

    @Test fun earlyCompleteConsumesPendingOrIdleStageZeroPrimaryOnce() {
        for (status in listOf("pending","idle")) {
            val r=daily().copy(status=status,firstTriggerAt=time("2026-10-20T09:00"))
            val completed=ReminderEngine.confirm(r,now)
            assertEquals(time("2026-10-21T09:00"),completed.nextTriggerAt)
            assertEquals(now,completed.lastConfirmedAt)
            assertEquals(r.firstTriggerAt,completed.firstTriggerAt)
            assertEquals(5L,completed.revision)
            assertEquals("pending",completed.status)
            assertEquals(0,completed.retryCount)
        }
    }
    @Test fun deliveredFutureRetryOrSnoozeDoesNotConsumeUpcomingPrimaryOnComplete() {
        for ((status,stage) in listOf("notifying" to 1,"snoozed" to 0,"pending" to 1,"overdue" to 5)) {
            val r=daily().copy(status=status,retryCount=stage,nextTriggerAt=time("2026-10-26T12:00"))
            val completed=ReminderEngine.confirm(r,now)
            assertEquals(time("2026-10-20T09:00"),completed.nextTriggerAt)
            assertEquals(now,completed.lastConfirmedAt)
        }
    }
    @Test fun relativeEarlyCompleteUsesActualCompletionTime() {
        val r=daily().copy(recurrenceMode="afterCompletion",firstTriggerAt=time("2026-10-20T09:00"))
        assertEquals(time("2026-10-20T12:00"),ReminderEngine.confirm(r,now).nextTriggerAt)
    }
    @Test fun skipFuturePrimaryAdvancesOnceWithoutChangingAnchors() {
        for (status in listOf("pending","idle")) {
            val r=daily().copy(status=status,lastConfirmedAt=time("2026-10-18T09:00"))
            val skipped=ReminderEngine.skip(r,now)!!
            assertEquals(time("2026-10-21T09:00"),skipped.nextTriggerAt)
            assertEquals(r.lastConfirmedAt,skipped.lastConfirmedAt)
            assertEquals(r.firstTriggerAt,skipped.firstTriggerAt)
            assertEquals(5L,skipped.revision)
        }
    }
    @Test fun skipFutureRetryOrSnoozeDoesNotConsumeUpcomingPrimary() {
        for ((status,stage) in listOf("notifying" to 1,"snoozed" to 0,"pending" to 1,"overdue" to 5)) {
            val r=daily().copy(status=status,retryCount=stage,nextTriggerAt=time("2026-10-26T12:00"))
            val skipped=ReminderEngine.skip(r,now)!!
            assertEquals(time("2026-10-20T09:00"),skipped.nextTriggerAt)
            assertNull(skipped.lastConfirmedAt)
            assertEquals(0,skipped.retryCount)
            assertEquals("pending",skipped.status)
        }
    }
    @Test fun relativeSkipRestartsAtActualSkipTimeWithoutWritingCompletionAnchor() {
        for (last in listOf(null,time("2026-10-18T09:00"))) {
            val r=daily().copy(recurrenceMode="afterCompletion",status="snoozed",
                lastConfirmedAt=last,nextTriggerAt=time("2026-10-26T12:00"))
            val skipped=ReminderEngine.skip(r,now)!!
            assertEquals(time("2026-10-20T12:00"),skipped.nextTriggerAt)
            assertEquals(last,skipped.lastConfirmedAt)
            assertEquals(r.firstTriggerAt,skipped.firstTriggerAt)
        }
    }
    @Test fun monthlyEarlyCompleteConsumesPrimaryAndRelativeSkipUsesCalendarMonth() {
        val r=daily().copy(cycle="monthly",firstTriggerAt=time("2026-01-31T09:00"),nextTriggerAt=time("2026-02-28T09:00"))
        assertEquals(time("2026-03-31T09:00"),ReminderEngine.confirm(r,time("2026-02-27T12:00")).nextTriggerAt)
        val skipped=ReminderEngine.skip(r.copy(recurrenceMode="afterCompletion"),time("2026-01-31T12:00"))!!
        assertEquals(time("2026-02-28T12:00"),skipped.nextTriggerAt)
        assertNull(skipped.lastConfirmedAt)
    }
    @Test fun onceDateAndRuleKeepCompletionMeaningAndSkipPrimaryDistinction() {
        val once=daily().copy(cycle="once")
        assertEquals("confirmed",ReminderEngine.confirm(once,now).status)
        assertNull(ReminderEngine.skip(once,now))
        val date=daily().copy(kind="date",dateType="solar_birthday",targetMonth=10,targetDay=20)
        val rule=daily().copy(kind="rule",rulePeriod="monthly",ruleWeek=3,ruleWeekday=2)
        for (r in listOf(date,rule)) {
            assertEquals(time("2026-10-20T09:00"),ReminderEngine.confirm(r,now).nextTriggerAt)
            val expected=if(r.kind=="date") time("2027-10-20T09:00") else time("2026-11-17T09:00")
            assertEquals(expected,ReminderEngine.skip(r,now)!!.nextTriggerAt)
            assertEquals(time("2026-10-20T09:00"),ReminderEngine.skip(r.copy(status="snoozed",nextTriggerAt=time("2026-10-26T12:00")),now)!!.nextTriggerAt)
        }
    }
}
