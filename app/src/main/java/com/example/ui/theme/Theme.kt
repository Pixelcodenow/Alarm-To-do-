package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentViolet,
    onPrimary = Color(0xFF090A0E),
    primaryContainer = Color(0xFF272445),
    onPrimaryContainer = Color(0xFFE4DFFF),
    secondary = AccentAmber,
    onSecondary = Color(0xFF1E1404),
    secondaryContainer = Color(0xFF3F2B0E),
    onSecondaryContainer = Color(0xFFFFDDB3),
    tertiary = AccentMint,
    onTertiary = Color(0xFF002114),
    tertiaryContainer = Color(0xFF003824),
    onTertiaryContainer = Color(0xFF75F8BE),
    background = DeepCharcoal,
    onBackground = TextWhite,
    surface = CharcoalSurface,
    onSurface = TextWhite,
    surfaceVariant = CharcoalElevated,
    onSurfaceVariant = TextMuted,
    outline = GlassBorder,
    outlineVariant = DividerColor,
    error = AccentCoral,
    onError = Color.White
)

@Composable
fun FocusAlarmTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
