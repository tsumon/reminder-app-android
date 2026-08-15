package com.reminderapp.service

import com.reminderapp.data.entity.ReminderEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 备份导入去重回归测试 — 文件导入与近场传输必须共用同一指纹规则，
 * 避免「文件导入重复新增、近场传输跳过重复」的跨入口不一致。
 */
class BackupServiceTest {

    private fun reminder(title: String, nextTriggerAt: Long) = ReminderEntity(
        title = title,
        nextTriggerAt = nextTriggerAt,
        firstTriggerAt = nextTriggerAt
    )

    @Test
    fun `dedupeByFingerprint 过滤与现有完全相同的条目`() {
        val existing = reminder("吃药", 1_000L)
        val incoming = reminder("遛狗", 2_000L)

        val result = BackupService.dedupeByFingerprint(
            listOf(existing, incoming),
            setOf(BackupService.fingerprint(existing))
        )
        assertEquals("重复条目应被过滤，只保留新增", listOf(incoming), result)
    }

    @Test
    fun `指纹区分同名但时间或备注不同的条目`() {
        val a = reminder("吃药", 1_000L).copy(note = "早饭后")
        val b = reminder("吃药", 1_000L).copy(note = "晚饭后")

        val result = BackupService.dedupeByFingerprint(listOf(a, b), setOf(BackupService.fingerprint(a)))
        assertTrue("备注不同的同名提醒不应被误判为重复", result.contains(b))
    }

    @Test
    fun `导入导出往返保持关键字段`() {
        val r = reminder("遛狗", 2_000L).copy(
            kind = "cycle", cycle = "daily", status = "notifying", retryCount = 2,
            isCritical = true
        )
        val json = BackupService.exportToJson(listOf(r), dataVersion = 0)
        val parsed = BackupService.importFromJson(json)
        assertEquals(1, parsed?.size)
        val back = parsed!![0]
        assertEquals(r.title, back.title)
        assertEquals(r.status, back.status)
        assertEquals(r.isCritical, back.isCritical)
    }
}
