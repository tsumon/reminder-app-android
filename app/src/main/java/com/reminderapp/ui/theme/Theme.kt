package com.reminderapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.reminderapp.ui.comic.ComicPalettes
import com.reminderapp.ui.comic.LocalComicColors

val LocalIsSoftDark = staticCompositionLocalOf { false }

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = CutCornerShape(topEnd = 5.5.dp, bottomStart = 5.5.dp),
    medium = CutCornerShape(topEnd = 11.dp, bottomStart = 11.dp),
    large = CutCornerShape(topEnd = 11.dp, bottomStart = 11.dp),
    extraLarge = CutCornerShape(topEnd = 12.dp, bottomStart = 12.dp)
)

@Composable
fun ReminderAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val manualMode = ThemeStore.mode(context)
    val effectiveDark = when (manualMode) {
        1 -> false
        2 -> true
        else -> darkTheme
    }

    val colorIdx = ThemeStore.colorIndex(context)
    val comic = ComicPalettes.resolve(index = colorIdx, dark = effectiveDark)

    Tokens.StatusReminding = comic.accent
    Tokens.StatusWaiting = comic.muted
    Tokens.StatusCompleted = comic.ok
    Tokens.StatusOverdue = comic.danger
    Tokens.StatusSnoozed = comic.warn
    Tokens.BrandPrimary = comic.accent
    Tokens.OnStrong = comic.onAccent
    Tokens.BrandPrimaryDark = comic.hero
    Tokens.BrandPrimaryContainer = comic.surface
    Tokens.BrandGradientStart = comic.accent

    val colorScheme = if (effectiveDark) {
        darkColorScheme(
            primary = comic.accent,
            onPrimary = comic.onAccent,
            primaryContainer = comic.elevated,
            onPrimaryContainer = comic.ink,
            secondary = comic.hero,
            onSecondary = comic.onAccent,
            secondaryContainer = comic.elevated,
            onSecondaryContainer = comic.ink,
            background = comic.canvas,
            surface = comic.surface,
            surfaceVariant = comic.elevated,
            onBackground = comic.ink,
            onSurface = comic.ink,
            onSurfaceVariant = comic.muted,
            outline = comic.rule,
            outlineVariant = comic.innerRule,
            error = comic.danger
        )
    } else {
        lightColorScheme(
            primary = comic.accent,
            onPrimary = comic.onAccent,
            primaryContainer = comic.surface,
            onPrimaryContainer = comic.ink,
            secondary = comic.hero,
            onSecondary = comic.onAccent,
            secondaryContainer = comic.surface,
            onSecondaryContainer = comic.ink,
            background = comic.canvas,
            surface = comic.surface,
            surfaceVariant = comic.canvas,
            onBackground = comic.ink,
            onSurface = comic.ink,
            onSurfaceVariant = comic.muted,
            outline = comic.rule,
            outlineVariant = comic.innerRule,
            error = comic.danger
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = comic.canvas.toArgb()
            window.navigationBarColor = comic.canvas.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !effectiveDark
                isAppearanceLightNavigationBars = !effectiveDark
            }
        }
    }

    CompositionLocalProvider(
        LocalIsSoftDark provides effectiveDark,
        LocalComicColors provides comic
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
