package com.reminderapp.service

import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.receiver.WidgetActionReceiver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SixFeaturesTest {

    @Test
    fun formatHttpError_keepsRawStatusAndBody() {
        val msg = AIService.formatHttpError(
            401,
            """{"error":{"message":"Incorrect API key provided: sk-xxx","type":"invalid_request_error"}}"""
        )
        assertTrue(msg.startsWith("HTTP 401:"))
        assertTrue(msg.contains("Incorrect API key"))
        assertFalse(msg.contains("API Key 无效"))
        assertEquals("HTTP 404: {\"error\":\"not found\"}", AIService.formatHttpError(404, "{\"error\":\"not found\"}"))
    }

    @Test
    fun orderedModels_putsLastSelectedFirst() {
        assertEquals(
            listOf("llama3.2", "a", "b"),
            AIService.orderedModels(listOf("a", "llama3.2", "b"), "llama3.2")
        )
        assertEquals(listOf("a", "b"), AIService.orderedModels(listOf("a", "b"), "missing"))
    }

    @Test
    fun templates_coverTaxDomainBirthday() {
        val ids = ReminderTemplate.all.map { it.id }
        assertTrue(ids.containsAll(listOf("tax", "domain", "birthday")))
        val tax = ReminderTemplate.all.first { it.id == "tax" }
        assertTrue(tax.holidayAware)
        assertEquals(ReminderTemplate.Kind.CYCLE, tax.kind)
        assertEquals(ReminderTemplate.Cycle.MONTHLY, tax.cycle)
        val domain = ReminderTemplate.all.first { it.id == "domain" }
        assertEquals(30, domain.advanceDays)
        assertEquals(ReminderTemplate.Kind.DATE, domain.kind)
        val bday = ReminderTemplate.all.first { it.id == "birthday" }
        assertEquals(ReminderTemplate.DateKind.SOLAR_BIRTHDAY, bday.dateType)
    }

    @Test
    fun sharePlainText_includesTitleCycleNext() {
        val text = ReminderShare.plainText(
            title = "域名续期",
            cycleLabel = "每年",
            nextTriggerAt = 1_800_000_000_000L,
            note = "提前 30 天"
        )
        assertTrue(text.contains("提醒：域名续期"))
        assertTrue(text.contains("周期：每年"))
        assertTrue(text.contains("下次触发："))
        assertTrue(text.contains("备注：提前 30 天"))
    }

    @Test
    fun shareCycleLabel_yearlyAndBirthday() {
        val yearly = ReminderEntity(
            title = "域名续期",
            kind = "cycle",
            cycle = "yearly",
            firstTriggerAt = 1L,
            nextTriggerAt = 1L
        )
        assertEquals("每年", ReminderShare.cycleLabel(yearly))
        val bday = yearly.copy(kind = "date", dateType = "solar_birthday")
        assertTrue(ReminderShare.cycleLabel(bday).isNotBlank())
    }

    @Test
    fun notificationActionIds_matchReceivers() {
        assertEquals("com.reminderapp.CONFIRM", NotificationManager.ACTION_CONFIRM)
        assertEquals("com.reminderapp.SNOOZE", NotificationManager.ACTION_SNOOZE)
        assertEquals("com.reminderapp.widget.ACTION_COMPLETE", WidgetActionReceiver.ACTION_COMPLETE)
        assertEquals("com.reminderapp.widget.ACTION_SNOOZE", WidgetActionReceiver.ACTION_SNOOZE)
        assertEquals("reminder_id", NotificationManager.EXTRA_REMINDER_ID)
        assertEquals("highlight_confirm", NotificationManager.EXTRA_HIGHLIGHT_CONFIRM)
    }
}
