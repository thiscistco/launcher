package com.workos.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFB0B0B0),
    background = Color(0xFF050505),
    surface = Color(0xFF0A0A0A),
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun WorkOSTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkScheme, content = content)
}
