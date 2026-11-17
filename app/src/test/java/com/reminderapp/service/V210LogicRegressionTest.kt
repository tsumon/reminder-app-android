package com.reminderapp.service

import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.data.entity.ReminderRecordEntity
import com.reminderapp.data.entity.SyncDeletion
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class V210LogicRegressionTest {

    private fun calAt(year: Int, month: Int, day: Int, hour: Int = 9, minute: Int = 0): Long {
        return Calendar.getInstance().apply {
            set(year, month - 1, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    @Test
    fun aiToolsSystemPromptFormatsTodayWithoutLiteralPlaceholders() {
        val prompt = AITools.systemPrompt
        assertFalse(prompt.contains("%d"))
        assertFalse(prompt.contains("%s"))
        val year = Calendar.getInstance().get(Calendar.YEAR).toString()
        assertTrue(prompt.contains(year))
    }

    @Test
    fun deferToWorkdayTreatsMakeupWorkdayAsWorkday() {
        // 2026-10-10 is Saturday
        val saturday = calAt(2026, 10, 10, 9, 0)
        val monday = calAt(2026, 10, 12, 9, 0)
        // Explicit makeup workday (false) stays on Saturday
        assertEquals(saturday, ReminderEngine.deferToWorkdayWithStatus(saturday) { false })
        // Explicit unknown (null) skips weekend to Monday
        assertEquals(monday, ReminderEngine.deferToWorkdayWithStatus(saturday) { null })
        // Explicit holiday on weekday advances past holiday
        val tuesday = calAt(2026, 10, 13, 9, 0)
        assertEquals(tuesday, ReminderEngine.deferToWorkdayWithStatus(monday) { c ->
            c.get(Calendar.MONTH) + 1 == 10 && c.get(Calendar.DAY_OF_MONTH) == 12
        })
    }

    @Test
    fun calculateDateNextTriggerHandlesFeb29AndLunarTwelfthMonthInJanFeb() {
        val now2025 = calAt(2025, 3, 1, 9, 0)
        val feb29Reminder = ReminderEntity(
            title = "Leap Birthday",
            kind = "date",
            dateType = "solar_birthday",
            targetMonth = 2,
            targetDay = 29,
            reminderHour = 9,
            reminderMinute = 0,
            firstTriggerAt = now2025,
            nextTriggerAt = now2025
        )
        val nextLeap = ReminderEngine.calculateNextTrigger(feb29Reminder, from = now2025)
        val leapCal = Calendar.getInstance().apply { timeInMillis = nextLeap }
        assertEquals(2028, leapCal.get(Calendar.YEAR))
        assertEquals(2, leapCal.get(Calendar.MONTH) + 1)
        assertEquals(29, leapCal.get(Calendar.DAY_OF_MONTH))

        // 2028-01-05 is before Lunar 2027 腊月十五 (2028-01-11, present in JVM lunarOverrides)
        val earlyJan2028 = calAt(2028, 1, 5, 9, 0)
        val lunarLaYue = ReminderEntity(
            title = "腊月十五",
            kind = "date",
            dateType = "lunar_birthday",
            targetMonth = 12,
            targetDay = 15,
            reminderHour = 9,
            reminderMinute = 0,
            firstTriggerAt = earlyJan2028,
            nextTriggerAt = earlyJan2028
        )
        val nextLunar = ReminderEngine.calculateNextTrigger(lunarLaYue, from = earlyJan2028)
        val lunarCal = Calendar.getInstance().apply { timeInMillis = nextLunar }
        assertEquals(2028, lunarCal.get(Calendar.YEAR))
        assertEquals(1, lunarCal.get(Calendar.MONTH) + 1)
        assertEquals(11, lunarCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun occursOnHandlesLunarInSolarNewYearAndRulePeriod() {
        val lunar2027M12D15Solar = LunarCalendar.lunarToSolar(2027, 12, 15)!!
        val sc = Calendar.getInstance().apply { timeInMillis = lunar2027M12D15Solar }
        val y = sc.get(Calendar.YEAR)
        val m = sc.get(Calendar.MONTH) + 1
        val d = sc.get(Calendar.DAY_OF_MONTH)
        val lunarReminder = ReminderEntity(
            title = "腊月",
            kind = "date",
            dateType = "lunar_birthday",
            targetMonth = 12,
            targetDay = 15,
            firstTriggerAt = lunar2027M12D15Solar,
            nextTriggerAt = lunar2027M12D15Solar
        )
        assertTrue(ReminderEngine.occursOn(lunarReminder, y, m, d))

        val quarterlyRule = ReminderEntity(
            title = "Quarterly",
            kind = "rule",
            rulePeriod = "quarterly",
            ruleWeek = 1,
            ruleWeekday = 1,
            firstTriggerAt = calAt(2026, 1, 1),
            nextTriggerAt = calAt(2026, 1, 1)
        )
        // Find first Monday of month 2 and month 4 of 2026
        val febDay = firstWeekdayDay(2026, 2, Calendar.MONDAY)
        val aprDay = firstWeekdayDay(2026, 4, Calendar.MONDAY)
        assertFalse(ReminderEngine.occursOn(quarterlyRule, 2026, 2, febDay))
        assertTrue(ReminderEngine.occursOn(quarterlyRule, 2026, 4, aprDay))

        val yearlyRule = quarterlyRule.copy(rulePeriod = "yearly")
        val janDay = firstWeekdayDay(2026, 1, Calendar.MONDAY)
        assertTrue(ReminderEngine.occursOn(yearlyRule, 2026, 1, janDay))
        assertFalse(ReminderEngine.occursOn(yearlyRule, 2026, 4, aprDay))
    }

    private fun firstWeekdayDay(year: Int, month: Int, dow: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        while (cal.get(Calendar.DAY_OF_WEEK) != dow) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return cal.get(Calendar.DAY_OF_MONTH)
    }

    @Test
    fun statsServiceExcludesPrimaryNotifiedRecords() {
        val day1 = calAt(2026, 5, 1, 9, 0)
        val day2 = calAt(2026, 5, 2, 10, 0)
        val day3 = calAt(2026, 5, 3, 11, 0)
        val day4 = calAt(2026, 5, 4, 12, 0)
        val records = listOf(
            ReminderRecordEntity(id = 1, reminderId = 1, action = ReminderRecordEntity.ACTION_CONFIRMED, timestamp = day1),
            ReminderRecordEntity(id = 2, reminderId = 1, action = ReminderRecordEntity.ACTION_NOTIFIED, timestamp = day2, note = "primary"),
            ReminderRecordEntity(id = 3, reminderId = 1, action = ReminderRecordEntity.ACTION_NOTIFIED, timestamp = day3, note = "retry:1"),
            ReminderRecordEntity(id = 4, reminderId = 1, action = ReminderRecordEntity.ACTION_NOTIFIED, timestamp = day4, note = "")
        )
        val summary = StatsService.summarize(records)
        assertEquals(1, summary.confirmCount)
        assertEquals(2, summary.missedCount)
    }

    @Test
    fun firstSyncConflictsWhenOnlyDeletionsExistAndRemoteHasData() {
        val snapshot = ReminderSyncCoordinator.Snapshot(
            items = emptyList(),
            generation = 1L,
            lastChange = 1000L,
            firstSync = true,
            lastSyncGeneration = 0L,
            lastSyncedAt = 0L,
            deletions = listOf(SyncDeletion("reminder", "11111111-1111-4111-8111-111111111111"))
        )
        assertTrue(ReminderSyncCoordinator.conflicts(snapshot, 5L, 2000L, remoteHasData = true))
        assertFalse(ReminderSyncCoordinator.conflicts(snapshot, 5L, 2000L, remoteHasData = false))
    }

    @Test
    fun contentFingerprintMatchesAcrossDifferentRevisionsAndIds() {
        val now = 1_770_000_000_000L
        val rA = ReminderEntity(
            id = 1,
            syncId = "11111111-2222-4333-8444-555555555555",
            title = "Water plants",
            cycle = "weekly",
            firstTriggerAt = now,
            nextTriggerAt = now + 86_400_000L,
            revision = 3
        )
        val recA = ReminderRecordEntity(
            id = 9,
            reminderId = 1,
            action = ReminderRecordEntity.ACTION_CONFIRMED,
            timestamp = now,
            syncId = "aaaaaaaa-bbbb-4ccc-8ddd-eeeeeeeeeeee"
        )
        val delA = listOf(SyncDeletion("reminder", "99999999-8888-4777-8666-555555555555"))
        val json = BackupService.exportToJson(listOf(rA), dataVersion = 42L, records = listOf(recA), deletions = delA)
        val fpDirect = BackupService.contentFingerprint(listOf(rA.copy(revision = 99)), listOf(recA), delA)
        val fpJson = BackupService.contentFingerprintOfJson(json)
        assertNotNull(fpJson)
        assertEquals(fpDirect, fpJson)
    }
}
