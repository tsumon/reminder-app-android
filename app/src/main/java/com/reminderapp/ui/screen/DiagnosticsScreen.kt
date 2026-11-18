package com.reminderapp.ui.screen

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.reminderapp.ReminderApp
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.receiver.NotificationActionReceiver
import com.reminderapp.service.AILogStore
import com.reminderapp.service.NotificationManager
import com.reminderapp.service.ReminderActions
import com.reminderapp.service.ReminderScheduler
import com.reminderapp.service.SyncStore
import com.reminderapp.ui.comic.ComicButton
import com.reminderapp.ui.comic.ComicHairline
import com.reminderapp.ui.comic.ComicTextAction
import com.reminderapp.ui.comic.ComicTopBar
import com.reminderapp.ui.comic.comicColors
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun DiagnosticsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = comicColors()
    val db = AppDatabase.getInstance(context)
    val scope = rememberCoroutineScope()
    var testMessage by remember { mutableStateOf("") }
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }

    var refreshKey by remember { mutableIntStateOf(0) }
    var total by remember { mutableIntStateOf(0) }
    var unconfirmed by remember { mutableIntStateOf(0) }
    var disabled by remember { mutableIntStateOf(0) }
    var critical by remember { mutableIntStateOf(0) }
    var upcoming by remember { mutableStateOf<List<ReminderEntity>>(emptyList()) }

    LaunchedEffect(refreshKey) {
        val all = db.reminderDao().getAllIncludingInactive()
        val active = all.filter { it.isActive }
        total = active.size
        unconfirmed = active.count { it.status != "confirmed" }
        disabled = all.count { !it.isActive }
        critical = active.count { it.isCritical }
        upcoming = active.filter { it.status != "confirmed" }.sortedBy { it.nextTriggerAt }.take(5)
    }

    val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
    val exact = ReminderScheduler(context).canScheduleExact()

    Column(modifier = Modifier.fillMaxSize().background(colors.canvas).navigationBarsPadding()) {
        ComicTopBar(
            title = zh("提醒诊断"),
            onBack = onBack,
            trailing = { ComicTextAction(text = zh("刷新"), accent = true, onClick = { refreshKey++ }) }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "diag-self-test") {
                SettingsCard {
                    Text(zh("提醒能力自检"), color = colors.ink, fontSize = 15.5.sp, fontWeight = FontWeight.ExtraBold)
                    Text(
                        if (exact) zh("精确闹钟已允许") else zh("精确闹钟未允许：目前使用非精确排期，可能延迟"),
                        color = if (exact) colors.ok else colors.warn,
                        fontSize = 13.sp, fontWeight = FontWeight.Bold
                    )
                    Text(
                        zh("普通通知受系统权限、通知渠道、静音及勿扰影响。重要提醒也需要相应系统授权。"),
                        color = colors.muted, fontSize = 12.sp
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ComicTextAction(
                            text = zh("设置精确闹钟权限"), accent = true,
                            onClick = {
                                val action = if (Build.VERSION.SDK_INT >= 31) Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                                else Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                context.startActivity(Intent(action, Uri.parse("package:${context.packageName}")))
                            }
                        )
                        ComicTextAction(
                            text = zh("设置通知权限与渠道"), accent = true,
                            onClick = {
                                context.startActivity(
                                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                )
                            }
                        )
                    }
                    ComicHairline(isInner = true)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        ComicButton(
                            text = zh("创建测试提醒"),
                            onClick = {
                                scope.launch {
                                    val at = System.currentTimeMillis() + 60_000
                                    val r = ReminderEntity(
                                        title = zh("提醒自检（1 分钟）"), cycle = "once",
                                        firstTriggerAt = at, nextTriggerAt = at, reminderStyle = "gentle"
                                    )
                                    ReminderActions(context, db).create(r)
                                    testMessage = zh("测试提醒已安排，请锁屏等待约 1 分钟；可在首页完成或删除。")
                                    refreshKey++
                                }
                            }
                        )
                    }
                    if (testMessage.isNotEmpty()) Text(testMessage, color = colors.ok, fontSize = 12.5.sp)
                }
            }

            item(key = "diag-metrics") {
                SettingsCard {
                    Text(zh("状态与数据"), color = colors.ink, fontSize = 15.5.sp, fontWeight = FontWeight.ExtraBold)
                    DiagRow(zh("通知权限"), if (notificationsEnabled) zh("已开启") else zh("已关闭"), notificationsEnabled)
                    DiagRow(zh("关键提醒"), zhf("%s 条", critical), true)
                    DiagRow(zh("提醒总数"), zhf("%s 条", total), true)
                    DiagRow(zh("未完成"), zhf("%s 条", unconfirmed), true)
                    DiagRow(zh("已停用"), zhf("%s 条", disabled), true)
                    DiagRow("WebDAV", if (SyncStore.isConfigured) zh("已配置") else zh("未配置"), SyncStore.isConfigured)
                }
            }

            item(key = "diag-upcoming-sim") {
                SettingsCard {
                    Text(zh("最近触发与调试"), color = colors.ink, fontSize = 15.5.sp, fontWeight = FontWeight.ExtraBold)
                    if (upcoming.isEmpty()) {
                        Text(zh("暂无待触发的提醒"), color = colors.muted, fontSize = 13.sp)
                    } else {
                        upcoming.forEach { r ->
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(r.title, color = colors.ink, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.weight(1f))
                                Text(dateFormat.format(Date(r.nextTriggerAt)), color = colors.muted, fontSize = 12.sp, style = TextStyle(fontFeatureSettings = "tnum"))
                            }
                        }
                        val target = upcoming.first()
                        ComicHairline(isInner = true)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            ComicTextAction(
                                text = zhf("模拟确认：%s", target.title), accent = true,
                                onClick = {
                                    val intent = Intent(context, NotificationActionReceiver::class.java).apply {
                                        action = NotificationManager.ACTION_CONFIRM
                                        putExtra(NotificationManager.EXTRA_REMINDER_ID, target.id)
                                        putExtra("expected_at", target.nextTriggerAt)
                                        putExtra("revision", target.revision)
                                        putExtra("expected_state", target.toString())
                                    }
                                    context.sendBroadcast(intent)
                                    Toast.makeText(context, zhf("已模拟确认：%s", target.title), Toast.LENGTH_SHORT).show()
                                    refreshKey++
                                }
                            )
                            ComicTextAction(
                                text = zhf("模拟稍后：%s", target.title),
                                onClick = {
                                    val intent = Intent(context, NotificationActionReceiver::class.java).apply {
                                        action = NotificationManager.ACTION_SNOOZE
                                        putExtra(NotificationManager.EXTRA_REMINDER_ID, target.id)
                                        putExtra("expected_at", target.nextTriggerAt)
                                        putExtra("revision", target.revision)
                                        putExtra("expected_state", target.toString())
                                    }
                                    context.sendBroadcast(intent)
                                    Toast.makeText(context, zhf("已模拟稍后：%s", target.title), Toast.LENGTH_SHORT).show()
                                    refreshKey++
                                }
                            )
                        }
                    }
                }
            }

            item(key = "diag-logs-privacy") {
                val aiLogs = AILogStore.recent(context).take(5)
                val crashLog = runCatching {
                    File(ReminderApp.instance.filesDir, "telemetry/crash.log")
                        .takeIf { it.exists() }?.readLines()?.takeLast(3) ?: emptyList()
                }.getOrDefault(emptyList())

                SettingsCard {
                    Text(zh("最近 AI 调用"), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    if (aiLogs.isEmpty()) Text(zh("暂无 AI 调用记录"), color = colors.muted, fontSize = 12.5.sp)
                    aiLogs.forEach { e ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(e.timeText(), color = colors.muted, fontSize = 11.5.sp, style = TextStyle(fontFeatureSettings = "tnum"))
                            Spacer(Modifier.width(8.dp))
                            Text(e.model, color = colors.ink, fontSize = 12.5.sp, maxLines = 1, modifier = Modifier.weight(1f))
                            Text(
                                if (e.ok) zhf("%s 轮 · %sms", e.turns, e.durationMs) else zh("失败"),
                                color = if (e.ok) colors.muted else colors.danger,
                                fontSize = 11.5.sp, style = TextStyle(fontFeatureSettings = "tnum")
                            )
                        }
                    }
                    ComicHairline(isInner = true)
                    Text(zh("最近崩溃"), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    if (crashLog.isEmpty()) Text(zh("无崩溃记录"), color = colors.muted, fontSize = 12.5.sp)
                    crashLog.forEach { line -> Text(line.take(200), color = colors.danger, fontSize = 11.5.sp, maxLines = 3) }
                    ComicHairline(isInner = true)
                    Text(zh("隐私与数据"), color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        zh("提醒与完成记录保存在本机。使用 AI 时，对话及相关提醒内容会发往你配置的模型服务地址。启用 WebDAV 后，提醒备份上传至你配置的服务器。本地备份也可能出现在下载目录，请妥善保管。"),
                        color = colors.muted, fontSize = 11.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagRow(label: String, value: String, ok: Boolean) {
    val colors = comicColors()
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = colors.muted, fontSize = 13.5.sp, modifier = Modifier.weight(1f))
        Text(value, color = if (ok) colors.ink else colors.danger, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, style = TextStyle(fontFeatureSettings = "tnum"))
    }
}
