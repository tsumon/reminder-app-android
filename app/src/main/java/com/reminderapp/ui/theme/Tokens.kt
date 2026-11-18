package com.reminderapp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.ui.comic.ComicPalettes
import com.reminderapp.ui.comic.comicColors

/**
 * Minimalist Cream Comic design tokens (极简奶油漫风).
 * Index 0 defaults to `奶油橙 Cream Orange` (1:1 sampled from IOTA MONITOR).
 */
object Tokens {
    var BrandPrimary = Color(0xFFF06A29)
    var BrandPrimaryDark = Color(0xFF774501)
    var BrandPrimaryContainer = Color(0xFFFFF5DF)
    var BrandGradientStart = Color(0xFFF06A29)

    data class Palette(
        val name: String,
        val primary: Color,
        val dark: Color,
        val gradient: Color,
        val container: Color,
        val canvas: Color
    )

    val Palettes: List<Palette> = ComicPalettes.All.map { spec ->
        Palette(
            name = spec.name,
            primary = spec.accent,
            dark = spec.hero,
            gradient = spec.accent,
            container = spec.surface,
            canvas = spec.canvas
        )
    }

    var StatusReminding = Color(0xFFF06A29)
    var StatusWaiting = Color(0xFF6D503B)
    var StatusCompleted = Color(0xFF2F7D4A)
    var StatusOverdue = Color(0xFFC43D2B)
    var StatusSnoozed = Color(0xFFB86214)

    val HolidayRest = Color(0xFFC43D2B)
    val HolidayWork = Color(0xFFB86214)

    var OnStrong = Color(0xFF27140B)

    val RadiusShallow = 8.dp
    val RadiusCard = 11.dp
    val RadiusElevated = 11.dp
    val RadiusChip = 6.dp
    val Gutter = 16.dp
    val FontTiny = 9.sp

    val TitleSize = 20.sp
    val TitleLine = 26.sp
    val SectionSize = 16.sp
    val SectionLine = 22.sp
    val BodySize = 14.5.sp
    val BodyLine = 19.sp

    val SettingsGroupPadX = 18.dp
    val SettingsGroupPadY = 12.dp
    val SettingsRowH = 48.dp
    val SettingsRowH2 = 58.dp
    val SettingsGroupGap = 14.dp
    val SettingsThemeDot = 30.dp

    object Light {
        val Canvas = Color(0xFFF6E5C8)
        val Surface = Color(0xFFFFF5DF)
        val Elevated = Color(0xFFFFF5DF)
        val Text = Color(0xFF472714)
        val Muted = Color(0xFF6D503B)
        val Border = Color(0xFF472714)
        val Warn = Color(0xFFB86214)
        val Ok = Color(0xFF2F7D4A)
        val Danger = Color(0xFFC43D2B)
    }

    object Dark {
        val Canvas = Color(0xFF1B1613)
        val Surface = Color(0xFF261F1B)
        val Elevated = Color(0xFF2E2520)
        val Text = Color(0xFFF3E3CF)
        val Muted = Color(0xFFA38D78)
        val Border = Color(0xFFC9AE92)
        val Warn = Color(0xFFF2A93B)
        val Ok = Color(0xFF6BC489)
        val Danger = Color(0xFFEF6B5B)
    }
}

@Composable
@ReadOnlyComposable
fun isSoftDark(): Boolean = LocalIsSoftDark.current

@Composable
@ReadOnlyComposable
fun softCanvas(): Color = comicColors().canvas

@Composable
@ReadOnlyComposable
fun softSurface(): Color = comicColors().surface

@Composable
@ReadOnlyComposable
fun softElevated(): Color = comicColors().elevated

@Composable
@ReadOnlyComposable
fun softText(): Color = comicColors().ink

@Composable
@ReadOnlyComposable
fun softMuted(): Color = comicColors().muted

@Composable
@ReadOnlyComposable
fun softBorder(): Color = comicColors().border

@Composable
@ReadOnlyComposable
fun softWarn(): Color = comicColors().warn

@Composable
@ReadOnlyComposable
fun softOk(): Color = comicColors().ok

@Composable
@ReadOnlyComposable
fun softDanger(): Color = comicColors().danger

@Composable
@ReadOnlyComposable
fun heatColor(level: Int): Color {
    val c = comicColors()
    return when (level.coerceIn(0, 4)) {
        0 -> c.dotEmpty
        1 -> c.accent.copy(alpha = if (c.isDark) 0.28f else 0.30f)
        2 -> c.accent.copy(alpha = if (c.isDark) 0.52f else 0.55f)
        3 -> c.accent.copy(alpha = if (c.isDark) 0.80f else 0.82f)
        else -> if (c.isDark) c.accent else c.hero
    }
}
