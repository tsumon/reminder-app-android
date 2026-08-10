package com.reminderapp.service

import com.reminderapp.data.entity.ReminderRecordEntity
import java.util.Calendar

/**
 * 统计洞察聚合（v1.8.7 任务③）— 镜像 iOS StatsService.swift
 *
 * 数据完全本地：复用 ReminderRecordEntity（操作记录）按日聚合。
 * 口径（双端一致，v2.0.17 落文档防漂移）：
 * - 完成率 = 确认天数 / (确认天数 + 漏掉天数)
 * - 「漏掉」= 到点未确认进入重试才算（Worker escalate 写 notified / iOS escalateRetry 写 trigger）；
 *   按时确认**不**记漏 —— 否则每次发通知都记一条，完成率恒 ≈50%
 * - 连续打卡 = confirmed 记录按天去重后的连续天数（当前/最长）
 * - 最常忘记时段 = notified(未确认) 记录按小时分布 Top3
 * - 月历热力图 = 每月每天 confirmed 次数
 */
object StatsService {

    data class Summary(
        val confirmCount: Int,
        val missedCount: Int,
        /** 完成率 0-1；无数据时为 null */
        val completionRate: Double?,
        val currentStreak: Int,
        val longestStreak: Int,
        /** 最常忘记的时段（hour 0-23 降序，最多 3 条） */
        val forgetHours: List<Pair<Int, Int>>,
        /** 月历热力图："yyyy-MM-dd" -> confirmed 次数 */
        val heatmap: Map<String, Int>
    )

    fun summarize(records: List<ReminderRecordEntity>): Summary {
        val confirmTimes = records.filter { it.action == com.reminderapp.data.entity.ReminderRecordEntity.ACTION_CONFIRMED }.map { it.timestamp }
        val missed = records.filter { it.action == com.reminderapp.data.entity.ReminderRecordEntity.ACTION_NOTIFIED }

        val confirmDays = confirmTimes.map { startOfDay(it) }.toSet()
        val confirmCount = confirmDays.size
        // v1.9.6 fix: missed 按天去重——同一天多次重试/多次通知只算一次漏掉，
        // 否则「每次发通知都记 notified」会让按时确认的用户完成率只有 ~50%
        val missedCount = missed.map { startOfDay(it.timestamp) }.toSet().size
        val completionRate: Double? = if (confirmCount + missedCount > 0) {
            confirmCount.toDouble() / (confirmCount + missedCount)
        } else null

        val sorted = confirmDays.sorted()
        val streak = calcStreak(sorted)

        // 最常忘记时段：notified 按小时计数，Top3
        val hourCounts = mutableMapOf<Int, Int>()
        missed.forEach { r ->
            val h = Calendar.getInstance().apply { timeInMillis = r.timestamp }.get(Calendar.HOUR_OF_DAY)
            hourCounts[h] = (hourCounts[h] ?: 0) + 1
        }
        val forgetHours = hourCounts.entries
            .sortedWith(compareByDescending<Map.Entry<Int, Int>> { it.value }.thenBy { it.key })
            .take(3)
            .map { it.key to it.value }

        // 月历热力图
        val df = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val heatmap = mutableMapOf<String, Int>()
        confirmTimes.forEach { ts ->
            val key = df.format(java.util.Date(ts))
            heatmap[key] = (heatmap[key] ?: 0) + 1
        }

        return Summary(
            confirmCount = confirmCount,
            missedCount = missedCount,
            completionRate = completionRate,
            currentStreak = streak.first,
            longestStreak = streak.second,
            forgetHours = forgetHours,
            heatmap = heatmap
        )
    }

    /** 返回 (当前连续, 最长连续) */
    private fun calcStreak(sortedDays: List<Long>): Pair<Int, Int> {
        if (sortedDays.isEmpty()) return 0 to 0
        val cal = Calendar.getInstance()
        val today = startOfDay(cal.timeInMillis)
        cal.add(Calendar.DAY_OF_MONTH, -1)
        val yesterday = cal.timeInMillis

        // 当前连续：今天有记录从今天数，否则从昨天数（今天还没打卡不算断）
        var cursor = if (sortedDays.contains(today)) today else yesterday
        var current = 0
        while (sortedDays.contains(cursor)) {
            current++
            cursor = startOfDay(cursor - 24 * 3600 * 1000L)
        }

        // 最长连续
        var longest = 1
        var run = 1
        for (i in 1 until sortedDays.size) {
            val diffDays = ((sortedDays[i] - sortedDays[i - 1]) / (24 * 3600 * 1000L)).toInt()
            if (diffDays == 1) {
                run++
                if (run > longest) longest = run
            } else {
                run = 1
            }
        }
        return current to maxOf(longest, current)
    }

    private fun startOfDay(ts: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = ts }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
