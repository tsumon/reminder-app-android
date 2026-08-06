package com.reminderapp.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.webkit.WebView
import com.reminderapp.ReminderApp
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.model.ReminderStatus
import com.reminderapp.service.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.*

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
    var showNoApiSheet by remember { mutableStateOf(false) }
    var webViewUrl by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()

    // 欢迎消息
    LaunchedEffect(Unit) {
        if (!settings.isConfigured) {
            val guide = if (settings.useNoAPIMode) {
                "👋 免 API 模式已开启！\n\n输入提醒需求后，会自动复制文字并跳转到网页版 AI，在那里粘贴发送即可。\n\n我能帮你：\n• 创建提醒「每天提醒我喝水」\n• 查看列表「有什么提醒」\n• 确认完成「确认喝水」\n• 推迟/删除提醒"
            } else {
                "👋 你好！请先在设置中配置 API Key，或者开启「免 API 模式」无需 Key 直接使用。\n\n我能帮你：\n• 创建提醒「每天提醒我喝水」\n• 查看列表「有什么提醒」\n• 确认完成「确认喝水」\n• 推迟/删除提醒"
            }
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
                title = { Text(if (settings.useNoAPIMode) "AI 助手 · 免 API" else "AI 助手") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "设置")
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
                                if (isListening) return@IconButton
                                isListening = true
                                scope.launch {
                                    voiceService.recognize().fold(
                                        onSuccess = { text ->
                                            if (text.isNotBlank()) {
                                                if (settings.useNoAPIMode) {
                                                    handleNoAPISend(text, settings, context, messages, { messages = it }) { url -> webViewUrl = url }
                                                } else {
                                                    scope.launch {
                                                        sendToAI(
                                                            text, settings, aiService, database, scheduler, notificationMgr, gson,
                                                            onMessages = { messages = it },
                                                            onLoading = { isLoading = it }
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        onFailure = { e -> errorMsg = e.message }
                                    )
                                    isListening = false
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Filled.Mic, contentDescription = "语音",
                                tint = if (isListening) MaterialTheme.colorScheme.error
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 输入框
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("输入或点击麦克风说话...") },
                            modifier = Modifier.weight(1f),
                            maxLines = 3,
                            shape = RoundedCornerShape(20.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        // 发送
                        val sendColor = if (settings.useNoAPIMode) Color(0xFFFF9800)
                                        else MaterialTheme.colorScheme.primary
                        IconButton(
                            onClick = {
                                val text = inputText.trim()
                                if (text.isEmpty() || isLoading) return@IconButton
                                val userMsg = text
                                inputText = ""
                                messages = messages + ChatMessage(role = ChatMessage.Role.USER, content = userMsg)
                                isLoading = true
                                errorMsg = null

                                if (settings.useNoAPIMode) {
                                    handleNoAPISend(userMsg, settings, context, messages, { messages = it }) { url -> webViewUrl = url }
                                    isLoading = false
                                } else if (settings.isConfigured) {
                                    scope.launch {
                                        sendToAI(
                                            userMsg, settings, aiService, database, scheduler, notificationMgr, gson,
                                            onMessages = { messages = it },
                                            onLoading = { isLoading = it }
                                        )
                                    }
                                } else {
                                    // 既未配置 API 也未开免 API 模式：解锁发送按钮，避免永久卡死
                                    isLoading = false
                                }
                            },
                            enabled = inputText.isNotBlank() && !isLoading,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Filled.Send, contentDescription = "发送",
                                tint = if (inputText.isNotBlank() && !isLoading) sendColor
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
                            if (settings.useNoAPIMode) Icons.Filled.Bolt else Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = if (settings.useNoAPIMode) Color(0xFFFF9800) else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            if (settings.useNoAPIMode) "免 API 模式" else "跟我说你想提醒什么",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            if (settings.useNoAPIMode) "输入需求 → 自动跳转网页版 AI\n粘贴即用，无需 Key"
                            else "\"每天8点提醒我吃药\"\n\"每年提醒我妈生日\"",
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
                                if (settings.useNoAPIMode) {
                                    Text("文字已复制，正在跳转...", style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 免 API 模式：应用内网页浏览器（修复「切换到网页对话无法返回」）
    if (webViewUrl != null) {
        InAppWebView(url = webViewUrl!!, onClose = { webViewUrl = null })
    }
}

/**
 * 应用内网页浏览器（Dialog 覆盖层，可返回）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InAppWebView(url: String, onClose: () -> Unit) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("网页 AI 助手") },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        loadUrl(url)
                    }
                }
            )
        }
    }
}

/**
 * 免 API 模式发送：复制文字 + 打开网页版
 */
private fun handleNoAPISend(
    text: String,
    settings: AISettings,
    context: Context,
    currentMessages: List<ChatMessage>,
    onMessages: (List<ChatMessage>) -> Unit,
    onOpenWeb: (String) -> Unit
) {
    // 复制到剪贴板
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("reminder_query", text)
    clipboard.setPrimaryClip(clip)

    // 找到选中提供商
    val provider = noApiProviders.find { it.id == settings.noAPIProvider } ?: noApiProviders[0]

    // 弹 toast
    Toast.makeText(context, "已复制到剪贴板，请在「${provider.name}」网页中粘贴发送", Toast.LENGTH_LONG).show()

    // 在应用内打开网页（保留返回能力，不会跳离 App）
    onOpenWeb(provider.webUrl)

    // 显示提示消息
    val truncated = if (text.length > 30) text.take(30) + "..." else text
    val newMessages = currentMessages + ChatMessage(
        role = ChatMessage.Role.ASSISTANT,
        content = "⚠️ 已复制「$truncated」到剪贴板，并在应用内打开了「${provider.name}」网页，请直接粘贴发送。\n\n若网页未自动弹出，也可手动打开：${provider.webUrl}"
    )
    onMessages(newMessages)
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
                .clip(RoundedCornerShape(16.dp))
                .background(if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = msg.content,
                color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
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
    onMessages: (List<ChatMessage>) -> Unit,
    onLoading: (Boolean) -> Unit = {}
) {
    var msgs = mutableListOf(
        ChatMessage(role = ChatMessage.Role.USER, content = userText)
    )
    var loading = true

    val conversation = mutableListOf<Map<String, Any?>>(
        mapOf("role" to "system", "content" to AITools.systemPrompt),
        mapOf("role" to "user", "content" to userText)
    )

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

            val content = reply.content ?: "好的，已处理。"
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

    msgs.add(ChatMessage(role = ChatMessage.Role.ASSISTANT, content = "对话轮次过多，请重新描述你的需求。"))
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
        else -> "未知工具: $name"
    }
}

private suspend fun handleCreate(args: Map<String, Any?>, database: AppDatabase, scheduler: ReminderScheduler, notificationMgr: NotificationManager): String {
    val title = (args["title"] as? String) ?: "未命名提醒"
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
        return "需要具体的公历/农历月日才能创建日期提醒（例如：农历八月十五、公历5月1日）。请补充月日，我再为你创建。"
    }
    if (kind == "date" && dateType == "holiday" && holidayName.isNullOrBlank()) {
        return "需要指定节假日名称（例如：春节、中秋节）才能创建节假日提醒。"
    }

    val entity = ReminderEntity(
        title = title, note = note, kind = kind, cycle = cycle, customDays = customDays,
        dateType = dateType, targetMonth = targetMonth, targetDay = targetDay,
        advanceDays = advanceDays, reminderHour = reminderHour, reminderMinute = reminderMinute,
        holidayName = holidayName, firstTriggerAt = anchorNext, nextTriggerAt = anchorNext,
        status = ReminderStatus.PENDING.name.lowercase(), retryCount = 0, isActive = true
    )

    // 用引擎重算 nextTriggerAt（日期/规则类按目标月日计算，避免落到 +1 分钟）
    val nextTrigger = ReminderEngine.calculateNextTrigger(entity)
    val finalEntity = entity.copy(nextTriggerAt = nextTrigger)

    val id = database.reminderDao().insert(finalEntity)
    scheduler.schedule(finalEntity.copy(id = id))
    return "已创建提醒：「$title」"
}

private suspend fun handleList(database: AppDatabase): String {
    val list = database.reminderDao().getAllSync()
    if (list.isEmpty()) return "当前没有提醒。"
    val sb = StringBuilder("当前共有 ${list.size} 个提醒：")
    for (r in list) {
        val display = when {
            r.kind == "date" && r.dateType == "holiday" -> r.holidayName ?: "节假日"
            r.kind == "date" -> "日期"
            else -> r.cycle
        }
        sb.append("\n · ${r.title} [$display]")
    }
    return sb.toString()
}

private suspend fun handleConfirm(args: Map<String, Any?>, database: AppDatabase, scheduler: ReminderScheduler, notificationMgr: NotificationManager): String {
    val keyword = (args["title_keyword"] as? String ?: "").lowercase()
    val list = database.reminderDao().getAllSync()
    val match = list.find { it.title.lowercase().contains(keyword) } ?: return "未找到包含「$keyword」的提醒"
    val updated = ReminderEngine.confirm(match)
    database.reminderDao().update(updated)
    scheduler.schedule(updated)
    return "已确认「${match.title}」，下次提醒时间已更新。"
}

private suspend fun handleSnooze(args: Map<String, Any?>, database: AppDatabase, scheduler: ReminderScheduler, notificationMgr: NotificationManager): String {
    val keyword = (args["title_keyword"] as? String ?: "").lowercase()
    val list = database.reminderDao().getAllSync()
    val match = list.find { it.title.lowercase().contains(keyword) } ?: return "未找到包含「$keyword」的提醒"
    val updated = ReminderEngine.snooze(match)
    database.reminderDao().update(updated)
    scheduler.schedule(updated)
    return "已推迟「${match.title}」，15 分钟后再次提醒。"
}

private suspend fun handleDelete(args: Map<String, Any?>, database: AppDatabase): String {
    val keyword = (args["title_keyword"] as? String ?: "").lowercase()
    val list = database.reminderDao().getAllSync()
    val match = list.find { it.title.lowercase().contains(keyword) } ?: return "未找到包含「$keyword」的提醒"
    database.reminderDao().delete(match)
    return "已删除「${match.title}」"
}
