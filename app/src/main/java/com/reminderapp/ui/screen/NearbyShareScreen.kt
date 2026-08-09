package com.reminderapp.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.service.BackupService
import com.reminderapp.service.NearbyShareService
import com.reminderapp.service.QrCodeUtils
import com.reminderapp.service.QrScanner
import com.reminderapp.service.ReminderScheduler
import com.reminderapp.service.SyncStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ServerSocket
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf

/**
 * 近场传输：同一局域网内互传提醒
 * - 发送：显示二维码 + 启动本地服务，对方扫码或输入地址即可收到全部提醒
 * - 接收：扫码（或输入 IP）→ 下载 → 导入（去重/重新调度）
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

    var mode by remember { mutableStateOf(zh("发送")) }
    var server by remember { mutableStateOf<ServerSocket?>(null) }
    var serverLog by remember { mutableStateOf("") }
    var serverLogIsError by remember { mutableStateOf(false) }
    var receiveIP by remember { mutableStateOf("") }
    var isReceiving by remember { mutableStateOf(false) }
    var resultMsg by remember { mutableStateOf<String?>(null) }
    var resultIsError by remember { mutableStateOf(false) }
    var ipAddress by remember { mutableStateOf(NearbyShareService.localIpAddress()) }
    var showScanner by remember { mutableStateOf(false) }

    // 相机权限：扫码前申请
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) showScanner = true
        else Toast.makeText(context, zh("需要相机权限才能扫码"), Toast.LENGTH_SHORT).show()
    }

    // 生成二维码（发送页用，IP 变化时重新生成）
    val qrBitmap = remember(ipAddress) {
        if (ipAddress != null) {
            QrCodeUtils.generateQrBitmap("http://$ipAddress:${NearbyShareService.PORT}${NearbyShareService.PATH}")
        } else null
    }

    DisposableEffect(Unit) {
        onDispose {
            NearbyShareService.stopServer(server)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(zh("附近传输")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = zh("返回"))
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
                listOf(zh("发送"), zh("接收")).forEachIndexed { index, m ->
                    SegmentedButton(
                        selected = mode == m,
                        onClick = { mode = m },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)
                    ) {
                        Text(m)
                    }
                }
            }

            if (mode == zh("发送")) {
                // ══════ 发送 ══════
                Icon(Icons.Filled.Wifi, contentDescription = null, modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary)
                Text(zh("两台设备连接同一 Wi-Fi 后即可互传"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (ipAddress != null && qrBitmap != null) {
                    Text(zh("本机地址"), style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "http://$ipAddress:${NearbyShareService.PORT}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    // 二维码：对方扫码即自动接收
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = zh("分享二维码"),
                        modifier = Modifier
                            .size(200.dp)
                            .padding(8.dp)
                            .background(Color.White, RoundedCornerShape(16.dp))
                    )
                    Text(zh("对方用「扫码接收」扫这里，自动开始接收"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(zh("无法获取局域网 IP，请确认已连接 Wi-Fi"),
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
                        Text(zh("停止共享"))
                    }
                } else {
                    if (serverLog.isNotEmpty()) {
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
                        Text(zh("开始共享当前提醒"))
                    }
                    Text(zh("对方在「接收」页扫码或输入上方地址即可收到全部提醒"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp))
                }
            } else {
                // ══════ 接收 ══════
                Icon(Icons.Filled.ArrowDownward, contentDescription = null, modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary)
                Text(zh("扫发送方二维码，或手动输入地址"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                // 扫码主操作
                Button(
                    onClick = {
                        val hasCam = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasCam) showScanner = true
                        else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(zh("扫码接收（扫对方二维码）"))
                }

                // 或手动输入
                HorizontalDivider()
                Text(zh("或手动输入对方地址"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = receiveIP,
                    onValueChange = { receiveIP = it },
                    label = { Text(zh("对方 IP 地址")) },
                    placeholder = { Text("192.168.1.100") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isReceiving) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    Text(zh("正在下载..."), style = MaterialTheme.typography.bodySmall)
                } else {
                    Button(
                        onClick = {
                            val host = receiveIP.trim()
                            if (host.isEmpty()) return@Button
                            receive(host, database, scheduler, context, scope,
                                setReceiving = { isReceiving = it },
                                setResult = { msg, isErr ->
                                    resultMsg = msg; resultIsError = isErr
                                })
                        },
                        enabled = receiveIP.isNotBlank() && !isReceiving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(zh("下载并导入"))
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

    // 扫码 Dialog
    if (showScanner) {
        Dialog(
            onDismissRequest = { showScanner = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                QrScanner(
                    onQrScanned = { value ->
                        showScanner = false
                        // 二维码内容是 http://ip:port/reminders.json → 解析后接收
                        val host = try { Uri.parse(value).host ?: value } catch (_: Exception) { value }
                        receive(host, database, scheduler, context, scope,
                            setReceiving = { isReceiving = it },
                            setResult = { msg, isErr ->
                                resultMsg = msg; resultIsError = isErr
                            })
                    },
                    modifier = Modifier.fillMaxSize()
                )
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .background(Color.Transparent, RoundedCornerShape(16.dp))
                            .then(
                                Modifier
                                    .padding(0.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.0f),
                                        RoundedCornerShape(16.dp)
                                    )
                            )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(zh("对准对方的二维码"), color = Color.White,
                        style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { showScanner = false }) {
                        Text(zh("取消"), color = Color.White)
                    }
                }
            }
        }
    }
}

/** 接收：下载 → 解析 → 导入（扫码与手动输入共用） */
private fun receive(
    hostInput: String,
    database: AppDatabase,
    scheduler: ReminderScheduler,
    context: android.content.Context,
    scope: CoroutineScope,
    setReceiving: (Boolean) -> Unit,
    setResult: (String, Boolean) -> Unit
) {
    val host = hostInput.trim()
    if (host.isEmpty()) return
    setReceiving(true)
    setResult("", false)
    scope.launch {
        val json = withContext(Dispatchers.IO) {
            NearbyShareService.fetchFrom(host)
        }
        setReceiving(false)
        if (json == null) {
            setResult(zh("下载失败：请确认两台设备在同一 Wi-Fi、地址正确，且发送方已开始共享"), true)
            return@launch
        }
        val entities = BackupService.importFromJson(json)
        if (entities == null) {
            setResult(zh("数据解析失败"), true)
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
        setResult(zhf("导入完成：新增 %1$s 条，跳过重复 %2$s 条", imported, skipped), false)
    }
}
