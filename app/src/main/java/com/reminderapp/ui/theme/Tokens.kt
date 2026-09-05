package com.reminderapp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 设计令牌 — soft-ui v2（Joe 2026-09-05）
 * 双端对齐：Android 本文件 / iOS ThemeTokens.swift
 *
 * `--strong` = 六色皮肤，默认青碧。皮肤压过 Material You。
 */
object Tokens {
    var BrandPrimary = Color(0xFF159A9C)
    var BrandPrimaryDark = Color(0xFF0E6E70)
    var BrandPrimaryContainer = Color(0xFFB2EBE4)
    var BrandGradientStart = Color(0xFF4DB6AC)

    data class Palette(
        val primary: Color, val dark: Color, val gradient: Color, val container: Color
    )

    val Palettes = listOf(
        Palette(Color(0xFF159A9C), Color(0xFF0E6E70), Color(0xFF4DB6AC), Color(0xFFB2EBE4)),
        Palette(Color(0xFF3B82F6), Color(0xFF1E5BC4), Color(0xFF7FB5FF), Color(0xFFC9E0FF)),
        Palette(Color(0xFFE0457B), Color(0xFFB02E5C), Color(0xFFF58FB0), Color(0xFFFCD3E1)),
        Palette(Color(0xFFF07B2F), Color(0xFFC05A16), Color(0xFFF7B27D), Color(0xFFFDE3CC)),
        Palette(Color(0xFF2E9E5B), Color(0xFF1D7A42), Color(0xFF74C99B), Color(0xFFD1F0DE)),
        Palette(Color(0xFF6C5CE7), Color(0xFF4A3FB8), Color(0xFFA69CF5), Color(0xFFE1DDFC))
    )

    val StatusReminding = Color(0xFFE74C3C)
    val StatusWaiting = Color(0xFF3498DB)
    val StatusCompleted = Color(0xFF27AE60)
    val StatusOverdue = Color(0xFFC0392B)
    val StatusSnoozed = Color(0xFFF39C12)

    val HolidayRest = Color(0xFFD32F2F)
    val HolidayWork = Color(0xFFEF6C00)

    val OnStrong = Color.White

    val RadiusShallow = 12.dp
    val RadiusCard = 16.dp
    val RadiusElevated = 18.dp
    val RadiusChip = 16.dp
    val BtnCircle = 44.dp
    val BtnInner = 6.dp
    val ChipH = 32.dp
    val RowBadge = 44.dp
    val Gutter = 16.dp
    val CardPad = 14.dp
    val CardGap = 8.dp

    val RadiusCell = 6.dp
    val RadiusLarge = 18.dp

    val FontTiny = 9.sp
    val FontMicro = 8.sp

    val TitleSize = 22.sp
    val TitleLine = 28.sp
    val SectionSize = 19.sp
    val SectionLine = 24.sp
    val BodySize = 14.5.sp
    val BodyLine = 18.sp

    val SpaceXS = 4.dp
    val SpaceS = 8.dp
    val SpaceM = 12.dp
    val SpaceL = 16.dp
    val SpaceXL = 24.dp

    val DockH = 48.dp
    val DockSelected = 32.dp
    val DockPadTop = 3.dp
    val DockPadBottom = 4.dp
    val DockBottomGap = 0.dp
    val SettingsGroupPadX = 16.dp
    val SettingsGroupPadY = 4.dp
    val SettingsRowH = 52.dp
    val SettingsRowH2 = 64.dp
    val SettingsRowPadX = 16.dp
    val SettingsRowPadY = 14.dp
    val SettingsRowPadY2 = 12.dp
    val SettingsGroupGap = 16.dp
    val SettingsThemeDot = 28.dp

    object Light {
        val Canvas = Color(0xFFF5F5F3)
        val Surface = Color(0xFFFBFBFA)
        val Elevated = Color(0xFFFFFFFF)
        val Text = Color(0xFF1A1A1C)
        val Muted = Color(0xFF5E5E66)
        val Track = Color(0x141A1A1C)
        val Warn = Color(0xFFC47A12)
        val Ok = Color(0xFF1F8A4C)
        val Danger = Color(0xFFC0392B)
        val Heat = listOf(
            Color(0xFFE8E4DC), Color(0xFFC8EBD4), Color(0xFF86D4A4),
            Color(0xFF3AAD72), Color(0xFF1B7A4C)
        )
    }

    object Dark {
        val Canvas = Color(0xFF12121A)
        val Surface = Color(0xFF1C1C26)
        val Elevated = Color(0xFF2A2A36)
        val Text = Color(0xFFEDEDF0)
        val Muted = Color(0xFF8E8E9A)
        val Track = Color(0x14FFFFFF)
        val Warn = Color(0xFFF39C12)
        val Ok = Color(0xFF27AE60)
        val Danger = Color(0xFFE74C3C)
        val Heat = listOf(
            Color(0xFF252422), Color(0xFF0E3D28), Color(0xFF0A6B38),
            Color(0xFF22A34A), Color(0xFF3DD15F)
        )
    }

    // 旧热力名保留，避免其它调用点崩；统计页用 Heat()
    val Heatmap0 = Color(0x1F9E9E9E)
    val Heatmap1 = Color(0x40159A9C)
    val Heatmap2 = Color(0x8C159A9C)
    val Heatmap3 = Color(0xFF159A9C)
}

@Composable
@ReadOnlyComposable
fun isSoftDark(): Boolean = LocalIsSoftDark.current

@Composable
@ReadOnlyComposable
fun softCanvas(): Color = if (isSoftDark()) Tokens.Dark.Canvas else Tokens.Light.Canvas

@Composable
@ReadOnlyComposable
fun softSurface(): Color = if (isSoftDark()) Tokens.Dark.Surface else Tokens.Light.Surface

@Composable
@ReadOnlyComposable
fun softElevated(): Color = if (isSoftDark()) Tokens.Dark.Elevated else Tokens.Light.Elevated

@Composable
@ReadOnlyComposable
fun softText(): Color = if (isSoftDark()) Tokens.Dark.Text else Tokens.Light.Text

@Composable
@ReadOnlyComposable
fun softMuted(): Color = if (isSoftDark()) Tokens.Dark.Muted else Tokens.Light.Muted

@Composable
@ReadOnlyComposable
fun softTrack(): Color = if (isSoftDark()) Tokens.Dark.Track else Tokens.Light.Track

@Composable
@ReadOnlyComposable
fun softWarn(): Color = if (isSoftDark()) Tokens.Dark.Warn else Tokens.Light.Warn

@Composable
@ReadOnlyComposable
fun softOk(): Color = if (isSoftDark()) Tokens.Dark.Ok else Tokens.Light.Ok

@Composable
@ReadOnlyComposable
fun softDanger(): Color = if (isSoftDark()) Tokens.Dark.Danger else Tokens.Light.Danger

@Composable
@ReadOnlyComposable
fun heatColor(level: Int): Color {
    val idx = level.coerceIn(0, 4)
    return if (isSoftDark()) Tokens.Dark.Heat[idx] else Tokens.Light.Heat[idx]
}
