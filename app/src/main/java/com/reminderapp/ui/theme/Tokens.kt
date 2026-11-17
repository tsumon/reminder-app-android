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
    var BrandPrimary = Color(0xFF0F766E)
    var BrandPrimaryDark = Color(0xFF115E59)
    var BrandPrimaryContainer = Color(0xFFCCFBF1)
    var BrandGradientStart = Color(0xFF2DD4BF)

    data class Palette(
        val primary: Color, val dark: Color, val gradient: Color, val container: Color
    )

    val Palettes = listOf(
        Palette(Color(0xFF0F766E), Color(0xFF115E59), Color(0xFF2DD4BF), Color(0xFFCCFBF1)),
        Palette(Color(0xFF2563EB), Color(0xFF1D4ED8), Color(0xFF60A5FA), Color(0xFFDBEAFE)),
        Palette(Color(0xFFD9467A), Color(0xFFBE185D), Color(0xFFF472B6), Color(0xFFFCE7F3)),
        Palette(Color(0xFFEA580C), Color(0xFFC2410C), Color(0xFFFB923C), Color(0xFFFFEDD5)),
        Palette(Color(0xFF16A34A), Color(0xFF15803D), Color(0xFF4ADE80), Color(0xFFDCFCE7)),
        Palette(Color(0xFF6D28D9), Color(0xFF5B21B6), Color(0xFFA78BFA), Color(0xFFEDE9FE))
    )

    var StatusReminding = Color(0xFFE74C3C)
    var StatusWaiting = Color(0xFF3498DB)
    var StatusCompleted = Color(0xFF27AE60)
    var StatusOverdue = Color(0xFFC0392B)
    var StatusSnoozed = Color(0xFFF39C12)

    val HolidayRest = Color(0xFFD32F2F)
    val HolidayWork = Color(0xFFEF6C00)

    var OnStrong = Color.White

    val RadiusShallow = 12.dp
    val RadiusCard = 16.dp
    val RadiusElevated = 18.dp
    val RadiusChip = 16.dp
    val BtnCircle = 48.dp
    val BtnInner = 6.dp
    val ChipH = 48.dp
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

    val DockH = 56.dp
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
        val Canvas = Color(0xFFF6F4EF)
        val Surface = Color(0xFFFFFFFF)
        val Elevated = Color(0xFFFFFFFF)
        val Text = Color(0xFF18181B)
        val Muted = Color(0xFF64748B)
        val Border = Color(0xFFE7E2D8)
        val Track = Color(0x1418181B)
        val Warn = Color(0xFFD97706)
        val Ok = Color(0xFF059669)
        val Danger = Color(0xFFDC2626)
        val Heat = listOf(
            Color(0xFFEBE6DC), Color(0xFFCCFBF1), Color(0xFF5EEAD4),
            Color(0xFF0D9488), Color(0xFF0F766E)
        )
    }

    object Dark {
        val Canvas = Color(0xFF111315)
        val Surface = Color(0xFF1A1D20)
        val Elevated = Color(0xFF22262B)
        val Text = Color(0xFFF4F4F5)
        val Muted = Color(0xFF94A3B8)
        val Border = Color(0xFF2A2F35)
        val Track = Color(0x14FFFFFF)
        val Warn = Color(0xFFF59E0B)
        val Ok = Color(0xFF34D399)
        val Danger = Color(0xFFF87171)
        val Heat = listOf(
            Color(0xFF22262B), Color(0xFF11423D), Color(0xFF0F6B62),
            Color(0xFF14B8A6), Color(0xFF2DD4BF)
        )
    }

    // 旧热力名保留，避免其它调用点崩；统计页用 Heat()
    val Heatmap0 = Color(0x1F9E9E9E)
    val Heatmap1 = Color(0x400F766E)
    val Heatmap2 = Color(0x8C0F766E)
    val Heatmap3 = Color(0xFF0F766E)
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
fun softBorder(): Color = if (isSoftDark()) Tokens.Dark.Border else Tokens.Light.Border

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
