package com.example.test

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color Scheme (replace with your desired color values)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF914F07),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBF7014),
    onPrimaryContainer = Color(0xFF291600),
    inversePrimary = Color(0xFFFFDCA4),
    secondary = Color(0xFFE6D8AD),
    onSecondary = Color(0xFF3E2D16),
    secondaryContainer = Color(0xFFFFE9C5),
    onSecondaryContainer = Color(0xFF291600),
    tertiary = Color(0xFFF7B977),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCA4),
    onTertiaryContainer = Color(0xFF291600),
    background = Color(0xFFFFF8F1),
    onBackground = Color(0xFF201A11),
    surface = Color(0xFFFFF8F1),
    onSurface = Color(0xFF201A11),
    surfaceVariant = Color(0xFFE6D8AD),
    onSurfaceVariant = Color(0xFF524439),
    inverseSurface = Color(0xFF362F26),
    inverseOnSurface = Color(0xFFFFF8F1),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    outline = Color(0xFF857467),
    outlineVariant = Color(0xFFD7C2B4),
    scrim = Color(0xFF000000)
)


private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF914F07),
    onPrimary = Color(0xFF0E0C0C),
    primaryContainer = Color(0xFFBF7014),
    onPrimaryContainer = Color(0xFF291600),
    inversePrimary = Color(0xFFFFDCA4),
    secondary = Color(0xFFE6D8AD),
    onSecondary = Color(0xFF3E2D16),
    secondaryContainer = Color(0xFFFFE9C5),
    onSecondaryContainer = Color(0xFF291600),
    tertiary = Color(0xFFF7B977),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCA4),
    onTertiaryContainer = Color(0xFF291600),
    background = Color(0xFF0E0A0A),
    onBackground = Color(0xFF201A11),
    surface = Color(0xFF090606),
    onSurface = Color(0xFF201A11),
    surfaceVariant = Color(0xFFE6D8AD),
    onSurfaceVariant = Color(0xFF524439),
    inverseSurface = Color(0xFF362F26),
    inverseOnSurface = Color(0xFFFFF8F1),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    outline = Color(0xFF857467),
    outlineVariant = Color(0xFFD7C2B4),
    scrim = Color(0xFF000000)
)

// Typography (replace with your desired font styles)
// Create your Typography object using Material 3 defaults
val typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = AppFontSize.medium,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // Customize other text styles (e.g., titleLarge, headlineSmall) as needed
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = AppFontSize.extraLarge,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )
)

// Global Padding Values
object AppPadding {
    val small: Dp = 1.dp
    val medium: Dp = 2.dp
    val large: Dp = 3.dp
    val extraLarge: Dp = 10.dp
}

// Global Font Sizes
object AppFontSize {
    val small = 10.sp
    val medium = 13.sp
    val large = 14.sp
    val extraLarge = 18.sp
}

// Your Theme Composable
@Composable
fun MySourdoughTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme, // Provide a light color scheme
        typography = typography, // Use the Material 3 Typography object
        content = content
    )
}