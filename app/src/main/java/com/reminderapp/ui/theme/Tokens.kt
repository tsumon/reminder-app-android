package com.reminderapp.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 设计令牌（v1.8.7 任务⑤）— 双端统一（Android 本文件 / iOS ThemeTokens.swift）
 *
 * 一份令牌，两端各映射一份；新 UI 一律引用令牌，不写硬编码颜色/圆角/字号。
 * 主色统一为 Material Design 3 紫色 #6750A4。
 */
object Tokens {
    // ── 品牌主色（v2.4.0: 可切换色板，默认青碧 Teal）──
    var BrandPrimary = Color(0xFF159A9C)
    var BrandPrimaryDark = Color(0xFF0E6E70)
    var BrandPrimaryContainer = Color(0xFFB2EBE4)
    var BrandGradientStart = Color(0xFF4DB6AC)

    /** v2.4.0: 主题色板（primary / primaryDark / gradientStart / container） */
    data class Palette(
        val primary: Color, val dark: Color, val gradient: Color, val container: Color
    )

    val Palettes = listOf(
        // 青碧（默认）
        Palette(Color(0xFF159A9C), Color(0xFF0E6E70), Color(0xFF4DB6AC), Color(0xFFB2EBE4)),
        // 活力蓝
        Palette(Color(0xFF3B82F6), Color(0xFF1E5BC4), Color(0xFF7FB5FF), Color(0xFFC9E0FF)),
        // 玫粉
        Palette(Color(0xFFE0457B), Color(0xFFB02E5C), Color(0xFFF58FB0), Color(0xFFFCD3E1)),
        // 暖橙
        Palette(Color(0xFFF07B2F), Color(0xFFC05A16), Color(0xFFF7B27D), Color(0xFFFDE3CC)),
        // 森林绿
        Palette(Color(0xFF2E9E5B), Color(0xFF1D7A42), Color(0xFF74C99B), Color(0xFFD1F0DE)),
        // 深空蓝紫
        Palette(Color(0xFF6C5CE7), Color(0xFF4A3FB8), Color(0xFFA69CF5), Color(0xFFE1DDFC))
    )

    // ── 状态色（与 iOS 一致）──
    val StatusReminding = Color(0xFFE74C3C)
    val StatusWaiting = Color(0xFF3498DB)
    val StatusCompleted = Color(0xFF27AE60)
    val StatusOverdue = Color(0xFFC0392B)   // v1.9.7: 递增重试到上限（比提醒中更深一档的红色）
    val StatusSnoozed = Color(0xFFF39C12)   // v2.1.0: 稍后/已推迟（原各屏硬编码橙，与 iOS statusSnoozed 对齐）

    // ── 节假日「休/班」──
    val HolidayRest = Color(0xFFD32F2F)
    val HolidayWork = Color(0xFFEF6C00)

    // ── 热力图色阶（统计页，双端一致；v2.1.0 由冷蓝统一为品牌紫系）──
    val Heatmap0 = Color(0x1F9E9E9E)
    val Heatmap1 = BrandPrimary.copy(alpha = 0.25f)
    val Heatmap2 = BrandPrimary.copy(alpha = 0.55f)
    val Heatmap3 = BrandPrimary

    // ── 圆角 ──
    val RadiusCard = 20.dp      // 液态玻璃大圆角（与 iOS glassCard 24 近似）
    val RadiusCell = 10.dp
    val RadiusLarge = 28.dp     // 弹窗/大组件

    // ── 字号（sp）──
    val FontTiny = 9.sp   // 日历农历/休班角标
    val FontMicro = 8.sp  // 热力图日期

    // ── 间距（v2.1.0: 统一布局节奏，替换 16.dp 字面量）──
    val SpaceXS = 4.dp
    val SpaceS = 8.dp
    val SpaceM = 12.dp
    val SpaceL = 16.dp
    val SpaceXL = 24.dp
}
