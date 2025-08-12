package com.example.reminderapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Modern, minimalistic neutral palette with warm undertones
private val LightColors = lightColorScheme(
    primary = Color(0xFF2D2A32), // Deep charcoal - sophisticated and unique
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF5F4F2),
    onPrimaryContainer = Color(0xFF1A1A1A),
    secondary = Color(0xFF6B6B6B), // Refined medium gray
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFAFAF9),
    onSecondaryContainer = Color(0xFF2A2A2A),
    tertiary = Color(0xFF8B7355), // Warm taupe accent
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF8F6F3),
    onTertiaryContainer = Color(0xFF3D3529),
    background = Color(0xFFFEFEFE), // Pure, clean white
    onBackground = Color(0xFF1C1C1C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1C1C),
    surfaceVariant = Color(0xFFFBFBFA),
    onSurfaceVariant = Color(0xFF757575),
    surfaceContainer = Color(0xFFF7F7F6),
    surfaceContainerHigh = Color(0xFFF0F0EF),
    outline = Color(0xFFE8E8E7),
    outlineVariant = Color(0xFFF4F4F3),
    error = Color(0xFFB91C1C),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEF2F2),
    onErrorContainer = Color(0xFF7F1D1D),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF5F4F2), // Light warm off-white
    onPrimary = Color(0xFF1A1A1A),
    primaryContainer = Color(0xFF3A3A3A),
    onPrimaryContainer = Color(0xFFF5F4F2),
    secondary = Color(0xFFB8B8B8), // Light gray for dark mode
    onSecondary = Color(0xFF2A2A2A),
    secondaryContainer = Color(0xFF2F2F2F),
    onSecondaryContainer = Color(0xFFE5E5E5),
    tertiary = Color(0xFFD4B896), // Warm sand accent for dark
    onTertiary = Color(0xFF3D3529),
    tertiaryContainer = Color(0xFF4A4237),
    onTertiaryContainer = Color(0xFFF1E6D3),
    background = Color(0xFF0F0F0F), // Rich, deep black
    onBackground = Color(0xFFF8F8F8),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFF0F0F0),
    surfaceVariant = Color(0xFF262626),
    onSurfaceVariant = Color(0xFFAAAAAA),
    surfaceContainer = Color(0xFF212121),
    surfaceContainerHigh = Color(0xFF2E2E2E),
    outline = Color(0xFF404040),
    outlineVariant = Color(0xFF2A2A2A),
    error = Color(0xFFEF4444),
    onError = Color(0xFF1A1A1A),
    errorContainer = Color(0xFF4C1D1D),
    onErrorContainer = Color(0xFFFECACA),
)

@Composable
fun ReminderAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // You can enable this for Android 12+ dynamic theming
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (darkTheme) androidx.compose.material3.dynamicDarkColorScheme(context)
            else androidx.compose.material3.dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        shapes = androidx.compose.material3.Shapes(),
        content = content
    )
}
