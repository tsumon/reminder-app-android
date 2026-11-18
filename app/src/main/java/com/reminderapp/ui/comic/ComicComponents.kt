package com.reminderapp.ui.comic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.i18n.zh

@Composable
fun Modifier.comicPanel(
    cut: Dp = 11.dp,
    fill: Color = comicColors().surface,
    strokeColor: Color = comicColors().border,
    shadowColor: Color = comicColors().shadow,
    shadowOffset: Dp = 2.2.dp,
    strokeWidth: Dp = 1.5.dp
): Modifier = this.drawWithContent {
    val cutPx = cut.toPx()
    val shiftPx = shadowOffset.toPx()
    val strokePx = strokeWidth.toPx()
    if (shiftPx > 0.05f) {
        val shadowPath = buildChamferPath(size.width, size.height, cutPx, dx = shiftPx, dy = shiftPx)
        drawPath(path = shadowPath, color = shadowColor)
    }
    val mainPath = buildChamferPath(size.width, size.height, cutPx)
    drawPath(path = mainPath, color = fill)
    clipPath(mainPath) {
        this@drawWithContent.drawContent()
    }
    drawPath(
        path = mainPath,
        color = strokeColor,
        style = Stroke(width = strokePx, join = StrokeJoin.Round)
    )
}

@Composable
fun ComicCard(
    modifier: Modifier = Modifier,
    cut: Dp = 11.dp,
    fill: Color = comicColors().surface,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .comicPanel(cut = cut, fill = fill)
            .padding(contentPadding),
        verticalArrangement = verticalArrangement,
        content = content
    )
}

@Composable
fun ComicButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    filled: Boolean = true,
    cut: Dp = 5.5.dp,
    leadingIcon: ImageVector? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
    fontSize: TextUnit = 13.5.sp
) {
    val colors = comicColors()
    val haptic = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val activePress = pressed && enabled
    val bg = if (filled) colors.accent else colors.surface
    val fg = if (filled) colors.onAccent else colors.ink
    val shadowOff = when {
        !enabled -> 0.dp
        activePress -> 0.5.dp
        else -> 2.0.dp
    }

    Row(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.45f)
            .graphicsLayer {
                val shift = if (activePress) 1.5.dp.toPx() else 0f
                translationX = shift
                translationY = shift
            }
            .comicPanel(
                cut = cut,
                fill = bg,
                strokeColor = colors.border,
                shadowColor = colors.shadow,
                shadowOffset = shadowOff,
                strokeWidth = 1.5.dp
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (leadingIcon != null) {
            Icon(imageVector = leadingIcon, contentDescription = null, tint = fg, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(5.dp))
        }
        Text(
            text = text,
            color = fg,
            fontSize = fontSize,
            lineHeight = (fontSize.value + 4f).sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(fontFeatureSettings = "tnum")
        )
    }
}

@Composable
fun ComicHairline(
    modifier: Modifier = Modifier,
    isInner: Boolean = false
) {
    val colors = comicColors()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(if (isInner) colors.innerRule else colors.rule)
    )
}

@Composable
fun ComicCollapsibleRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    warning: Boolean = false,
    trailing: String? = null
) {
    val colors = comicColors()
    val tint = if (warning) colors.accent else colors.muted
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = if (expanded) "▾" else "›", color = tint, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(6.dp))
        Text(
            text = title,
            color = if (warning) colors.ink else colors.muted,
            fontSize = 13.sp,
            fontWeight = if (warning) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (!trailing.isNullOrEmpty()) {
            Spacer(Modifier.width(8.dp))
            Text(text = trailing, color = tint, fontSize = 12.sp, style = TextStyle(fontFeatureSettings = "tnum"))
        }
    }
}

@Composable
fun ComicTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    backText: String? = null,
    showShard: Boolean = false,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    val colors = comicColors()
    Column(modifier = modifier.fillMaxWidth().background(colors.canvas).statusBarsPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                Text(
                    text = backText ?: ("‹ " + zh("返回")),
                    color = colors.muted,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable(onClick = onBack)
                        .padding(end = 12.dp, top = 4.dp, bottom = 4.dp)
                )
            } else if (showShard) {
                ComicBrandMark(size = 19.dp)
                Spacer(Modifier.width(8.dp))
            }
            ComicBrandTitle(
                text = title,
                fontSize = if (onBack != null) 18.sp else 20.sp,
                modifier = Modifier.weight(1f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                content = trailing
            )
        }
        ComicHairline(isInner = false)
    }
}

@Composable
fun ComicTextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Boolean = false,
    danger: Boolean = false,
    fontSize: TextUnit = 13.sp
) {
    val colors = comicColors()
    val fg = when {
        !enabled -> colors.muted.copy(alpha = 0.45f)
        danger -> colors.danger
        accent -> colors.accent
        else -> colors.muted
    }
    Text(
        text = text,
        color = fg,
        fontSize = fontSize,
        fontWeight = if (accent || danger) FontWeight.Bold else FontWeight.Medium,
        modifier = modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
    )
}
