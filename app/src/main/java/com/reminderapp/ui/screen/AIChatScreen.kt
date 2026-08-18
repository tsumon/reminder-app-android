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
import android.content.Context
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
import com.reminderapp.ui.theme.Tokens

// 批量导入预览状态（import_tasks 工具解析后挂起，等用户在弹窗确认后再批量写入）
private val pendingImportState = mutableStateOf<List<ReminderEntity>?>(null)

// ---- Message Model ----

/** v2.2.0: AI 工具调用步骤（Agent 可视化：执行中 → 完成/失败） */
data class ToolStep(
    val name: String,
    val status: String,   // running / done / error
    val summary: String? = null
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: Role,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    // v2.4.2: 历史持久化用 @Transient 忽略（Gson 跳过该字段）
    @com.google.gson.annotations.Expose(serialize = false, deserialize = false)
    val toolSteps: List<ToolStep> = emptyList()
) {
    enum class Role { USER, ASSISTANT, SYSTEM }
}

/** v2.4.2: AI 对话历史持久化（SharedPreferences JSON，保留最近 200 条） */
object ChatHistoryStore {
    private const val PREFS = "ai_chat_history"
    private const val KEY = "messages"
    private const val MAX = 200
    private val gson = Gson()

    fun save(context: Context, messages: List<ChatMessage>) {
        val slim = messages.filter { it.content.isNotBlank() }.takeLast(MAX)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, gson.toJson(slim)).apply()
    }

    fun load(context: Context): List<ChatMessage> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
            ?: return emptyList()
        return try {
            val list: List<ChatMessage> = gson.fromJson(raw, object : TypeToken<List<ChatMessage>>() {}.type)
                ?: return emptyList()
            // v2.4.3: Gson 用 Unsafe 绕过构造器默认值——JSON 缺字段时为 null：
            // toolSteps null → ChatBubble NPE 闪退（模拟器注入数据实测复现）；
            // role null → 发送携带历史时 msg.role.name NPE；
            // id null/重复 → LazyColumn key 崩溃。逐条修复 + 去重。
            list.filter { it.role != null && !it.content.isNullOrBlank() }
                .map { it.copy(
                    id = it.id ?: UUID.randomUUID().toString(),
                    content = it.content ?: "",
                    toolSteps = it.toolSteps ?: emptyList()
                ) }
                .distinctBy { it.id }
        } catch (e: Exception) {
            // v2.4.4: 静默吞异常让「历史消失」无从诊断（release 复现：R8 剥掉
            // 泛型 Signature → TypeToken 擦除 → 反序列化抛异常 → 恒空）。必须打日志。
            android.util.Log.e("ChatHistory", "load failed, raw=${raw.take(120)}", e)
            emptyList()
        }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY).apply()
    }
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

    // v2.4.2: 历史持久化——进入恢复、变化保存
    var messages by remember { mutableStateOf<List<ChatMessage>>(ChatHistoryStore.load(context)) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isListening by remember { mutableStateOf(false) }
    // v2.4.2→2.4.3: 持久化改为「更新即保存」（直接调用，不再依赖 effect 时序）
    // 流式增量/步骤气泡用 upsert（按 id 替换），避免追加语义造成的重复气泡
    fun upsertMessage(m: ChatMessage) {
        val idx = messages.indexOfFirst { it.id == m.id }
        messages = if (idx >= 0) messages.toMutableList().also { it[idx] = m } else messages + m
        ChatHistoryStore.save(context, messages)
    }

    fun appendMessages(newMsgs: List<ChatMessage>) {
        messages = messages + newMsgs
        ChatHistoryStore.save(context, messages)
    }
    fun startListening() {
        if (isListening) return
        isListening = true
        scope.launch {
            voiceService.recognize().fold(
                onSuccess = { text ->
                    if (text.isNotBlank()) {
                        // v1.9.6 fix: 语音识别结果先上屏（与文本路径一致），
                        // 否则对话列表只显示 AI 回复、看不到用户气泡
                        appendMessages(listOf(ChatMessage(role = ChatMessage.Role.USER, content = text)))
                        // v2.4.9 fix: 与文本发送路径一致——先置 isLoading 再请求，
                        // 否则语音请求进行中用户仍可再发一条，两个 Agent 循环并发、
                        // 固定 id 气泡互相覆盖、历史保存交错
                        if (settings.isConfigured) {
                            isLoading = true
                            scope.launch {
                                sendToAI(
                                    text, settings, aiService, database, scheduler, notificationMgr, gson,
                                    history = messages,
                                    onMessages = ::appendMessages,
                                    onUpsert = ::upsertMessage,
                                    onLoading = { isLoading = it }
                                )
                            }
                        } else {
                            Toast.makeText(context, zh("请先在 AI 设置中配置 API Key"), Toast.LENGTH_SHORT).show()
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
        // v2.4.3 fix: 加 messages.isEmpty() 守卫——原实现无条件用欢迎语整体替换，
        // 重装/清掉 API Key 后进入会直接抹掉已恢复的历史记录
        if (!settings.isConfigured && messages.isEmpty()) {
            val guide = zh("👋 你好！请先在设置中配置 API Key（支持 DeepSeek / 通义千问 / 豆包等，均有免费额度）。\n\n我能帮你：\n• 创建提醒「每天提醒我喝水」\n• 查看列表「有什么提醒」\n• 确认完成「确认喝水」\n• 修改提醒「把交房租改成每月5号」\n• 推迟/删除提醒")
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
                    // v2.4.2: 历史记录下拉（查看/清空）
                    var historyMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { historyMenu = true }) {
                            Icon(Icons.Default.History, contentDescription = zh("历史记录"))
                        }
                        DropdownMenu(expanded = historyMenu, onDismissRequest = { historyMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(zhf("查看历史（%s 条）", messages.size)) },
                                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                enabled = messages.isNotEmpty(),
                                onClick = {
                                    historyMenu = false
                                    // v2.4.4: 滚到顶部（最早的记录）——原实现滚到底部，
                                    // 用户本来就在底部，点了毫无反应
                                    scope.launch {
                                        kotlinx.coroutines.delay(100)
                                        if (messages.isNotEmpty()) listState.animateScrollToItem(0)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(zh("清空历史")) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                enabled = messages.isNotEmpty(),
                                onClick = {
                                    historyMenu = false
                                    messages = emptyList()
                                    ChatHistoryStore.clear(context)
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text(zh("AI 设置")) },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = {
                                    historyMenu = false
                                    onNavigateSettings()
                                }
                            )
                        }
                    }
                    // v2.4.9: 本周洞察（触发 AI 周报）
                    IconButton(onClick = {
                        if (settings.isConfigured) {
                            inputText = ""
                            appendMessages(listOf(ChatMessage(role = ChatMessage.Role.USER, content = "给我一份本周提醒总结和洞察")))
                            isLoading = true
                            scope.launch {
                                sendToAI(
                                    "给我一份本周提醒总结和洞察", settings, aiService, database, scheduler, notificationMgr, gson,
                                    history = messages,
                                    onMessages = ::appendMessages,
                                    onUpsert = ::upsertMessage,
                                    onLoading = { isLoading = it }
                                )
                            }
                        } else {
                            Toast.makeText(context, zh("请先在 AI 设置中配置 API Key"), Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.BarChart, contentDescription = zh("本周洞察"))
                    }
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
                                // v2.4.4: 用户消息上屏即持久化（appendMessages 内含 save）——
                                // 原来 AI 回复失败/中途退出时这条消息不会落盘
                                appendMessages(listOf(ChatMessage(role = ChatMessage.Role.USER, content = userMsg)))
                                isLoading = true
                                errorMsg = null

                                if (settings.isConfigured) {
                                    scope.launch {
                                        sendToAI(
                                            userMsg, settings, aiService, database, scheduler, notificationMgr, gson,
                                            history = messages,
                                            onMessages = ::appendMessages,
                                            onUpsert = ::upsertMessage,
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
        // 批量导入预览弹窗（import_tasks 工具解析后确认）
        val pendingImport = pendingImportState.value
        if (pendingImport != null) {
            AlertDialog(
                onDismissRequest = { pendingImportState.value = null },
                confirmButton = {
                    TextButton(onClick = {
                        val items = pendingImport
                        pendingImportState.value = null
                        scope.launch {
                            commitImport(items, database, scheduler, notificationMgr)
                            Toast.makeText(context, zhf("已批量创建 %d 条提醒", items.size), Toast.LENGTH_SHORT).show()
                        }
                    }) { Text(zh("确认创建")) }
                },
                dismissButton = {
                    TextButton(onClick = { pendingImportState.value = null }) { Text(zh("取消")) }
                },
                title = { Text(zh("批量创建提醒")) },
                text = {
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(count = pendingImport.size) { i ->
                            val e = pendingImport[i]
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(e.title, style = MaterialTheme.typography.bodyMedium)
                                    Text(describeReminder(e), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            )
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
                    // v2.1.0: AI 气泡深色模式修复——原来硬编码白底 + onSurface 文字，
                    // 深色下白底浅字对比度崩塌。改为 surface 语义色（深浅自适应），
                    // 用户气泡沿用品牌紫渐变（进 Tokens 系）
                    if (isUser) Brush.linearGradient(listOf(Tokens.BrandGradientStart, Tokens.BrandPrimary))
                    else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // v2.2.0: Agent 工具步骤可视化（执行中 → 完成/失败）
            if (!msg.toolSteps.isNullOrEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    msg.toolSteps.orEmpty().forEach { step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            when (step.status) {
                                "running" -> CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp
                                )
                                "error" -> Icon(
                                    Icons.Default.Warning, contentDescription = null,
                                    tint = Tokens.StatusOverdue, modifier = Modifier.size(14.dp)
                                )
                                else -> Icon(
                                    Icons.Default.CheckCircle, contentDescription = null,
                                    tint = Tokens.StatusCompleted, modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = step.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (step.status == "running") {
                                Text(
                                    zh("执行中…"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
            Text(
                text = msg.content,
                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ---- AI Loop（v2.2.0：Agent 多步循环 + 流式输出 + 备用降级 + 调用日志） ----

/** v2.4.3: 流式累积气泡的固定 id（upsert 按 id 替换，避免重复追加） */
private const val STREAM_BUBBLE_ID = "stream-current-bubble"
/** v2.4.3: 工具步骤气泡的固定 id */
private const val STEP_BUBBLE_ID = "tool-steps-bubble"

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
    onUpsert: (ChatMessage) -> Unit = {},
    onLoading: (Boolean) -> Unit = {}
) {
    // v1.9.6 fix: msgs 只放本轮 assistant 回复（user 消息由调用方上屏），
    // onMessages 语义为「追加」——原实现整体替换导致多轮历史清空
    var msgs = mutableListOf<ChatMessage>()
    var loading = true
    val startedAt = System.currentTimeMillis()

    val conversation = mutableListOf<Map<String, Any?>>(
        mapOf("role" to "system", "content" to AITools.systemPrompt)
    )
    // v1.9.6 fix: 携带完整历史（截断最近 20 条）——否则「确认喝水」「再提醒一次」
    // 这类依赖上下文的指令永远失联（原实现只发 system+当前 user）
    history.takeLast(20).forEach { msg ->
        conversation.add(mapOf("role" to msg.role.name.lowercase(), "content" to msg.content))
    }

    var maxTurns = 5
    var usedFallback = false
    var finalUsage: AIService.Usage? = null
    // v2.4.3: 流式累积气泡（固定 id，upsert 语义）
    var streamBubble: ChatMessage? = null

    // v2.2.0→2.4.3: 工具步骤可视化（upsert 固定 id，按 id 替换避免重复）
    fun pushStep(steps: List<ToolStep>) {
        onUpsert(ChatMessage(
            id = STEP_BUBBLE_ID,
            role = ChatMessage.Role.ASSISTANT,
            content = "",
            toolSteps = steps
        ))
    }

    while (maxTurns > 0) {
        maxTurns--
        try {
            // v2.2.0: 流式只在后续轮次启用（首轮大概率是工具调用，保持非流式；
            // 工具调用后的最终文本轮次流式输出——工程取舍，可面试展开）
            val streamEnabled = maxTurns < 4
            val reply = withTimeout(30_000) {
                aiService.chatWithFallback(settings, conversation, onStream = if (streamEnabled) { delta ->
                        // v2.4.3: 流式增量改 upsert（固定 id，按 id 替换）——
                        // 原「追加」语义每个 delta 都会新增一条重复气泡
                        streamBubble = streamBubble?.copy(content = streamBubble!!.content + delta)
                            ?: ChatMessage(id = STREAM_BUBBLE_ID, role = ChatMessage.Role.ASSISTANT, content = delta)
                        onUpsert(streamBubble!!)
                    } else null)
            }
            usedFallback = reply.usedFallback
            finalUsage = reply.usage

            if (!reply.toolCalls.isNullOrEmpty()) {
                // Agent 步骤：逐个工具执行，UI 实时显示状态
                val steps = mutableListOf<ToolStep>()
                for (tc in reply.toolCalls) {
                    steps.add(ToolStep(name = tc.function.name, status = "running"))
                    pushStep(steps.toList())
                    val result = executeTool(tc.function.name, tc.function.arguments, database, scheduler, notificationMgr, gson)
                    // 仅「解析失败/未知工具」视为 error（业务失败如「未找到」是合法 tool result，模型会自行处理）
                    val isError = result == zh("参数解析失败") || result.startsWith(zh("未知工具"))
                    steps[steps.size - 1] = ToolStep(
                        name = tc.function.name,
                        status = if (isError) "error" else "done",
                        summary = result.take(80)
                    )
                    pushStep(steps.toList())
                    conversation.add(mapOf(
                        "role" to "assistant",
                        "tool_calls" to listOf(mapOf(
                            "id" to tc.id, "type" to "function",
                            "function" to mapOf("name" to tc.function.name, "arguments" to tc.function.arguments)
                        ))
                    ))
                    conversation.add(mapOf("role" to "tool", "content" to result, "tool_call_id" to tc.id))
                }
                // 步骤结束：清掉临时步骤气泡，下一轮由最终回复接管
                msgs = mutableListOf()
                continue
            }

            var content = reply.content ?: zh("好的，已处理。")
            // v2.2.0: finish_reason 判断——length 表示输出被截断
            if (reply.finishReason == "length") {
                content += "\n\n（响应长度受限，已截断）"
            }
            // v2.4.3: 流式轮次已 upsert 过气泡 → 用最终完整内容收尾（同 id 替换）；
            // 非流式轮次 → 正常追加
            if (streamBubble != null) {
                onUpsert(ChatMessage(id = STREAM_BUBBLE_ID, role = ChatMessage.Role.ASSISTANT, content = content))
            } else {
                msgs.add(ChatMessage(role = ChatMessage.Role.ASSISTANT, content = content))
                onMessages(msgs)
            }
            onLoading(false)

            // v2.2.0: AI 调用日志（诊断页可观测）
            com.reminderapp.service.AILogStore.add(ReminderApp.instance, com.reminderapp.service.AILogStore.Entry(
                model = if (usedFallback) settings.fallbackModel else settings.model,
                provider = if (usedFallback) "fallback" else "primary",
                turns = 5 - maxTurns,
                promptTokens = finalUsage?.prompt_tokens,
                completionTokens = finalUsage?.completion_tokens,
                durationMs = System.currentTimeMillis() - startedAt,
                ok = true
            ))
            return

        } catch (e: Exception) {
            msgs.add(ChatMessage(role = ChatMessage.Role.ASSISTANT, content = "❌ ${e.message}"))
            onLoading(false)
            onMessages(msgs)
            com.reminderapp.service.AILogStore.add(ReminderApp.instance, com.reminderapp.service.AILogStore.Entry(
                model = if (usedFallback) settings.fallbackModel else settings.model,
                provider = if (usedFallback) "fallback" else "primary",
                turns = 5 - maxTurns,
                durationMs = System.currentTimeMillis() - startedAt,
                ok = false,
                error = e.message
            ))
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
    // v2.2.0: 参数解析失败不再静默降级为空 map——返回明确错误文本回喂模型自纠正
    val args: Map<String, Any?> = try {
        gson.fromJson(argsJson, object : TypeToken<Map<String, Any?>>() {}.type)
    } catch (e: Exception) {
        return zh("参数解析失败：工具参数不是合法 JSON，请检查后重试。")
    }

    return when (name) {
        "create_reminder" -> handleCreate(args, database, scheduler, notificationMgr)
        "list_reminders" -> handleList(database)
        "confirm_reminder" -> handleConfirm(args, database, scheduler, notificationMgr)
        "snooze_reminder" -> handleSnooze(args, database, scheduler, notificationMgr)
        "delete_reminder" -> handleDelete(args, database)
        "update_reminder" -> handleUpdate(args, database, scheduler)
        "import_tasks" -> handleImportTasks(args, database, scheduler, notificationMgr, gson)
        "get_stats_context" -> handleStatsContext(database)
        else -> zhf("未知工具: %s", name)
    }
}

/**
 * 解析工具参数 → 构造 ReminderEntity。
 * 返回 (entity, null) 成功；或 (null, errorMessage) 校验失败。
 * handleCreate 与 import_tasks 共用，避免重复解析逻辑。
 */
private fun tryBuildEntity(args: Map<String, Any?>): Pair<ReminderEntity?, String?> {
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
    // v2.4.8: 避开节假日/周末（Boolean 容错——部分模型输出字符串 "true"/"false"）
    val holidayAware = when (val v = args["holiday_aware"]) {
        is Boolean -> v
        is String -> v.equals("true", ignoreCase = true)
        is Number -> v.toInt() != 0
        else -> false
    }
    // v1.9.0 fix: 规则提醒（第N周周X）参数
    val rulePeriod = args["rule_period"] as? String
    val ruleWeek = (args["rule_week"] as? Double)?.toInt() ?: (args["rule_week"] as? Int)
    val ruleWeekday = (args["rule_weekday"] as? Double)?.toInt() ?: (args["rule_weekday"] as? Int)

    // v1.9.6 fix: kind/cycle/dateType 白名单校验。
    // 非法值会静默降级（调度走 else 分支、日历不显示）→「创建成功但永远不对」的幽灵提醒
    if (kind !in setOf("cycle", "date", "rule")) {
        return Pair(null, zhf("提醒类型无效（%s），只支持：周期(cycle)/日期(date)/规则(rule)。", kind))
    }
    if (kind == "cycle" && cycle !in setOf("daily", "weekly", "biweekly", "monthly", "quarterly", "yearly", "custom", "once")) {
        return Pair(null, zhf("周期类型无效（%s），只支持：每天/每周/每两周/每月/每季度/每年/自定义/一次。", cycle))
    }
    // v1.9.6 fix: kind=date 必须带 dateType——null 会退化成「一年后随机时刻」的幽灵提醒
    if (kind == "date" && dateType !in setOf("solar_birthday", "lunar_birthday", "holiday")) {
        return Pair(null, zh("日期提醒需要指定类型（公历生日/农历生日/节假日），例如：农历八月十五。请补充类型，我再为你创建。"))
    }
    // v1.9.6 fix: cycle=custom 必须 customDays>=1，否则 interval=0 → Worker 立即重触发死循环
    if (cycle == "custom" && customDays < 1) {
        return Pair(null, zh("自定义周期需要指定间隔天数（如：每3天），custom_days 至少为 1。"))
    }

    val now = System.currentTimeMillis()

    // 首次锚点（优先级从高到低）：
    // 1) weekly/biweekly 且 AI 传了 weekday（1=周一..7=周日）→ 对齐到下一个该星期的
    //    reminderHour:Minute（今天就是且未过 → 今天）。v2.4.2 根治：模型对「每周日」这类
    //    周期描述往往不传 trigger_date，v2.4.1 只修 trigger_date 路径仍会落到错误的星期。
    // 2) AI 明确给了 trigger_date → 用它 + reminderHour/Minute（v2.4.1）。
    // 3) 默认 → 下一个到达 reminderHour:reminderMinute 的时刻（今天已过则明天）。
    val weekdayParam = listOf("weekday", "week_day").firstNotNullOfOrNull { args[it] }
        ?.let { (it as? Double)?.toInt() ?: (it as? Int) ?: (it as? String)?.toIntOrNull() }
        ?.takeIf { it in 1..7 }
    val cal = Calendar.getInstance().apply { timeInMillis = now }
    cal.set(Calendar.HOUR_OF_DAY, reminderHour.coerceIn(0, 23))
    cal.set(Calendar.MINUTE, reminderMinute.coerceIn(0, 59))
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)

    if ((cycle == "weekly" || cycle == "biweekly") && weekdayParam != null) {
        val cur = ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7) + 1  // 1=周一..7=周日
        var diff = (weekdayParam - cur + 7) % 7
        if (diff == 0 && cal.timeInMillis <= now) diff = 7
        cal.add(Calendar.DAY_OF_MONTH, diff)
    } else {
        val triggerDateRaw = args["trigger_date"] as? String
        val triggerDate = triggerDateRaw?.let { raw ->
            runCatching {
                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(raw.trim())
            }.getOrNull()?.time
        }
        if (triggerDate != null && triggerDate > now) {
            cal.timeInMillis = triggerDate
            cal.set(Calendar.HOUR_OF_DAY, reminderHour.coerceIn(0, 23))
            cal.set(Calendar.MINUTE, reminderMinute.coerceIn(0, 59))
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= now) cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    val anchorNext = cal.timeInMillis

    // 日期类提醒必须带有合法的月日，否则引擎无法算出正确触发时间，
    // 会退化成「立刻触发一次就完事」。AI 无法自行推算农历/公历生日，
    // 此时请用户补充月日，而不是创建一个会误触发的提醒。
    if (kind == "date" && dateType != "holiday" &&
        (targetMonth !in 1..12 || targetDay !in 1..31)
    ) {
        return Pair(null, zh("需要具体的公历/农历月日才能创建日期提醒（例如：农历八月十五、公历5月1日）。请补充月日，我再为你创建。"))
    }
    if (kind == "date" && dateType == "holiday" && holidayName.isNullOrBlank()) {
        return Pair(null, zh("需要指定节假日名称（例如：春节、中秋节）才能创建节假日提醒。"))
    }
    // v1.9.0 fix: 规则提醒必须带全 频率/第几周/周几
    if (kind == "rule" && (rulePeriod == null || ruleWeek == null || ruleWeekday == null)) {
        return Pair(null, zh("规则提醒需要指定频率（每月/每季度/每年）、第几周和星期几，例如：每季度第一周周四。请补充完整，我再为你创建。"))
    }

    val entity = ReminderEntity(
        title = title, note = note, kind = kind, cycle = cycle, customDays = customDays,
        dateType = dateType, targetMonth = targetMonth, targetDay = targetDay,
        advanceDays = advanceDays, reminderHour = reminderHour, reminderMinute = reminderMinute,
        holidayName = holidayName,
        rulePeriod = rulePeriod, ruleWeek = ruleWeek, ruleWeekday = ruleWeekday,
        // v2.4.2: 存意图星期（启动锚点检测/修正用；非 weekly 清空）
        weeklyWeekday = if (cycle == "weekly" || cycle == "biweekly") weekdayParam else null,
        holidayAware = holidayAware,
        firstTriggerAt = anchorNext, nextTriggerAt = anchorNext,
        status = ReminderStatus.PENDING.name.lowercase(), retryCount = 0, isActive = true
    )

    // 用引擎重算 nextTriggerAt（日期/规则类按目标月日计算，避免落到 +1 分钟）
    val nextTrigger = ReminderEngine.calculateNextTrigger(entity, com.reminderapp.ReminderApp.instance)
    return Pair(entity.copy(nextTriggerAt = nextTrigger), null)
}

private suspend fun handleCreate(args: Map<String, Any?>, database: AppDatabase, scheduler: ReminderScheduler, notificationMgr: NotificationManager): String {
    val (entity, err) = tryBuildEntity(args)
    if (entity == null) return err ?: zh("创建失败")
    val id = database.reminderDao().insert(entity)
    scheduler.schedule(entity.copy(id = id))
    // v1.9.6 fix: 漏 touchLocalChange → AI 新建的提醒永远不同步 / 被远程旧数据覆盖
    com.reminderapp.service.SyncStore.touchLocalChange()
    com.reminderapp.receiver.ReminderWidgetProvider.refresh(com.reminderapp.ReminderApp.instance)
    return zhf("已创建提醒：「%s」", entity.title)
}

/**
 * import_tasks 工具：把多段待办文本解析为多条提醒，挂起预览弹窗等用户确认。
 * 不直接写入数据库——确认逻辑在 commitImport（用户点「确认创建」时调用）。
 */
private suspend fun handleImportTasks(
    args: Map<String, Any?>, database: AppDatabase, scheduler: ReminderScheduler, notificationMgr: NotificationManager, gson: Gson
): String {
    val rawItems = (args["items"] as? List<*>) ?: emptyList<Any?>()
    if (rawItems.isEmpty()) return zh("没有解析到可批量创建的提醒。")
    val built = mutableListOf<ReminderEntity>()
    val skipped = mutableListOf<String>()
    for (raw in rawItems) {
        val m = (raw as? Map<*, *>)?.mapNotNull { (k, v) -> (k as? String)?.let { it to v } }?.toMap() ?: emptyMap()
        val (e, err) = tryBuildEntity(m)
        if (e != null) built.add(e) else skipped.add(err ?: zh("无法解析"))
    }
    if (built.isEmpty()) {
        return zh("解析失败：") + skipped.joinToString("；")
    }
    pendingImportState.value = built
    val sb = StringBuilder(zhf("已解析出 %d 条提醒，请确认后批量创建：", built.size))
    built.forEachIndexed { i, e -> sb.append("\n${i + 1}. ${e.title} 〔${describeReminder(e)}〕") }
    if (skipped.isNotEmpty()) sb.append("\n（${skipped.size} 条无法解析已跳过）")
    return sb.toString()
}

private fun describeReminder(e: ReminderEntity): String {
    return when {
        e.kind == "date" && e.dateType == "holiday" -> e.holidayName ?: zh("节假日")
        e.kind == "date" -> zh("日期")
        else -> e.cycle
    }
}

/** 用户确认预览后批量写入：逐条 insert + 调度 + 标记本地变更 + 刷新小组件 */
private suspend fun commitImport(
    items: List<ReminderEntity>, database: AppDatabase, scheduler: ReminderScheduler, notificationMgr: NotificationManager
) {
    for (e in items) {
        val id = database.reminderDao().insert(e)
        scheduler.schedule(e.copy(id = id))
    }
    com.reminderapp.service.SyncStore.touchLocalChange()
    com.reminderapp.receiver.ReminderWidgetProvider.refresh(com.reminderapp.ReminderApp.instance)
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

/**
 * v2.4.9: 本周统计上下文（AI 周报数据源）——确认/错过/完成率/时段习惯/AI 调用量。
 * 只读数据库与 AILogStore，无副作用，可安全被模型调用。
 */
private suspend fun handleStatsContext(database: AppDatabase): String {
    val records = database.reminderRecordDao().getAll()
    val summary = com.reminderapp.service.StatsService.summarize(records)

    // 本周（周一 00:00 起）
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
    val dow = ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7)  // 0=周一
    cal.add(Calendar.DAY_OF_MONTH, -dow)
    val weekStart = cal.timeInMillis
    val weekConfirm = records.count { it.action == com.reminderapp.data.entity.ReminderRecordEntity.ACTION_CONFIRMED && it.timestamp >= weekStart }
    val startOfDayLocal: (Long) -> Long = { ts ->
        val c = Calendar.getInstance().apply { timeInMillis = ts }
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        c.timeInMillis
    }
    val weekMissed = records.filter { it.action == com.reminderapp.data.entity.ReminderRecordEntity.ACTION_NOTIFIED && it.timestamp >= weekStart }
        .map { startOfDayLocal(it.timestamp) }.toSet().size
    val rate = if (weekConfirm + weekMissed > 0) {
        (weekConfirm.toDouble() / (weekConfirm + weekMissed) * 100).toInt()
    } else null

    // 本周各提醒完成情况（标题 → 确认次数）
    val df = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    val weekKeys = mutableSetOf<String>()
    var c = Calendar.getInstance().apply { timeInMillis = weekStart }
    for (i in 0 until 7) {
        weekKeys.add(df.format(Date(c.timeInMillis)))
        c.add(Calendar.DAY_OF_MONTH, 1)
    }
    val titleById = database.reminderDao().getAllSync().associate { it.id to it.title }
    val byReminder = mutableMapOf<String, Int>()
    records.filter { it.timestamp >= weekStart && it.action == com.reminderapp.data.entity.ReminderRecordEntity.ACTION_CONFIRMED }
        .forEach { r -> val t = titleById[r.reminderId] ?: "已删除的提醒"; byReminder[t] = (byReminder[t] ?: 0) + 1 }

    // 最常忘记时段（复用 summary）
    val forget = summary.forgetHours.take(3).joinToString("、") { zhf("%s点(%s次)", it.first, it.second) }

    // AI 调用量（本周）
    val aiLogs = com.reminderapp.service.AILogStore.recent(com.reminderapp.ReminderApp.instance)
        .filter { it.time >= weekStart }
    val aiOk = aiLogs.count { it.ok }
    val aiFail = aiLogs.count { !it.ok }

    val sb = StringBuilder()
    sb.append(zhf("本周统计（%s 起 7 天）：确认 %s 次，错过 %s 天，完成率 %s。", df.format(Date(weekStart)), weekConfirm, weekMissed, rate?.let { "$it%" } ?: "暂无数据"))
    sb.append(zhf("当前连续 %s 天，最长连续 %s 天。", summary.currentStreak, summary.longestStreak))
    if (forget.isNotBlank()) sb.append(zhf("最常忘记的时段：%s。", forget))
    if (byReminder.isNotEmpty()) sb.append("本周各提醒确认次数：" + byReminder.entries.joinToString("、") { zhf("%s×%s", it.key, it.value) } + "。")
    sb.append(zhf("本周 AI 调用 %s 次（成功 %s，失败 %s）。", aiLogs.size, aiOk, aiFail))
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

private suspend fun handleUpdate(
    args: Map<String, Any?>,
    database: AppDatabase,
    scheduler: ReminderScheduler
): String {
    val keyword = (args["title_keyword"] as? String ?: "").lowercase()
    if (keyword.isEmpty()) return zh("请指定要修改的提醒标题（如：把「交房租」改成每月 5 号）。")
    val list = database.reminderDao().getAllSync()
    val match = list.find { it.title.lowercase().contains(keyword) } ?: return zhf("未找到包含「%s」的提醒", keyword)
    val ctx = com.reminderapp.ReminderApp.instance

    // 仅在用户传了该字段时更新；不传的字段保留原值
    val newTitle = args["new_title"] as? String
    val newNote = (args["note"] as? String) ?: match.note
    val cycleRaw = args["cycle"] as? String
    val customDays = (args["custom_days"] as? Double)?.toInt() ?: match.customDays
    val rulePeriod = args["rule_period"] as? String ?: match.rulePeriod
    val ruleWeek = (args["rule_week"] as? Double)?.toInt() ?: match.ruleWeek
    val ruleWeekday = (args["rule_weekday"] as? Double)?.toInt() ?: match.ruleWeekday
    val dateType = args["date_type"] as? String ?: match.dateType
    val targetMonth = (args["target_month"] as? Double)?.toInt() ?: match.targetMonth
    val targetDay = (args["target_day"] as? Double)?.toInt() ?: match.targetDay
    val advanceDays = (args["advance_days"] as? Double)?.toInt() ?: match.advanceDays
    val reminderHour = (args["reminder_hour"] as? Double)?.toInt() ?: match.reminderHour
    val reminderMinute = (args["reminder_minute"] as? Double)?.toInt() ?: match.reminderMinute
    val hasHour = args.containsKey("reminder_hour")
    val hasMinute = args.containsKey("reminder_minute")
    val holidayName = args["holiday_name"] as? String ?: match.holidayName
    // v2.4.8: 修改提醒时支持切换「避开节假日/周末」
    val holidayAware = when (val v = args["holiday_aware"]) {
        is Boolean -> v
        is String -> v.equals("true", ignoreCase = true)
        is Number -> v.toInt() != 0
        else -> match.holidayAware
    }

        // C3: 用「生效后的周期」判断——提醒已是 custom 时模型只传 custom_days 不传 cycle，旧守卫会漏
        val effectiveCycle = cycleRaw ?: match.cycle
        if (effectiveCycle == "custom" && customDays < 1) {
            return zh("自定义周期需要指定间隔天数（如：每3天），custom_days 至少为 1。")
        }

        val wasOverdue = match.status == "overdue"
        val updated = match.copy(
            title = newTitle ?: match.title,
            note = newNote,
            cycle = cycleRaw ?: match.cycle,
            customDays = customDays,
            rulePeriod = rulePeriod,
            ruleWeek = ruleWeek,
            ruleWeekday = ruleWeekday,
            dateType = dateType,
            targetMonth = targetMonth,
            targetDay = targetDay,
            advanceDays = advanceDays,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute,
            holidayName = holidayName,
            holidayAware = holidayAware,
            holidayAdjustNote = null
        )
        // C1: AI 修改已逾期提醒必须重置状态，否则 Worker 幽灵守卫(status=="overdue" 直接 return)
        // 会拦掉全部通知 → 「AI 说改好了，提醒却永久不响」。对齐 ViewModels.kt:100 确认路径的重置写法。
        val updatedBase = if (wasOverdue) updated.copy(status = "pending", retryCount = 0) else updated
        // Bug 3: cycle 类需同步改写锚点时分，否则 AI 改提醒时间对 cycle 无效
        // （date/rule 已用 reminderHour/Minute 计算，仅 cycle 走 firstTriggerAt 锚点）
        var updatedForTime = updatedBase
        // A7: 仅当本次 AI 真传了时分字段才重写锚点，避免「只改标题」也被对齐一次、静默改变触发时分
        if (updatedBase.kind == "cycle" && updatedBase.cycle != "once" && (hasHour || hasMinute)) {
            val cal = Calendar.getInstance().apply { timeInMillis = updatedBase.firstTriggerAt }
            cal.set(Calendar.HOUR_OF_DAY, updatedBase.reminderHour)
            cal.set(Calendar.MINUTE, updatedBase.reminderMinute)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            updatedForTime = updatedBase.copy(firstTriggerAt = cal.timeInMillis)
        }
        val nextTrigger = ReminderEngine.calculateNextTrigger(updatedForTime, com.reminderapp.ReminderApp.instance)
        val finalUpdated = updatedForTime.copy(nextTriggerAt = nextTrigger, holidayAdjustNote = null)
        database.reminderDao().update(finalUpdated)
        scheduler.schedule(finalUpdated)
    com.reminderapp.service.SyncStore.touchLocalChange()
    com.reminderapp.receiver.ReminderWidgetProvider.refresh(ctx)
    return zhf("已修改「%s」", finalUpdated.title)
}
