package com.reminderapp.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zh

@Composable
fun ReminderPolicyEditor(value: ReminderEntity, onChange: (ReminderEntity) -> Unit, onValidityChange: (Boolean) -> Unit = {}) {
    var intervals by remember(value.id) { mutableStateOf(value.retryIntervalsMinutes) }
    var cap by remember(value.id) { mutableStateOf(value.maxReminderAttempts.toString()) }
    var quietStart by remember(value.id) { mutableStateOf(value.quietStartMinute?.let { String.format(java.util.Locale.ROOT,"%02d:%02d",it/60,it%60) } ?: "") }
    var quietEnd by remember(value.id) { mutableStateOf(value.quietEndMinute?.let { String.format(java.util.Locale.ROOT,"%02d:%02d",it/60,it%60) } ?: "") }
    val retryValid=com.reminderapp.service.ReminderPolicy.validEditorInput(value.reminderStyle,intervals,cap,"","")
    val quietValid=com.reminderapp.service.ReminderPolicy.validEditorInput("gentle",intervals,cap,quietStart,quietEnd)
    LaunchedEffect(retryValid,quietValid) { onValidityChange(retryValid && quietValid) }
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text(zh("提醒方式"),style=MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            listOf("gentle" to "轻柔", "persistent" to "持续", "important" to "重要").forEach { (style,label) ->
                FilterChip(selected=value.reminderStyle==style,onClick={onChange(value.copy(reminderStyle=style,isCritical=style=="important"))},label={Text(zh(label))})
            }
        }
        if(value.reminderStyle=="important") Text(zh("重要提醒使用高优先级通知；全屏和勿扰权限由系统控制。"),style=MaterialTheme.typography.bodySmall)
        if(value.kind=="cycle" && value.cycle!="once") {
            Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
                FilterChip(selected=value.recurrenceMode=="fixed",onClick={onChange(value.copy(recurrenceMode="fixed"))},label={Text(zh("固定周期"))})
                FilterChip(selected=value.recurrenceMode=="afterCompletion",onClick={onChange(value.copy(recurrenceMode="afterCompletion"))},label={Text(zh("完成后重新计时"))})
            }
        }
        if(value.reminderStyle!="gentle") {
            intervals.split(",").forEachIndexed { index, text ->
                Row(Modifier.fillMaxWidth(),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
                    OutlinedTextField(value=text,onValueChange={ input ->
                        val parts=intervals.split(",").toMutableList(); if(index !in parts.indices) return@OutlinedTextField; parts[index]=input; intervals=parts.joinToString(",")
                        if(parts.all { it.toIntOrNull() in 1..10080 }) onChange(value.copy(retryIntervalsMinutes=intervals))
                    },label={Text(com.reminderapp.i18n.zhf("第 %s 次重试（分钟）",index+1))},isError=text.toIntOrNull() !in 1..10080,
                    keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=androidx.compose.ui.text.input.KeyboardType.Number),modifier=Modifier.weight(1f))
                    TextButton(enabled=intervals.split(",").size>1,onClick={
                        val parts=intervals.split(",").toMutableList();if(index !in parts.indices) return@TextButton;parts.removeAt(index);intervals=parts.joinToString(",")
                        if(parts.all { it.toIntOrNull() in 1..10080 }) onChange(value.copy(retryIntervalsMinutes=intervals))
                    }) { Text(zh("移除")) }
                }
            }
            TextButton(enabled=intervals.split(",").size<10,onClick={
                intervals+=",60";if(intervals.split(",").all { it.toIntOrNull() in 1..10080 }) onChange(value.copy(retryIntervalsMinutes=intervals))
            }) { Text(zh("添加重试间隔")) }
            Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
                Text(com.reminderapp.i18n.zhf("最多提醒 %s 次（含首次）",value.maxReminderAttempts),modifier=Modifier.weight(1f))
                TextButton(enabled=value.maxReminderAttempts>1,onClick={cap=(value.maxReminderAttempts-1).toString();onChange(value.copy(maxReminderAttempts=value.maxReminderAttempts-1))}) { Text("−") }
                TextButton(enabled=value.maxReminderAttempts<10,onClick={cap=(value.maxReminderAttempts+1).toString();onChange(value.copy(maxReminderAttempts=value.maxReminderAttempts+1))}) { Text("+") }
            }
            Text(zh("最多 10 项；最后一个间隔重复使用"),style=MaterialTheme.typography.bodySmall)

        }
        fun minute(text: String): Int? { val p=text.split(":").map { it.toIntOrNull() };return if(p.size==2 && p[0] in 0..23 && p[1] in 0..59) p[0]!!*60+p[1]!! else null }
        fun updateQuiet() { val start=minute(quietStart);val end=minute(quietEnd);if(start!=null && end!=null) onChange(value.copy(quietStartMinute=start,quietEndMinute=end));if(quietStart.isBlank() && quietEnd.isBlank()) onChange(value.copy(quietStartMinute=null,quietEndMinute=null)) }
        OutlinedTextField(value=quietStart,onValueChange={quietStart=it;updateQuiet()},label={Text(zh("勿扰开始 HH:mm（留空关闭）"))},isError=!quietValid,modifier=Modifier.fillMaxWidth())
        if(!retryValid || !quietValid) Text(zh("请修正标红的设置后保存"),color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)
        OutlinedTextField(value=quietEnd,onValueChange={quietEnd=it;updateQuiet()},label={Text(zh("勿扰结束 HH:mm"))},isError=!quietValid,modifier=Modifier.fillMaxWidth())
    }
}
