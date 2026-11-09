package com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.ui.theme

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
    primary = Volt,
    onPrimary = OnVolt,
    primaryContainer = VoltContainer,
    onPrimaryContainer = OnVoltContainer,
    secondary = Ember,
    onSecondary = OnEmber,
    secondaryContainer = EmberContainer,
    onSecondaryContainer = OnEmberContainer,
    tertiary = Tide,
    onTertiary = OnTide,
    tertiaryContainer = TideContainer,
    onTertiaryContainer = OnTideContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = StateStopped,
    onError = OnStateStopped,
)

private val LightColorScheme = lightColorScheme(
    primary = VoltDim,
    onPrimary = LightBackground,
    primaryContainer = OnVoltContainer,
    onPrimaryContainer = OnVolt,
    secondary = EmberDim,
    onSecondary = LightBackground,
    secondaryContainer = OnEmberContainer,
    onSecondaryContainer = OnEmber,
    tertiary = TideDim,
    onTertiary = LightBackground,
    tertiaryContainer = OnTideContainer,
    onTertiaryContainer = OnTide,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = StateStopped,
    onError = OnStateStopped,
)

@Composable
fun FitnessTrackerWorkoutSessionCompanionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled by default to preserve the intentional Pulse Circuit palette.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
        content = content,
    )
}
