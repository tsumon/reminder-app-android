package com.reminderapp.service

import android.content.Context
import java.util.Calendar

/**
 * 勿扰时段（v2.1.1）：单条提醒的每日静默窗口。
 * 存 SharedPreferences（按提醒本地 id 索引）——与 iOS QuietHoursStore 语义一致，
 * 避免改 DB schema 引发 migration 风险。
 * 语义：start 到 end（分钟，0..1439）之间不弹通知；end < start 表示跨天窗口（如 22:00–08:00）。
 */
object QuietHoursStore {
    private const val PREFS = "quiet_hours_settings"
    private fun key(id: Long, suffix: String) = "quiet_hours_${id}_$suffix"

    fun isEnabled(context: Context, id: Long): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .contains(key(id, "start"))

    fun startMinute(context: Context, id: Long): Int? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .let { if (it.contains(key(id, "start"))) it.getInt(key(id, "start"), 0) else null }

    fun endMinute(context: Context, id: Long): Int? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .let { if (it.contains(key(id, "end"))) it.getInt(key(id, "end"), 0) else null }

    /** 设置勿扰窗口；传 null 关闭 */
    fun set(context: Context, id: Long, start: Int?, end: Int?) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        if (start != null && end != null) {
            editor.putInt(key(id, "start"), start).putInt(key(id, "end"), end)
        } else {
            editor.remove(key(id, "start")).remove(key(id, "end"))
        }
        editor.apply()
    }

    /**
     * 时间落在窗口内时顺延到窗口结束（未启用/不在窗口内原样返回）。
     * 供 ReminderWorker 到点执行时检查：在窗口内则不弹、重排到窗口结束。
     */
    fun adjust(context: Context, id: Long, timestamp: Long): Long {
        val start = startMinute(context, id) ?: return timestamp
        val end = endMinute(context, id) ?: return timestamp
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val mins = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        if (start < end) {
            // 当天窗口：start ≤ mins < end → 顺延到 end
            if (mins in start until end) {
                cal.set(Calendar.HOUR_OF_DAY, end / 60)
                cal.set(Calendar.MINUTE, end % 60)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                return cal.timeInMillis
            }
        } else {
            // 跨天窗口（22:00–08:00）：mins ≥ start（深夜）或 mins < end（凌晨）
            if (mins >= start) {
                cal.set(Calendar.HOUR_OF_DAY, end / 60)
                cal.set(Calendar.MINUTE, end % 60)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.add(Calendar.DAY_OF_YEAR, 1)
                return cal.timeInMillis
            }
            if (mins < end) {
                cal.set(Calendar.HOUR_OF_DAY, end / 60)
                cal.set(Calendar.MINUTE, end % 60)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                return cal.timeInMillis
            }
        }
        return timestamp
    }
}
