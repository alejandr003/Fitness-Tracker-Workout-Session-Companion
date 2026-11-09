package com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.FitnessPalette
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.WorkoutSessionState
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.ui.theme.FitnessTrackerWorkoutSessionCompanionTheme
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.workout.WorkoutSessionViewModel
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.workout.WorkoutUiState
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

@Composable
fun WorkoutDashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: WorkoutSessionViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    WorkoutDashboardContent(
        state = state,
        onStart = viewModel::onStart,
        onPause = viewModel::onPause,
        onStop = viewModel::onStop,
        onDailyGoalChange = viewModel::onDailyGoalChange,
        modifier = modifier,
    )
}

@Composable
fun WorkoutDashboardContent(
    state: WorkoutUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onDailyGoalChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusColor = sessionStatusColor(state.sessionState)
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Sesión de entrenamiento",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Dashboard móvil · listo para recibir comandos del reloj (Issue #3)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SessionStatusCard(state = state, statusColor = statusColor)
            TimerCard(state = state, statusColor = statusColor)
            DailyGoalCard(
                dailyGoalMinutes = state.dailyGoalMinutes,
                elapsedTimeMs = state.elapsedTimeMs,
                onDailyGoalChange = onDailyGoalChange,
            )
            ControlsCard(
                sessionState = state.sessionState,
                onStart = onStart,
                onPause = onPause,
                onStop = onStop,
            )

            state.lastAction?.let { action ->
                Text(
                    text = "Última acción: $action",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SessionStatusCard(state: WorkoutUiState, statusColor: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(statusColor),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Estado",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = sessionStatusLabel(state.sessionState),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                )
            }
        }
    }
}

@Composable
private fun TimerCard(state: WorkoutUiState, statusColor: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Cronómetro",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatElapsed(state.elapsedTimeMs),
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor.takeIf { state.sessionState == WorkoutSessionState.ACTIVE }
                    ?: MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DailyGoalCard(
    dailyGoalMinutes: Int,
    elapsedTimeMs: Long,
    onDailyGoalChange: (Int) -> Unit,
) {
    val elapsedMinutes = (elapsedTimeMs / 60_000L).toInt()
    val progress = if (dailyGoalMinutes > 0) (elapsedMinutes.toFloat() / dailyGoalMinutes).coerceIn(0f, 1f) else 0f
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Meta diaria",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "$dailyGoalMinutes min",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Slider(
                value = dailyGoalMinutes.toFloat(),
                onValueChange = { onDailyGoalChange(it.roundToInt()) },
                valueRange = 5f..120f,
                steps = 22,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.secondary,
                    activeTrackColor = MaterialTheme.colorScheme.secondary,
                ),
            )
            Text(
                text = "Progreso: $elapsedMinutes / $dailyGoalMinutes min (${(progress * 100).roundToInt()}%)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ControlsCard(
    sessionState: WorkoutSessionState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
) {
    val isActive = sessionState == WorkoutSessionState.ACTIVE
    val canStart = sessionState != WorkoutSessionState.ACTIVE
    val canPause = sessionState == WorkoutSessionState.ACTIVE
    val canStop = sessionState == WorkoutSessionState.ACTIVE || sessionState == WorkoutSessionState.PAUSED

    Card(shape = RoundedCornerShape(20.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Controles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = onStart,
                    enabled = canStart,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(if (sessionState == WorkoutSessionState.PAUSED) "Reanudar" else "Start")
                }
                OutlinedButton(
                    onClick = onPause,
                    enabled = canPause,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Pause")
                }
                Button(
                    onClick = onStop,
                    enabled = canStop,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text("Stop")
                }
            }
            if (isActive) {
                Text(
                    text = "Sesión activa: el cronómetro está corriendo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun sessionStatusLabel(state: WorkoutSessionState): String = when (state) {
    WorkoutSessionState.IDLE -> "Inactivo"
    WorkoutSessionState.ACTIVE -> "Activo"
    WorkoutSessionState.PAUSED -> "Pausado"
    WorkoutSessionState.STOPPED -> "Detenido"
}

private fun sessionStatusColor(state: WorkoutSessionState): Color = when (state) {
    WorkoutSessionState.IDLE -> Color(FitnessPalette.STATE_IDLE)
    WorkoutSessionState.ACTIVE -> Color(FitnessPalette.STATE_ACTIVE)
    WorkoutSessionState.PAUSED -> Color(FitnessPalette.STATE_PAUSED)
    WorkoutSessionState.STOPPED -> Color(FitnessPalette.STATE_STOPPED)
}

private fun formatElapsed(elapsedMs: Long): String {
    val totalSeconds = (elapsedMs / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutDashboardContentPreview() {
    FitnessTrackerWorkoutSessionCompanionTheme(darkTheme = false, dynamicColor = false) {
        WorkoutDashboardContent(
            state = WorkoutUiState(
                sessionState = WorkoutSessionState.ACTIVE,
                elapsedTimeMs = 12 * 60_000L + 34_000L,
                dailyGoalMinutes = 30,
                lastAction = "START",
            ),
            onStart = {},
            onPause = {},
            onStop = {},
            onDailyGoalChange = {},
        )
    }
}
