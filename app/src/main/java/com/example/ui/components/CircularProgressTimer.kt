package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.AccentVioletVariant
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun CircularProgressTimer(
    progress: Float, // 0.0f .. 1.0f
    timeText: String, // "18:42"
    label: String, // "DEEP WORK"
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    strokeWidth: Dp = 12.dp,
    primaryColor: Color = AccentViolet,
    secondaryColor: Color = AccentVioletVariant,
    trackColor: Color = CharcoalElevated
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300),
        label = "timer_progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val radius = (size.toPx() - strokePx) / 2f
            val centerOffset = Offset(size.toPx() / 2f, size.toPx() / 2f)

            // Background Track Arc
            drawCircle(
                color = trackColor,
                radius = radius,
                center = centerOffset,
                style = Stroke(width = strokePx)
            )

            // Foreground Glowing Sweep Arc
            val sweepAngle = animatedProgress * 360f
            if (sweepAngle > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to primaryColor,
                        0.7f to secondaryColor,
                        1.0f to primaryColor
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(strokePx / 2f, strokePx / 2f),
                    size = Size(size.toPx() - strokePx, size.toPx() - strokePx),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = timeText,
                color = TextWhite,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label.uppercase(),
                color = primaryColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}
