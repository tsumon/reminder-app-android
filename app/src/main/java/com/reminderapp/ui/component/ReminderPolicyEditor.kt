package com.reminderapp.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zh

@Composable
fun ReminderPolicyEditor(value: ReminderEntity, onChange: (ReminderEntity) -> Unit) {
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text(zh("提醒方式"),style=MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            listOf("gentle" to "轻柔", "persistent" to "持续", "important" to "重要").forEach { (style,label) ->
                FilterChip(selected=value.reminderStyle==style,onClick={onChange(value.copy(reminderStyle=style,isCritical=style=="important"))},label={Text(zh(label))})
            }
        }
        if(value.reminderStyle=="important") Text(zh("重要提醒使用高优先级通知；全屏和勿扰权限由系统控制。"),style=MaterialTheme.typography.bodySmall)
        if(value.kind=="cycle" && value.cycle!="once") {
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                FilterChip(selected=value.recurrenceMode=="fixed",onClick={onChange(value.copy(recurrenceMode="fixed"))},label={Text(zh("固定周期"))})
                FilterChip(selected=value.recurrenceMode=="afterCompletion",onClick={onChange(value.copy(recurrenceMode="afterCompletion"))},label={Text(zh("完成后重新计时"))})
            }
        }
        if(value.reminderStyle!="gentle") {
            var intervals by remember(value.id) { mutableStateOf(value.retryIntervalsMinutes) }
            var cap by remember(value.id) { mutableStateOf(value.maxReminderAttempts.toString()) }
            OutlinedTextField(value=intervals,onValueChange={ text ->
                intervals=text
                val list=text.split(",").map { it.trim().toIntOrNull() }
                if(list.size in 1..10 && list.all { it != null && it in 1..10080 }) onChange(value.copy(retryIntervalsMinutes=list.joinToString(",")))
            },label={Text(zh("重试间隔（分钟，逗号分隔，1–10080）"))},supportingText={Text(zh("最多 10 项；最后一个间隔重复使用"))},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(value=cap,onValueChange={ cap=it;it.toIntOrNull()?.takeIf { n -> n in 1..10 }?.let { n -> onChange(value.copy(maxReminderAttempts=n)) } },label={Text(zh("最多提醒次数（1–10，含首次）"))},modifier=Modifier.fillMaxWidth())
        }
        var quietStart by remember(value.id) { mutableStateOf(value.quietStartMinute?.let { "%02d:%02d".format(it/60,it%60) } ?: "") }
        var quietEnd by remember(value.id) { mutableStateOf(value.quietEndMinute?.let { "%02d:%02d".format(it/60,it%60) } ?: "") }
        fun minute(text: String): Int? { val p=text.split(":").map { it.toIntOrNull() };return if(p.size==2 && p[0] in 0..23 && p[1] in 0..59) p[0]!!*60+p[1]!! else null }
        fun updateQuiet() { val start=minute(quietStart);val end=minute(quietEnd);if(start!=null && end!=null) onChange(value.copy(quietStartMinute=start,quietEndMinute=end));if(quietStart.isBlank() && quietEnd.isBlank()) onChange(value.copy(quietStartMinute=null,quietEndMinute=null)) }
        OutlinedTextField(value=quietStart,onValueChange={quietStart=it;updateQuiet()},label={Text(zh("勿扰开始 HH:mm（留空关闭）"))},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(value=quietEnd,onValueChange={quietEnd=it;updateQuiet()},label={Text(zh("勿扰结束 HH:mm"))},modifier=Modifier.fillMaxWidth())
    }
}
