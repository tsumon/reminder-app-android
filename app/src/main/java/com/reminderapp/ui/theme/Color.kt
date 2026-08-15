package com.reminderapp.ui.theme

import androidx.compose.ui.graphics.Color

// 主色调 — v1.8.7 任务⑤ 统一为 M3 紫 #6750A4（与 iOS ThemeTokens.BrandPrimary 一致）
// 令牌统一见 Tokens.kt；此处保留旧名供既有代码引用
val Primary = Tokens.BrandPrimary
val PrimaryVariant = Color(0xFF0E6E70)
val Secondary = Color(0xFFFFA07A)

// 状态颜色（令牌化）
val StatusReminding = Tokens.StatusReminding
val StatusWaiting = Tokens.StatusWaiting
val StatusCompleted = Tokens.StatusCompleted
val StatusOverdue = Tokens.StatusOverdue

// 表面和背景
val Surface = Color(0xFFFFFFFF)
val Background = Color(0xFFF8F8F8)
val OnSurface = Color(0xFF1A1A1A)
val OnSurfaceVariant = Color(0xFF666666)

// Dark theme
val DarkPrimary = Tokens.BrandPrimaryDark
val DarkBackground = Color(0xFF1A1A1A)
val DarkSurface = Color(0xFF2C2C2C)
val DarkOnSurface = Color(0xFFE0E0E0)
val DarkOnSurfaceVariant = Color(0xFFAAAAAA)
