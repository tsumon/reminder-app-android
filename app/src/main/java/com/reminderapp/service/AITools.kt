package com.reminderapp.service

/**
 * AI 工具定义 — Function Calling JSON Schema
 * 镜像 iOS AITools
 */
object AITools {

        /** v2.4.1: 当前日期上下文（模型不知道"今天"是几号，必须注入才能算对"下周日"） */
    private fun todayContext(): String {
        val cal = java.util.Calendar.getInstance()
        val weekdays = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
        val dow = cal.get(java.util.Calendar.DAY_OF_WEEK) - 1
        return String.format(
            "今天是 %%d年%%d月%%d日（%%s）。用户说「下周日」「明天」等相对时间时，必须基于这个日期计算 trigger_date（yyyy-MM-dd）。\n",
            cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH), weekdays[dow.coerceIn(0, 6)]
        )
    }

    val systemPrompt = "${todayContext()}" + """
        你是一个循环提醒助手，帮用户管理重复提醒事项。

        **核心规则：**
        - 用户描述提醒需求时 → 调用 create_reminder
        - 用户要确认某个提醒已完成 → 调用 confirm_reminder
        - 用户要推迟某个提醒 → 调用 snooze_reminder
        - 用户要删除提醒 → 调用 delete_reminder
        - 用户要修改/调整已有提醒（改标题、改周期、改时间、改备注等） → 调用 update_reminder（只传要改的字段，其余保留原值）
        - 用户问'有什么提醒''列表' → 调用 list_reminders

        **生日 / 日期提醒（重要）：**
        - "X生日:新历/公历 M月D号" → kind=date, date_type=solar_birthday, target_month=M, target_day=D
        - "X生日:旧历/农历/阴历 M月初D / M月D号" → kind=date, date_type=lunar_birthday, target_month=M, target_day=D
        - "每年""周年" → kind=date, date_type=solar_birthday
        - "春节/中秋/端午/清明/国庆/元旦" → kind=date, date_type=holiday, holiday_name=名称

        **批量创建（关键）：** 若用户一次给出多个生日，必须为**每个人、每个日期**分别调用一次 create_reminder（一次只创建一条）。v2.4.6 fix：同一人同时给出"新历/公历"和"旧历/农历"两个日期时，**必须创建两条**——一条 date_type=solar_birthday（title 后缀"（公历）"），一条 date_type=lunar_birthday（title 后缀"（农历）"），不可只取其一。只给了一个历法的日期时建对应一条。注意"旧历，12月18"这类用逗号代替冒号的写法也要解析。示例"老娘生日:新历1月14号，旧历12月18"→ 两条：〔老娘生日（公历）, solar 1/14〕+〔老娘生日（农历）, lunar 12/18〕。不要漏掉任何一个人或任何一个日期。

        **批量整理（关键）：** 若用户粘贴了一段包含多条待办的文字（聊天记录 / 便签 / 需求文档 / 多行清单），→ 调用 import_tasks，把每段解析为一条提醒（尽量补全 title / 周期 / 时间），批量预览确认后再创建。不要逐条调用 create_reminder。

        **周期提醒：**
        - 每天 → kind=cycle, cycle=daily
        - 每周/周一 → kind=cycle, cycle=weekly
        - 每月 → kind=cycle, cycle=monthly
        - 每年 → kind=cycle, cycle=yearly
        - 每 N 天 → kind=cycle, cycle=custom, custom_days=N

        **规则提醒（第N周周X，重要）：**
        - 每月/每季度/每年 第N周周X → kind=rule, rule_period=monthly/quarterly/yearly,
          rule_week=N(1-5), rule_weekday=周几(1=周一...7=周日)
        - 例「每季度第二周周二」→ kind=rule, rule_period=quarterly, rule_week=2, rule_weekday=2
        - 例「每年1、4、7、10月第一周周四报税」→ 这是每季度(1/4/7/10月)第一周周四：
          kind=rule, rule_period=quarterly, rule_week=1, rule_weekday=4
        - 「每月最后一个周五」等 → 近似用 rule_week=5（该月无第5周时自动跳过）

        **提醒时间：** 用 reminder_hour / reminder_minute 指定时分（默认 9:00）。日期类提醒会自动按目标月日计算，无需传 trigger_date。

        **回复风格：** 简洁友好，执行完告知创建了哪些提醒（例如"已创建 5 条生日提醒"）。
    """.trimIndent()

    /**
     * 返回 OpenAI 兼容格式的工具定义 JSON
     */
    fun toolDefinitions(): List<Map<String, Any>> = listOf(
        mapOf(
            "type" to "function",
            "function" to mapOf(
                "name" to "create_reminder",
                "description" to "创建一个新的循环提醒或日期提醒",
                "parameters" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "title" to mapOf("type" to "string", "description" to "提醒标题"),
                        "note" to mapOf("type" to "string", "description" to "备注"),
                        "kind" to mapOf("type" to "string", "enum" to listOf("cycle", "date", "rule"), "description" to "周期/日期/规则(第N周周X)"),
                        "cycle" to mapOf("type" to "string", "enum" to listOf("daily", "weekly", "biweekly", "monthly", "quarterly", "yearly", "custom", "once")),
                        "custom_days" to mapOf("type" to "integer"),
                        "weekday" to mapOf("type" to "integer", "description" to "【每周/每两周提醒必须填】用户指定的星期：1=周一…7=周日。例：「每周日打针」→ cycle=weekly, weekday=7；「每周三开会」→ weekday=3"),
                        "rule_period" to mapOf("type" to "string", "enum" to listOf("monthly", "quarterly", "yearly"), "description" to "规则提醒频率，如每季度/每年"),
                        "rule_week" to mapOf("type" to "integer", "description" to "规则提醒：第几周(1-5)"),
                        "rule_weekday" to mapOf("type" to "integer", "description" to "规则提醒：周几(1=周一...7=周日)"),
                        "date_type" to mapOf("type" to "string", "enum" to listOf("solar_birthday", "lunar_birthday", "holiday")),
                        "target_month" to mapOf("type" to "integer"),
                        "target_day" to mapOf("type" to "integer"),
                        "holiday_name" to mapOf("type" to "string", "description" to "节假日名称，如春节/中秋"),
                        "advance_days" to mapOf("type" to "integer"),
                        "reminder_hour" to mapOf("type" to "integer"),
                        "reminder_minute" to mapOf("type" to "integer"),
                        "trigger_date" to mapOf("type" to "string", "description" to "yyyy-MM-dd"),
                        "trigger_time" to mapOf("type" to "string", "description" to "HH:mm")
                    ),
                    "required" to listOf("title", "kind")
                )
            )
        ),
        mapOf(
            "type" to "function",
            "function" to mapOf(
                "name" to "list_reminders",
                "description" to "列出所有提醒",
                "parameters" to mapOf("type" to "object", "properties" to emptyMap<String, Any>())
            )
        ),
        mapOf(
            "type" to "function",
            "function" to mapOf(
                "name" to "confirm_reminder",
                "description" to "确认完成一个提醒",
                "parameters" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "title_keyword" to mapOf("type" to "string", "description" to "提醒标题关键词")
                    ),
                    "required" to listOf("title_keyword")
                )
            )
        ),
        mapOf(
            "type" to "function",
            "function" to mapOf(
                "name" to "snooze_reminder",
                "description" to "推迟一个提醒15分钟",
                "parameters" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "title_keyword" to mapOf("type" to "string")
                    ),
                    "required" to listOf("title_keyword")
                )
            )
        ),
        mapOf(
            "type" to "function",
            "function" to mapOf(
                "name" to "delete_reminder",
                "description" to "删除一个提醒",
                "parameters" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "title_keyword" to mapOf("type" to "string")
                    ),
                    "required" to listOf("title_keyword")
                )
            )
        ),
        mapOf(
            "type" to "function",
            "function" to mapOf(
                "name" to "update_reminder",
                "description" to "修改一个已有提醒的字段（不改的字段不要传，会保留原值）。用于：把XX改成每周二、把交房租时间改到10点、把XX备注改成XXX等。",
                "parameters" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "title_keyword" to mapOf("type" to "string", "description" to "提醒标题关键词，用于定位要修改的提醒"),
                        "new_title" to mapOf("type" to "string", "description" to "新的标题（可选）"),
                        "note" to mapOf("type" to "string", "description" to "新的备注（可选）"),
                        "cycle" to mapOf("type" to "string", "enum" to listOf("daily", "weekly", "biweekly", "monthly", "quarterly", "yearly", "custom", "once")),
                        "custom_days" to mapOf("type" to "integer"),
                        "weekday" to mapOf("type" to "integer", "description" to "【每周/每两周提醒必须填】用户指定的星期：1=周一…7=周日。例：「每周日打针」→ cycle=weekly, weekday=7；「每周三开会」→ weekday=3"),
                        "rule_period" to mapOf("type" to "string", "enum" to listOf("monthly", "quarterly", "yearly")),
                        "rule_week" to mapOf("type" to "integer"),
                        "rule_weekday" to mapOf("type" to "integer"),
                        "date_type" to mapOf("type" to "string", "enum" to listOf("solar_birthday", "lunar_birthday", "holiday")),
                        "target_month" to mapOf("type" to "integer"),
                        "target_day" to mapOf("type" to "integer"),
                        "holiday_name" to mapOf("type" to "string"),
                        "advance_days" to mapOf("type" to "integer"),
                        "reminder_hour" to mapOf("type" to "integer"),
                        "reminder_minute" to mapOf("type" to "integer")
                    ),
                    "required" to listOf("title_keyword")
                )
            )
        ),
            mapOf(
                "type" to "function",
                "function" to mapOf(
                    "name" to "import_tasks",
                    "description" to "把用户给的多段待办文本批量解析为多条提醒并预览，确认后再批量创建",
                    "parameters" to mapOf(
                        "type" to "object",
                        "properties" to mapOf(
                            "items" to mapOf(
                                "type" to "array",
                                "description" to "逐条待办，每条解析为一条提醒。一行同时含新历+旧历生日时必须拆成两条 item（solar_birthday 与 lunar_birthday 各一条，title 加（公历）/（农历）后缀），不许只取其一",
                                "items" to mapOf(
                                    "type" to "object",
                                    "properties" to mapOf(
                                        "title" to mapOf("type" to "string", "description" to "提醒标题"),
                                        "note" to mapOf("type" to "string", "description" to "备注"),
                                        "kind" to mapOf("type" to "string", "enum" to listOf("cycle", "date", "rule"), "description" to "周期/日期/规则"),
                                        "cycle" to mapOf("type" to "string", "enum" to listOf("daily", "weekly", "biweekly", "monthly", "quarterly", "yearly", "custom", "once")),
                                        "custom_days" to mapOf("type" to "integer"),
                        "weekday" to mapOf("type" to "integer", "description" to "【每周/每两周提醒必须填】用户指定的星期：1=周一…7=周日。例：「每周日打针」→ cycle=weekly, weekday=7；「每周三开会」→ weekday=3"),
                                        "rule_period" to mapOf("type" to "string", "enum" to listOf("monthly", "quarterly", "yearly")),
                                        "rule_week" to mapOf("type" to "integer"),
                                        "rule_weekday" to mapOf("type" to "integer"),
                                        "date_type" to mapOf("type" to "string", "enum" to listOf("solar_birthday", "lunar_birthday", "holiday")),
                                        "target_month" to mapOf("type" to "integer"),
                                        "target_day" to mapOf("type" to "integer"),
                                        "holiday_name" to mapOf("type" to "string"),
                                        "advance_days" to mapOf("type" to "integer"),
                                        "reminder_hour" to mapOf("type" to "integer"),
                                        "reminder_minute" to mapOf("type" to "integer")
                                    )
                                )
                            )
                        ),
                        "required" to listOf("items")
                    )
                )
            )
        )
}
