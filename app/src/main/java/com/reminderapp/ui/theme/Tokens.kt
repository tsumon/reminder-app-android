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

    // ── 品牌主色（双端统一 #6750A4）──
    val BrandPrimary = Color(0xFF6750A4)
    val BrandPrimaryDark = Color(0xFF4F3B7A)   // 深色主题用
    val BrandPrimaryContainer = Color(0xFFEADDFF)

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
