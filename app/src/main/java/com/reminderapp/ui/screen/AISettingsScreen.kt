package com.reminderapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.ReminderApp
import com.reminderapp.i18n.zh
import com.reminderapp.service.AIService
import com.reminderapp.service.AISettings
import com.reminderapp.ui.comic.ComicButton
import com.reminderapp.ui.comic.ComicCard
import com.reminderapp.ui.comic.ComicHairline
import com.reminderapp.ui.comic.ComicTextAction
import com.reminderapp.ui.comic.ComicTopBar
import com.reminderapp.ui.comic.comicColors
import com.reminderapp.ui.screen.ai.ModelPickerDialog
import com.reminderapp.ui.screen.ai.ProviderTemplatesAndFreeKeysCard
import com.reminderapp.ui.screen.ai.fetchModelList
import com.reminderapp.ui.screen.ai.pingModels

/**
 * Minimalist Cream Comic AI Settings Screen.
 * Preserves ALL 8 testTags:
 * "ai-endpoint", "ai-model", "fetch-models-primary", "ping-models-primary",
 * "fetch-models-fallback", "ping-models-fallback", "model-search", "model-choice-$name".
 */
@Composable
fun AISettingsScreen(
    settings: AISettings,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = comicColors()
    val scope = rememberCoroutineScope()
    val aiService = remember { ReminderApp.instance.aiService }

    var endpoint by remember { mutableStateOf(settings.apiEndpoint) }
    var apiKey by remember { mutableStateOf(settings.apiKey) }
    var model by remember { mutableStateOf(settings.model) }
    var showKey by remember { mutableStateOf(false) }

    var isLocal by remember { mutableStateOf(settings.isLocal) }
    var fallbackEnabled by remember { mutableStateOf(settings.fallbackEnabled) }
    var fallbackEndpoint by remember { mutableStateOf(settings.fallbackEndpoint) }
    var fallbackKey by remember { mutableStateOf(settings.fallbackApiKey) }
    var fallbackModel by remember { mutableStateOf(settings.fallbackModel) }
    var showFallbackKey by remember { mutableStateOf(false) }

    var fetchingPrimary by remember { mutableStateOf(false) }
    var fetchingFallback by remember { mutableStateOf(false) }
    var pingingPrimary by remember { mutableStateOf(false) }
    var pingingFallback by remember { mutableStateOf(false) }
    var modelChoices by remember { mutableStateOf<List<String>?>(null) }
    var pickingFallback by remember { mutableStateOf(false) }
    var modelSearch by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(colors.canvas).navigationBarsPadding()) {
        ComicTopBar(
            title = zh("AI 设置"),
            onBack = onBack,
            trailing = {
                ComicButton(
                    text = zh("保存"),
                    cut = 5.5.dp,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp),
                    onClick = {
                        settings.apiEndpoint = endpoint.ifBlank { "https://api.openai.com/v1" }
                        settings.apiKey = apiKey
                        settings.model = model.ifBlank { "gpt-4o-mini" }
                        settings.isLocal = isLocal
                        settings.fallbackEnabled = fallbackEnabled
                        settings.fallbackEndpoint = fallbackEndpoint
                        settings.fallbackApiKey = fallbackKey
                        settings.fallbackModel = fallbackModel
                        onBack()
                    }
                )
            }
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ComicCard(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(zh("API 配置"), color = colors.ink, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)

                OutlinedTextField(
                    value = endpoint, onValueChange = { endpoint = it },
                    label = { Text(zh("接口地址")) }, placeholder = { Text("https://api.openai.com/v1") },
                    modifier = Modifier.fillMaxWidth().testTag("ai-endpoint"),
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
                OutlinedTextField(
                    value = apiKey, onValueChange = { apiKey = it },
                    label = { Text("API Key") }, placeholder = { Text("sk-...") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showKey) zh("隐藏") else zh("显示")
                            )
                        }
                    }
                )
                OutlinedTextField(
                    value = model, onValueChange = { model = it },
                    label = { Text(zh("模型")) }, placeholder = { Text("gpt-4o-mini") },
                    modifier = Modifier.fillMaxWidth().testTag("ai-model"), singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    ComicTextAction(
                        text = if (fetchingPrimary) zh("加载中…") else zh("获取模型列表"),
                        enabled = !fetchingPrimary, accent = true,
                        modifier = Modifier.testTag("fetch-models-primary"),
                        onClick = {
                            fetchModelList(scope, context, aiService, endpoint, apiKey, fetchingPrimary, { fetchingPrimary = it }) {
                                pickingFallback = false; modelSearch = ""
                                modelChoices = AIService.orderedModels(it, settings.lastSelectedModel)
                            }
                        }
                    )
                    ComicTextAction(
                        text = if (pingingPrimary) zh("测试中...") else zh("测连通"),
                        enabled = !pingingPrimary,
                        modifier = Modifier.testTag("ping-models-primary"),
                        onClick = { pingModels(scope, context, aiService, endpoint, apiKey, pingingPrimary) { pingingPrimary = it } }
                    )
                }

                ComicHairline(isInner = true)

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(zh("本地模型"), color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(zh("如 Ollama（http://localhost:11434/v1），无需 API Key"), color = colors.muted, fontSize = 11.5.sp)
                    }
                    Switch(checked = isLocal, onCheckedChange = { isLocal = it })
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(zh("备用模型"), color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(zh("主模型不可用时自动切换（如 DeepSeek）"), color = colors.muted, fontSize = 11.5.sp)
                    }
                    Switch(checked = fallbackEnabled, onCheckedChange = { fallbackEnabled = it })
                }

                if (fallbackEnabled) {
                    OutlinedTextField(
                        value = fallbackEndpoint, onValueChange = { fallbackEndpoint = it },
                        label = { Text(zh("备用接口地址")) }, placeholder = { Text("https://api.deepseek.com/v1") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )
                    OutlinedTextField(
                        value = fallbackKey, onValueChange = { fallbackKey = it },
                        label = { Text(zh("备用 API Key")) }, placeholder = { Text("sk-...") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        visualTransformation = if (showFallbackKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showFallbackKey = !showFallbackKey }) {
                                Icon(
                                    if (showFallbackKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showFallbackKey) zh("隐藏") else zh("显示")
                                )
                            }
                        }
                    )
                    OutlinedTextField(
                        value = fallbackModel, onValueChange = { fallbackModel = it },
                        label = { Text(zh("备用模型名")) }, placeholder = { Text("deepseek-chat") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        ComicTextAction(
                            text = if (fetchingFallback) zh("加载中…") else zh("获取模型列表"),
                            enabled = !fetchingFallback, accent = true,
                            modifier = Modifier.testTag("fetch-models-fallback"),
                            onClick = {
                                fetchModelList(scope, context, aiService, fallbackEndpoint, fallbackKey, fetchingFallback, { fetchingFallback = it }) {
                                    pickingFallback = true; modelSearch = ""
                                    modelChoices = AIService.orderedModels(it, settings.lastSelectedFallbackModel)
                                }
                            }
                        )
                        ComicTextAction(
                            text = if (pingingFallback) zh("测试中...") else zh("测连通"),
                            enabled = !pingingFallback,
                            modifier = Modifier.testTag("ping-models-fallback"),
                            onClick = { pingModels(scope, context, aiService, fallbackEndpoint, fallbackKey, pingingFallback) { pingingFallback = it } }
                        )
                    }
                }
            }

            ProviderTemplatesAndFreeKeysCard { ep, m -> endpoint = ep; model = m }
            Spacer(Modifier.height(16.dp))
        }
    }

    ModelPickerDialog(
        choices = modelChoices,
        currentModel = if (pickingFallback) fallbackModel else model,
        modelSearch = modelSearch,
        onSearchChange = { modelSearch = it },
        onSelect = { name ->
            if (pickingFallback) { fallbackModel = name; settings.lastSelectedFallbackModel = name }
            else { model = name; settings.lastSelectedModel = name }
            modelChoices = null; modelSearch = ""
        },
        onDismiss = { modelChoices = null; modelSearch = "" }
    )
}
