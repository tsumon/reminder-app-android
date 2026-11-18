package com.reminderapp.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.i18n.zh
import com.reminderapp.service.BackupService
import com.reminderapp.service.NearbyShareService
import com.reminderapp.service.QrCodeUtils
import com.reminderapp.service.ReminderScheduler
import com.reminderapp.ui.comic.ComicButton
import com.reminderapp.ui.comic.ComicCard
import com.reminderapp.ui.comic.ComicHairline
import com.reminderapp.ui.comic.ComicTextAction
import com.reminderapp.ui.comic.ComicTopBar
import com.reminderapp.ui.comic.comicColors
import com.reminderapp.ui.screen.settings.NearbyQrScannerDialog
import com.reminderapp.ui.screen.settings.receiveNearbyReminders
import java.net.ServerSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Suppress("UNUSED_PARAMETER")
@Composable
fun NearbyShareScreen(
    database: AppDatabase,
    scheduler: ReminderScheduler,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = comicColors()
    val scope = rememberCoroutineScope()

    var isSendMode by remember { mutableStateOf(true) }
    var server by remember { mutableStateOf<ServerSocket?>(null) }
    var serverLog by remember { mutableStateOf("") }
    var serverLogIsError by remember { mutableStateOf(false) }
    var receiveIP by remember { mutableStateOf("") }
    var isReceiving by remember { mutableStateOf(false) }
    var resultMsg by remember { mutableStateOf<String?>(null) }
    var resultIsError by remember { mutableStateOf(false) }
    var ipAddress by remember { mutableStateOf(NearbyShareService.localIpAddress()) }
    var showScanner by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) showScanner = true
        else Toast.makeText(context, zh("需要相机权限才能扫码"), Toast.LENGTH_SHORT).show()
    }

    val qrBitmap = remember(ipAddress) {
        if (ipAddress != null) {
            QrCodeUtils.generateQrBitmap("http://$ipAddress:${NearbyShareService.PORT}${NearbyShareService.PATH}")
        } else null
    }

    DisposableEffect(Unit) { onDispose { NearbyShareService.stopServer(server) } }

    Column(modifier = Modifier.fillMaxSize().background(colors.canvas).navigationBarsPadding()) {
        ComicTopBar(title = zh("附近传输"), onBack = onBack)

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            listOf(true to zh("发送"), false to zh("接收")).forEach { (send, label) ->
                val active = isSendMode == send
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { isSendMode = send }
                ) {
                    Text(
                        text = label,
                        color = if (active) colors.ink else colors.muted,
                        fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Box(Modifier.width(22.dp).height(2.dp).background(if (active) colors.accent else colors.canvas))
                }
            }
        }
        ComicHairline(isInner = false)

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isSendMode) {
                ComicCard(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(zh("两台设备连接同一 Wi-Fi 后即可互传"), color = colors.muted, fontSize = 12.5.sp)
                    if (ipAddress != null && qrBitmap != null) {
                        Text(zh("本机地址"), color = colors.muted, fontSize = 12.sp)
                        Text(
                            "http://$ipAddress:${NearbyShareService.PORT}",
                            color = colors.ink,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = zh("分享二维码"),
                                modifier = Modifier.size(196.dp).background(Color.White, RoundedCornerShape(8.dp)).padding(8.dp)
                            )
                        }
                        Text(zh("对方用「扫码接收」扫这里，自动开始接收"), color = colors.muted, fontSize = 12.sp)
                    } else {
                        Text(zh("无法获取局域网 IP，请确认已连接 Wi-Fi"), color = colors.danger, fontSize = 13.sp)
                    }
                    if (serverLog.isNotEmpty()) {
                        Text(serverLog, color = if (serverLogIsError) colors.danger else colors.ok, fontSize = 12.5.sp)
                    }
                    ComicHairline(isInner = true)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        if (server != null) {
                            ComicTextAction(
                                text = zh("停止共享"),
                                danger = true,
                                fontSize = 14.sp,
                                onClick = { NearbyShareService.stopServer(server); server = null }
                            )
                        } else {
                            ComicButton(
                                text = zh("开始共享当前提醒"),
                                onClick = {
                                    serverLog = ""; serverLogIsError = false
                                    server = NearbyShareService.startServer(
                                        jsonProvider = { runBlocking(Dispatchers.IO) { BackupService.exportDatabase(database) } },
                                        onEvent = { msg, isErr -> scope.launch { serverLog = msg; serverLogIsError = isErr } }
                                    )
                                    ipAddress = NearbyShareService.localIpAddress()
                                }
                            )
                        }
                    }
                }
            } else {
                ComicCard(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(zh("扫发送方二维码，或手动输入地址"), color = colors.muted, fontSize = 12.5.sp)
                    ComicTextAction(
                        text = "› " + zh("扫码接收（扫对方二维码）"),
                        accent = true,
                        fontSize = 14.sp,
                        onClick = {
                            val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            if (hasCam) showScanner = true else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    )
                    ComicHairline(isInner = true)
                    OutlinedTextField(
                        value = receiveIP,
                        onValueChange = { receiveIP = it },
                        label = { Text(zh("对方 IP 地址")) },
                        placeholder = { Text("192.168.1.100") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (isReceiving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.accent)
                            Spacer(Modifier.width(8.dp))
                            Text(zh("正在下载..."), color = colors.muted, fontSize = 13.sp)
                        } else {
                            ComicButton(
                                text = zh("下载并导入"),
                                enabled = receiveIP.isNotBlank() && !isReceiving,
                                onClick = {
                                    receiveNearbyReminders(
                                        hostInput = receiveIP,
                                        database = database,
                                        context = context,
                                        scope = scope,
                                        setReceiving = { isReceiving = it },
                                        setResult = { msg, isErr -> resultMsg = msg; resultIsError = isErr }
                                    )
                                }
                            )
                        }
                    }
                    resultMsg?.let { msg ->
                        if (msg.isNotEmpty()) {
                            Text(msg, color = if (resultIsError) colors.danger else colors.ok, fontSize = 12.5.sp)
                        }
                    }
                }
            }
        }
    }

    NearbyQrScannerDialog(
        visible = showScanner,
        onScanned = { value ->
            showScanner = false
            val host = runCatching { Uri.parse(value).host ?: value }.getOrDefault(value)
            receiveNearbyReminders(
                hostInput = host,
                database = database,
                context = context,
                scope = scope,
                setReceiving = { isReceiving = it },
                setResult = { msg, isErr -> resultMsg = msg; resultIsError = isErr }
            )
        },
        onDismiss = { showScanner = false }
    )
}
