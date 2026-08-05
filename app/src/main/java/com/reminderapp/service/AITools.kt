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

        **周期与日期参数：**
        - "每天" → kind=cycle, cycle=daily
        - "每周/周一" → kind=cycle, cycle=weekly
        - "每月" → kind=cycle, cycle=monthly
        - "每年/生日" → kind=date, date_type=solar_birthday
        - "农历生日/阴历生日" → kind=date, date_type=lunar_birthday
        - "春节/中秋/端午/清明/国庆/元旦" → kind=date, date_type=holiday

        **回复风格：** 简洁友好，每次只做一件事，执行完告知结果。
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
                        "kind" to mapOf("type" to "string", "enum" to listOf("cycle", "date"), "description" to "周期/日期"),
                        "cycle" to mapOf("type" to "string", "enum" to listOf("daily", "weekly", "biweekly", "monthly", "quarterly", "yearly")),
                        "custom_days" to mapOf("type" to "integer"),
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
