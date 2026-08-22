package com.reminderapp.service

import java.text.SimpleDateFormat
import java.util.*

/**
 * 自然语言日期/时间解析（本地、无需 API）
 *
 * 支持示例：
 *  - 今天 / 明天 / 后天 / 大后天
 *  - 周一 / 下周一 / 下周日（周X/星期X/礼拜X）
 *  - 每月15号 / 每月5日
 *  - 9月5号 / 1月14日（公历生日）
 *  - 农历八月初五 / 旧历8月11
 *  - 每天 / 每周 / 每月 / 每年
 *  - 明早9点 / 下午3点 / 晚上8点半 / 09:30
 *  - 3天后 / 2小时后 / 30分钟后 / 2周后
 *
 * 返回结构化的下次触发时间与重复模式。
 */
object NaturalDateParser {

    data class ParsedSchedule(
        val nextTriggerAt: Long,
        val repeatMode: String,   // once | daily | weekly | biweekly | monthly | quarterly | yearly | lunar
        val dateType: String?,    // null | solar_birthday | lunar_birthday
        val targetMonth: Int?,
        val targetDay: Int?,
        val title: String,
        val label: String,
        // v2.4.9: 手动创建对齐 AI——周几（1=周一..7=周日，weekly 用）与避开节假日/周末
        val weekday: Int? = null,
        val holidayAware: Boolean = false
    )

    private val weekdayMap = mapOf(
        "一" to 1, "二" to 2, "三" to 3, "四" to 4, "五" to 5, "六" to 6, "日" to 7, "天" to 7,
        "1" to 1, "2" to 2, "3" to 3, "4" to 4, "5" to 5, "6" to 6, "7" to 7
    )

    fun parse(input: String, now: Long = System.currentTimeMillis()): ParsedSchedule? {
        val text = input.trim()
        if (text.isEmpty()) return null

        var hour = 9
        var minute = 0
        var repeatMode = "once"
        var weekday: Int? = null
        var dateType: String? = null
        var targetMonth: Int? = null
        var targetDay: Int? = null

        val cal = Calendar.getInstance().apply { timeInMillis = now }

        // ---------- 重复模式 ----------
        when {
            text.contains("农历") || text.contains("旧历") || text.contains("阴历") -> {
                repeatMode = "lunar"; dateType = "lunar_birthday"
            }
            text.contains("每天") || text.contains("每日") || text.contains("天天") -> repeatMode = "daily"
            text.contains("每年") || text.contains("年年") -> repeatMode = "yearly"
            text.contains("每季度") || text.contains("每个季度") || text.contains("每季") -> repeatMode = "quarterly"
            text.contains("每两周") || text.contains("每二个礼拜") || text.contains("每2周") || text.contains("双周") -> repeatMode = "biweekly"
            text.contains("每月") || text.contains("每个月") -> repeatMode = "monthly"
            text.contains("每周") || text.contains("每星期") || text.contains("每礼拜") -> repeatMode = "weekly"
        }

        // ---------- 时间 ----------
        // 显式 HH:mm 或 H点M分
        val hmReg = Regex("""(\d{1,2})[:：](\d{2})""").find(text)
        if (hmReg != null) {
            hour = hmReg.groupValues[1].toInt().coerceIn(0, 23)
            minute = hmReg.groupValues[2].toInt().coerceIn(0, 59)
        } else {
            val dianReg = Regex("""(\d{1,2})\s*点\s*(?:(\d{1,2})\s*分?)?""").find(text)
            if (dianReg != null) {
                var h = dianReg.groupValues[1].toInt().coerceIn(0, 23)
                val m = if (dianReg.groupValues[2].isNotEmpty()) dianReg.groupValues[2].toInt().coerceIn(0, 59) else 0
                // 下午/晚上 且未超过12 → +12
                if ((text.contains("下午") || text.contains("晚上") || text.contains("傍晚")) && h < 12) h += 12
                if (text.contains("中午") && h <= 12) h = 12
                hour = h; minute = m
            } else {
                // 仅时段词
                when {
                    text.contains("中午") -> hour = 12
                    text.contains("下午") || text.contains("晚上") || text.contains("傍晚") -> hour = 15
                    text.contains("凌晨") -> hour = 5
                    text.contains("早上") || text.contains("上午") -> hour = 9
                }
            }
        }
        // 半小时
        if (text.contains("半") && Regex("""\d{1,2}\s*点""").containsMatchIn(text) && minute == 0) minute = 30

        // ---------- 日期锚点 ----------
        // 相对天数
        val dayAfter = Regex("""(\d{1,2})\s*天[后以後]""").find(text)
        val hourAfter = Regex("""(\d{1,2})\s*小时[后以後]""").find(text)
        val minAfter = Regex("""(\d{1,2})\s*分钟[后以後]""").find(text)
        val weekAfter = Regex("""(\d{1,2})\s*周[后以後]""").find(text)

        if (dayAfter != null) {
            cal.add(Calendar.DAY_OF_MONTH, dayAfter.groupValues[1].toInt())
        } else if (weekAfter != null) {
            cal.add(Calendar.DAY_OF_MONTH, weekAfter.groupValues[1].toInt() * 7)
        } else if (hourAfter != null) {
            cal.add(Calendar.HOUR_OF_DAY, hourAfter.groupValues[1].toInt())
        } else if (minAfter != null) {
            cal.add(Calendar.MINUTE, minAfter.groupValues[1].toInt())
        } else if (text.contains("大后天")) {
            cal.add(Calendar.DAY_OF_MONTH, 3)
        } else if (text.contains("后天")) {
            cal.add(Calendar.DAY_OF_MONTH, 2)
        } else if (text.contains("明天") || text.contains("明日") || text.contains("明早") || text.contains("明晚")) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
        } else if (text.contains("昨天") || text.contains("昨日")) {
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }
        // 今天/今日：不偏移

        // 周X / 星期X / 礼拜X（可带"下"）
        val wdReg = Regex("""(?:下|上)?(?:周|星期|礼拜)([一二三四五六日天1-7])""").find(text)
        if (wdReg != null) {
            val target = weekdayMap[wdReg.groupValues[1]] ?: 1
            val nextWeek = wdReg.value.startsWith("下")
            val previousWeek = wdReg.value.startsWith("上")
            val curDow = dowFromCalendar(cal)
            var diff = (target - curDow + 7) % 7
            if (nextWeek) diff += 7
            if (previousWeek) diff -= 7
            if (!nextWeek && !previousWeek && diff == 0) diff = 7 // 本周已过的同一天 → 下周
            cal.add(Calendar.DAY_OF_MONTH, diff)
            if (repeatMode == "once") repeatMode = "weekly"
            weekday = target
        }

        // 每月X号
        val monthDayReg = Regex("""每月\s*(\d{1,2})\s*[号日]""").find(text)
        if (monthDayReg != null) {
            targetDay = monthDayReg.groupValues[1].toInt().coerceIn(1, 31)
            repeatMode = "monthly"
        }

        // 公历 X月X日 / X月X号（生日）
        val solarReg = Regex("""(\d{1,2})\s*月\s*(\d{1,2})\s*[号日]""").find(text)
        if (solarReg != null && repeatMode != "lunar") {
            targetMonth = solarReg.groupValues[1].toInt().coerceIn(1, 12)
            targetDay = solarReg.groupValues[2].toInt().coerceIn(1, 31)
            repeatMode = "yearly"; dateType = "solar_birthday"
            // 设置为当年该日期
            cal.set(Calendar.MONTH, targetMonth!! - 1)
            cal.set(Calendar.DAY_OF_MONTH, targetDay!!)
        }

        // 公历数字简写 2.10 / 2-10 / 2/10。紧凑无分隔格式（211）歧义太大
        // （2/11 还是 21/1 无从判断），不在此处理——AI 入口会反问确认
        val solarShortReg = Regex("""(?<![0-9.])(\d{1,2})[.\-/](\d{1,2})(?![0-9.\-/])""").find(text)
        if (solarShortReg != null && repeatMode != "lunar" && solarReg == null) {
            targetMonth = solarShortReg.groupValues[1].toInt().coerceIn(1, 12)
            targetDay = solarShortReg.groupValues[2].toInt().coerceIn(1, 31)
            repeatMode = "yearly"; dateType = "solar_birthday"
            cal.set(Calendar.MONTH, targetMonth!! - 1)
            cal.set(Calendar.DAY_OF_MONTH, targetDay!!)
        }

        // 农历 X月X
        val lunarReg = Regex("""(?:农历|旧历|阴历)\s*(\d{1,2})\s*月\s*(\d{1,2})""").find(text)
        if (lunarReg != null) {
            val lm = lunarReg.groupValues[1].toInt().coerceIn(1, 12)
            val ld = lunarReg.groupValues[2].toInt().coerceIn(1, 30)
            val solar = LunarCalendar.lunarToSolar(cal.get(Calendar.YEAR), lm, ld)
            if (solar != null) {
                cal.timeInMillis = solar
                targetMonth = lm; targetDay = ld
                repeatMode = "lunar"; dateType = "lunar_birthday"
            }
        }

        // 应用时分
        if (hourAfter == null && minAfter == null) {
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
        } else {
            // 相对小时后/分钟后：保留已加的时间，仅清秒
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
        }

        var next = cal.timeInMillis
        // 一次性且过去 → 顺延到明天同时刻（避免立即触发）
        if (repeatMode == "once" && next <= now) {
            val c2 = Calendar.getInstance().apply { timeInMillis = next }
            c2.add(Calendar.DAY_OF_MONTH, 1)
            next = c2.timeInMillis
        }

        // v2.4.9: 避开节假日/周末——显式关键词优先，其次事务类关键词自动开启
        val holidayAware = when {
            text.contains("避开节假日") || text.contains("顺延") || text.contains("工作日") -> true
            text.contains("报税") || text.contains("缴费") || text.contains("还款") ||
                text.contains("办证") || text.contains("开会") || text.contains("取件") ||
                text.contains("办事") || text.contains("银行") || text.contains("上班") -> true
            else -> false
        }

        val title = extractTitle(text)
        val label = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(next))
        return ParsedSchedule(next, repeatMode, dateType, targetMonth, targetDay, title, label, weekday, holidayAware)
    }

    private fun dowFromCalendar(cal: Calendar): Int {
        val c = cal.get(Calendar.DAY_OF_WEEK) // 1=Sunday
        return if (c == 1) 7 else c - 1
    }

    private fun extractTitle(text: String): String {
        var t = text
        // 日期/时间片段
        t = Regex("""(\d{1,2})\s*[:：]\s*\d{2}""").replace(t, " ")
        t = Regex("""(\d{1,2})\s*点\s*\d{0,2}\s*分?半?""").replace(t, " ")
        t = Regex("""(今天|今日|明天|明日|明早|明晚|后天|大后天|昨天|昨日)""").replace(t, " ")
        t = Regex("""(每)?(下|上)?(周|星期|礼拜)[一二三四五六日天1-7]""").replace(t, " ")
        t = Regex("""每月\s*\d{1,2}\s*[号日]""").replace(t, " ")
        t = Regex("""\d{1,2}\s*月\s*\d{1,2}\s*[号日]""").replace(t, " ")
        t = Regex("""(?<![0-9.])\d{1,2}[.\-/]\d{1,2}(?![0-9.\-/])""").replace(t, " ")
        t = Regex("""(农历|旧历|阴历)\s*\d{1,2}\s*月\s*\d{1,2}""").replace(t, " ")
        t = Regex("""(早上|上午|中午|下午|晚上|凌晨|傍晚)""").replace(t, " ")
        t = Regex("""\d{1,3}\s*(天|周|小时|分钟)\s*[后以後]""").replace(t, " ")
        t = Regex("""(每天|每日|天天|每年|年年|每周|每星期|每礼拜|每月|每个月)""").replace(t, " ")
        // 常见口语前缀
        t = Regex("""(提醒我|提醒|帮我|记得|设置|设定|闹钟|我想|让我|请)""").replace(t, " ")
        t = Regex("""\s+""").replace(t, " ")
        return t.trim().ifEmpty { "提醒" }
    }
}
