package com.reminderapp.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.reminderapp.ui.theme.MascotBadge
import com.reminderapp.ui.theme.MascotMood
import com.reminderapp.ui.theme.Playful
import com.reminderapp.ui.theme.clayCard

/**
 * 批次2 功能2: 正向反馈 —— 打卡成功卡片
 *
 * 确认提醒后弹出「打卡成功 · 连续 N 天」，约 2 秒自动淡出。
 * v2.5.0: 粘土拟态庆祝卡——吉祥物庆祝表情 + 🎉 弹跳 + 「+1 ⭐」星星奖励胶囊。
 * 挂载方式：页面根 Box 顶部，text 非空即展示；onDismiss 由 VM 消费状态。
 */
@Composable
fun CheckInFeedbackCard(
    text: String?,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(text) {
        if (text != null) {
            visible = true
            delay(2200)
            visible = false
            delay(300)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.85f, animationSpec = tween(250)),
        exit = fadeOut(tween(300)) + scaleOut(targetScale = 0.9f, animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .clayCard(radiusDp = 22.dp)
                    .padding(horizontal = 26.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 庆祝小狐狸 + 🎉 弹跳
                Box {
                    MascotBadge(mood = MascotMood.CHEER, sizeDp = 56.dp)
                    PartyPopper()
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = text.orEmpty(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "+1 ⭐",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Playful.ink,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Playful.gold.copy(alpha = 0.35f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        }
    }
}

/** 右上角 🎉 弹跳（0.85↔1.2 无限往复，spring 质感） */
@Composable
private fun PartyPopper() {
    val transition = rememberInfiniteTransition(label = "partyPopper")
    val scale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "partyPopperScale"
    )
    Text(
        text = "🎉",
        fontSize = 17.sp,
        modifier = Modifier
            .offset(x = 8.dp, y = (-2).dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
    )
}
