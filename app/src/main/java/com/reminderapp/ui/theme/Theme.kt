package com.reminderapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

val LocalIsSoftDark = staticCompositionLocalOf { false }

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun ReminderAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val manualMode = ThemeStore.mode(context)
    val effectiveDark = when (manualMode) {
        1 -> false
        2 -> true
        else -> darkTheme
    }

    val palette = Tokens.Palettes.getOrElse(ThemeStore.colorIndex(context)) { Tokens.Palettes.first() }
    Tokens.BrandPrimary = palette.primary
    Tokens.BrandPrimaryDark = palette.dark
    Tokens.BrandPrimaryContainer = palette.container
    Tokens.BrandGradientStart = palette.gradient

    // 皮肤压过 Material You：不用 dynamicLight/DarkColorScheme
    val colorScheme = if (effectiveDark) {
        darkColorScheme(
            primary = palette.primary,
            onPrimary = Color.White,
            primaryContainer = palette.dark,
            onPrimaryContainer = Color.White,
            secondary = palette.primary,
            background = Tokens.Dark.Canvas,
            surface = Tokens.Dark.Surface,
            surfaceVariant = Tokens.Dark.Elevated,
            onBackground = Tokens.Dark.Text,
            onSurface = Tokens.Dark.Text,
            onSurfaceVariant = Tokens.Dark.Muted,
            outline = Tokens.Dark.Track,
            error = Tokens.Dark.Danger
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            onPrimary = Color.White,
            primaryContainer = palette.container,
            onPrimaryContainer = palette.dark,
            secondary = palette.primary,
            background = Tokens.Light.Canvas,
            surface = Tokens.Light.Surface,
            surfaceVariant = Tokens.Light.Elevated,
            onBackground = Tokens.Light.Text,
            onSurface = Tokens.Light.Text,
            onSurfaceVariant = Tokens.Light.Muted,
            outline = Tokens.Light.Track,
            error = Tokens.Light.Danger
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !effectiveDark
                isAppearanceLightNavigationBars = !effectiveDark
            }
        }
    }

    CompositionLocalProvider(LocalIsSoftDark provides effectiveDark) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
