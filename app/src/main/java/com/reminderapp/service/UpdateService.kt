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
    /// 检查源：releases.atom（HTML 域名走 CDN，无匿名 API 限流；api.github.com 常被限流 403）
    private const val ATOM = "https://github.com/$REPO/releases.atom"
    /// 下载路径：latest/download 固定名（无需版本号/API，永远指向最新 Release 的该资产）
    private const val APK_ASSET_URL = "https://github.com/$REPO/releases/latest/download/app-release.apk"
    /// 兜底：api.github.com（可能限流，仅当 atom 不可用时尝试）
    private const val API = "https://api.github.com/repos/$REPO/releases/latest"

    data class UpdateInfo(
        val latestVersion: String,   // 去 v 前缀，如 "1.8.7"
        val apkUrl: String?,         // release 里第一个 .apk 资产
        val releaseUrl: String
    )

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    /** 当前 App 版本（BuildConfig.VERSION_NAME） */
    fun currentVersion(): String = com.reminderapp.BuildConfig.VERSION_NAME

    /** 检查最新 release；失败返回 null（离线/网络异常静默降级，可重试） */
    suspend fun checkLatest(): UpdateInfo? = withContext(Dispatchers.IO) {
        // 优先 releases.atom（不限流），重试 1 次；仍失败再兜底 api.github.com
        var result: UpdateInfo? = null
        for (attempt in 0..1) {
            result = tryFetchAtom()
            if (result != null) break
        }
        result ?: tryFetchApi()
    }

    /** releases.atom：解析第一个 <entry> 的 title(=tag) 与 link(=release 页) */
    private fun tryFetchAtom(): UpdateInfo? {
        return try {
            val req = Request.Builder().url(ATOM)
                .header("User-Agent", "reminder-app-android")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body?.string() ?: return null
                parseAtom(body)
            }
        } catch (e: Exception) {
            Log.i("UpdateService", "atom 检查失败: ${e.message}")
            null
        }
    }

    private fun parseAtom(body: String): UpdateInfo? {
        val entry = Regex("<entry>(.*?)</entry>", RegexOption.DOT_MATCHES_ALL)
            .find(body)?.groupValues?.get(1) ?: return null
        val tag = Regex("<title[^>]*>(.*?)</title>", RegexOption.DOT_MATCHES_ALL)
            .find(entry)?.groupValues?.get(1)?.trim() ?: return null
        val link = Regex("href=\"([^\"]*releases/tag/[^\"]*)\"")
            .find(entry)?.groupValues?.get(1) ?: return null
        return UpdateInfo(
            latestVersion = tag.removePrefix("v"),
            apkUrl = APK_ASSET_URL,
            releaseUrl = link
        )
    }

    /** 兜底：api.github.com 解析（可能被限流 403） */
    private fun tryFetchApi(): UpdateInfo? {
        return try {
            val req = Request.Builder().url(API)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "reminder-app-android")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body?.string() ?: return null
                val json = JsonParser.parseString(body).asJsonObject
                val tag = json.get("tag_name")?.asString ?: return null
                val releaseUrl = json.get("html_url")?.asString ?: "https://github.com/$REPO/releases"
                UpdateInfo(
                    latestVersion = tag.removePrefix("v"),
                    apkUrl = APK_ASSET_URL,
                    releaseUrl = releaseUrl
                )
            }
        } catch (e: Exception) {
            Log.i("UpdateService", "API 检查失败: ${e.message}")
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
