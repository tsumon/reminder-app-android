package com.reminderapp.service

/**
 * AI 工具定义 — Function Calling JSON Schema
 * 镜像 iOS AITools
 */
object AITools {

    val systemPrompt = """
        你是一个循环提醒助手，帮用户管理重复提醒事项。

        **核心规则：**
        - 用户描述提醒需求时 → 调用 create_reminder
        - 用户要确认某个提醒已完成 → 调用 confirm_reminder
        - 用户要推迟某个提醒 → 调用 snooze_reminder
        - 用户要删除提醒 → 调用 delete_reminder
        - 用户问'有什么提醒''列表' → 调用 list_reminders

        **生日 / 日期提醒（重要）：**
        - "X生日:新历/公历 M月D号" → kind=date, date_type=solar_birthday, target_month=M, target_day=D
        - "X生日:旧历/农历/阴历 M月初D / M月D号" → kind=date, date_type=lunar_birthday, target_month=M, target_day=D
        - "每年""周年" → kind=date, date_type=solar_birthday
        - "春节/中秋/端午/清明/国庆/元旦" → kind=date, date_type=holiday, holiday_name=名称

        **批量创建（关键）：** 若用户一次给出多个生日（例如"老公生日:新历9月5号 / 婆婆生日:农历7月初五 / 老娘生日:新历1月14号，旧历12月18 / 啊姨生日:新历7月14号，旧历6月15"），必须为每一个人分别调用一次 create_reminder（一次只创建一条），title 用"XX生日"。优先取"新历/公历"日期；若只给了"旧历/农历"，则使用 lunar_birthday 与对应月日。不要合并、也不要漏掉任何一个人。

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
                        "cycle" to mapOf("type" to "string", "enum" to listOf("daily", "weekly", "biweekly", "monthly", "quarterly", "yearly")),
                        "custom_days" to mapOf("type" to "integer"),
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
        )
    )
}
