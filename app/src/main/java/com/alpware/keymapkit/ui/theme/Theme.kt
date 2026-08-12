package com.alpware.keymapkit.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = KeymapBlueDark,
    onPrimary = KeymapOnBlueDark,
    primaryContainer = KeymapBlueContainerDark,
    onPrimaryContainer = KeymapOnBlueContainerDark,
    secondary = KeymapTealDark,
    onSecondary = KeymapOnTealDark,
    secondaryContainer = KeymapTealContainerDark,
    onSecondaryContainer = KeymapOnTealContainerDark,
    surface = KeymapSurfaceDark,
    background = KeymapSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = KeymapBlue,
    onPrimary = KeymapOnBlue,
    primaryContainer = KeymapBlueContainer,
    onPrimaryContainer = KeymapOnBlueContainer,
    secondary = KeymapTeal,
    onSecondary = KeymapOnTeal,
    secondaryContainer = KeymapTealContainer,
    onSecondaryContainer = KeymapOnTealContainer,
    tertiary = KeymapOrange,
    tertiaryContainer = KeymapOrangeContainer,
    surface = KeymapSurface,
    background = KeymapSurface
)

@Composable
fun KeymapKitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
