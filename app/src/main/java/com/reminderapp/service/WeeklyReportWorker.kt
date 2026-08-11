package com.reminderapp.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.reminderapp.ReminderApp
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import java.util.Calendar

/**
 * 批次2 功能3: 统计周报推送
 *
 * WorkManager 周期任务（每天跑一次，initialDelay 对齐 20:00）：判定「应报告的那一周」，
 * 计算该周（周一 00:00 ~ 下周一 00:00）确认/漏掉/完成率 + 全量连续打卡天数，
 * 发一条周报通知，并以 ISO 周 key 记录已发防止重复。
 *
 * 为什么是每天跑而不是 7 天周期（v2.0.21 修 G1）：WorkManager 周期任务触发有弹性，
 * Doze/省电可能把周日 20:00 的任务推迟到周一。7 天周期 + 「非周日直接 return」会让
 * 本周周报永久漏发（去重 key 还没写就退出了）。改为每天跑 + 周一~周三补发上一周，
 * 系统漂移下不漏不重。
 */
class WeeklyReportWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        const val UNIQUE_NAME = "weekly_report"
        private const val PREFS_KEY_LAST_REPORT = "last_weekly_report_week"
        /** 补发窗口：周日任务被延迟时，周一~周三仍补发上一周（再晚数据已过时，不打扰） */
        private const val MAKEUP_MAX_DAYS = 3
    }

    override fun doWork(): Result {
        val cal = Calendar.getInstance()
        // G1: 算出「本轮应报告哪一周」（null = 现在不该发）
        val weekStart = targetWeekStart(cal) ?: return Result.success()

        // 去重 key 取自被报告的那一周（而非"今天"），补发时才不会串周
        val weekKey = isoWeekKey(weekStart)
        val prefs = applicationContext.getSharedPreferences("weekly_report", Context.MODE_PRIVATE)
        if (prefs.getString(PREFS_KEY_LAST_REPORT, null) == weekKey) {
            return Result.success() // 该周已发过
        }

        val db = AppDatabase.getInstance(applicationContext)
        val recordDao = db.reminderRecordDao()
        val records = kotlinx.coroutines.runBlocking { recordDao.getAll() }
        if (records.isEmpty()) {
            // 没有任何操作记录：不打扰用户
            prefs.edit().putString(PREFS_KEY_LAST_REPORT, weekKey).apply()
            return Result.success()
        }

        // 上界必须有：补发上一周时不能把本周的记录也算进去
        val weekEnd = weekStart + 7L * 24 * 60 * 60 * 1000
        val weekRecords = records.filter { it.timestamp >= weekStart && it.timestamp < weekEnd }
        val weekSummary = StatsService.summarize(weekRecords)
        val fullSummary = StatsService.summarize(records)

        val rateText = if (weekSummary.completionRate != null) {
            "${(weekSummary.completionRate * 100).toInt()}%"
        } else {
            zh("暂无")
        }

        // I12: 标题按「被报告的周」推算——周一~周三补发的是上一周，标题应为「上周」而非恒「本周」
        val nowCal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val weekLabel = if (weekStart < nowCal.timeInMillis) zh("上周") else zh("本周")
        val title = zhf("📊 %1\$s统计周报", weekLabel)
        val body = buildString {
            append(zhf("完成 %1\$d 项 · 漏掉 %2\$d 项", weekSummary.confirmCount, weekSummary.missedCount))
            append("\n")
            append(zhf("完成率 %1\$s", rateText))
            if (fullSummary.currentStreak > 0) {
                append("\n")
                append(zhf("连续打卡 %1\$d 天", fullSummary.currentStreak))
            }
            // 批次3 功能3: AI 解读——把数字变成一句可执行的洞察（未配置 AI 或调用失败则不追加）
            aiInsight(weekSummary, fullSummary, rateText)?.let {
                append("\n💡 ")
                append(it)
            }
        }

        // 点通知 → 打开 App（首页；统计 Tab 由用户从菜单进）
        NotificationManager(applicationContext).sendWeeklyReportNotification(title, body)

        // 记录本周已发（即使发送失败也标记，避免重复轰炸；下周日重新计算）
        prefs.edit().putString(PREFS_KEY_LAST_REPORT, weekKey).apply()
        return Result.success()
    }

    /**
     * 批次3 功能3: 周报 AI 解读。
     *
     * 把「完成/漏掉/连续/常忘时段」几个数字丢给已配置的 AI，换回一句可执行的短建议，
     * 追加到周报通知末尾。设计上是**纯增量**：
     * - 未配置 API Key → 直接返回 null，周报维持原样；
     * - 超时（25s）或任何异常 → 返回 null，绝不让周报因为 AI 挂掉而发不出去；
     * - 结果截断到 80 字，避免模型话痨把通知撑爆。
     */
    private fun aiInsight(
        week: StatsService.Summary,
        full: StatsService.Summary,
        rateText: String
    ): String? {
        val settings = AISettings(applicationContext)
        if (!settings.isConfigured) return null

        val forget = week.forgetHours.take(2).joinToString("、") {
            zhf("%1\$d 点(%2\$d 次)", it.first, it.second)
        }
        val facts = buildString {
            append(zhf("本周完成 %1\$d 项，漏掉 %2\$d 项，完成率 %3\$s。", week.confirmCount, week.missedCount, rateText))
            append(zhf("当前连续打卡 %1\$d 天，历史最长 %2\$d 天。", full.currentStreak, full.longestStreak))
            if (forget.isNotEmpty()) append(zhf("最常漏掉的时段：%1\$s。", forget))
        }
        val systemPrompt = zh(
            "你是一位简洁克制的习惯教练。根据用户本周的提醒完成数据，用中文写一句 40 字以内的解读：" +
                "先点评趋势，再给一条具体可执行的建议。不要寒暄、不要分点、不要复述原始数字。"
        )

        return try {
            kotlinx.coroutines.runBlocking {
                kotlinx.coroutines.withTimeoutOrNull(25_000L) {
                    AIService().complete(
                        model = settings.model,
                        messages = listOf(
                            mapOf("role" to "system", "content" to systemPrompt),
                            mapOf("role" to "user", "content" to facts)
                        ),
                        endpoint = settings.apiEndpoint,
                        apiKey = settings.apiKey
                    )
                }
            }?.takeIf { it.isNotBlank() }?.take(80)
        } catch (e: Exception) {
            android.util.Log.w("WeeklyReport", "AI 解读失败，跳过: ${e.message}")
            null
        }
    }

    /**
     * 判定本轮应报告的那一周（返回该周周一 00:00 的毫秒；null = 现在不该发）。
     *
     * - 周日 20:00 之后 → 报告本周（周一 ~ 今天）
     * - 周一 ~ 周三     → 周日的任务被系统延迟了，补发上一周
     * - 其余时段        → 不发
     */
    private fun targetWeekStart(now: Calendar): Long? {
        val dow = now.get(Calendar.DAY_OF_WEEK)
        if (dow == Calendar.SUNDAY) {
            if (now.get(Calendar.HOUR_OF_DAY) < 20) return null // 今天还没到点
            return startOfWeek(now.timeInMillis)
        }
        // 距上一个周日的天数（周一=1 … 周六=6）；超过补发窗口不再发
        val daysSinceSunday = when (dow) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            else -> return null
        }
        if (daysSinceSunday > MAKEUP_MAX_DAYS) return null
        val c = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -daysSinceSunday) }
        // 回到上一个周日后再取所在周的周一 → 上一整周
        return startOfWeek(c.timeInMillis)
    }

    /**
     * ISO-8601 周标识：2026-W33（跨年去重用）。
     *
     * v2.0.21 修 G2：原实现直接取 `Calendar.WEEK_OF_YEAR`，其周起始日与「第 1 周」定义随
     * Locale 变化（中国区默认周日起、minimalDays=1），跨年边界会与 ISO 错位造成多发/漏发。
     * 这里显式设 firstDayOfWeek=MONDAY + minimalDaysInFirstWeek=4，并以该周周四所属年份
     * 作为 ISO 年（标准做法），保证 key 全球一致且跨年不串。
     */
    private fun isoWeekKey(weekStartMillis: Long): String {
        val c = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            minimalDaysInFirstWeek = 4
            timeInMillis = weekStartMillis
        }
        val week = c.get(Calendar.WEEK_OF_YEAR)
        // ISO 年 = 该周周四所在年份（weekStart 是周一，+3 天即周四）
        val thursday = (c.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 3) }
        return String.format(java.util.Locale.US, "%d-W%02d", thursday.get(Calendar.YEAR), week)
    }

    private fun startOfWeek(now: Long): Long {
        val c = Calendar.getInstance().apply { timeInMillis = now }
        // 周一到周日为统计窗口（对齐 StatsService 的按天口径）
        while (c.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            c.add(Calendar.DAY_OF_MONTH, -1)
        }
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
}
