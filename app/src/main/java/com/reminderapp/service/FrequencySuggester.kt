package com.reminderapp.service

import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.data.entity.ReminderRecordEntity
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf
import com.reminderapp.model.Cycle
import kotlin.math.roundToInt

/**
 * 智能频率建议（功能8）——两者结合：
 *  1) 同类（标题字符级相似）提醒的历史频率；
 *  2) 这些提醒的「确认」记录时间分布（打卡完成历史）。
 * 加权结合后给出 cycle + customDays 建议，并附可解释文案。
 *
 * 纯同步、无网络。调用方（创建页）在 IO 协程取好全部提醒与确认记录后传入。
 * 加权策略：打卡间隔更贴近真实行为 → 0.6 权重；同类历史频率 → 0.4 权重。
 */
object FrequencySuggester {

    data class Suggestion(
        val cycle: Cycle,
        val customDays: Int,
        val reason: String,
        /** false 表示无足够历史，给了默认「每周」 */
        val hasSuggestion: Boolean
    )

    /** 周期 → 标准天数（用于取平均/贴近） */
    private fun cycleDays(cycle: String, customDays: Int): Int = when (cycle.lowercase()) {
        "daily" -> 1
        "weekly" -> 7
        "biweekly" -> 14
        "monthly" -> 30
        "quarterly" -> 90
        "yearly" -> 365
        "custom" -> customDays.coerceAtLeast(1)
        else -> 0 // once / 其它视为无周期信号
    }

    private val CANONICAL = listOf(1, 7, 14, 30, 90, 365)

    /** 把任意天数贴近到最相近的标准周期；偏差 > 容差则建议 custom */
    private fun snapToCycle(days: Double): Pair<Cycle, Int> {
        if (days <= 0) return Cycle.WEEKLY to 7
        var best = CANONICAL.first()
        var bestDiff = Double.MAX_VALUE
        for (c in CANONICAL) {
            val d = kotlin.math.abs(c - days)
            if (d < bestDiff) { bestDiff = d; best = c }
        }
        // 容差：与最近标准点差距 > 该标准点的 35% → 走自定义（更贴合真实节奏）
        return if (bestDiff > best * 0.35) {
            val cd = days.roundToInt().coerceAtLeast(1)
            // I21: 四舍五入后若等价「每天」则归并为 DAILY，避免返回「每 1 天」自定义
            if (cd <= 1) Cycle.DAILY to 1 else Cycle.CUSTOM to cd
        } else when (best) {
            1 -> Cycle.DAILY to 1
            7 -> Cycle.WEEKLY to 7
            14 -> Cycle.BIWEEKLY to 14
            30 -> Cycle.MONTHLY to 30
            90 -> Cycle.QUARTERLY to 90
            365 -> Cycle.YEARLY to 365
            else -> Cycle.WEEKLY to 7
        }
    }

    /** 字符级 Jaccard 相似度（中英文通用，无需分词） */
    private fun similarity(a: String, b: String): Double {
        val sa = a.filter { !it.isWhitespace() }.toSet()
        val sb = b.filter { !it.isWhitespace() }.toSet()
        if (sa.isEmpty() || sb.isEmpty()) return 0.0
        val inter = sa.intersect(sb).size
        return inter.toDouble() / sa.union(sb).size
    }

    /**
     * @param title 当前正在编辑的标题（相似度基准）
     * @param reminders 全部提醒（找同类 + 取历史频率）
     * @param confirmMillisByReminder reminderId -> 该提醒「确认」记录时间戳(ms)列表
     */
    fun suggest(
        title: String,
        reminders: List<ReminderEntity>,
        confirmMillisByReminder: Map<Long, List<Long>>
    ): Suggestion {
        if (reminders.isEmpty()) {
            return Suggestion(Cycle.WEEKLY, 7, zh("暂无历史数据，已为你默认「每周」"), false)
        }

        // 1) 同类提醒（仅 cycle 类参与频率统计）
        val similar = reminders.filter { it.kind == "cycle" && similarity(title, it.title) >= 0.2 }
        val similarDays = similar.map { cycleDays(it.cycle, it.customDays) }.filter { it > 0 }

        // 2) 打卡历史间隔（天）：同类提醒优先，否则退化为全部 cycle 提醒
        val pool = if (similar.isNotEmpty()) similar else reminders.filter { it.kind == "cycle" }
        val intervals = mutableListOf<Double>()
        pool.forEach { r ->
            val times = (confirmMillisByReminder[r.id] ?: emptyList()).sorted()
            for (i in 1 until times.size) {
                val gap = (times[i] - times[i - 1]) / (24.0 * 3600 * 1000)
                if (gap in 0.5..400.0) intervals.add(gap)
            }
        }

        val fromHistory = if (similarDays.isNotEmpty()) modeOrMedian(similarDays) else null
        // I23: median 空输入返回 null（而非 0），直接作为可选值
        val fromCheckin = median(intervals)

        return when {
            fromHistory != null && fromCheckin != null -> {
                // 加权结合：打卡间隔 0.6，同类频率 0.4
                val blended = fromHistory * 0.4 + fromCheckin * 0.6
                val (cyc, cd) = snapToCycle(blended)
                // I22: fromHistory 非 null ⇒ similar 非空，similar.isEmpty 分支恒不可达，删死分支
                // I20: 实际是中位数，文案改为「中位间隔」
                val reason = zhf("参考 %1\$d 条同类提醒与你的打卡节奏（中位间隔 %2\$.1f 天），建议每 %3\$s",
                    similar.size, fromCheckin, label(cyc, cd))
                Suggestion(cyc, cd, reason, true)
            }
            fromHistory != null -> {
                val (cyc, cd) = snapToCycle(fromHistory.toDouble())
                // I22: fromHistory 非 null ⇒ similar 非空，similar.isEmpty 分支恒不可达，删死分支
                val reason = zhf("你有 %1\$d 条类似「%2\$s」的提醒多为每 %3\$s，建议保持一致",
                    similar.size, similar.first().title, label(cyc, cd))
                Suggestion(cyc, cd, reason, true)
            }
            fromCheckin != null -> {
                val (cyc, cd) = snapToCycle(fromCheckin)
                // I20: 实际是中位数，文案改为「中位间隔」
                Suggestion(cyc, cd, zhf("根据打卡记录（中位间隔 %1\$.1f 天），建议每 %2\$s", fromCheckin, label(cyc, cd)), true)
            }
            else -> Suggestion(Cycle.WEEKLY, 7, zh("暂无足够历史，已为你默认「每周」"), false)
        }
    }

    private fun label(cycle: Cycle, customDays: Int): String =
        if (cycle == Cycle.CUSTOM) zhf("每 %1\$d 天", customDays) else cycle.label

    // I23: 空输入返回 null（而非 0），避免未来误造「0 天」建议
    private fun median(xs: List<Double>): Double? {
        val s = xs.sorted()
        val n = s.size
        return if (n == 0) null else if (n % 2 == 1) s[n / 2] else (s[n / 2 - 1] + s[n / 2]) / 2.0
    }

    /** 有众数（出现≥2）取众数，否则取中位数 */
    private fun modeOrMedian(xs: List<Int>): Int {
        val freq = xs.groupingBy { it }.eachCount()
        val max = freq.maxByOrNull { it.value }?.value ?: 0
        if (max >= 2) {
            val modes = freq.filter { it.value == max }.keys.sorted()
            return if (modes.size == 1) modes.first() else median(modes.map { it.toDouble() })?.toInt() ?: 0
        }
        return median(xs.map { it.toDouble() })?.toInt() ?: 0
    }
}
