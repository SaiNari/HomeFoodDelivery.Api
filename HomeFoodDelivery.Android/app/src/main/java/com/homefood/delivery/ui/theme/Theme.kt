package com.homefood.delivery.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Orange = Color(0xFFFF6E40)
private val OrangeDark = Color(0xFFE64A19)
private val Teal = Color(0xFF00897B)

private val LightColors = lightColorScheme(
    primary = Orange,
    onPrimary = Color.White,
    secondary = Teal,
    onSecondary = Color.White,
    background = Color(0xFFF7F7F7),
)

private val DarkColors = darkColorScheme(
    primary = Orange,
    onPrimary = Color.White,
    secondary = Teal,
    primaryContainer = OrangeDark,
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
