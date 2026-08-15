package com.reminderapp.service

import android.content.Context
import android.net.Uri
import com.reminderapp.data.entity.ReminderEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import com.reminderapp.i18n.zh

/**
 * 提醒数据导入/导出（JSON 格式，双端统一）
 *
 * 导出文件结构：
 * {
 *   "version": 1,          // 历史格式版本（旧版读取兼容）
 *   "schemaVersion": 2,    // 阶段2: 协议版本（2 = syncId/holidayId/isCritical；缺省按 1 解析）
 *   "exportedAt": <timestamp>,
 *   "dataVersion": <本地自增版本, v2.0.17>,
 *   "reminders": [ { ... } ]
 * }
 *
 * 协议 v2 变更（阶段2）：
 * - 每条新增 syncId（跨平台稳定 UUID，iOS 端直接复用 Reminder.id）
 * - 每条新增 holidayId（节假日稳定 ID，与 iOS Holiday.id 对齐；holidayName 保留兼容旧文件）
 * - isCritical 改为 camelCase（is_critical 继续导出供旧版读取）
 * - 导入旧文件（无 schemaVersion / 无 syncId）时：字段回落默认值，syncId 由调用方补 UUID
 */
object BackupService {

    private const val BACKUP_VERSION = 1
    private const val SCHEMA_VERSION = 2

    /** 将提醒列表导出为 JSON 字符串（dataVersion 取当前本地单调版本，判新主依据） */
    fun exportToJson(reminders: List<ReminderEntity>, dataVersion: Long = SyncStore.localVersion): String {
        val root = JSONObject()
        root.put("version", BACKUP_VERSION)
        root.put("schemaVersion", SCHEMA_VERSION)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("dataVersion", dataVersion)

        val arr = JSONArray()
        reminders.forEach { r ->
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("syncId", r.syncId ?: java.util.UUID.randomUUID().toString())
            obj.put("title", r.title)
            obj.put("note", r.note)
            obj.put("kind", r.kind)
            obj.put("cycle", r.cycle)
            obj.put("customDays", r.customDays)
            obj.put("dateType", r.dateType ?: JSONObject.NULL)
            obj.put("targetMonth", r.targetMonth ?: JSONObject.NULL)
            obj.put("targetDay", r.targetDay ?: JSONObject.NULL)
            // 阶段2: 协议统一为 holidayId（稳定 ID）；holidayName 保留导出供旧版读取
            obj.put("holidayId", r.holidayId ?: JSONObject.NULL)
            obj.put("holidayName", r.holidayName ?: JSONObject.NULL)
            obj.put("advanceDays", r.advanceDays)
            obj.put("reminderHour", r.reminderHour)
            obj.put("reminderMinute", r.reminderMinute)
            obj.put("rulePeriod", r.rulePeriod ?: JSONObject.NULL)
            obj.put("ruleWeek", r.ruleWeek ?: JSONObject.NULL)
            obj.put("ruleWeekday", r.ruleWeekday ?: JSONObject.NULL)
            obj.put("priority", r.priority)
            obj.put("firstTriggerAt", r.firstTriggerAt)
            obj.put("nextTriggerAt", r.nextTriggerAt)
            obj.put("status", r.status)
            obj.put("isActive", r.isActive)
            // v2.0.22: 补齐此前遗漏的调度/元数据字段 —— 旧版备份缺字段时导入端
            // 用 optLong/optString 默认值兼容，不会破坏旧文件
            obj.put("retryCount", r.retryCount)
            obj.put("lastConfirmedAt", r.lastConfirmedAt ?: JSONObject.NULL)
            obj.put("createdAt", r.createdAt)
            obj.put("holidayAdjustNote", r.holidayAdjustNote ?: JSONObject.NULL)
            // H1: 关键提醒标记纳入备份/同步（缺省 false），覆盖 WebDAV/备份/局域网/分享卡片全部通道。
            // 阶段2: 统一为 camelCase，is_critical 双写兼容旧版读取
            obj.put("isCritical", r.isCritical)
            obj.put("is_critical", r.isCritical)
            arr.put(obj)
        }
        root.put("reminders", arr)
        return root.toString(2)!!
    }

    /** 从 JSON 字符串解析提醒列表（失败返回 null） */
    fun importFromJson(json: String): List<ReminderEntity>? {        return try {
            val root = JSONObject(json)
            val arr = root.optJSONArray("reminders") ?: return emptyList()
            val list = mutableListOf<ReminderEntity>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val entity = ReminderEntity(
                    id = o.optLong("id", 0),
                    // 阶段2: syncId 缺省为 null（旧文件无此字段），由调用方 ensureSyncId 补 UUID
                    syncId = if (o.isNull("syncId")) null else o.optString("syncId"),
                    title = o.optString("title", zh("未命名提醒")),
                    note = o.optString("note", ""),
                    kind = o.optString("kind", "cycle"),
                    cycle = o.optString("cycle", "weekly"),
                    customDays = o.optInt("customDays", 0),
                    dateType = if (o.isNull("dateType")) null else o.optString("dateType"),
                    targetMonth = if (o.isNull("targetMonth")) null else o.optInt("targetMonth"),
                    targetDay = if (o.isNull("targetDay")) null else o.optInt("targetDay"),
                    // 阶段2: holidayId 优先；旧文件只有 holidayName（iOS 旧导出实际塞的是 ID）→ 兜底读入
                    holidayId = if (o.isNull("holidayId")) null else o.optString("holidayId"),
                    holidayName = if (o.isNull("holidayName")) null else o.optString("holidayName"),
                    advanceDays = o.optInt("advanceDays", 3),
                    reminderHour = o.optInt("reminderHour", 9),
                    reminderMinute = o.optInt("reminderMinute", 0),
                    rulePeriod = if (o.isNull("rulePeriod")) null else o.optString("rulePeriod"),
                    ruleWeek = if (o.isNull("ruleWeek")) null else o.optInt("ruleWeek"),
                    ruleWeekday = if (o.isNull("ruleWeekday")) null else o.optInt("ruleWeekday"),
                    priority = o.optString("priority", "normal"),
                    firstTriggerAt = o.optLong("firstTriggerAt", System.currentTimeMillis()),
                    nextTriggerAt = o.optLong("nextTriggerAt", System.currentTimeMillis()),
                    status = o.optString("status", "idle"),
                    isActive = o.optBoolean("isActive", true),
                    // v2.0.22: 恢复补齐的调度/元数据字段（旧文件缺字段回落默认值）
                    retryCount = o.optInt("retryCount", 0),
                    lastConfirmedAt = if (o.isNull("lastConfirmedAt")) null else o.optLong("lastConfirmedAt", -1L).takeIf { it > 0L },
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                    holidayAdjustNote = if (o.isNull("holidayAdjustNote")) null else o.optString("holidayAdjustNote", "").ifEmpty { null },
                    // H1: 导入时写回关键标记（缺省 false，兼容旧备份文件缺字段）；阶段2 改 camelCase，旧 snake 兜底
                    isCritical = o.optBoolean("isCritical", o.optBoolean("is_critical", false))
                )
                list.add(entity)
            }
            list
        } catch (e: Exception) {
            null
        }
    }

    /** 导入指纹（与近场传输共用，防误判）：title|nextTriggerAt|kind|cycle|note */
    fun fingerprint(r: ReminderEntity): String =
        "${r.title}|${r.nextTriggerAt}|${r.kind}|${r.cycle}|${r.note ?: ""}"

    /** 阶段2: syncId 缺失时补 UUID（旧文件/手写数据），保证落库的提醒都有跨端稳定 ID */
    fun ensureSyncId(r: ReminderEntity): ReminderEntity =
        if (r.syncId.isNullOrBlank()) r.copy(syncId = java.util.UUID.randomUUID().toString()) else r

    /**
     * 统一导入去重：按指纹过滤掉与现有提醒重复的条目。
     * 文件导入与近场传输共用同一规则，避免「文件导入重复新增、近场传输跳过重复」的跨入口不一致。
     */
    fun dedupeByFingerprint(
        entities: List<ReminderEntity>,
        existingFingerprints: Set<String>
    ): List<ReminderEntity> = entities.filter { fingerprint(it) !in existingFingerprints }

    /** 批次3 功能6: 单条提醒分享卡片 —— 复用备份信封格式导出单条 */
    fun exportSingle(reminder: ReminderEntity): String = exportToJson(listOf(reminder))

    /** 批次3 功能6: 从分享卡片 JSON 解析出单条（失败返回 null） */
    fun importSingle(json: String): ReminderEntity? = importFromJson(json)?.firstOrNull()

    /** 读取 JSON 中的 dataVersion（v2.0.17 单调版本；旧文件缺字段返回 0） */
    fun dataVersionOf(json: String): Long {
        return try {
            JSONObject(json).optLong("dataVersion", 0L)
        } catch (e: Exception) {
            0L
        }
    }

    /** 阶段2: 读取协议 schemaVersion（缺省视为 1——旧协议无 syncId/holidayId） */
    fun schemaVersionOf(json: String): Int {
        return try {
            JSONObject(json).optInt("schemaVersion", 1)
        } catch (e: Exception) {
            1
        }
    }

    /** 轻量读取 JSON 内提醒条数（v2.0.21 F1：首次同步冲突判定用，不做完整解析） */
    fun remindersCountOf(json: String): Int {
        return try {
            JSONObject(json).optJSONArray("reminders")?.length() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /** 将 JSON 字符串写入 Uri（SAF） */
    fun writeToUri(context: Context, uri: Uri, json: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(json.toByteArray(Charsets.UTF_8))
            } != null
        } catch (e: Exception) {
            false
        }
    }

    /** 从 Uri 读取全部文本 */
    fun readFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
