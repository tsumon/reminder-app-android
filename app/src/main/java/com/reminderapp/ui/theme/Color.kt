package com.reminderapp.ui.theme

import androidx.compose.ui.graphics.Color

val Primary = Tokens.BrandPrimary
val PrimaryVariant = Color(0xFF0E6E70)
val Secondary = Color(0xFFFFA07A)

val StatusReminding: Color get() = Tokens.StatusReminding
val StatusWaiting: Color get() = Tokens.StatusWaiting
val StatusCompleted: Color get() = Tokens.StatusCompleted
val StatusOverdue: Color get() = Tokens.StatusOverdue

val Surface = Tokens.Light.Surface
val Background = Tokens.Light.Canvas
val OnSurface = Tokens.Light.Text
val OnSurfaceVariant = Tokens.Light.Muted

val DarkPrimary = Tokens.BrandPrimaryDark
val DarkBackground = Tokens.Dark.Canvas
val DarkSurface = Tokens.Dark.Surface
val DarkOnSurface = Tokens.Dark.Text
val DarkOnSurfaceVariant = Tokens.Dark.Muted
