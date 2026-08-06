package com.reminderapp.service

import android.content.Context
import android.util.Log
import com.reminderapp.ReminderApp
import org.json.JSONObject
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 崩溃监控 + 埋点基础设施（v1.8.7 任务⑥）
 *
 * 先做本地基础设施，后续填 Bugly/AppCenter 的 AppID 即可启用真实上报：
 * - 崩溃捕获：Thread.setDefaultUncaughtExceptionHandler → 写崩溃日志文件
 * - 埋点事件：事件日志 JSON Lines 写本地文件（filesDir/telemetry/events.jsonl）
 * - 可插拔上报接口：CrashReporter / EventReporter，默认本地文件实现，
 *   未来替换为 Bugly/AppCenter 实现即可（无需改业务代码）
 */
object TelemetryService {

    // ── 可插拔上报接口 ──

    interface CrashReporter {
        /** 上报一次崩溃；返回 false 表示未消费（由默认处理器兜底崩溃流程） */
        fun reportCrash(thread: Thread, throwable: Throwable): Boolean
    }

    interface EventReporter {
        /** 上报一条埋点事件 */
        fun reportEvent(name: String, params: Map<String, String>)
    }

    /** 默认实现：写本地日志文件（不上传） */
    object LocalFileReporter : CrashReporter, EventReporter {
        override fun reportCrash(thread: Thread, throwable: Throwable): Boolean {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            writeLine(
                crashFile(),
                buildString {
                    append("{\"type\":\"crash\",\"time\":\"").append(now()).append("\"")
                    append(",\"thread\":\"").append(escape(thread.name)).append("\"")
                    append(",\"throwable\":\"").append(escape(sw.toString())).append("\"}")
                }
            )
            return false
        }

        override fun reportEvent(name: String, params: Map<String, String>) {
            val json = JSONObject().apply {
                put("type", "event")
                put("name", name)
                put("time", now())
                put("params", JSONObject(params))
            }
            writeLine(eventFile(), json.toString())
        }

        private fun crashFile(): File =
            File(ReminderApp.instance.filesDir, "telemetry/crash.log").apply { parentFile?.mkdirs() }

        private fun eventFile(): File =
            File(ReminderApp.instance.filesDir, "telemetry/events.jsonl").apply { parentFile?.mkdirs() }
    }

    /** 上报器（默认本地文件；接入 Bugly/AppCenter 时替换为云实现） */
    @Volatile
    var crashReporter: CrashReporter = LocalFileReporter

    @Volatile
    var eventReporter: EventReporter = LocalFileReporter

    // ── 安装 ──

    private var installed = false

    /** 在 Application.onCreate 调用一次 */
    @Synchronized
    fun install() {
        if (installed) return
        installed = true

        // 崩溃捕获：先写日志，再交回默认处理器（保证崩溃流程正常）
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                crashReporter.reportCrash(thread, throwable)
            } catch (e: Exception) {
                Log.e("Telemetry", "crash report failed", e)
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }

        logEvent("app_start")
        Log.i("Telemetry", "崩溃监控+埋点已安装（本地文件模式，可插拔上报）")
    }

    // ── 埋点 ──

    /** 记录一条埋点事件（业务代码调用，如 confirm / snooze / reminder_created） */
    fun logEvent(name: String, params: Map<String, String> = emptyMap()) {
        try {
            eventReporter.reportEvent(name, params)
        } catch (e: Exception) {
            Log.w("Telemetry", "event log failed: $e")
        }
    }

    // ── 工具 ──

    private fun now(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).format(Date())

    private fun writeLine(file: File, line: String) {
        try {
            file.appendText(line + "\n")
        } catch (_: Exception) {
        }
    }

    private fun escape(s: String): String = s
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
}
