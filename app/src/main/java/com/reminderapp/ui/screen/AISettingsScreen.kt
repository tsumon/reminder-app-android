package com.reminderapp.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.reminderapp.service.AISettings
import com.reminderapp.i18n.zh

data class NoApiProvider(val id: String, val name: String, val apiKeyUrl: String, val freeInfo: String, val apiEndpoint: String, val apiModel: String)

val noApiProviders = listOf(
    NoApiProvider("deepseek", "DeepSeek", "https://platform.deepseek.com/api_keys", zh("注册即送 500 万 tokens"), "https://api.deepseek.com/v1", "deepseek-chat"),
    NoApiProvider("qwen", zh("通义千问"), "https://dashscope.console.aliyun.com/apiKey", zh("开通即送 100 万 tokens"), "https://dashscope.aliyuncs.com/compatible-mode/v1", "qwen-plus"),
    NoApiProvider("doubao", zh("豆包"), "https://console.volcengine.com/ark/region:ark+cn-beijing/apiKey", zh("注册即送 50 万 tokens"), "https://ark.cn-beijing.volces.com/api/v3", "doubao-lite-32k")
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(zh("AI 设置")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = zh("返回"))
                    }
                },
                actions = {
                    TextButton(onClick = {
                        settings.apiEndpoint = endpoint.ifBlank { "https://api.openai.com/v1" }
                        settings.apiKey = apiKey
                        settings.model = model.ifBlank { "gpt-4o-mini" }
                        onBack()
                    }) {
                        Text(zh("保存"), fontWeight = FontWeight.Bold)
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
            // ══════ API 配置 ══════
            Text(zh("API 配置"), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = endpoint,
                onValueChange = { endpoint = it },
                label = { Text(zh("接口地址")) },
                placeholder = { Text("https://api.openai.com/v1") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
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
                            contentDescription = if (showKey) zh("隐藏") else zh("显示")
                        )
                    }
                }
            )

            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text(zh("模型")) },
                placeholder = { Text("gpt-4o-mini") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                zh("支持任意兼容 OpenAI 格式的 API（DeepSeek、通义千问、豆包、智谱等）"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            // ══════ 快速模板 ══════
            Text(zh("快速模板"), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

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

            // ══════ 💰 免费获取 API Key ══════
            Text(zh("💰 免费获取 API Key"), style = MaterialTheme.typography.titleSmall, color = Color(0xFF4CAF50))

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
                                Text(zh("免费额度"), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
                            }
                        }
                        Text(p.freeInfo, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(p.apiKeyUrl)))
                        }) {
                            Text(zh("获取 API Key →"), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            Text(
                zh("以上服务均提供免费额度，注册后即可获取 API Key。获取后粘贴到上方配置即可使用。"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            // ══════ 说明 ══════
            Text(zh("说明"), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Text(
                zh("🎤 语音或文字输入即可管理提醒\n✨ 说「每天提醒我喝水」自动创建\n✅ 说「确认喝水提醒」即可标记完成"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

enum class ProviderTemplate(val endpoint: String, val model: String, val displayName: String) {
    OPENAI("https://api.openai.com/v1", "gpt-4o-mini", "OpenAI"),
    DEEPSEEK("https://api.deepseek.com/v1", "deepseek-chat", "DeepSeek"),
    QWEN("https://dashscope.aliyuncs.com/compatible-mode/v1", "qwen-plus", zh("通义千问")),
    DOUBAO("https://ark.cn-beijing.volces.com/api/v3", "doubao-lite-32k", zh("豆包（火山引擎）")),
    GLM("https://open.bigmodel.cn/api/paas/v4", "glm-4-flash", zh("智谱 GLM")),
    MOONSHOT("https://api.moonshot.cn/v1", "moonshot-v1-8k", "Moonshot")
}
