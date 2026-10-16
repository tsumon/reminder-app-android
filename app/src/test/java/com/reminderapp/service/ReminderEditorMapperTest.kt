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
    private val now = t("2026-10-16T11:00")
    private fun weekendRule() = ReminderEntity(
        title="Workday task", kind="rule", rulePeriod="monthly", ruleWeek=1, ruleWeekday=7,
        holidayAware=true, reminderHour=9, reminderMinute=15, firstTriggerAt=now, nextTriggerAt=now)
    // Same calculation and workday core used by the context-aware production resolver.
    private fun resolveWorkday(r: ReminderEntity, from: Long): Long {
        val raw = ReminderEngine.calculateNextTrigger(r, from)
        return if (r.holidayAware) ReminderEngine.deferToWorkdayCore(raw) { false } else raw
    }
    @Test fun newWeekendRuleDefersToMondayWithOriginalClock() {
        val result=ReminderEditorMapper.map(null,weekendRule(),true,now,::resolveWorkday)
        assertEquals(t("2026-11-02T09:15"),result.nextTriggerAt)
    }
    @Test fun newWeekendDateDefersToMondayWithOriginalClock() {
        val date=weekendRule().copy(kind="date",dateType="solar_birthday",targetMonth=11,targetDay=1)
        val result=ReminderEditorMapper.map(null,date,true,now,::resolveWorkday)
        assertEquals(t("2026-11-02T09:15"),result.nextTriggerAt)
    }
    @Test fun intentionalRuleRescheduleUsesWorkdayPolicy() {
        val old=monthly().copy(retryCount=3,status="snoozed")
        val result=ReminderEditorMapper.map(old,weekendRule(),true,now,::resolveWorkday)
        assertEquals(t("2026-11-02T09:15"),result.nextTriggerAt)
        assertEquals(old.id,result.id);assertEquals(old.syncId,result.syncId)
        assertEquals("pending",result.status);assertEquals(0,result.retryCount)
    }
    @Test fun titleOnlyWorkdayEditNeverResolvesOrMovesExistingOccurrence() {
        for (status in listOf("pending","snoozed","notifying","confirmed")) {
            val old=weekendRule().copy(id=42,status=status,retryCount=3,
                nextTriggerAt=t("2026-11-01T13:00"),lastConfirmedAt=t("2026-10-15T14:00"),revision=4)
            val result=ReminderEditorMapper.map(old,old.copy(title="Renamed"),false,now) { _, _ ->
                error("Metadata edits must not resolve a new occurrence")
            }
            assertEquals(old.copy(title="Renamed",revision=5),result)
        }
    }
    @Test fun workdayResolverKeepsWeekendWhenAvoidanceDisabled() {
        val result=ReminderEditorMapper.map(null,weekendRule().copy(holidayAware=false),true,now,::resolveWorkday)
        assertEquals(t("2026-11-01T09:15"),result.nextTriggerAt)
    }
    @Test fun injectedHolidayCalendarUsesExistingProductionDeferralCore() {
        val result=ReminderEditorMapper.map(null,weekendRule(),true,now) { r, from ->
            ReminderEngine.deferToWorkdayCore(ReminderEngine.calculateNextTrigger(r,from)) { cal ->
                cal.get(java.util.Calendar.MONTH)==java.util.Calendar.NOVEMBER &&
                    cal.get(java.util.Calendar.DAY_OF_MONTH)==2
            }
        }
        assertEquals(t("2026-11-03T09:15"),result.nextTriggerAt)
    }
    @Test fun visibleInvalidPolicyTextMustBlockSave() {
        assertFalse(ReminderPolicy.validEditorInput("persistent","60,0","5","",""))
        assertFalse(ReminderPolicy.validEditorInput("persistent","60,240","","",""))
        assertFalse(ReminderPolicy.validEditorInput("gentle","60,240","5","22:00",""))
        assertTrue(ReminderPolicy.validEditorInput("persistent","60,240","5","22:00","08:00"))
    }
}
