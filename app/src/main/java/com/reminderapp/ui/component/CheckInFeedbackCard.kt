package com.reminderapp.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.i18n.zh
import com.reminderapp.ui.comic.comicColors
import com.reminderapp.ui.comic.comicPanel
import kotlinx.coroutines.delay

/**
 * Minimalist Cream Comic check-in toast banner at top of screen.
 */
@Composable
fun CheckInFeedbackCard(
    text: String?,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val colors = comicColors()

    LaunchedEffect(text) {
        if (text != null) {
            visible = true
            delay(2000)
            visible = false
            delay(250)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.92f, animationSpec = tween(200)),
        exit = fadeOut(tween(220)) + scaleOut(targetScale = 0.95f, animationSpec = tween(220))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .comicPanel(cut = 8.dp, fill = colors.surface)
                    .padding(horizontal = 16.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = text.orEmpty(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = colors.ink
                    )
                    Text(
                        text = zh("+1 次打卡"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.muted
                    )
                }
            }
        }
    }
}
