package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.TextWhite

enum class BreathPhase(val displayName: String) {
    INHALE("Inhale"),
    HOLD("Hold"),
    EXHALE("Exhale"),
    HOLD_POST("Hold (Post)")
}

@Composable
fun BreathOrb(
    currentPhase: BreathPhase,
    phaseSecondsRemaining: Int,
    phaseFraction: Float, // 0.0f .. 1.0f within current phase
    modifier: Modifier = Modifier,
    size: Dp = 280.dp
) {
    // Determine the visual expansion scale based on phase
    val targetScale = when (currentPhase) {
        BreathPhase.INHALE -> 0.45f + (0.55f * phaseFraction) // expands from 0.45 to 1.0
        BreathPhase.HOLD -> 1.0f // stays expanded
        BreathPhase.EXHALE -> 1.0f - (0.55f * phaseFraction) // contracts from 1.0 to 0.45
        BreathPhase.HOLD_POST -> 0.45f // stays contracted
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "orb_scale"
    )

    val phaseColor = when (currentPhase) {
        BreathPhase.INHALE -> AccentViolet
        BreathPhase.HOLD -> AccentMint
        BreathPhase.EXHALE -> Color(0xFF6366F1)
        BreathPhase.HOLD_POST -> Color(0xFFEAB308)
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Outer concentric guide rings
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val maxRadius = size.toPx() / 2f - 10f

            // Outer dashed circle
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = maxRadius,
                center = center,
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                )
            )

            // Inner solid boundary ring
            drawCircle(
                color = phaseColor.copy(alpha = 0.35f),
                radius = maxRadius * 0.72f,
                center = center,
                style = Stroke(width = 2f)
            )
        }

        // Animated Breathing Orb
        val orbPixelSize = size * animatedScale * 0.75f
        Box(
            modifier = Modifier
                .size(orbPixelSize)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            phaseColor.copy(alpha = 0.45f),
                            phaseColor.copy(alpha = 0.15f),
                            Color(0xFF141620)
                        )
                    )
                )
                .border(2.5.dp, phaseColor.copy(alpha = 0.85f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$phaseSecondsRemaining",
                    color = TextWhite,
                    fontSize = (28 + (animatedScale * 14)).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
