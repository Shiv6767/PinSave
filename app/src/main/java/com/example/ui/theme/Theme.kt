package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = MonolithPrimary,
    onPrimary = MonolithOnPrimary,
    secondary = MonolithSecondary,
    onSecondary = MonolithOnSecondary,
    secondaryContainer = MonolithSecondaryContainer,
    onSecondaryContainer = MonolithOnSecondaryContainer,
    background = MonolithBackground,
    onBackground = MonolithOnBackground,
    surface = MonolithSurface,
    onSurface = MonolithOnSurface,
    surfaceVariant = MonolithSurfaceVariant,
    onSurfaceVariant = MonolithOnSurfaceVariant,
    error = MonolithError,
    onError = MonolithOnError,
    errorContainer = MonolithErrorContainer,
    onErrorContainer = MonolithOnErrorContainer,
    outline = MonolithOutline,
    outlineVariant = MonolithOutlineVariant
)

// We'll stick to a minimal grayscale for dark theme too
private val DarkColorScheme = darkColorScheme(
    primary = MonolithSurfaceContainerLowest,
    onPrimary = MonolithPrimary,
    secondary = MonolithOutlineVariant,
    onSecondary = MonolithPrimary,
    secondaryContainer = MonolithSecondary,
    onSecondaryContainer = MonolithSurfaceContainerLowest,
    background = MonolithOnSurface,
    onBackground = MonolithSurface,
    surface = MonolithOnSurface,
    onSurface = MonolithSurface,
    surfaceVariant = MonolithOnSurfaceVariant,
    onSurfaceVariant = MonolithSurfaceVariant,
    error = MonolithErrorContainer,
    onError = MonolithOnErrorContainer,
    errorContainer = MonolithError,
    onErrorContainer = MonolithOnError,
    outline = MonolithOutlineVariant,
    outlineVariant = MonolithOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We disable dynamic color by default to maintain the Monolith aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

