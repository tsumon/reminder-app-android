package com.reminderapp.service

import android.content.Context
import android.util.Log
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * 联网节假日数据服务（v1.8.7 任务②）— 镜像 iOS HolidayRemoteService.swift
 *
 * 拉取官方口径的法定节假日安排（放假 + 调休上班日），供首页/日历卡显示「休/班」。
 * - 数据源：holiday-cn（NateScarlet/holiday-cn，GitHub 官方口径 JSON，免 key 无反爬）
 *   GET https://raw.githubusercontent.com/NateScarlet/holiday-cn/master/{year}.json
 *   结构：{ "days": [ { "name": "元旦", "date": "2026-01-01", "isOffDay": true }, ... ] }
 * - 缓存：按年存 SharedPreferences（key=yyyy-MM-dd，value="isOffDay|name"）
 * - 离线/失败：保留旧缓存，UI 静默降级；提醒功能继续走内置 HolidayService，互不影响
 */
object HolidayRemoteService {

    data class DayStatus(val isHoliday: Boolean, val name: String)

    private const val BASE_URL = "https://raw.githubusercontent.com/NateScarlet/holiday-cn/master"
    private const val CACHE_KEY_PREFIX = "remote_holiday_cache_"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    /** 查询某公历日期（yyyy-MM-dd）的节假日状态；无数据（普通工作日/未拉到）返回 null */
    fun status(context: Context, year: Int, month: Int, day: Int): DayStatus? {
        val key = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day)
        val prefs = context.getSharedPreferences(CACHE_KEY_PREFIX + year, Context.MODE_PRIVATE)
        val raw = prefs.getString(key, null) ?: return null
        val parts = raw.split("|", limit = 2)
        if (parts.size != 2) return null
        return DayStatus(isHoliday = parts[0] == "true", name = parts[1])
    }

    /** 拉取指定年份的节假日安排并缓存到本地；失败静默（保留旧缓存，UI 降级） */
    suspend fun refresh(context: Context, year: Int) = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("$BASE_URL/$year.json").build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext
                val body = resp.body?.string() ?: return@withContext
                val json = try {
                    JsonParser.parseString(body).asJsonObject
                } catch (e: Exception) {
                    Log.i("HolidayRemote", "$year 响应解析失败: ${e.message}")
                    return@withContext
                }
                val days = json.getAsJsonArray("days") ?: return@withContext
                val edit = context.getSharedPreferences(CACHE_KEY_PREFIX + year, Context.MODE_PRIVATE).edit()
                var count = 0
                for (el in days) {
                    val obj = el.asJsonObject
                    val name = obj.get("name")?.asString ?: continue
                    val date = obj.get("date")?.asString ?: continue
                    val isOffDay = obj.get("isOffDay")?.asBoolean ?: continue
                    if (date.startsWith("$year-")) {
                        edit.putString(date, "$isOffDay|$name")
                        count++
                    }
                }
                edit.apply()
                Log.i("HolidayRemote", "$year 节假日数据已缓存 $count 条")
            }
        } catch (e: Exception) {
            // 离线/接口失败：保留旧缓存，不打扰用户
            Log.i("HolidayRemote", "$year 刷新失败，使用缓存/降级: ${e.message}")
        }
    }
}
