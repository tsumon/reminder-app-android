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
            isCritical = true, holidayAware = true
        )
        val json = BackupService.exportToJson(listOf(r), dataVersion = 0)
        val parsed = BackupService.importFromJson(json)
        assertEquals(1, parsed?.size)
        val back = parsed!![0]
        assertEquals(r.title, back.title)
        assertEquals(r.status, back.status)
        assertEquals(r.isCritical, back.isCritical)
        assertEquals(r.holidayAware, back.holidayAware)
    }

    // ===== 阶段2: 跨端协议 golden fixture（与 iOS 共享同一份内容）=====

    private fun fixture(name: String): String {
        val stream = javaClass.getResourceAsStream("/fixtures/$name")
            ?: throw AssertionError("fixture 缺失: /fixtures/$name")
        return stream.bufferedReader().use { it.readText() }
    }

    @Test
    fun `协议v2 fixture 完整解析`() {
        val json = fixture("protocol_v2.json")
        assertEquals(2, BackupService.schemaVersionOf(json))
        assertEquals(42L, BackupService.dataVersionOf(json))

        val items = BackupService.importFromJson(json)!!
        assertEquals(4, items.size)

        // syncId 跨端稳定 ID 原样保留
        assertEquals("8f14e45f-ceea-4b5f-8d1a-9c3f2b7e5d01", items[0].syncId)

        // 节假日提醒：holidayId 优先于 holidayName
        val holiday = items[1]
        assertEquals("zhongqiu", holiday.holidayId)
        assertEquals("中秋节", holiday.holidayName)

        // 关键提醒 camelCase
        assertTrue(items[0].isCritical)
        assertTrue(!items[1].isCritical)

        // v2.4.8: 避开节假日/周末
        assertTrue(items[0].holidayAware)
        assertTrue(!items[1].holidayAware)

        // 派生/元数据字段
        assertEquals(2, items[1].retryCount)
        assertEquals("notifying", items[1].status)
        assertEquals("confirmed", items[2].status)
        assertEquals("overdue", items[3].status)
        assertEquals(5, items[3].retryCount)
        assertEquals("因中秋节放假，已前移至假期前最近工作日", items[1].holidayAdjustNote)
        assertEquals(1_755_200_000_000L, items[2].lastConfirmedAt)
    }

    @Test
    fun `协议v1 旧文件兼容解析`() {
        val json = fixture("protocol_v1_legacy.json")
        // 旧文件无 schemaVersion → 视为 1
        assertEquals(1, BackupService.schemaVersionOf(json))
        // 旧文件无 syncId → null，由调用方 ensureSyncId 补 UUID
        val items = BackupService.importFromJson(json)!!
        assertEquals(2, items.size)
        assertTrue(items[0].syncId == null)

        // is_critical snake_case 兜底
        assertTrue(items[0].isCritical)
        assertTrue(!items[1].isCritical)

        // iOS 旧导出 holidayName 实际塞 ID → 读入 holidayName，holidayId 为 null（按名称反查逻辑兜底）
        assertEquals("zhongqiu", items[1].holidayName)
        assertTrue(items[1].holidayId == null)

        // 缺字段回落默认值
        assertEquals("daily", items[0].cycle)
        assertEquals("pending", items[0].status)
        assertEquals(0, items[0].retryCount)
    }

    @Test
    fun `ensureSyncId 缺失时补 UUID 且保留已有值`() {
        val withSync = reminder("吃药", 1_000L).copy(syncId = "11111111-2222-3333-4444-555555555555")
        assertEquals("11111111-2222-3333-4444-555555555555", BackupService.ensureSyncId(withSync).syncId)

        val withoutSync = reminder("遛狗", 2_000L).copy(syncId = null)
        val fixed = BackupService.ensureSyncId(withoutSync)
        assertTrue(fixed.syncId != null)
        assertTrue(!fixed.syncId.isNullOrBlank())
    }

    @Test
    fun `导出包含 schemaVersion 与 syncId`() {
        val r = reminder("吃药", 1_000L).copy(syncId = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
        val json = BackupService.exportToJson(listOf(r), dataVersion = 0)
        assertTrue(json.contains("\"schemaVersion\": 2"))
        assertTrue(json.contains("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"))
        // 双写：新旧字段都在
        assertTrue(json.contains("\"isCritical\": false"))
        assertTrue(json.contains("\"is_critical\": false"))
        assertTrue(json.contains("\"holidayAware\": false"))
    }
}
