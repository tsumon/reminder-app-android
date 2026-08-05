package com.reminderapp.ui.screen

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.reminderapp.service.AISettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsScreen(
    settings: AISettings,
    onBack: () -> Unit
) {
    var endpoint by remember { mutableStateOf(settings.apiEndpoint) }
    var apiKey by remember { mutableStateOf(settings.apiKey) }
    var model by remember { mutableStateOf(settings.model) }
    var showKey by remember { mutableStateOf(false) }

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
            // API 配置区
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
                "支持任意兼容 OpenAI 格式的 API（DeepSeek、通义千问、智谱等）",
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
                    Text(tpl.name, modifier = Modifier.weight(1f))
                    Text(tpl.model, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            HorizontalDivider()

            // 说明
            Text("说明", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Text(
                "🎤 语音或文字输入即可管理提醒\n✨ 说「每天提醒我喝水」自动创建\n✅ 说「确认喝水提醒」即可标记完成",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

enum class ProviderTemplate(val endpoint: String, val model: String, val name: String) {
    OPENAI("https://api.openai.com/v1", "gpt-4o-mini", "OpenAI"),
    DEEPSEEK("https://api.deepseek.com/v1", "deepseek-chat", "DeepSeek"),
    QWEN("https://dashscope.aliyuncs.com/compatible-mode/v1", "qwen-plus", "通义千问"),
    GLM("https://open.bigmodel.cn/api/paas/v4", "glm-4-flash", "智谱 GLM"),
    MOONSHOT("https://api.moonshot.cn/v1", "moonshot-v1-8k", "Moonshot")
}
