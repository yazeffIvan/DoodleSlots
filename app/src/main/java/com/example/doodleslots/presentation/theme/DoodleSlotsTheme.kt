package com.example.doodleslots.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Палитра "деген-трейдер" ---
object GamePalette {
    // Основные
    val BgTop = Color(0xFF0A0E1A)         // почти чёрный с синим
    val BgBottom = Color(0xFF1A1F3A)      // тёмно-синий
    val Surface = Color(0xFF161B2E)       // карточки
    val SurfaceHigh = Color(0xFF1E2440)   // активные карточки

    // Акценты
    val NeonGreen = Color(0xFF00E676)     // прибыль
    val NeonRed = Color(0xFFFF1744)       // убыток
    val NeonCyan = Color(0xFF00E5FF)      // акцент
    val NeonPurple = Color(0xFF7C4DFF)    // плечо
    val Gold = Color(0xFFFFD700)          // премиум

    // Текст
    val TextPrimary = Color(0xFFECEFF1)
    val TextSecondary = Color(0xFF90A4AE)
    val TextMuted = Color(0xFF546E7A)
}

private val DarkColors = darkColorScheme(
    primary = GamePalette.NeonCyan,
    onPrimary = Color.Black,
    secondary = GamePalette.NeonGreen,
    onSecondary = Color.Black,
    tertiary = GamePalette.NeonPurple,
    background = GamePalette.BgTop,
    onBackground = GamePalette.TextPrimary,
    surface = GamePalette.Surface,
    onSurface = GamePalette.TextPrimary,
    error = GamePalette.NeonRed
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Black,
        fontSize = 48.sp,
        letterSpacing = 2.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = 1.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 14.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        letterSpacing = 1.sp
    )
)

@Composable
fun DoodleSlotsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        shapes = AppShapes,
        typography = AppTypography,
        content = content
    )
}