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
 *   "version": 1,
 *   "exportedAt": <timestamp>,
 *   "dataVersion": <本地自增版本, v2.0.17>,
 *   "reminders": [ { ...ReminderEntity 全部字段... } ]
 * }
 */
object BackupService {

    private const val BACKUP_VERSION = 1

    /** 将提醒列表导出为 JSON 字符串（dataVersion 取当前本地单调版本，判新主依据） */
    fun exportToJson(reminders: List<ReminderEntity>): String {
        val root = JSONObject()
        root.put("version", BACKUP_VERSION)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("dataVersion", SyncStore.localVersion)

        val arr = JSONArray()
        reminders.forEach { r ->
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("title", r.title)
            obj.put("note", r.note)
            obj.put("kind", r.kind)
            obj.put("cycle", r.cycle)
            obj.put("customDays", r.customDays)
            obj.put("dateType", r.dateType ?: JSONObject.NULL)
            obj.put("targetMonth", r.targetMonth ?: JSONObject.NULL)
            obj.put("targetDay", r.targetDay ?: JSONObject.NULL)
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
            // H1: 关键提醒标记纳入备份/同步（缺省 false），覆盖 WebDAV/备份/局域网/分享卡片全部通道
            obj.put("is_critical", r.isCritical)
            arr.put(obj)
        }
        root.put("reminders", arr)
        return root.toString(2)
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
                    title = o.optString("title", zh("未命名提醒")),
                    note = o.optString("note", ""),
                    kind = o.optString("kind", "cycle"),
                    cycle = o.optString("cycle", "weekly"),
                    customDays = o.optInt("customDays", 0),
                    dateType = if (o.isNull("dateType")) null else o.optString("dateType"),
                    targetMonth = if (o.isNull("targetMonth")) null else o.optInt("targetMonth"),
                    targetDay = if (o.isNull("targetDay")) null else o.optInt("targetDay"),
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
                    // H1: 导入时写回关键标记（缺省 false，兼容旧备份文件缺字段）
                    isCritical = o.optBoolean("is_critical", false)
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
