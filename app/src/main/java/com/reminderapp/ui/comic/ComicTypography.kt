package com.reminderapp.ui.comic

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Dual-layer offset display number (#02, #18, #00) with tabular numerals.
 */
@Composable
fun ComicHeroNumber(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 46.sp
) {
    val colors = comicColors()
    val style = TextStyle(
        fontFamily = ComicDisplayFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = fontSize,
        lineHeight = (fontSize.value + 4f).sp,
        letterSpacing = (-1.2).sp,
        fontFeatureSettings = "tnum"
    )
    Box(modifier = modifier) {
        Text(
            text = text,
            color = colors.heroShadow,
            style = style,
            maxLines = 1,
            modifier = Modifier
                .offset(x = 2.2.dp, y = 2.2.dp)
                .clearAndSetSemantics {}
        )
        Text(
            text = text,
            color = colors.hero,
            style = style,
            maxLines = 1
        )
    }
}

/**
 * Dual-layer offset brand/page title sitting bare on canvas.
 */
@Composable
fun ComicBrandTitle(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp
) {
    val colors = comicColors()
    val style = TextStyle(
        fontFamily = ComicDisplayFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = fontSize,
        lineHeight = (fontSize.value + 5f).sp,
        letterSpacing = 0.4.sp
    )
    Box(modifier = modifier) {
        Text(
            text = text,
            color = colors.heroShadow,
            style = style,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .offset(x = 1.8.dp, y = 1.8.dp)
                .clearAndSetSemantics {}
        )
        Text(
            text = text,
            color = colors.ink,
            style = style,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Asymmetric tilted orange shard mark (1:1 homage to IOTA MONITOR's left polygon icon).
 */
@Composable
fun ComicBrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp
) {
    val accent = comicColors().accent
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val shard = Path().apply {
            moveTo(w * 0.22f, h * 0.06f)
            lineTo(w * 0.88f, h * 0.18f)
            lineTo(w * 0.94f, h * 0.86f)
            lineTo(w * 0.34f, h * 0.96f)
            lineTo(w * 0.08f, h * 0.68f)
            close()
        }
        drawPath(path = shard, color = accent)
    }
}
