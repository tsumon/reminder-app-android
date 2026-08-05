package com.reminderapp.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.reminderapp.service.AISettings

data class NoApiProvider(val id: String, val name: String, val webUrl: String, val apiKeyUrl: String, val freeInfo: String, val apiEndpoint: String, val apiModel: String)

val noApiProviders = listOf(
    NoApiProvider("deepseek", "DeepSeek", "https://chat.deepseek.com/", "https://platform.deepseek.com/api_keys", "注册即送 500 万 tokens", "https://api.deepseek.com/v1", "deepseek-chat"),
    NoApiProvider("qwen", "通义千问", "https://tongyi.aliyun.com/qianwen/", "https://dashscope.console.aliyun.com/apiKey", "开通即送 100 万 tokens", "https://dashscope.aliyuncs.com/compatible-mode/v1", "qwen-plus"),
    NoApiProvider("doubao", "豆包", "https://www.doubao.com/chat/", "https://console.volcengine.com/ark/region:ark+cn-beijing/apiKey", "注册即送 50 万 tokens", "https://ark.cn-beijing.volces.com/api/v3", "doubao-lite-32k")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsScreen(
    settings: AISettings,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var endpoint by remember { mutableStateOf(settings.apiEndpoint) }
    var apiKey by remember { mutableStateOf(settings.apiKey) }
    var model by remember { mutableStateOf(settings.model) }
    var showKey by remember { mutableStateOf(false) }
    var useNoAPI by remember { mutableStateOf(settings.useNoAPIMode) }
    var noApiProvider by remember { mutableStateOf(settings.noAPIProvider) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        settings.useNoAPIMode = useNoAPI
                        settings.noAPIProvider = noApiProvider
                        settings.apiEndpoint = endpoint.ifBlank { "https://api.openai.com/v1" }
                        settings.apiKey = apiKey
                        settings.model = model.ifBlank { "gpt-4o-mini" }
                        onBack()
                    }) {
                        Text("保存", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ══════ 模式开关 ══════
            Text("模式选择", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Bolt, contentDescription = null, tint = Color(0xFFFF9800))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("免 API 模式", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = Color(0xFFFF9800).copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                        Text("无需 Key", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF9800))
                    }
                }
                Switch(checked = useNoAPI, onCheckedChange = { useNoAPI = it })
            }

            Text(
                if (useNoAPI) "无需 API Key，输入问题后自动复制并跳转到 AI 网页版粘贴发送。"
                else "使用你自己的 API Key 调用 AI，数据不走第三方网页。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ══════ 免 API 模式：选择服务商 ══════
            if (useNoAPI) {
                HorizontalDivider()
                Text("免 API 服务商", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

                noApiProviders.forEach { p ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { noApiProvider = p.id },
                        shape = MaterialTheme.shapes.medium,
                        color = if (noApiProvider == p.id) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.name, fontWeight = FontWeight.Medium)
                                Text(p.webUrl, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                            if (noApiProvider == p.id) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFFFF9800))
                            }
                        }
                    }
                }

                // 快速打开按钮
                Text("快速打开", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                noApiProviders.forEach { p ->
                    OutlinedButton(
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(p.webUrl)))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("打开 ${p.name} 网页版")
                    }
                }
            }

            // ══════ API 模式配置 ══════
            if (!useNoAPI) {
                HorizontalDivider()
                Text("API 配置", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    label = { Text("接口地址") },
                    placeholder = { Text("https://api.openai.com/v1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    placeholder = { Text("sk-...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showKey) "隐藏" else "显示"
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("模型") },
                    placeholder = { Text("gpt-4o-mini") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    "支持任意兼容 OpenAI 格式的 API（DeepSeek、通义千问、豆包、智谱等）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                // 快速模板
                Text("快速模板", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

                ProviderTemplate.values().forEach { tpl ->
                    OutlinedButton(
                        onClick = {
                            endpoint = tpl.endpoint
                            model = tpl.model
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(tpl.displayName, modifier = Modifier.weight(1f))
                        Text(tpl.model, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider()

                // 💰 免费获取 API Key
                Text("💰 免费获取 API Key", style = MaterialTheme.typography.titleSmall, color = Color(0xFF4CAF50))

                noApiProviders.forEach { p ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(p.name, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.weight(1f))
                                Surface(color = Color(0xFF4CAF50).copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                                    Text("免费额度", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
                                }
                            }
                            Text(p.freeInfo, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(p.apiKeyUrl)))
                            }) {
                                Text("获取 API Key →", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                Text(
                    "以上服务均提供免费额度，注册后即可获取 API Key。获取后粘贴到上方配置即可使用。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            // 说明
            Text("说明", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Text(
                if (useNoAPI) "🎤 语音或文字输入即可管理提醒\n✨ 输入问题 → 自动复制并跳转网页版 AI\n📋 粘贴即用，无需任何 Key\n✅ 说「确认喝水提醒」即可标记完成"
                else "🎤 语音或文字输入即可管理提醒\n✨ 说「每天提醒我喝水」自动创建\n✅ 说「确认喝水提醒」即可标记完成",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

enum class ProviderTemplate(val endpoint: String, val model: String, val displayName: String) {
    OPENAI("https://api.openai.com/v1", "gpt-4o-mini", "OpenAI"),
    DEEPSEEK("https://api.deepseek.com/v1", "deepseek-chat", "DeepSeek"),
    QWEN("https://dashscope.aliyuncs.com/compatible-mode/v1", "qwen-plus", "通义千问"),
    DOUBAO("https://ark.cn-beijing.volces.com/api/v3", "doubao-lite-32k", "豆包（火山引擎）"),
    GLM("https://open.bigmodel.cn/api/paas/v4", "glm-4-flash", "智谱 GLM"),
    MOONSHOT("https://api.moonshot.cn/v1", "moonshot-v1-8k", "Moonshot")
}
