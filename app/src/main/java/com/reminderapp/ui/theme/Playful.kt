package com.reminderapp.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderapp.data.entity.ReminderEntity
import com.reminderapp.data.entity.ReminderRecordEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 治愈游戏化设计令牌（v2.5.0）— 移植 iOS PlayfulUI.swift（Claymorphism 粘土拟态）。
 *
 * 装饰/奖励色固定，不随主题色板切换；主强调色（日历热力等）仍走 Tokens.BrandPrimary。
 * 业务逻辑零改动：本文件只含纯展示组件与只读统计辅助。
 */
object Playful {
    val cream = Color(0xFFFFF5E6)    // 暖白基底
    val purple = Color(0xFF6C5CE7)   // 紫（打卡按钮/数据高亮）
    val coral = Color(0xFFFF6B6B)    // 珊瑚粉
    val gold = Color(0xFFFECA57)     // 星星金（奖励反馈）
    val mint = Color(0xFF55EFC4)     // 薄荷绿（奖励反馈）
    val ink = Color(0xFF2D3436)      // 深灰（周期规则徽章，形成对比）
    val peach = Color(0xFFFFE1C4)    // 背景·桃
    val lavender = Color(0xFFE4DCF7) // 背景·薰衣草
}

// MARK: - 粘土卡样式

/**
 * 粘土拟态卡片：大圆角 + 暖白渐变底 + 顶部内高光描边 + 双层柔和阴影（外彩内灰）。
 * 深色主题回落深紫灰底 + 白 10% 描边（purple 阴影在深色下关闭）。
 */
@Composable
fun Modifier.clayCard(radiusDp: Dp = 20.dp): Modifier {
    val dark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(radiusDp)
    val withShadows = if (dark) {
        shadow(3.dp, shape, spotColor = Color.Black.copy(alpha = 0.06f), ambientColor = Color.Black.copy(alpha = 0.06f))
    } else {
        shadow(8.dp, shape, spotColor = Playful.purple.copy(alpha = 0.10f), ambientColor = Playful.purple.copy(alpha = 0.10f))
            .shadow(3.dp, shape, spotColor = Color.Black.copy(alpha = 0.06f), ambientColor = Color.Black.copy(alpha = 0.06f))
    }
    return withShadows
        .then(
            if (dark) Modifier.background(Color(0xFF2A2735), shape)
            else Modifier.background(
                Brush.linearGradient(listOf(Color.White, Playful.cream)),
                shape
            )
        )
        .border(
            width = 1.5.dp,
            brush = Brush.verticalGradient(
                if (dark) listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.03f))
                else listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0.25f))
            ),
            shape = shape
        )
}

// MARK: - 桃粉薰衣草渐变背景 + 漂浮装饰

private class PastelDecor(
    val emoji: String,
    val x: Float,        // 0-1 水平相对位置
    val y: Float,        // 0-1 垂直相对位置
    val sizeSp: Int,
    val opacity: Float,
    val durationMs: Int
)

/** 与 iOS PastelPlaygroundBackground 相同的 6 个固定装饰位 */
private val pastelDecors = listOf(
    PastelDecor("☁️", 0.12f, 0.10f, 30, 0.9f, 5200),
    PastelDecor("⭐️", 0.88f, 0.16f, 16, 0.9f, 4100),
    PastelDecor("✨", 0.76f, 0.62f, 14, 0.8f, 4800),
    PastelDecor("☁️", 0.80f, 0.86f, 24, 0.7f, 5600),
    PastelDecor("⭐️", 0.08f, 0.72f, 13, 0.8f, 4400),
    PastelDecor("✨", 0.30f, 0.40f, 12, 0.6f, 5000)
)

/**
 * 页面背景：桃 + 薰衣草软渐变，散布缓慢漂浮的星星/云朵（深色模式回落深紫灰）。
 * 纯装饰不拦截点击：无任何 pointer 输入修饰，放在内容层之下即可。
 */
@Composable
fun PastelBackground(modifier: Modifier = Modifier) {
    val dark = isSystemInDarkTheme()
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    if (dark) listOf(Color(0xFF272333), Color(0xFF1C1A26))
                    else listOf(Playful.peach, Playful.lavender)
                )
            )
    ) {
        val w = maxWidth
        val h = maxHeight
        pastelDecors.forEachIndexed { idx, d ->
            val transition = rememberInfiniteTransition(label = "pastel$idx")
            val dy by transition.animateFloat(
                initialValue = 7f,
                targetValue = -7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(d.durationMs, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "float$idx"
            )
            val decorAlpha = if (dark) d.opacity * 0.45f else d.opacity
            Text(
                text = d.emoji,
                fontSize = d.sizeSp.sp,
                modifier = Modifier
                    // 以 (x, y) 为中心漂浮（偏移半个字号居中）
                    .offset(x = w * d.x - (d.sizeSp / 2).dp, y = h * d.y + dy.dp - (d.sizeSp / 2).dp)
                    .alpha(decorAlpha)
            )
        }
    }
}

// MARK: - 3D 吉祥物

/** 吉祥物心情：cheer=全部完成/高完成率 🎉，happy=有进展 ✨，idle=待办中，sleepy=无任务 💤 */
enum class MascotMood(val accessory: String?) {
    CHEER("🎉"),
    HAPPY("✨"),
    IDLE(null),
    SLEEPY("💤")
}

/** 吉祥物（小狐狸）徽章：白→cream 渐变圆底 + 描边 + 紫晕阴影，整体缓慢浮动 */
@Composable
fun MascotBadge(
    mood: MascotMood,
    sizeDp: Dp,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "mascot")
    val floatUp by transition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascotFloat"
    )
    Box(modifier = modifier.size(sizeDp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(3.dp, CircleShape, spotColor = Playful.purple.copy(alpha = 0.14f), ambientColor = Playful.purple.copy(alpha = 0.14f))
                .background(
                    Brush.linearGradient(listOf(Color.White.copy(alpha = 0.95f), Playful.cream)),
                    CircleShape
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🦊",
                fontSize = (sizeDp.value * 0.55f).sp,
                modifier = Modifier.offset { IntOffset(0, floatUp.roundToInt()) }
            )
        }
        mood.accessory?.let { acc ->
            Text(
                text = acc,
                fontSize = (sizeDp.value * 0.26f).sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = sizeDp * 0.18f, y = (-sizeDp.value * 0.04f).dp)
            )
        }
    }
}

// MARK: - 连续打卡城堡

/**
 * 连续打卡城堡：连续天数越长楼层越高（每 3 天一层，封顶 7 层 + 金冠）。
 * StreakCastle(...) 直接调用（operator invoke），StreakCastle.level(n) 取等级。
 */
object StreakCastle {

    /** 楼层数（=城堡等级）：0 天=空地小旗，每 3 天 +1 层，最多 7 层 */
    fun level(streakDays: Int): Int {
        if (streakDays <= 0) return 0
        return min(7, 1 + (streakDays - 1) / 3)
    }

    @Composable
    operator fun invoke(
        streakDays: Int,
        compact: Boolean = false,
        modifier: Modifier = Modifier
    ) {
        val floors = level(streakDays)
        // 楼层变化时弹性加盖（spring 近似 iOS response 0.5 / damping 0.8）
        val animatedFloors by animateFloatAsState(
            targetValue = floors.toFloat(),
            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
            label = "castleFloors"
        )
        val floorHeight = if (compact) 11.dp else 16.dp
        val towerWidth = if (compact) 46.dp else 64.dp

        Column(
            modifier = modifier.graphicsLayer { alpha = if (floors == 0) 0.55f else 1f },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (streakDays > 0) {
                // 「🔥 第 N 天」coral→gold 渐变胶囊
                Text(
                    text = "🔥 第 $streakDays 天",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .shadow(2.dp, CircleShape, spotColor = Playful.coral.copy(alpha = 0.35f))
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(listOf(Playful.coral, Playful.gold))
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                )
            } else {
                Text("🌱", fontSize = if (compact) 14.sp else 18.sp)
            }

            // 城堡主体
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (floors >= 7) {
                    Text("👑", fontSize = if (compact) 13.sp else 17.sp)
                } else {
                    Text("🚩", fontSize = if (compact) 11.sp else 13.sp)
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    val visible = max(1, animatedFloors.roundToInt())
                    repeat(visible) { i ->
                        val floor = visible - i // 顶层（floor=1）最窄
                        val isTop = floor == 1
                        val ratio = if (floors > 1) (floor - 1).toFloat() / (floors - 1) else 0f
                        val tint = androidx.compose.ui.graphics.lerp(Playful.mint, Playful.gold, ratio.coerceIn(0f, 1f))
                        Box(
                            modifier = Modifier
                                .width(towerWidth * (if (isTop) 0.72f else 1f))
                                .height(floorHeight)
                                .background(
                                    Brush.verticalGradient(listOf(tint.copy(alpha = 0.95f), tint.copy(alpha = 0.7f))),
                                    RoundedCornerShape(4.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // 小圆窗
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 1.dp)
                                    .size(5.dp)
                                    .background(Playful.ink.copy(alpha = 0.55f), CircleShape)
                            )
                        }
                    }
                }
                // 底座 + 城门
                Box(
                    modifier = Modifier
                        .width(towerWidth + 14.dp)
                        .height(floorHeight + 4.dp)
                        .background(
                            Brush.horizontalGradient(listOf(Playful.gold, Playful.coral.copy(alpha = 0.85f))),
                            RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 1.dp)
                            .width(12.dp)
                            .height(floorHeight - 1.dp)
                            .background(Playful.ink, RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp, bottomStart = 0.dp, bottomEnd = 0.dp))
                    )
                }
            }
        }
    }
}

// MARK: - 彩带粒子（打卡庆祝）

private class ConfettiParticle(
    val angle: Double,     // 弧度
    val speed: Float,      // dp/s
    val spin: Double,      // rad/s
    val size: Float,       // dp
    val emoji: String?,    // null = 粉彩纸屑矩形
    val color: Color,
    val delay: Double      // s
)

/** 与 iOS 相同的固定种子粒子表（视觉双端一致） */
private val confettiParticles: List<ConfettiParticle> = run {
    var seed = 20260823L
    fun rand(): Double {
        seed = seed * 6364136223846793005L + 1442695040888963407L
        return ((seed ushr 33) and 0xFFFFFF).toDouble() / 0xFFFFFF.toDouble()
    }
    val palette = listOf(Playful.gold, Playful.coral, Playful.mint, Playful.purple, Color(0xFFFFD9E8))
    // v2.5.1: 必须与 iOS 一致为 5 元素（末位 null=纯彩纸矩形）——原 4 元素配
    // (rand()*4.99).toInt() 会在静态初始化越界（index=4），首页加载即闪退
    val emojis: List<String?> = listOf("⭐️", "✨", "🎉", "💫", null)
    (0 until 46).map { i ->
        ConfettiParticle(
            angle = Math.PI / 2 + (rand() - 0.5) * Math.PI * 1.3,
            speed = (320 + rand() * 360).toFloat(),
            spin = (rand() - 0.5) * 12,
            size = (6 + rand() * 8).toFloat(),
            emoji = if (i % 3 == 0) emojis[(rand() * 4.99).toInt()] else null,
            color = palette[(rand() * 4.99).toInt()],
            delay = rand() * 0.12
        )
    }
}

/**
 * 打卡成功彩带：粉彩纸屑 + 星星从 (0.5w, 0.3h) 爆开，约 1.5s 自然消散。
 * trigger 变化即重放一次；全屏 overlay 位置，不拦截点击。
 * Animatable 到 1f 后不再推进——空闲时 Canvas 不重绘、不重组。
 */
@Composable
fun ConfettiBurst(trigger: Int, modifier: Modifier = Modifier) {
    val density = LocalDensity.current.density
    val progress = remember { Animatable(1f) }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec = tween(durationMillis = 1500, easing = LinearEasing))
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = progress.value * 1.5
        if (t <= 0f || t >= 1.5f) return@Canvas // 空闲：不画任何东西

        val originX = size.width / 2f
        val originY = size.height * 0.3f
        val gravity = 640f * density // dp/s² → px/s²
        val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

        for (p in confettiParticles) {
            val pt = t - p.delay
            if (pt < 0) continue
            val x = originX + cos(p.angle).toFloat() * p.speed * density * pt.toFloat()
            val y = originY + sin(p.angle).toFloat() * p.speed * density * pt.toFloat() + 0.5f * gravity * pt.toFloat() * pt.toFloat()
            if (y > size.height + 30f * density) continue
            val alpha = (max(0.0, 1.3 - t) / 1.3).toFloat()

            if (p.emoji != null) {
                textPaint.textSize = (p.size + 4) * density
                textPaint.alpha = (alpha * 255).toInt().coerceIn(0, 255)
                val cy = y - (textPaint.ascent() + textPaint.descent()) / 2f
                drawContext.canvas.nativeCanvas.save()
                drawContext.canvas.nativeCanvas.rotate(
                    Math.toDegrees(p.spin * pt).toFloat(), x, y
                )
                drawContext.canvas.nativeCanvas.drawText(p.emoji, x, cy, textPaint)
                drawContext.canvas.nativeCanvas.restore()
            } else {
                val w = p.size * density
                val h = p.size * 0.66f * density
                rotate(degrees = Math.toDegrees(p.spin * pt).toFloat(), pivot = Offset(x, y)) {
                    drawRoundRect(
                        color = p.color.copy(alpha = alpha),
                        topLeft = Offset(x - w / 2f, y - h / 2f),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(2f * density, 2f * density)
                    )
                }
            }
        }
    }
}

// MARK: - 发光球打卡按钮

/**
 * 魔法光球打卡按钮：未完成=暖白球+紫色虚线环+✨；完成=薄荷→金渐变+白✓+金光晕。
 * spring 弹跳；无障碍描述「打卡」/「已完成」。
 */
@Composable
fun OrbCheckButton(
    done: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 34.dp
) {
    val scale by animateFloatAsState(
        targetValue = if (done) 1.12f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
        label = "orbScale"
    )
    Box(
        modifier = modifier
            .size(sizeDp)
            .scale(scale)
            .shadow(                if (done) 6.dp else 3.dp, CircleShape,
                spotColor = if (done) Playful.gold.copy(alpha = 0.75f) else Playful.purple.copy(alpha = 0.12f),
                ambientColor = if (done) Playful.gold.copy(alpha = 0.75f) else Playful.purple.copy(alpha = 0.12f)
            )
            .clip(CircleShape)
            .background(
                if (done) Brush.linearGradient(listOf(Playful.mint, Playful.gold))
                else Brush.linearGradient(listOf(Color.White, Playful.cream.copy(alpha = 0.9f)))
            )
            .drawBehind {
                if (!done) {
                    drawCircle(
                        color = Playful.purple.copy(alpha = 0.35f),
                        radius = (size.minDimension - 3.dp.toPx()) / 2f,
                        style = Stroke(
                            width = 1.6.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))
                        )
                    )
                }
            }
            .clickable(
                onClick = onClick,
                onClickLabel = if (done) "已完成" else "打卡"
            )
            .semantics { contentDescription = if (done) "已完成" else "打卡" },
        contentAlignment = Alignment.Center
    ) {
        if (done) {
            Text("✓", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        } else {
            Text("✨", fontSize = 10.sp)
        }
    }
}

// MARK: - 本周彩虹跑道进度条

/** 本周进度彩虹跑道：「本周进度 | N/7 🎯」，跑道头挂一颗星星 */
@Composable
fun WeeklyProgressTrack(
    done: Int,
    total: Int = 7,
    modifier: Modifier = Modifier
) {
    val targetProgress = if (total > 0) min(1f, done.toFloat() / total) else 0f
    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "weeklyProgress"
    )
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "本周进度",
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "$done/$total 🎯",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Playful.purple
            )
        }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
        ) {
            val trackWidth = maxWidth
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Playful.purple.copy(alpha = 0.10f))
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(maxOf(0.04f, progress))
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(listOf(Playful.mint, Playful.gold, Playful.coral))
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
            )
            if (targetProgress > 0.02f) {
                Text(
                    "⭐️",
                    fontSize = 13.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(
                            Playful.gold.copy(alpha = 0.6f),
                            blurRadius = with(LocalDensity.current) { 4.dp.toPx() }
                        )
                    ),
                    modifier = Modifier
                        .offset(x = trackWidth * progress - 9.dp)
                        .align(Alignment.CenterStart)
                )
            }
        }
    }
}

// MARK: - 智能重复规则徽章

/** 周期中文标签（cycle kind） */
private fun cycleLabelOf(entity: ReminderEntity): String = when (entity.cycle) {
    "once" -> "仅一次"
    "daily" -> "每天"
    "weekly" -> "每周"
    "biweekly" -> "每两周"
    "monthly" -> "每月"
    "quarterly" -> "每季度"
    "yearly" -> "每年"
    "custom" -> "每 ${maxOf(entity.customDays, 1)} 天"
    else -> entity.cycle
}

/** 规则文本（rule kind）：如「每季度第2周周二」（与 HomeScreen.ruleLabel 同口径） */
private fun ruleLabelOf(entity: ReminderEntity): String {
    val periodLabel = when (entity.rulePeriod) {
        "monthly" -> "每月"
        "yearly" -> "每年"
        else -> "每季度"
    }
    val weekday = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        .getOrElse(((entity.ruleWeekday ?: 1) - 1).coerceIn(0, 6)) { "周${entity.ruleWeekday}" }
    return "${periodLabel}第${entity.ruleWeek ?: 1}周$weekday"
}

/** 日期文本（date kind）：公历生日 M月d日 / 农历生日 月名+日名 / 节假日名 */
private fun dateDisplayOf(entity: ReminderEntity): String = when (entity.dateType) {
    "solar_birthday" -> "${entity.targetMonth ?: 1}月${entity.targetDay ?: 1}日"
    "lunar_birthday" -> {
        val monthNames = arrayOf("", "正月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "冬月", "腊月")
        val dayNames = arrayOf("", "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十")
        val m = monthNames.getOrElse((entity.targetMonth ?: 1).coerceIn(1, 12)) { "" }
        val d = dayNames.getOrElse((entity.targetDay ?: 1).coerceIn(1, 30)) { "${entity.targetDay}日" }
        m + d
    }
    "holiday" -> entity.holidayName ?: entity.holidayId ?: ""
    else -> ""
}

/**
 * 周期规则徽章：`--strong` 浅容器洗色，圆角 8，无外描边。
 */
@Composable
fun RepeatRuleBadge(entity: ReminderEntity, modifier: Modifier = Modifier) {
    val time = if (entity.kind == "rule" || entity.kind == "cycle") {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(entity.firstTriggerAt))
    } else ""
    val base = when (entity.kind) {
        "rule" -> ruleLabelOf(entity)
        "date" -> dateDisplayOf(entity)
        else -> cycleLabelOf(entity)
    }
    val ruleText = if (time.isEmpty()) base else "$base · $time"
    val dark = isSoftDark()
    Text(
        ruleText,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = if (dark) Tokens.BrandGradientStart else Tokens.BrandPrimaryDark,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (dark) Tokens.BrandPrimary.copy(alpha = 0.22f)
                else Tokens.BrandPrimaryContainer.copy(alpha = 0.62f)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

// MARK: - 里程碑宝箱

/** 时间线底部里程碑宝箱：完成今日全部任务即可开启 */
@Composable
fun MilestoneChest(
    doneToday: Int,
    totalToday: Int,
    modifier: Modifier = Modifier
) {
    val unlocked = totalToday > 0 && doneToday >= totalToday
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clayCard(radiusDp = 18.dp)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 未解锁：降低透明度近似 iOS 灰度 35%（Compose Text 无法对 emoji 做真灰度）
        Text(
            if (unlocked) "🎊" else "🎁",
            fontSize = 26.sp,
            modifier = Modifier.then(if (unlocked) Modifier else Modifier.alpha(0.62f))
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                if (unlocked) "宝箱已开启！" else "完成全部任务开启宝箱",
                style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (unlocked) Playful.purple else androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )
            if (!unlocked) {
                Text(
                    "还差 ${max(totalToday - doneToday, 0)} 项 · 今日 $doneToday/$totalToday",
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (unlocked) {
            Text(
                "+1 🏆",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Playful.coral,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Playful.gold.copy(alpha = 0.25f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
    }
}

// MARK: - 打卡数据辅助

private fun startOfDay(ts: Long): Long {
    val c = Calendar.getInstance().apply { timeInMillis = ts }
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

/** 本周（周一起）有确认打卡记录的天数（0-7），用于彩虹跑道与宝箱 */
fun weekDoneDays(records: List<ReminderRecordEntity>): Int {
    val cal = Calendar.getInstance()
    val weekday = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // 0=周一
    val mondayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -weekday) }
    val monday = startOfDay(mondayCal.timeInMillis)
    val confirmDays = records
        .filter { it.action == ReminderRecordEntity.ACTION_CONFIRMED }
        .map { startOfDay(it.timestamp) }
        .toSet()
    var done = 0
    for (offset in 0..6) {
        val probe = Calendar.getInstance().apply {
            timeInMillis = monday
            add(Calendar.DAY_OF_MONTH, offset)
        }
        if (startOfDay(probe.timeInMillis) in confirmDays) done++
    }
    return done
}

/** 今日已确认打卡次数（宝箱 doneToday 口径） */
fun todayDoneCount(records: List<ReminderRecordEntity>): Int {
    val today = startOfDay(System.currentTimeMillis())
    return records.count {
        it.action == ReminderRecordEntity.ACTION_CONFIRMED && startOfDay(it.timestamp) == today
    }
}

/** 本周（周一起）逐日 confirm 次数（下标 0=周一 … 6=周日），供「本周花园」用 */
fun weekDayConfirmCounts(records: List<ReminderRecordEntity>): IntArray {
    val cal = Calendar.getInstance()
    val weekday = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val mondayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -weekday) }
    val monday = startOfDay(mondayCal.timeInMillis)
    val confirms = records.filter { it.action == ReminderRecordEntity.ACTION_CONFIRMED }
    return IntArray(7) { offset ->
        val probe = Calendar.getInstance().apply {
            timeInMillis = monday
            add(Calendar.DAY_OF_MONTH, offset)
        }
        val day = startOfDay(probe.timeInMillis)
        confirms.count { startOfDay(it.timestamp) == day }
    }
}

// MARK: - 空状态挥手小动物

/** 空状态：小狐狸挥手「今天想做什么呀？」 */
@Composable
fun WavingEmptyMascot(onCreate: () -> Unit, modifier: Modifier = Modifier) {
    val waveTransition = rememberInfiniteTransition(label = "wave")
    val wave by waveTransition.animateFloat(
        initialValue = -12f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveAngle"
    )
    val waveBounce by waveTransition.animateFloat(
        initialValue = 4f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveY"
    )
    val floatTransition = rememberInfiniteTransition(label = "emptyFloat")
    val floatUp by floatTransition.animateFloat(
        initialValue = 3f,
        targetValue = -3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emptyFloatY"
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.offset { IntOffset(0, floatUp.roundToInt()) }
        ) {
            MascotBadge(mood = MascotMood.SLEEPY, sizeDp = 92.dp)
            Text(
                "👋",
                fontSize = 30.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 16.dp)
                    .offset { IntOffset(0, waveBounce.roundToInt()) }
                    .graphicsLayer { rotationZ = wave }
            )
        }
        Text(
            "今天想做什么呀？",
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            "创建你的第一个循环提醒，小狐狸帮你记住",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "✨ 创建提醒",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .padding(top = 6.dp)
                .shadow(5.dp, CircleShape, spotColor = Playful.purple.copy(alpha = 0.35f), ambientColor = Playful.purple.copy(alpha = 0.35f))
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(Playful.purple, Playful.coral)))
                .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                .clickable(onClickLabel = "创建提醒", onClick = onCreate)
                .padding(horizontal = 26.dp, vertical = 13.dp)
        )
    }
}
