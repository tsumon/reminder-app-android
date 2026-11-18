package com.reminderapp.ui.comic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.i18n.zh

data class ComicTabItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

/**
 * Minimalist Cream Comic bottom navigation dock:
 * - Sits on canvas with a 1dp `rule` top hairline + `.navigationBarsPadding()`.
 * - Inactive tabs have ZERO border and ZERO background pill.
 * - ONLY the active tab gets a compact `accent` chamfered pill (`cut = 5.dp`, `onAccent` content).
 * - Preserves exact 4 equal-width columns covering `y = screen.height - 54dp` and `x = width*(2*i+1)/8`.
 */
@Composable
fun ComicTabDock(
    currentRoute: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = comicColors()
    val haptic = LocalHapticFeedback.current
    val items = listOf(
        ComicTabItem("home", zh("首页"), Icons.Filled.Home),
        ComicTabItem("calendar", zh("日历"), Icons.Filled.CalendarMonth),
        ComicTabItem("stats", zh("统计"), Icons.Filled.BarChart),
        ComicTabItem("settings", zh("设置"), Icons.Filled.Settings)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.canvas)
            .testTag("soft-tab-dock")
    ) {
        ComicHairline(isInner = false)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                val interaction = remember(item.route) { MutableInteractionSource() }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clickable(
                            interactionSource = interaction,
                            indication = null,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelect(item.route)
                            }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (selected) {
                        Row(
                            modifier = Modifier
                                .comicPanel(
                                    cut = 5.dp,
                                    fill = colors.accent,
                                    strokeColor = colors.border,
                                    shadowColor = colors.shadow,
                                    shadowOffset = 1.6.dp,
                                    strokeWidth = 1.4.dp
                                )
                                .padding(PaddingValues(horizontal = 11.dp, vertical = 6.dp)),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = colors.onAccent,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = item.title,
                                color = colors.onAccent,
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = colors.muted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = item.title,
                            color = colors.muted,
                            fontSize = 11.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
