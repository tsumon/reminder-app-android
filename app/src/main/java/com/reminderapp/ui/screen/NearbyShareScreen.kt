package com.reminderapp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.service.BackupService
import com.reminderapp.service.NearbyShareService
import com.reminderapp.service.ReminderScheduler
import com.reminderapp.service.SyncStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ServerSocket

/**
 * 近场传输：同一局域网内互传提醒
 * - 发送：启动本地服务，对方在「接收」页输入本机地址即可收到全部提醒
 * - 接收：输入对方 IP → 下载 → 导入（去重/重新调度）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyShareScreen(
    database: AppDatabase,
    scheduler: ReminderScheduler,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var mode by remember { mutableStateOf("发送") }
    var server by remember { mutableStateOf<ServerSocket?>(null) }
    var serverLog by remember { mutableStateOf("") }
    var serverLogIsError by remember { mutableStateOf(false) }
    var receiveIP by remember { mutableStateOf("") }
    var isReceiving by remember { mutableStateOf(false) }
    var resultMsg by remember { mutableStateOf<String?>(null) }
    var resultIsError by remember { mutableStateOf(false) }
    var ipAddress by remember { mutableStateOf(NearbyShareService.localIpAddress()) }

    DisposableEffect(Unit) {
        onDispose {
            NearbyShareService.stopServer(server)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("附近传输") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 模式切换
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf("发送", "接收").forEachIndexed { index, m ->
                    SegmentedButton(
                        selected = mode == m,
                        onClick = { mode = m },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)
                    ) {
                        Text(m)
                    }
                }
            }

            if (mode == "发送") {
                // ══════ 发送 ══════
                Icon(Icons.Filled.Wifi, contentDescription = null, modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary)
                Text("两台设备连接同一 Wi-Fi 后即可互传",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (ipAddress != null) {
                    Text("本机地址", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "http://$ipAddress:${NearbyShareService.PORT}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text("无法获取局域网 IP，请确认已连接 Wi-Fi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error)
                }

                if (server != null) {
                    if (serverLog.isNotEmpty()) {
                        Text(serverLog, style = MaterialTheme.typography.bodySmall,
                            color = if (serverLogIsError) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.tertiary)
                    }
                    Button(
                        onClick = {
                            NearbyShareService.stopServer(server)
                            server = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("停止共享")
                    }
                } else {
                    if (serverLog.isNotEmpty()) {
                        // 启动失败等错误日志在未运行时也显示
                        Text(serverLog, style = MaterialTheme.typography.bodySmall,
                            color = if (serverLogIsError) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.tertiary)
                    }
                    Button(
                        onClick = {
                            serverLog = ""
                            serverLogIsError = false
                            server = NearbyShareService.startServer(
                                jsonProvider = {
                                    // 每次请求拉最新数据（DAO 是挂起函数，这里同步拉取）
                                    kotlinx.coroutines.runBlocking(Dispatchers.IO) {
                                        BackupService.exportToJson(database.reminderDao().getAllSync())
                                    }
                                },
                                onEvent = { msg, isErr ->
                                    scope.launch {
                                        serverLog = msg
                                        serverLogIsError = isErr
                                    }
                                }
                            )
                            ipAddress = NearbyShareService.localIpAddress()
                        }
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开始共享当前提醒")
                    }
                    Text("对方在「接收」页输入上方地址即可收到全部提醒",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp))
                }
            } else {
                // ══════ 接收 ══════
                Icon(Icons.Filled.ArrowDownward, contentDescription = null, modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary)
                Text("输入发送方显示的地址\n（支持 192.168.1.100 或完整 http://192.168.1.100:47823）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = receiveIP,
                    onValueChange = { receiveIP = it },
                    label = { Text("对方 IP 地址") },
                    placeholder = { Text("192.168.1.100") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isReceiving) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    Text("正在下载...", style = MaterialTheme.typography.bodySmall)
                } else {
                    Button(
                        onClick = {
                            val host = receiveIP.trim()
                            if (host.isEmpty()) return@Button
                            isReceiving = true
                            resultMsg = null
                            scope.launch {
                                val json = withContext(Dispatchers.IO) {
                                    NearbyShareService.fetchFrom(host)
                                }
                                isReceiving = false
                                if (json == null) {
                                    resultIsError = true
                                    resultMsg = "下载失败：请确认两台设备在同一 Wi-Fi、地址正确，且发送方已开始共享"
                                    return@launch
                                }
                                val entities = BackupService.importFromJson(json)
                                if (entities == null) {
                                    resultIsError = true
                                    resultMsg = "数据解析失败"
                                    return@launch
                                }
                                // 导入去重：⚠️ 不能用 id——发送方的 id 是它本地自增的，
                                // 接收端已有 id 1..N 时会把对方同 id 的提醒误跳过（丢数据）。
                                // 只用指纹（title|nextTriggerAt|kind|cycle，加类型防「同名同时间」误判）
                                val existingFp = database.reminderDao().getAllSync()
                                    .map { "${it.title}|${it.nextTriggerAt}|${it.kind}|${it.cycle}" }
                                    .toSet()
                                var imported = 0
                                var skipped = 0
                                for (r in entities) {
                                    val fp = "${r.title}|${r.nextTriggerAt}|${r.kind}|${r.cycle}"
                                    if (existingFp.contains(fp)) {
                                        skipped++
                                        continue
                                    }
                                    val newId = try {
                                        database.reminderDao().insert(r.copy(id = 0))
                                    } catch (e: Exception) {
                                        skipped++
                                        continue
                                    }
                                    scheduler.schedule(r.copy(id = newId))
                                    imported++
                                }
                                SyncStore.touchLocalChange()
                                com.reminderapp.receiver.ReminderWidgetProvider.refresh(context)
                                resultIsError = false
                                resultMsg = "导入完成：新增 $imported 条，跳过重复 $skipped 条"
                            }
                        },
                        enabled = receiveIP.isNotBlank() && !isReceiving
                    ) {
                        Text("下载并导入")
                    }
                }

                resultMsg?.let { msg ->
                    Text(msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (resultIsError) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.tertiary)
                }
            }
        }
    }
}
