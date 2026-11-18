package com.reminderapp.ui.comic

import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * Minimalist Cream Comic (极简奶油漫风) Palette & Geometry System.
 * Index 0 (`奶油橙`) is 1:1 sampled from the IOTA MONITOR reference sheet.
 */
data class ComicPaletteSpec(
    val id: String,
    val name: String,
    val canvas: Color,
    val surface: Color,
    val elevated: Color,
    val ink: Color,
    val border: Color,
    val shadow: Color,
    val muted: Color,
    val rule: Color,
    val innerRule: Color,
    val accent: Color,
    val onAccent: Color,
    val hero: Color,
    val heroShadow: Color,
    val dotEmpty: Color
)

data class ComicColors(
    val isDark: Boolean,
    val paletteIndex: Int,
    val paletteName: String,
    val canvas: Color,
    val surface: Color,
    val elevated: Color,
    val ink: Color,
    val border: Color,
    val shadow: Color,
    val muted: Color,
    val rule: Color,
    val innerRule: Color,
    val accent: Color,
    val onAccent: Color,
    val hero: Color,
    val heroShadow: Color,
    val dotEmpty: Color,
    val ok: Color,
    val warn: Color,
    val danger: Color
)

object ComicPalettes {
    val All: List<ComicPaletteSpec> = listOf(
        // 0: 奶油橙 Cream Orange (1:1 sampled from IOTA MONITOR — DEFAULT)
        ComicPaletteSpec(
            id = "cream_orange",
            name = "奶油橙",
            canvas = Color(0xFFF6E5C8),
            surface = Color(0xFFFFF5DF),
            elevated = Color(0xFFFFF5DF),
            ink = Color(0xFF472714),
            border = Color(0xFF472714),
            shadow = Color(0xFF472714),
            muted = Color(0xFF6D503B),
            rule = Color(0xFFCDA780),
            innerRule = Color(0xFFE5CCA9),
            accent = Color(0xFFF06A29),
            onAccent = Color(0xFF27140B),
            hero = Color(0xFF774501),
            heroShadow = Color(0xFFFCC5A2),
            dotEmpty = Color(0xFFECD9BC)
        ),
        // 1: 抹茶奶纸 Matcha
        ComicPaletteSpec(
            id = "matcha",
            name = "抹茶奶纸",
            canvas = Color(0xFFE5EFE0),
            surface = Color(0xFFFBFDF8),
            elevated = Color(0xFFFBFDF8),
            ink = Color(0xFF1F3524),
            border = Color(0xFF1F3524),
            shadow = Color(0xFF1F3524),
            muted = Color(0xFF536E58),
            rule = Color(0xFFB5CBB0),
            innerRule = Color(0xFFD5E4D1),
            accent = Color(0xFF58A973),
            onAccent = Color(0xFF132919),
            hero = Color(0xFF265938),
            heroShadow = Color(0xFFBCE0C6),
            dotEmpty = Color(0xFFD2E2CE)
        ),
        // 2: 海盐汽水 Cobalt
        ComicPaletteSpec(
            id = "cobalt",
            name = "海盐汽水",
            canvas = Color(0xFFE3EDF6),
            surface = Color(0xFFF9FCFF),
            elevated = Color(0xFFF9FCFF),
            ink = Color(0xFF1B2D42),
            border = Color(0xFF1B2D42),
            shadow = Color(0xFF1B2D42),
            muted = Color(0xFF526982),
            rule = Color(0xFFB4C8DC),
            innerRule = Color(0xFFD3E1EE),
            accent = Color(0xFF4A8FE7),
            onAccent = Color(0xFF102238),
            hero = Color(0xFF1E4C8A),
            heroShadow = Color(0xFFBDD6F7),
            dotEmpty = Color(0xFFCFE0EE)
        ),
        // 3: 莓果奶昔 Berry
        ComicPaletteSpec(
            id = "berry",
            name = "莓果奶昔",
            canvas = Color(0xFFF4E4EA),
            surface = Color(0xFFFFF8FA),
            elevated = Color(0xFFFFF8FA),
            ink = Color(0xFF3D1C28),
            border = Color(0xFF3D1C28),
            shadow = Color(0xFF3D1C28),
            muted = Color(0xFF734D5B),
            rule = Color(0xFFD9B6C3),
            innerRule = Color(0xFFEBD4DC),
            accent = Color(0xFFE05A8B),
            onAccent = Color(0xFF2B0E19),
            hero = Color(0xFF7A2444),
            heroShadow = Color(0xFFF5C2D4),
            dotEmpty = Color(0xFFE8CED7)
        ),
        // 4: 黄油柠檬 Butter
        ComicPaletteSpec(
            id = "butter",
            name = "黄油柠檬",
            canvas = Color(0xFFF6EECA),
            surface = Color(0xFFFFFDF0),
            elevated = Color(0xFFFFFDF0),
            ink = Color(0xFF3D2E10),
            border = Color(0xFF3D2E10),
            shadow = Color(0xFF3D2E10),
            muted = Color(0xFF6E5B35),
            rule = Color(0xFFD6C48E),
            innerRule = Color(0xFFE8DCB6),
            accent = Color(0xFFE59A24),
            onAccent = Color(0xFF291B05),
            hero = Color(0xFF734B08),
            heroShadow = Color(0xFFF5DC9B),
            dotEmpty = Color(0xFFE8DFB9)
        ),
        // 5: 复古电紫 Lilac
        ComicPaletteSpec(
            id = "lilac",
            name = "复古电紫",
            canvas = Color(0xFFEAE4F4),
            surface = Color(0xFFFAF8FF),
            elevated = Color(0xFFFAF8FF),
            ink = Color(0xFF2A1E3D),
            border = Color(0xFF2A1E3D),
            shadow = Color(0xFF2A1E3D),
            muted = Color(0xFF5E5075),
            rule = Color(0xFFC2B5D9),
            innerRule = Color(0xFFDAD1EA),
            accent = Color(0xFF8B6CE8),
            onAccent = Color(0xFF190E2E),
            hero = Color(0xFF482B85),
            heroShadow = Color(0xFFD4C4F7),
            dotEmpty = Color(0xFFD7CEE8)
        )
    )

    fun resolve(index: Int, dark: Boolean): ComicColors {
        val safeIdx = index.coerceIn(0, All.lastIndex)
        val p = All[safeIdx]
        return if (dark) {
            ComicColors(
                isDark = true,
                paletteIndex = safeIdx,
                paletteName = p.name,
                canvas = Color(0xFF1B1613),
                surface = Color(0xFF261F1B),
                elevated = Color(0xFF2E2520),
                ink = Color(0xFFF3E3CF),
                border = Color(0xFFC9AE92),
                shadow = Color(0xFF0B0807),
                muted = Color(0xFFA38D78),
                rule = Color(0xFF3E322A),
                innerRule = Color(0xFF382D26),
                accent = p.accent,
                onAccent = Color(0xFF1F0E05),
                hero = if (safeIdx == 0) Color(0xFFF6A86E) else p.accent,
                heroShadow = Color(0xFF4A240B),
                dotEmpty = Color(0xFF342A24),
                ok = Color(0xFF6BC489),
                warn = Color(0xFFF2A93B),
                danger = Color(0xFFEF6B5B)
            )
        } else {
            ComicColors(
                isDark = false,
                paletteIndex = safeIdx,
                paletteName = p.name,
                canvas = p.canvas,
                surface = p.surface,
                elevated = p.elevated,
                ink = p.ink,
                border = p.border,
                shadow = p.shadow,
                muted = p.muted,
                rule = p.rule,
                innerRule = p.innerRule,
                accent = p.accent,
                onAccent = p.onAccent,
                hero = p.hero,
                heroShadow = p.heroShadow,
                dotEmpty = p.dotEmpty,
                ok = Color(0xFF2F7D4A),
                warn = Color(0xFFB86214),
                danger = Color(0xFFC43D2B)
            )
        }
    }
}

val LocalComicColors = staticCompositionLocalOf { ComicPalettes.resolve(index = 0, dark = false) }

@Composable
@ReadOnlyComposable
fun comicColors(): ComicColors = LocalComicColors.current

val ComicDisplayFontFamily: FontFamily = FontFamily(
    Typeface.create("sans-serif-rounded", Typeface.BOLD)
)

/**
 * 6-point chamfered polygon (top-right and bottom-left 45-degree cuts):
 * (0,0) -> (w-cut, 0) -> (w, cut) -> (w, h) -> (cut, h) -> (0, h-cut)
 */
class ChamferShape(private val cut: Dp = 11.dp) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cutPx = with(density) { cut.toPx() }
        return Outline.Generic(buildChamferPath(size.width, size.height, cutPx))
    }
}

fun buildChamferPath(
    width: Float,
    height: Float,
    cutPx: Float,
    dx: Float = 0f,
    dy: Float = 0f
): Path {
    val c = min(cutPx, min(width, height) * 0.45f).coerceAtLeast(0f)
    return Path().apply {
        moveTo(dx, dy)
        lineTo(dx + width - c, dy)
        lineTo(dx + width, dy + c)
        lineTo(dx + width, dy + height)
        lineTo(dx + c, dy + height)
        lineTo(dx, dy + height - c)
        close()
    }
}
