package com.carflow.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carflow.app.ui.theme.MonoFamily
import com.carflow.app.ui.theme.carflowColors

enum class TagTone { DEFAULT, ACCENT, WARN, DANGER, OK, GHOST }

@Composable
fun CarflowTag(
    tone: TagTone = TagTone.DEFAULT,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val c = carflowColors
    val (bg, fg, bd) = when (tone) {
        TagTone.DEFAULT -> Triple(c.cardHi, c.fg2, c.line)
        TagTone.ACCENT  -> Triple(c.accent, c.accentInk, c.accent)
        TagTone.WARN    -> Triple(c.warn.copy(alpha = 0.18f), c.warn, c.warn.copy(alpha = 0.45f))
        TagTone.DANGER  -> Triple(c.danger.copy(alpha = 0.18f), c.danger, c.danger.copy(alpha = 0.45f))
        TagTone.OK      -> Triple(c.ok.copy(alpha = 0.18f), c.ok, c.ok.copy(alpha = 0.45f))
        TagTone.GHOST   -> Triple(Color.Transparent, c.fg2, c.line)
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(0.5.dp, bd, RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides fg,
        ) {
            content()
        }
    }
}

@Composable
fun MonoTag(text: String, tone: TagTone = TagTone.DEFAULT, modifier: Modifier = Modifier) {
    CarflowTag(tone = tone, modifier = modifier) {
        Text(
            text = text.uppercase(),
            fontFamily = MonoFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.W500,
            letterSpacing = 0.6.sp,
            color = when (tone) {
                TagTone.DEFAULT -> carflowColors.fg2
                TagTone.ACCENT  -> carflowColors.accentInk
                TagTone.WARN    -> carflowColors.warn
                TagTone.DANGER  -> carflowColors.danger
                TagTone.OK      -> carflowColors.ok
                TagTone.GHOST   -> carflowColors.fg2
            },
        )
    }
}

@Composable
fun CarflowCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    padding: Dp = 20.dp,
    content: @Composable () -> Unit,
) {
    val c = carflowColors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(if (elevated) c.cardHi else c.card)
            .border(0.5.dp, c.line, RoundedCornerShape(22.dp))
            .padding(padding),
    ) {
        content()
    }
}

@Composable
fun SectionHeader(
    eyebrow: String? = null,
    title: String,
    action: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val c = carflowColors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            if (eyebrow != null) {
                Text(
                    text = eyebrow.uppercase(),
                    fontFamily = MonoFamily,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    color = c.fg3,
                )
                Spacer(Modifier.height(6.dp))
            }
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.W600,
                letterSpacing = (-0.4).sp,
                color = c.fg,
            )
        }
        action?.invoke()
    }
}

@Composable
fun Ring(
    value: Int,
    size: Dp = 88.dp,
    strokeWidth: Dp = 8.dp,
    label: String,
    sub: String? = null,
    tone: Color,
    modifier: Modifier = Modifier,
) {
    val c = carflowColors
    var animStarted by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animStarted) value / 100f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "ring",
    )
    LaunchedEffect(Unit) { animStarted = true }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .drawBehind {
                    val sw = strokeWidth.toPx()
                    val r = (size.toPx() - sw) / 2f
                    val topLeft = Offset(sw / 2f, sw / 2f)
                    val arcSize = Size(r * 2, r * 2)
                    // Track
                    drawArc(
                        color = c.line,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(sw, cap = StrokeCap.Round),
                    )
                    // Progress
                    drawArc(
                        color = tone,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(sw, cap = StrokeCap.Round),
                    )
                },
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontWeight = FontWeight.W600,
                fontSize = (size.value * 0.32).sp,
                lineHeight = (size.value * 0.32).sp,
                color = c.fg,
            )
            if (sub != null) {
                Text(
                    text = sub.uppercase(),
                    fontFamily = MonoFamily,
                    fontSize = 9.sp,
                    letterSpacing = 0.6.sp,
                    color = c.fg3,
                )
            }
        }
    }
}

@Composable
fun Bars(
    data: List<Int>,
    highlightIdx: Int,
    height: Dp = 64.dp,
    tone: Color,
    modifier: Modifier = Modifier,
) {
    val c = carflowColors
    val max = data.maxOrNull()?.toFloat() ?: 1f
    var animStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animStarted = true }

    Row(
        modifier = modifier.fillMaxWidth().height(height),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        data.forEachIndexed { i, v ->
            val fraction = if (animStarted) (v / max).coerceAtLeast(0.03f) else 0.03f
            val animFraction by animateFloatAsState(
                targetValue = fraction,
                animationSpec = tween(durationMillis = 500, delayMillis = i * 30),
                label = "bar$i",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .height(height * animFraction)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (i == highlightIdx) tone else c.line2),
            )
        }
    }
}

@Composable
fun CarSilhouette(
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val sx = w / 320f
        val sy = h / 120f

        // Body path approximation using drawPath
        val bodyPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(20 * sx, 85 * sy)
            quadraticBezierTo(28 * sx, 62 * sy, 60 * sx, 58 * sy)
            lineTo(110 * sx, 40 * sy)
            quadraticBezierTo(140 * sx, 30 * sy, 175 * sx, 30 * sy)
            lineTo(220 * sx, 32 * sy)
            quadraticBezierTo(245 * sx, 34 * sy, 270 * sx, 55 * sy)
            lineTo(295 * sx, 62 * sy)
            quadraticBezierTo(305 * sx, 66 * sy, 305 * sx, 78 * sy)
            lineTo(305 * sx, 92 * sy)
            quadraticBezierTo(305 * sx, 98 * sy, 299 * sx, 98 * sy)
            lineTo(275 * sx, 98 * sy)
            // right wheel arch
            lineTo(235 * sx, 98 * sy)
            lineTo(95 * sx, 98 * sy)
            // left wheel arch
            lineTo(55 * sx, 98 * sy)
            lineTo(26 * sx, 98 * sy)
            quadraticBezierTo(20 * sx, 98 * sy, 20 * sx, 92 * sy)
            close()
        }
        drawPath(bodyPath, color = accentColor.copy(alpha = 0.9f))
        drawPath(
            bodyPath,
            color = Color.Black.copy(alpha = 0.2f),
            style = Stroke(width = 1f),
        )

        // Windows
        val win1 = androidx.compose.ui.graphics.Path().apply {
            moveTo(118 * sx, 43 * sy)
            lineTo(170 * sx, 35 * sy)
            lineTo(210 * sx, 36 * sy)
            lineTo(240 * sx, 55 * sy)
            lineTo(190 * sx, 55 * sy)
            close()
        }
        drawPath(win1, color = Color.Black.copy(alpha = 0.35f))

        val win2 = androidx.compose.ui.graphics.Path().apply {
            moveTo(115 * sx, 45 * sy)
            lineTo(165 * sx, 38 * sy)
            lineTo(165 * sx, 56 * sy)
            lineTo(115 * sx, 56 * sy)
            close()
        }
        drawPath(win2, color = Color.Black.copy(alpha = 0.40f))

        // Door line
        drawLine(
            color = Color.Black.copy(alpha = 0.25f),
            start = Offset(168 * sx, 36 * sy),
            end = Offset(168 * sx, 90 * sy),
            strokeWidth = 1.2f,
        )

        // Wheels
        listOf(75f, 255f).forEach { cx ->
            drawCircle(Color(0xFF0A0A0A), radius = 18 * sx, center = Offset(cx * sx, 98 * sy))
            drawCircle(Color(0xFF333333), radius = 18 * sx, center = Offset(cx * sx, 98 * sy), style = Stroke(1f))
            drawCircle(Color(0xFF2A2A2A), radius = 9 * sx, center = Offset(cx * sx, 98 * sy))
        }

        // Highlight
        val hlPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(30 * sx, 70 * sy)
            quadraticBezierTo(60 * sx, 68 * sy, 100 * sx, 68 * sy)
        }
        drawPath(hlPath, color = Color.White.copy(alpha = 0.3f), style = Stroke(1.5f, cap = StrokeCap.Round))
    }
}

@Composable
fun MonoEyebrow(text: String, modifier: Modifier = Modifier) {
    Text(
        text = "· ${text.uppercase()}",
        fontFamily = MonoFamily,
        fontSize = 10.sp,
        letterSpacing = 1.4.sp,
        color = carflowColors.fg3,
        modifier = modifier,
    )
}
