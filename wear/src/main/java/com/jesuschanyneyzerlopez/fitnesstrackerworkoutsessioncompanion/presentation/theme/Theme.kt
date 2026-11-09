package com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

private val PulseCircuitColorScheme = ColorScheme(
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
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainer = DarkSurface,
    surfaceContainerLow = DarkSurface,
    surfaceContainerHigh = DarkSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = StateStopped,
    onError = OnStateStopped,
)

@Composable
fun FitnessTrackerWorkoutSessionCompanionTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = PulseCircuitColorScheme,
        content = content,
    )
}
