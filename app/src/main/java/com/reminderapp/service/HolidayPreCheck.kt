package com.reminderapp.service

import android.content.Context
import com.reminderapp.ReminderApp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zhf
import com.reminderapp.receiver.ReminderWidgetProvider
import java.util.Calendar

/**
 * 节假日前移预检（Item 2）
 *
 * 行为：对未完成的「周期 / 规则」提醒（不含生日、节假日等日期提醒），
 * 若其【下一次触发】落在约 31 天窗口内，提前联网检索该天是否为法定节假日：
 * - 命中节假日 → 把【这一次的触发】前移到「假期前最近工作日」，
 *   并在 holiday_adjust_note 写入说明文案；整体循环锚点 firstTriggerAt 不变，
 *   确认完成后该备注清空、下一周期照常推进。
 * - 未命中 → 不动。
 *
 * 触发时机：App 启动（刷新完远端节假日数据后）+ 每次确认完成后。
 * 设计要点：
 * - isOffDay 判定综合「远端节假日(isHoliday)」与「普通周末」：
 *   远端只列出法定节假日与调休上班日，普通周末 status 为 null，
 *   因此 null 时按星期判断周末，调休上班日(isHoliday=false)视为工作日。
 * - 仅在「已识别的节假日」上触发前移（周末不触发），避免对正常周末循环误移。
 * - 已前移过（holiday_adjust_note 非空）的本次不再重复前移。
 */
object HolidayPreCheck {

    private const val WINDOW_DAYS = 31
    private const val DAY_MS = 86_400_000L

    suspend fun run(context: Context) {
        val db = ReminderApp.instance.database
        val scheduler = ReminderApp.instance.scheduler
        val now = System.currentTimeMillis()
        val windowMs = WINDOW_DAYS * DAY_MS

        val all = db.reminderDao().getAllSync()
        val refreshedYears = mutableSetOf<Int>()

        for (r in all) {
            if (!r.isActive) continue
            if (r.status == "confirmed" || r.status == "overdue") continue
            // 仅非生日循环/规则提醒参与（日期提醒：生日、节假日本身不参与前移）
            if (r.kind != "cycle" && r.kind != "rule") continue
            // 本次已前移过则跳过，避免重复
            if (!r.holidayAdjustNote.isNullOrEmpty()) continue

            val occ = r.nextTriggerAt
            if (occ <= now) continue
            if (occ - now > windowMs) continue

            val cal = Calendar.getInstance().apply { timeInMillis = occ }
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH) + 1
            val d = cal.get(Calendar.DAY_OF_MONTH)

            // 确保当年节假日数据就绪（同一年只刷一次）
            if (refreshedYears.add(y)) {
                HolidayRemoteService.refresh(context, y)
            }

            val st = HolidayRemoteService.status(context, y, m, d) ?: continue
            if (!st.isHoliday) continue

            // 前移到假期前最近工作日
            val shifted = lastWorkingDay(context, occ) ?: continue
            if (shifted <= now) continue

            val newTs = buildShiftedTs(cal, shifted)
            val shiftedCal = Calendar.getInstance().apply { timeInMillis = shifted }
            val note = zhf(
                "因%s放假，已前移至假期前最近工作日（%02d-%02d）",
                st.name,
                shiftedCal.get(Calendar.MONTH) + 1,
                shiftedCal.get(Calendar.DAY_OF_MONTH)
            )

            val updated = r.copy(nextTriggerAt = newTs, holidayAdjustNote = note)
            db.reminderDao().update(updated)
            scheduler.schedule(updated)
            SyncStore.touchLocalChange()
            ReminderWidgetProvider.refresh(context)

            android.util.Log.i(
                "HolidayPreCheck",
                "前移: ${r.title} ${y}-${m}-${d} → ${shiftedCal.get(Calendar.YEAR)}-${shiftedCal.get(Calendar.MONTH) + 1}-${shiftedCal.get(Calendar.DAY_OF_MONTH)}"
            )
        }
    }

    /** 返回 occ 之前（不含）最近的一个工作日时间戳；找不到返回 null */
    private fun lastWorkingDay(context: Context, occ: Long): Long? {
        var cursor = occ - DAY_MS
        repeat(10) {
            if (!isOffDay(context, cursor)) return cursor
            cursor -= DAY_MS
        }
        return null
    }

    /** 某天是否休息日：法定节假日(isHoliday) 或 普通周末（远端无数据时按星期判断） */
    private fun isOffDay(context: Context, t: Long): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = t }
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val st = HolidayRemoteService.status(context, y, m, d)
        if (st != null) return st.isHoliday
        val dow = cal.get(Calendar.DAY_OF_WEEK) // 1=周日..7=周六
        return dow == Calendar.SATURDAY || dow == Calendar.SUNDAY
    }

    /** 用 shifted 的日期 + occ 的「时分」拼出新的触发时间戳 */
    private fun buildShiftedTs(occCal: Calendar, shifted: Long): Long {
        val sc = Calendar.getInstance().apply { timeInMillis = shifted }
        sc.set(Calendar.HOUR_OF_DAY, occCal.get(Calendar.HOUR_OF_DAY))
        sc.set(Calendar.MINUTE, occCal.get(Calendar.MINUTE))
        sc.set(Calendar.SECOND, 0)
        sc.set(Calendar.MILLISECOND, 0)
        return sc.timeInMillis
    }
}
