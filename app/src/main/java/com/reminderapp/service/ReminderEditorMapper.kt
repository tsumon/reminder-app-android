package com.reminderapp.service

import com.reminderapp.data.entity.ReminderEntity
import java.util.Calendar

/** Pure form mapping. Metadata/policy edits never reconstruct runtime or occurrence state. */
object ReminderEditorMapper {
    fun seed(r: ReminderEntity): ReminderEntity {
        if(r.kind!="cycle") return r
        val anchor=Calendar.getInstance().apply { timeInMillis=r.firstTriggerAt }
        return r.copy(reminderHour=anchor.get(Calendar.HOUR_OF_DAY),reminderMinute=anchor.get(Calendar.MINUTE),
            targetDay=if(r.cycle=="monthly") anchor.get(Calendar.DAY_OF_MONTH) else r.targetDay)
    }
    fun map(original: ReminderEntity?, proposed: ReminderEntity, scheduleChanged: Boolean, now: Long): ReminderEntity {
        val old=original?.takeIf { it.id>0 }
        if(old==null) return proposed.copy(nextTriggerAt=if(proposed.kind=="cycle") proposed.firstTriggerAt else ReminderEngine.calculateNextTrigger(proposed,now))
        val edited=old.copy(title=proposed.title,note=proposed.note,priority=proposed.priority,
            recurrenceMode=proposed.recurrenceMode,reminderStyle=proposed.reminderStyle,isCritical=proposed.isCritical,
            retryIntervalsMinutes=proposed.retryIntervalsMinutes,maxReminderAttempts=proposed.maxReminderAttempts,
            quietStartMinute=proposed.quietStartMinute,quietEndMinute=proposed.quietEndMinute,
            advanceDays=proposed.advanceDays,revision=old.revision+1)
        if(!scheduleChanged) return edited
        val configured=edited.copy(kind=proposed.kind,cycle=proposed.cycle,customDays=proposed.customDays,
            dateType=proposed.dateType,targetMonth=proposed.targetMonth,targetDay=proposed.targetDay,
            holidayName=proposed.holidayName,holidayId=proposed.holidayId,holidayAware=proposed.holidayAware,
            rulePeriod=proposed.rulePeriod,ruleWeek=proposed.ruleWeek,ruleWeekday=proposed.ruleWeekday,
            weeklyWeekday=proposed.weeklyWeekday,reminderHour=proposed.reminderHour,reminderMinute=proposed.reminderMinute,
            firstTriggerAt=proposed.firstTriggerAt,status="pending",retryCount=0,holidayAdjustNote=null)
        val next=if(configured.kind=="cycle" && configured.firstTriggerAt>now && configured.recurrenceMode!="afterCompletion") configured.firstTriggerAt else ReminderEngine.calculateNextTrigger(configured,now)
        return configured.copy(nextTriggerAt=next)
    }
}
