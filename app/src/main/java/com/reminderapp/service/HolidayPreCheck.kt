package com.reminderapp.service

import android.content.Context
import com.reminderapp.ReminderApp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zhf
import java.util.Calendar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull

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

    suspend fun adjusted(context: Context, r: ReminderEntity, now: Long,
        refresh: suspend (Int) -> Unit = { HolidayRemoteService.refresh(context,it) }): ReminderEntity {
        if (!r.isActive || r.status in setOf("confirmed","overdue") || r.kind !in setOf("cycle","rule") ||
            !r.holidayAdjustNote.isNullOrEmpty() || r.nextTriggerAt <= now || r.nextTriggerAt-now > WINDOW_DAYS*DAY_MS) return r
        val cal=Calendar.getInstance().apply { timeInMillis=r.nextTriggerAt }
        val y=cal.get(Calendar.YEAR);val m=cal.get(Calendar.MONTH)+1;val d=cal.get(Calendar.DAY_OF_MONTH)
        // Known cached data completes immediately. A missing entry may refresh without holding the mutation gate.
        val cached=HolidayRemoteService.status(context,y,m,d)
        if(cached==null && !HolidayRemoteService.hasCachedYear(context,y)) {
            try { withTimeoutOrNull(1500) { refresh(y) } }
            catch(e: CancellationException) { throw e }
            catch(_: Exception) { /* Optional refresh never blocks an offline completion. */ }
        }
        val st=HolidayRemoteService.status(context,y,m,d) ?: return r
        if(!st.isHoliday) return r
        val shifted=lastWorkingDay(context,r.nextTriggerAt) ?: return r
        val newTs=buildShiftedTs(cal,shifted)
        if(newTs<=now) return r
        val day=Calendar.getInstance().apply { timeInMillis=newTs }
        return r.copy(nextTriggerAt=newTs,holidayAdjustNote=zhf(
            "因%s放假，已前移至假期前最近工作日（%02d-%02d）",st.name,day.get(Calendar.MONTH)+1,day.get(Calendar.DAY_OF_MONTH)))
    }

    suspend fun run(context: Context) {
        val db=ReminderApp.instance.database
        val now=System.currentTimeMillis()
        val refreshedYears=mutableSetOf<Int>()
        for(r in db.reminderDao().getAllSync()) {
            val adjusted=adjusted(context,r,now) { year ->
                if(refreshedYears.add(year)) HolidayRemoteService.refresh(context,year)
            }
            if(adjusted!=r) ReminderActions(context,db).edit(r,adjusted)
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
