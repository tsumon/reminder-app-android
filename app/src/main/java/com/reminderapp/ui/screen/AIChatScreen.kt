package com.reminderapp.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.reminderapp.ReminderApp
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.model.ReminderStatus
import com.reminderapp.service.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.*
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf

// ---- Message Model ----

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: Role,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class Role { USER, ASSISTANT, SYSTEM }
}

// ---- Screen ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(
    settings: AISettings,
    aiService: AIService,
    database: AppDatabase,
    scheduler: ReminderScheduler,
    notificationMgr: NotificationManager,
    onBack: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val gson = remember { Gson() }
    val voiceService = remember { VoiceService(ReminderApp.instance) }
    val context = LocalContext.current

    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isListening by remember { mutableStateOf(false) }
    fun startListening() {
        if (isListening) return
        isListening = true
        scope.launch {
            voiceService.recognize().fold(
                onSuccess = { text ->
                    if (text.isNotBlank()) {
                        // v1.9.6 fix: 语音识别结果先上屏（与文本路径一致），
                        // 否则对话列表只显示 AI 回复、看不到用户气泡
                        messages = messages + ChatMessage(role = ChatMessage.Role.USER, content = text)
                        scope.launch {
                            sendToAI(
                                text, settings, aiService, database, scheduler, notificationMgr, gson,
                                history = messages,
                                onMessages = { newMsgs -> messages = messages + newMsgs },
                                onLoading = { isLoading = it }
                            )
                        }
                    }
                },
                onFailure = { e -> errorMsg = e.message }
            )
            isListening = false
        }
    }
    // v1.9.6 fix: RECORD_AUDIO 是危险权限，Android 12+ 不申请 SpeechRecognizer 必失败
    val recordPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startListening()
        } else {
            Toast.makeText(context, zh("需要录音权限才能使用语音输入"), Toast.LENGTH_SHORT).show()
        }
    }


    val listState = rememberLazyListState()

    // 欢迎消息
    LaunchedEffect(Unit) {
        if (!settings.isConfigured) {
            val guide = zh("👋 你好！请先在设置中配置 API Key（支持 DeepSeek / 通义千问 / 豆包等，均有免费额度）。\n\n我能帮你：\n• 创建提醒「每天提醒我喝水」\n• 查看列表「有什么提醒」\n• 确认完成「确认喝水」\n• 推迟/删除提醒")
            messages = listOf(ChatMessage(role = ChatMessage.Role.ASSISTANT, content = guide))
        }
    }

    // 自动滚到底部
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(zh("AI 助手")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = zh("返回"))
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = zh("设置"))
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Column {
                    AnimatedVisibility(visible = isListening) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // 语音按钮
                        IconButton(
                            onClick = {
                                // v1.9.6 fix: 先检查录音权限，未授权先申请再识别
                                val hasMic = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context, android.Manifest.permission.RECORD_AUDIO
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                if (!hasMic) {
                                    recordPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                    return@IconButton
                                }
                                startListening()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Filled.Mic, contentDescription = zh("语音"),
                                tint = if (isListening) MaterialTheme.colorScheme.error
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 输入框
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text(zh("输入或点击麦克风说话...")) },
                            modifier = Modifier.weight(1f),
                            maxLines = 3,
                            shape = RoundedCornerShape(20.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        // 发送
                        IconButton(
                            onClick = {
                                val text = inputText.trim()
                                if (text.isEmpty() || isLoading) return@IconButton
                                val userMsg = text
                                inputText = ""
                                messages = messages + ChatMessage(role = ChatMessage.Role.USER, content = userMsg)
                                isLoading = true
                                errorMsg = null

                                if (settings.isConfigured) {
                                    scope.launch {
                                        sendToAI(
                                            userMsg, settings, aiService, database, scheduler, notificationMgr, gson,
                                            history = messages,
                                            onMessages = { newMsgs -> messages = messages + newMsgs },
                                            onLoading = { isLoading = it }
                                        )
                                    }
                                } else {
                                    // 未配置 API Key：解锁按钮并提示
                                    isLoading = false
                                    Toast.makeText(context, zh("请先在 AI 设置中配置 API Key"), Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = inputText.isNotBlank() && !isLoading,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Filled.Send, contentDescription = zh("发送"),
                                tint = if (inputText.isNotBlank() && !isLoading) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 错误提示
            errorMsg?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(msg, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
                LaunchedEffect(msg) { kotlinx.coroutines.delay(5000); errorMsg = null }
            }

            // 消息列表
            if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            zh("跟我说你想提醒什么"),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            zh("\"每天8点提醒我吃药\"\n\"每年提醒我妈生日\""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        ChatBubble(msg)
                    }
                    if (isLoading) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.role == ChatMessage.Role.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                // v1.9.8 设计图风格：AI 白底(左上尖角) / 用户紫渐变(右上尖角)
                .clip(
                    RoundedCornerShape(
                        topStart = if (isUser) 18.dp else 6.dp,
                        topEnd = if (isUser) 6.dp else 18.dp,
                        bottomEnd = 18.dp,
                        bottomStart = 18.dp
                    )
                )
                .background(
                    // if 两个分支必须同为 Brush，否则类型推断为 Any 无法匹配 background 重载
                    if (isUser) Brush.linearGradient(listOf(Color(0xFF7C66C2), Color(0xFF6750A4)))
                    else Brush.linearGradient(listOf(Color.White, Color.White))
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = msg.content,
                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ---- AI Loop ----

private suspend fun sendToAI(
    userText: String,
    settings: AISettings,
    aiService: AIService,
    database: AppDatabase,
    scheduler: ReminderScheduler,
    notificationMgr: NotificationManager,
    gson: Gson,
    history: List<ChatMessage> = emptyList(),
    onMessages: (List<ChatMessage>) -> Unit,
    onLoading: (Boolean) -> Unit = {}
) {
    // v1.9.6 fix: msgs 只放本轮 assistant 回复（user 消息由调用方上屏），
    // onMessages 语义为「追加」——原实现整体替换导致多轮历史清空
    var msgs = mutableListOf<ChatMessage>()
    var loading = true

    val conversation = mutableListOf<Map<String, Any?>>(
        mapOf("role" to "system", "content" to AITools.systemPrompt)
    )
    // v1.9.6 fix: 携带完整历史（截断最近 20 条）——否则「确认喝水」「再提醒一次」
    // 这类依赖上下文的指令永远失联（原实现只发 system+当前 user）
    // 注意：当前 user 消息已由调用方上屏并包含在 history 里，这里不要再追加，
    // 否则同一指令会发给模型两次（文本路径曾因此重复）
    history.takeLast(20).forEach { msg ->
        conversation.add(mapOf("role" to msg.role.name.lowercase(), "content" to msg.content))
    }

    var maxTurns = 5

    while (maxTurns > 0) {
        maxTurns--
        try {
            val reply = withTimeout(30_000) {
                aiService.chat(settings.model, conversation, settings.apiEndpoint, settings.apiKey)
            }

            if (!reply.tool_calls.isNullOrEmpty()) {
                for (tc in reply.tool_calls) {
                    val result = executeTool(tc.function.name, tc.function.arguments, database, scheduler, notificationMgr, gson)
                    conversation.add(mapOf(
                        "role" to "assistant",
                        "tool_calls" to listOf(mapOf(
                            "id" to tc.id, "type" to "function",
                            "function" to mapOf("name" to tc.function.name, "arguments" to tc.function.arguments)
                        ))
                    ))
                    conversation.add(mapOf("role" to "tool", "content" to result, "tool_call_id" to tc.id))
                }
                continue
            }

            val content = reply.content ?: zh("好的，已处理。")
            msgs.add(ChatMessage(role = ChatMessage.Role.ASSISTANT, content = content))
            onLoading(false)
            onMessages(msgs)
            return

        } catch (e: Exception) {
            msgs.add(ChatMessage(role = ChatMessage.Role.ASSISTANT, content = "❌ ${e.message}"))
            onLoading(false)
            onMessages(msgs)
            return
        }
    }

    msgs.add(ChatMessage(role = ChatMessage.Role.ASSISTANT, content = zh("对话轮次过多，请重新描述你的需求。")))
    onLoading(false)
    onMessages(msgs)
}

// ---- Tool Execution ----

private suspend fun executeTool(
    name: String, argsJson: String, database: AppDatabase,
    scheduler: ReminderScheduler, notificationMgr: NotificationManager, gson: Gson
): String {
    val args: Map<String, Any?> = try {
        gson.fromJson(argsJson, object : TypeToken<Map<String, Any?>>() {}.type)
    } catch (e: Exception) { emptyMap() }

    return when (name) {
        "create_reminder" -> handleCreate(args, database, scheduler, notificationMgr)
        "list_reminders" -> handleList(database)
        "confirm_reminder" -> handleConfirm(args, database, scheduler, notificationMgr)
        "snooze_reminder" -> handleSnooze(args, database, scheduler, notificationMgr)
        "delete_reminder" -> handleDelete(args, database)
        else -> zhf("未知工具: %s", name)
    }
}

private suspend fun handleCreate(args: Map<String, Any?>, database: AppDatabase, scheduler: ReminderScheduler, notificationMgr: NotificationManager): String {
    val title = (args["title"] as? String) ?: zh("未命名提醒")
    val kind = (args["kind"] as? String) ?: "cycle"
    val note = (args["note"] as? String) ?: ""
    val cycle = (args["cycle"] as? String) ?: "weekly"
    val customDays = (args["custom_days"] as? Double)?.toInt() ?: 0
    val dateType = args["date_type"] as? String
    // 注意：这里不能给月/日一个「看起来合法」的缺省值（如 1），
    // 否则 AI 漏传参数时会被静默当成 1月1日，绕过下面的合法性守卫。
    val targetMonthRaw = (args["target_month"] as? Double)?.toInt()
        ?: (args["target_month"] as? Int)
        ?: (args["target_month"] as? String)?.toIntOrNull()
    val targetDayRaw = (args["target_day"] as? Double)?.toInt()
        ?: (args["target_day"] as? Int)
        ?: (args["target_day"] as? String)?.toIntOrNull()
    val targetMonth = targetMonthRaw ?: 0
    val targetDay = targetDayRaw ?: 0
    val advanceDays = (args["advance_days"] as? Double)?.toInt() ?: 3
    val reminderHour = (args["reminder_hour"] as? Double)?.toInt() ?: 9
    val reminderMinute = (args["reminder_minute"] as? Double)?.toInt() ?: 0
    val holidayName = args["holiday_name"] as? String
    // v1.9.0 fix: 规则提醒（第N周周X）参数
    val rulePeriod = args["rule_period"] as? String
    val ruleWeek = (args["rule_week"] as? Double)?.toInt() ?: (args["rule_week"] as? Int)
    val ruleWeekday = (args["rule_weekday"] as? Double)?.toInt() ?: (args["rule_weekday"] as? Int)

    // v1.9.6 fix: kind/cycle/dateType 白名单校验。
    // 非法值会静默降级（调度走 else 分支、日历不显示）→「创建成功但永远不对」的幽灵提醒
    if (kind !in setOf("cycle", "date", "rule")) {
        return zhf("提醒类型无效（%s），只支持：周期(cycle)/日期(date)/规则(rule)。", kind)
    }
    if (kind == "cycle" && cycle !in setOf("daily", "weekly", "biweekly", "monthly", "quarterly", "yearly", "custom", "once")) {
        return zhf("周期类型无效（%s），只支持：每天/每周/每两周/每月/每季度/每年/自定义/一次。", cycle)
    }
    // v1.9.6 fix: kind=date 必须带 dateType——null 会退化成「一年后随机时刻」的幽灵提醒
    if (kind == "date" && dateType !in setOf("solar_birthday", "lunar_birthday", "holiday")) {
        return zh("日期提醒需要指定类型（公历生日/农历生日/节假日），例如：农历八月十五。请补充类型，我再为你创建。")
    }
    // v1.9.6 fix: cycle=custom 必须 customDays>=1，否则 interval=0 → Worker 立即重触发死循环
    if (cycle == "custom" && customDays < 1) {
        return zh("自定义周期需要指定间隔天数（如：每3天），custom_days 至少为 1。")
    }

    val now = System.currentTimeMillis()

    // 首次锚点：下一个到达 reminderHour:reminderMinute 的时刻
    // 对 cycle 作为周期锚点；对 date/rule 仅用于确定日期类提醒的时分
    val cal = Calendar.getInstance().apply { timeInMillis = now }
    cal.set(Calendar.HOUR_OF_DAY, reminderHour.coerceIn(0, 23))
    cal.set(Calendar.MINUTE, reminderMinute.coerceIn(0, 59))
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    if (cal.timeInMillis <= now) cal.add(Calendar.DAY_OF_MONTH, 1)
    val anchorNext = cal.timeInMillis

    // 日期类提醒必须带有合法的月日，否则引擎无法算出正确触发时间，
    // 会退化成「立刻触发一次就完事」。AI 无法自行推算农历/公历生日，
    // 此时请用户补充月日，而不是创建一个会误触发的提醒。
    if (kind == "date" && dateType != "holiday" &&
        (targetMonth !in 1..12 || targetDay !in 1..31)
    ) {
        return zh("需要具体的公历/农历月日才能创建日期提醒（例如：农历八月十五、公历5月1日）。请补充月日，我再为你创建。")
    }
    if (kind == "date" && dateType == "holiday" && holidayName.isNullOrBlank()) {
        return zh("需要指定节假日名称（例如：春节、中秋节）才能创建节假日提醒。")
    }
    // v1.9.0 fix: 规则提醒必须带全 频率/第几周/周几
    if (kind == "rule" && (rulePeriod == null || ruleWeek == null || ruleWeekday == null)) {
        return zh("规则提醒需要指定频率（每月/每季度/每年）、第几周和星期几，例如：每季度第一周周四。请补充完整，我再为你创建。")
    }

    val entity = ReminderEntity(
        title = title, note = note, kind = kind, cycle = cycle, customDays = customDays,
        dateType = dateType, targetMonth = targetMonth, targetDay = targetDay,
        advanceDays = advanceDays, reminderHour = reminderHour, reminderMinute = reminderMinute,
        holidayName = holidayName,
        rulePeriod = rulePeriod, ruleWeek = ruleWeek, ruleWeekday = ruleWeekday,
        firstTriggerAt = anchorNext, nextTriggerAt = anchorNext,
        status = ReminderStatus.PENDING.name.lowercase(), retryCount = 0, isActive = true
    )

    // 用引擎重算 nextTriggerAt（日期/规则类按目标月日计算，避免落到 +1 分钟）
    val nextTrigger = ReminderEngine.calculateNextTrigger(entity)
    val finalEntity = entity.copy(nextTriggerAt = nextTrigger)

    val id = database.reminderDao().insert(finalEntity)
    scheduler.schedule(finalEntity.copy(id = id))
    // v1.9.6 fix: 漏 touchLocalChange → AI 新建的提醒永远不同步 / 被远程旧数据覆盖
    com.reminderapp.service.SyncStore.touchLocalChange()
    com.reminderapp.receiver.ReminderWidgetProvider.refresh(com.reminderapp.ReminderApp.instance)
    return zhf("已创建提醒：「%s」", title)
}

private suspend fun handleList(database: AppDatabase): String {
    val list = database.reminderDao().getAllSync()
    if (list.isEmpty()) return zh("当前没有提醒。")
    val sb = StringBuilder(zhf("当前共有 %s 个提醒：", list.size))
    for (r in list) {
        val display = when {
            r.kind == "date" && r.dateType == "holiday" -> r.holidayName ?: zh("节假日")
            r.kind == "date" -> zh("日期")
            else -> r.cycle
        }
        sb.append("\n · ${r.title} [$display]")
    }
    return sb.toString()
}

private suspend fun handleConfirm(args: Map<String, Any?>, database: AppDatabase, scheduler: ReminderScheduler, notificationMgr: NotificationManager): String {
    val keyword = (args["title_keyword"] as? String ?: "").lowercase()
    // 空关键词 contains("") 恒 true 会命中第一条无关提醒 → 必须守卫
    if (keyword.isEmpty()) return zh("请指定要确认的提醒标题（如：确认「交房租」）。")
    val list = database.reminderDao().getAllSync()
    val match = list.find { it.title.lowercase().contains(keyword) } ?: return zhf("未找到包含「%s」的提醒", keyword)
    val updated = ReminderEngine.confirm(match)
    database.reminderDao().update(updated)
    // v1.9.6 fix: 确认后取消已显示的通知，避免通知栏残留可反复点击
    notificationMgr.cancelReminderNotifications(match.id)
    scheduler.schedule(updated)
    com.reminderapp.service.SyncStore.touchLocalChange()
    com.reminderapp.receiver.ReminderWidgetProvider.refresh(com.reminderapp.ReminderApp.instance)
    return zhf("已确认「%s」，下次提醒时间已更新。", match.title)
}

private suspend fun handleSnooze(args: Map<String, Any?>, database: AppDatabase, scheduler: ReminderScheduler, notificationMgr: NotificationManager): String {
    val keyword = (args["title_keyword"] as? String ?: "").lowercase()
    if (keyword.isEmpty()) return zh("请指定要推迟的提醒标题（如：推迟「交房租」）。")
    val list = database.reminderDao().getAllSync()
    val match = list.find { it.title.lowercase().contains(keyword) } ?: return zhf("未找到包含「%s」的提醒", keyword)
    val updated = ReminderEngine.snooze(match)
    database.reminderDao().update(updated)
    scheduler.schedule(updated)
    com.reminderapp.service.SyncStore.touchLocalChange()
    com.reminderapp.receiver.ReminderWidgetProvider.refresh(com.reminderapp.ReminderApp.instance)
    return zhf("已推迟「%s」，15 分钟后再次提醒。", match.title)
}

private suspend fun handleDelete(args: Map<String, Any?>, database: AppDatabase): String {
    val keyword = (args["title_keyword"] as? String ?: "").lowercase()
    if (keyword.isEmpty()) return zh("请指定要删除的提醒标题（如：删除「交房租」）。")
    val list = database.reminderDao().getAllSync()
    val match = list.find { it.title.lowercase().contains(keyword) } ?: return zhf("未找到包含「%s」的提醒", keyword)
    // v1.9.6 fix: 必须先取消 WorkManager 排期 + 已显示通知，再用软删。
    // 原实现物理 delete：遗留任务触发时 Worker 先发通知才查库 → 幽灵通知；
    // 且物理删会级联清掉统计记录（与 UI 路径软删不一致）
    ReminderScheduler(com.reminderapp.ReminderApp.instance).cancel(match.id)
    NotificationManager(com.reminderapp.ReminderApp.instance).cancelReminderNotifications(match.id)
    database.reminderDao().softDelete(match.id)
    com.reminderapp.service.SyncStore.touchLocalChange()
    com.reminderapp.receiver.ReminderWidgetProvider.refresh(com.reminderapp.ReminderApp.instance)
    return zhf("已删除「%s」", match.title)
}
