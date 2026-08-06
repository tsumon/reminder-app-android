package com.reminderapp.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * GitHub 在线升级（v1.8.7 任务：在线升级）
 *
 * 检查 GitHub Releases 最新版本 → 对比当前版本 → 下载 APK → FileProvider 引导安装。
 * 版本号对齐：Android versionName / GitHub tag（如 v1.8.7）。
 */
object UpdateService {

    private const val REPO = "tsumon/reminder-app-android"
    private const val API = "https://api.github.com/repos/$REPO/releases/latest"

    data class UpdateInfo(
        val latestVersion: String,   // 去 v 前缀，如 "1.8.7"
        val apkUrl: String?,         // release 里第一个 .apk 资产
        val releaseUrl: String
    )

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /** 当前 App 版本（BuildConfig.VERSION_NAME） */
    fun currentVersion(): String = com.reminderapp.BuildConfig.VERSION_NAME

    /** 检查最新 release；失败返回 null（离线/限流静默降级） */
    suspend fun checkLatest(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url(API)
                .header("Accept", "application/vnd.github+json")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val body = resp.body?.string() ?: return@withContext null
                val json = JsonParser.parseString(body).asJsonObject
                val tag = json.get("tag_name")?.asString ?: return@withContext null
                val releaseUrl = json.get("html_url")?.asString ?: "https://github.com/$REPO/releases"
                // 找第一个 .apk 资产
                var apkUrl: String? = null
                json.getAsJsonArray("assets")?.forEach { el ->
                    val name = el.asJsonObject.get("name")?.asString ?: ""
                    if (name.endsWith(".apk") && apkUrl == null) {
                        apkUrl = el.asJsonObject.get("browser_download_url")?.asString
                    }
                }
                UpdateInfo(
                    latestVersion = tag.removePrefix("v"),
                    apkUrl = apkUrl,
                    releaseUrl = releaseUrl
                )
            }
        } catch (e: Exception) {
            Log.i("UpdateService", "检查更新失败: ${e.message}")
            null
        }
    }

    /** 语义化版本比较：latest > current ? */
    fun isNewer(latest: String, current: String): Boolean {
        val a = latest.split(".").mapNotNull { it.toIntOrNull() }
        val b = current.split(".").mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }

    /**
     * 下载 APK 到 cacheDir/update/ 并返回文件；失败抛异常
     */
    suspend fun downloadApk(context: Context, url: String): File = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "update").apply { mkdirs() }
        val file = File(dir, "reminder-update.apk")
        val req = Request.Builder().url(url).build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw IllegalStateException("下载失败 HTTP ${resp.code}")
            val body = resp.body ?: throw IllegalStateException("下载失败: 空响应")
            file.outputStream().use { out -> body.byteStream().copyTo(out) }
        }
        file
    }

    /** 引导安装（FileProvider + ACTION_VIEW；Android 8+ 需用户允许未知来源） */
    fun install(context: Context, apk: File) {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", apk
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    /** 打开 GitHub Releases 页（APK 资产缺失时的兜底） */
    fun openReleasePage(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
