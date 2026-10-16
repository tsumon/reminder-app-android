package com.reminderapp.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.service.NaturalDateParser
import com.reminderapp.i18n.zh

/** Parse locally then open the real editable form; nothing is inserted at parse time. */
@Composable
fun QuickCreate(onEdit: (ReminderEntity) -> Unit, onAI: () -> Unit) {
    var text by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(value=text,onValueChange={text=it;error=false},modifier=Modifier.fillMaxWidth(),
            placeholder={Text(zh("一句话创建：每周一9点晨会"))},singleLine=true,isError=error)
        Row {
            TextButton(enabled=text.isNotBlank(),onClick={
                val unsupported=Regex("最后|工作日|第[一二三四五1-5].*周|每隔|每[0-9]+天|last|weekday",RegexOption.IGNORE_CASE).containsMatchIn(text)
                val p=if(unsupported) null else NaturalDateParser.parse(text)
                if(p==null) error=true else {
                    val clock=java.util.Calendar.getInstance().apply { timeInMillis=p.nextTriggerAt }
                    onEdit(ReminderEntity(title=p.title.ifBlank { text },kind=if(p.dateType!=null) "date" else "cycle",cycle=if(p.repeatMode=="lunar") "yearly" else p.repeatMode,
                        dateType=p.dateType,targetMonth=p.targetMonth,targetDay=p.targetDay,weeklyWeekday=p.weekday,holidayAware=p.holidayAware,
                        reminderHour=clock.get(java.util.Calendar.HOUR_OF_DAY),reminderMinute=clock.get(java.util.Calendar.MINUTE),
                        firstTriggerAt=p.nextTriggerAt,nextTriggerAt=p.nextTriggerAt))
                }
            }) { Text(zh("编辑规则并创建")) }
            TextButton(onClick=onAI) { Text(zh("AI / 语音")) }
        }
        if(error) Text(zh("暂未识别时间，请使用手动创建或 AI 助手"),color=MaterialTheme.colorScheme.error)
    }
}
