package com.homefood.delivery.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Orange = Color(0xFFF4511E)
private val OrangeDark = Color(0xFFE64A19)
private val OrangeContainer = Color(0xFFFFE0D6)
private val Teal = Color(0xFF00897B)
private val TealContainer = Color(0xFFB2DFDB)

private val LightColors = lightColorScheme(
    primary = Orange,
    onPrimary = Color.White,
    primaryContainer = OrangeContainer,
    onPrimaryContainer = Color(0xFF3E0A00),
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = TealContainer,
    tertiary = Color(0xFFF9A825),
    background = Color(0xFFF6F6F8),
    onBackground = Color(0xFF1B1B1F),
    surface = Color.White,
    surfaceVariant = Color(0xFFF0EEF2),
    onSurfaceVariant = Color(0xFF49454E),
)

private val DarkColors = darkColorScheme(
    primary = Orange,
    onPrimary = Color.White,
    primaryContainer = OrangeDark,
    secondary = Teal,
    onSecondary = Color.White,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
)

@Composable
fun HomeFoodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
