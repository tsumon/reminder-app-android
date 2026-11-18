package com.reminderapp.ui.screen.ai

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.service.AIService
import com.reminderapp.ui.comic.ComicCard
import com.reminderapp.ui.comic.ComicHairline
import com.reminderapp.ui.comic.ComicTextAction
import com.reminderapp.ui.comic.comicColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class NoApiProvider(
    val id: String,
    val name: String,
    val apiKeyUrl: String,
    val freeInfo: String,
    val apiEndpoint: String,
    val apiModel: String
)

val noApiProviders = listOf(
    NoApiProvider("deepseek", "DeepSeek", "https://platform.deepseek.com/api_keys", zh("注册即送 500 万 tokens"), "https://api.deepseek.com/v1", "deepseek-chat"),
    NoApiProvider("qwen", zh("通义千问"), "https://dashscope.console.aliyun.com/apiKey", zh("开通即送 100 万 tokens"), "https://dashscope.aliyuncs.com/compatible-mode/v1", "qwen-plus"),
    NoApiProvider("doubao", zh("豆包"), "https://console.volcengine.com/ark/region:ark+cn-beijing/apiKey", zh("注册即送 50 万 tokens"), "https://ark.cn-beijing.volces.com/api/v3", "doubao-lite-32k")
)

enum class ProviderTemplate(val endpoint: String, val model: String, val displayName: String) {
    OPENAI("https://api.openai.com/v1", "gpt-4o-mini", "OpenAI"),
    DEEPSEEK("https://api.deepseek.com/v1", "deepseek-chat", "DeepSeek"),
    QWEN("https://dashscope.aliyuncs.com/compatible-mode/v1", "qwen-plus", zh("通义千问")),
    DOUBAO("https://ark.cn-beijing.volces.com/api/v3", "doubao-lite-32k", zh("豆包（火山引擎）")),
    GLM("https://open.bigmodel.cn/api/paas/v4", "glm-4-flash", zh("智谱 GLM")),
    MOONSHOT("https://api.moonshot.cn/v1", "moonshot-v1-8k", "Moonshot")
}

@Composable
fun ProviderTemplatesAndFreeKeysCard(
    onApplyTemplate: (endpoint: String, model: String) -> Unit
) {
    val context = LocalContext.current
    val colors = comicColors()
    ComicCard(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(zh("快速模板"), color = colors.ink, fontSize = 15.5.sp, fontWeight = FontWeight.ExtraBold)
        ProviderTemplate.entries.forEach { tpl ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onApplyTemplate(tpl.endpoint, tpl.model) }
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(tpl.displayName, color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(tpl.model, color = colors.muted, fontSize = 12.sp)
            }
        }

        ComicHairline(isInner = true)
        Text(zh("免费获取 API Key"), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        noApiProviders.forEach { p ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(p.name, color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(p.freeInfo, color = colors.muted, fontSize = 12.sp)
                }
                ComicTextAction(
                    text = zh("获取 API Key →"),
                    accent = true,
                    onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(p.apiKeyUrl))) }
                )
            }
        }
    }
}

fun fetchModelList(
    scope: CoroutineScope,
    context: Context,
    aiService: AIService,
    base: String,
    key: String,
    fetching: Boolean,
    setFetching: (Boolean) -> Unit,
    onModels: (List<String>) -> Unit
) {
    if (fetching) return
    if (base.trim().isEmpty()) {
        Toast.makeText(context, zh("请先填写接口地址"), Toast.LENGTH_SHORT).show()
        return
    }
    setFetching(true)
    scope.launch {
        try {
            onModels(aiService.fetchModels(base, key))
        } catch (e: Exception) {
            Toast.makeText(context, e.message?.ifBlank { null } ?: zh("获取模型列表失败"), Toast.LENGTH_LONG).show()
        } finally {
            setFetching(false)
        }
    }
}

fun pingModels(
    scope: CoroutineScope,
    context: Context,
    aiService: AIService,
    base: String,
    key: String,
    pinging: Boolean,
    setPinging: (Boolean) -> Unit
) {
    if (pinging) return
    if (base.trim().isEmpty()) {
        Toast.makeText(context, zh("请先填写接口地址"), Toast.LENGTH_SHORT).show()
        return
    }
    setPinging(true)
    scope.launch {
        try {
            val ids = aiService.fetchModels(base, key)
            Toast.makeText(context, zhf("连通成功，共 %1\$s 个模型", ids.size), Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, e.message?.ifBlank { null } ?: zh("测连通失败"), Toast.LENGTH_LONG).show()
        } finally {
            setPinging(false)
        }
    }
}

@Composable
fun ModelPickerDialog(
    choices: List<String>?,
    currentModel: String,
    modelSearch: String,
    onSearchChange: (String) -> Unit,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val list = choices ?: return
    val filtered = remember(list, modelSearch) {
        val q = modelSearch.trim()
        if (q.isEmpty()) list else list.filter { it.contains(q, ignoreCase = true) }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(zh("选择模型")) },
        text = {
            Column {
                OutlinedTextField(
                    value = modelSearch,
                    onValueChange = onSearchChange,
                    label = { Text(zh("搜索模型")) },
                    modifier = Modifier.fillMaxWidth().testTag("model-search"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(filtered) { name ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(name) }
                                .padding(vertical = 10.dp)
                                .testTag("model-choice-$name"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            if (name == currentModel) {
                                Icon(Icons.Filled.Check, contentDescription = zh("已选"), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(zh("取消")) } }
    )
}
