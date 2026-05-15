package com.example.trainwise.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Orange,
    secondary = GrayText,
    tertiary = Orange,
    background = DarkBackground,
    surface = DarkBackground,
    surfaceVariant = CardBackground,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = White,
    onSurface = White,
    onSurfaceVariant = GrayText,
    secondaryContainer = SurfaceColor,
    onSecondaryContainer = White
)

private val LightColorScheme = lightColorScheme(
    primary = Orange,
    secondary = GrayText,
    tertiary = Orange,
    background = LightBackground,
    surface = LightBackground,
    surfaceVariant = LightCardBackground,
    onPrimary = White,
    onSecondary = Black,
    onTertiary = White,
    onBackground = Black,
    onSurface = Black,
    onSurfaceVariant = DarkGrayText,
    secondaryContainer = LightSurfaceColor,
    onSecondaryContainer = Black
)

@Composable
fun TrainWiseTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
