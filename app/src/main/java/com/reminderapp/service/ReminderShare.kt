package com.reminderapp.service

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zh
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 提醒详情分享：自然语言一段 + `.ics` 文件，走系统分享表。
 */
object ReminderShare {

    fun plainText(title: String, cycleLabel: String, nextTriggerAt: Long, note: String): String {
        val df = SimpleDateFormat("yyyy年M月d日 HH:mm", Locale.CHINA)
        val lines = mutableListOf(
            "提醒：$title",
            "周期：$cycleLabel",
            "下次触发：${df.format(Date(nextTriggerAt))}"
        )
        if (note.isNotBlank()) lines += "备注：$note"
        return lines.joinToString("\n")
    }

    fun cycleLabel(reminder: ReminderEntity): String {
        return when (reminder.kind) {
            "date" -> when (reminder.dateType) {
                "lunar_birthday" -> zh("农历生日")
                "holiday" -> reminder.holidayName ?: zh("节假日")
                else -> zh("公历每年")
            }
            "rule" -> listOfNotNull(reminder.rulePeriod, reminder.ruleWeek?.let { "第${it}周" }, reminder.ruleWeekday?.let { "周$it" })
                .joinToString("")
                .ifBlank { zh("规则提醒") }
            else -> when (reminder.cycle) {
                "once" -> zh("仅一次")
                "daily" -> zh("每天")
                "weekly" -> zh("每周")
                "biweekly" -> zh("每两周")
                "monthly" -> zh("每月")
                "quarterly" -> zh("每季度")
                "yearly" -> zh("每年")
                "custom" -> "每${reminder.customDays}天"
                else -> reminder.cycle
            }
        }
    }

    fun share(context: Context, reminder: ReminderEntity) {
        val text = plainText(
            title = reminder.title,
            cycleLabel = cycleLabel(reminder),
            nextTriggerAt = reminder.nextTriggerAt,
            note = reminder.note
        )
        val ics = IcsExporter.generateIcs(listOf(reminder))
        val safe = reminder.title.replace("/", "-").replace(":", "-").ifBlank { "reminder" }
        val file = File(context.cacheDir, "${safe.take(40)}.ics")
        file.writeText(ics)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/calendar"
            putExtra(Intent.EXTRA_SUBJECT, reminder.title)
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, zh("分享提醒")))
    }
}
