package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderGlow
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = GlassSurface,
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = AccentViolet.copy(alpha = 0.25f)),
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(borderWidth, borderColor, shape)
            .then(clickableModifier)
    ) {
        content()
    }
}

@Composable
fun GlowGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(32.dp),
    glowColor: Color = AccentAmber.copy(alpha = 0.18f),
    borderColor: Color = GlassBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = AccentAmber.copy(alpha = 0.25f)),
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        glowColor,
                        GlassSurface,
                        Color(0x05FFFFFF)
                    ),
                    radius = 800f
                )
            )
            .border(1.dp, borderColor, shape)
            .then(clickableModifier)
    ) {
        content()
    }
}

@Composable
fun GlassPill(
    text: String,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    activeColor: Color = TextWhite,
    activeTextColor: Color = DeepCharcoal,
    inactiveColor: Color = GlassSurface,
    inactiveTextColor: Color = TextWhite,
    onClick: (() -> Unit)? = null
) {
    val bg = if (isActive) activeColor else inactiveColor
    val textCol = if (isActive) activeTextColor else inactiveTextColor
    val borderCol = if (isActive) Color.Transparent else GlassBorder

    val clickMod = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = activeColor.copy(alpha = 0.3f)),
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(12.dp))
            .then(clickMod)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textCol,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GlassActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    primaryColor: Color = AccentViolet,
    textColor: Color = if (isPrimary) DeepCharcoal else TextWhite,
    icon: (@Composable () -> Unit)? = null
) {
    val bg = if (isPrimary) primaryColor else GlassSurfaceElevated
    val border = if (isPrimary) primaryColor.copy(alpha = 0.8f) else GlassBorder

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = if (isPrimary) Color.Black.copy(alpha = 0.2f) else AccentViolet.copy(alpha = 0.3f)),
                onClick = onClick
            )
            .padding(vertical = 15.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
