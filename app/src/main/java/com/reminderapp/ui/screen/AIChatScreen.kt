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
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.model.ReminderStatus
import com.reminderapp.service.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
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
    val voiceService = remember { VoiceService(App.instance) }
    val context = LocalContext.current

    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isListening by remember { mutableStateOf(false) }
    var showNoApiSheet by remember { mutableStateOf(false) }

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
                                                    handleNoAPISend(text, settings, context, messages) { messages = it }
                                                } else {
                                                    sendToAI(text, settings, aiService, database, scheduler, notificationMgr, gson) { messages = it }
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
                                    handleNoAPISend(userMsg, settings, context, messages) { messages = it }
                                } else if (settings.isConfigured) {
                                    scope.launch {
                                        sendToAI(userMsg, settings, aiService, database, scheduler, notificationMgr, gson) { messages = it }
                                    }
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
}

/**
 * 免 API 模式发送：复制文字 + 打开网页版
 */
private fun handleNoAPISend(
    text: String,
    settings: AISettings,
    context: Context,
    currentMessages: List<ChatMessage>,
    onMessages: (List<ChatMessage>) -> Unit
) {
    // 复制到剪贴板
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("reminder_query", text)
    clipboard.setPrimaryClip(clip)

    // 找到选中提供商
    val provider = noApiProviders.find { it.id == settings.noAPIProvider } ?: noApiProviders[0]

    // 弹 toast
    Toast.makeText(context, "已复制到剪贴板，请在「${provider.name}」网页中粘贴发送", Toast.LENGTH_LONG).show()

    // 打开网页
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(provider.webUrl))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)

    // 显示提示消息
    val truncated = if (text.length > 30) text.take(30) + "..." else text
    val newMessages = currentMessages + ChatMessage(
        role = ChatMessage.Role.ASSISTANT,
        content = "⚠️ 已复制「$truncated」到剪贴板，请在弹出的「${provider.name}」网页中粘贴发送。\n\n如果页面未自动弹出，你也可以手动打开：${provider.webUrl}"
    )
    onMessages(newMessages)

    // 延迟重置 loading
    kotlinx.coroutines.GlobalScope.launch {
        kotlinx.coroutines.delay(500)
        // reset via the caller's scope (handled by the caller already)
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
    onMessages: (List<ChatMessage>) -> Unit
) {
    var msgs = onMessages
    var loading = true

    val conversation = mutableListOf<Map<String, Any?>>(
        mapOf("role" to "system", "content" to AITools.systemPrompt),
        mapOf("role" to "user", "content" to userText)
    )

    var maxTurns = 5

    while (maxTurns > 0) {
        maxTurns--
        try {
            val reply = aiService.chat(settings.model, conversation, settings.apiEndpoint, settings.apiKey)

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
            msgs = msgs + ChatMessage(role = ChatMessage.Role.ASSISTANT, content = content)
            loading = false
            onMessages(msgs)
            return

        } catch (e: Exception) {
            msgs = msgs + ChatMessage(role = ChatMessage.Role.ASSISTANT, content = "❌ ${e.message}")
            loading = false
            onMessages(msgs)
            return
        }
    }

    msgs = msgs + ChatMessage(role = ChatMessage.Role.ASSISTANT, content = "对话轮次过多，请重新描述你的需求。")
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
    val targetMonth = (args["target_month"] as? Double)?.toInt() ?: 1
    val targetDay = (args["target_day"] as? Double)?.toInt() ?: 1
    val advanceDays = (args["advance_days"] as? Double)?.toInt() ?: 3
    val reminderHour = (args["reminder_hour"] as? Double)?.toInt() ?: 9
    val reminderMinute = (args["reminder_minute"] as? Double)?.toInt() ?: 0
    val holidayName = args["holiday_name"] as? String

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val now = System.currentTimeMillis()

    val firstTrigger = when {
        args["trigger_date"] is String && args["trigger_time"] is String ->
            dateFormat.parse("${args["trigger_date"]} ${args["trigger_time"]}")?.time ?: (now + 60000)
        else -> now + 60000
    }

    val entity = ReminderEntity(
        title = title, note = note, kind = kind, cycle = cycle, customDays = customDays,
        dateType = dateType, targetMonth = targetMonth, targetDay = targetDay,
        advanceDays = advanceDays, reminderHour = reminderHour, reminderMinute = reminderMinute,
        holidayName = holidayName, firstTriggerAt = firstTrigger, nextTriggerAt = firstTrigger,
        status = ReminderStatus.PENDING.name.lowercase(), retryCount = 0, isEnabled = true
    )

    val id = database.reminderDao().insert(entity)
    scheduler.schedule(entity.copy(id = id))
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
