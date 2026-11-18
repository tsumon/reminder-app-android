package com.reminderapp.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.service.AIService
import com.reminderapp.service.AISettings
import com.reminderapp.service.NotificationManager
import com.reminderapp.service.ReminderScheduler
import com.reminderapp.ui.comic.ComicButton
import com.reminderapp.ui.comic.ComicHairline
import com.reminderapp.ui.comic.ComicTextAction
import com.reminderapp.ui.comic.ComicTopBar
import com.reminderapp.ui.comic.comicColors
import com.reminderapp.ui.screen.ai.BatchImportConfirmDialog
import com.reminderapp.ui.screen.ai.ComicChatPanel
import com.reminderapp.ui.viewmodel.AIChatViewModel
import com.reminderapp.ui.viewmodel.commitAIImport
import com.reminderapp.ui.viewmodel.pendingAIImportState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val colors = comicColors()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val viewModel = remember {
        AIChatViewModel(
            appContext = context.applicationContext,
            settings = settings,
            aiService = aiService,
            database = database,
            scheduler = scheduler,
            notificationMgr = notificationMgr
        )
    }

    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val errorMsg by viewModel.errorMsg.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var historyMenu by remember { mutableStateOf(false) }

    val missingKeyToast = {
        Toast.makeText(context, zh("请先在 AI 设置中配置 API Key"), Toast.LENGTH_SHORT).show()
    }

    val recordPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startVoiceInput(missingKeyToast)
        else Toast.makeText(context, zh("需要录音权限才能使用语音输入"), Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .imePadding()
    ) {
        ComicTopBar(
            title = zh("AI 助手"),
            onBack = onBack,
            trailing = {
                ComicTextAction(
                    text = zh("洞察"),
                    onClick = { viewModel.sendUserMessage("给我一份本周提醒总结和洞察", missingKeyToast) }
                )
                Box {
                    ComicTextAction(text = zh("历史") + " ▾", onClick = { historyMenu = true })
                    DropdownMenu(expanded = historyMenu, onDismissRequest = { historyMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(zhf("查看历史（%s 条）", messages.size)) },
                            enabled = messages.isNotEmpty(),
                            onClick = {
                                historyMenu = false
                                scope.launch {
                                    delay(80)
                                    if (messages.isNotEmpty()) listState.animateScrollToItem(0)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(zh("清空历史")) },
                            enabled = messages.isNotEmpty(),
                            onClick = { historyMenu = false; viewModel.clearHistory() }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(zh("AI 设置")) },
                            onClick = { historyMenu = false; onNavigateSettings() }
                        )
                    }
                }
                ComicTextAction(text = zh("设置"), onClick = onNavigateSettings, accent = true)
            }
        )

        errorMsg?.let { msg ->
            Text(
                text = "✕ $msg",
                color = colors.danger,
                fontSize = 12.5.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            LaunchedEffect(msg) { delay(5000); viewModel.dismissError() }
        }

        if (messages.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(zh("跟我说你想提醒什么"), color = colors.ink, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Text(zh("\"每天8点提醒我吃药\"\n\"每年提醒我妈生日\""), color = colors.muted, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ComicChatPanel(
                        msg = msg,
                        onSelectOption = { opt -> viewModel.selectOption(msg, opt, missingKeyToast) }
                    )
                }
                if (isLoading) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = colors.accent)
                            Spacer(Modifier.width(8.dp))
                            Text(zh("思考中…"), color = colors.muted, fontSize = 12.5.sp)
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.canvas)
                .navigationBarsPadding()
        ) {
            AnimatedVisibility(visible = isListening) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = colors.accent)
            }
            ComicHairline(isInner = false)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = zh("语音"),
                    tint = if (isListening) colors.danger else colors.muted,
                    modifier = Modifier.size(22.dp).clickable {
                        val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                        if (!hasMic) recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        else viewModel.startVoiceInput(missingKeyToast)
                    }
                )
                Spacer(Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (inputText.isEmpty()) {
                        Text(zh("输入或点击麦克风说话..."), color = colors.muted.copy(alpha = 0.8f), fontSize = 14.sp)
                    }
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        maxLines = 3,
                        textStyle = LocalTextStyle.current.copy(color = colors.ink, fontSize = 14.5.sp),
                        cursorBrush = SolidColor(colors.accent),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.width(10.dp))
                ComicButton(
                    text = zh("发送"),
                    enabled = inputText.isNotBlank() && !isLoading,
                    cut = 5.dp,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp),
                    onClick = {
                        val payload = inputText
                        inputText = ""
                        viewModel.sendUserMessage(payload, missingKeyToast)
                    }
                )
            }
        }
    }

    BatchImportConfirmDialog(
        items = pendingAIImportState.value,
        onConfirm = { items ->
            pendingAIImportState.value = null
            scope.launch {
                commitAIImport(items, database)
                Toast.makeText(context, zhf("已批量创建 %d 条提醒", items.size), Toast.LENGTH_SHORT).show()
            }
        },
        onDismiss = { pendingAIImportState.value = null }
    )
}
