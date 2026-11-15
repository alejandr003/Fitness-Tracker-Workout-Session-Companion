package com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.presentation.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.R
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.presentation.theme.FitnessTrackerWorkoutSessionCompanionTheme
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.WorkoutAction
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.WorkoutSessionState
import kotlin.math.roundToInt

@Composable
fun WearWorkoutRoute(
    modifier: Modifier = Modifier,
    viewModel: WearWorkoutViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    WearWorkoutScreen(
        state = state,
        onStart = viewModel::onStart,
        onPause = viewModel::onPause,
        onStop = viewModel::onStop,
        modifier = modifier,
    )
}

@Composable
fun WearWorkoutScreen(
    state: WearWorkoutUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusColor = sessionStatusColor(state.sessionState)
    val isSending = state.commandFeedback == CommandFeedback.SENDING
    val canStart = state.sessionState != WorkoutSessionState.ACTIVE && !isSending
    val canPause = state.sessionState == WorkoutSessionState.ACTIVE && !isSending
    val canStop = (state.sessionState == WorkoutSessionState.ACTIVE ||
        state.sessionState == WorkoutSessionState.PAUSED) && !isSending

    AppScaffold {
        val listState = rememberTransformingLazyColumnState()
        val transformationSpec = rememberTransformationSpec()
        ScreenScaffold(scrollState = listState, modifier = modifier) { contentPadding ->
            TransformingLazyColumn(
                contentPadding = contentPadding,
                state = listState,
                modifier = Modifier.fillMaxWidth(),
            ) {
                item {
                    ListHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                    ) {
                        Text(text = stringResource(R.string.wear_workout_title))
                    }
                }
                item {
                    SessionStatusPanel(
                        sessionState = state.sessionState,
                        statusColor = statusColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .transformedHeight(this, transformationSpec),
                    )
                }
                item {
                    TimerPanel(
                        elapsedTimeMs = state.elapsedTimeMs,
                        sessionState = state.sessionState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .transformedHeight(this, transformationSpec),
                    )
                }
                item {
                    DailyGoalPanel(
                        dailyGoalMinutes = state.dailyGoalMinutes,
                        elapsedTimeMs = state.elapsedTimeMs,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .transformedHeight(this, transformationSpec),
                    )
                }
                item {
                    Button(
                        onClick = onStart,
                        enabled = canStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                    ) {
                        Text(
                            text = stringResource(
                                if (state.sessionState == WorkoutSessionState.PAUSED) {
                                    R.string.wear_action_resume
                                } else {
                                    R.string.wear_action_start
                                },
                            ),
                        )
                    }
                }
                item {
                    Button(
                        onClick = onPause,
                        enabled = canPause,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                    ) {
                        Text(text = stringResource(R.string.wear_action_pause))
                    }
                }
                item {
                    Button(
                        onClick = onStop,
                        enabled = canStop,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                    ) {
                        Text(text = stringResource(R.string.wear_action_stop))
                    }
                }
                if (state.commandFeedback != CommandFeedback.NONE) {
                    item {
                        Text(
                            text = commandFeedbackMessage(
                                feedback = state.commandFeedback,
                                lastCommand = state.lastCommand,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = commandFeedbackColor(state.commandFeedback),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .transformedHeight(this, transformationSpec),
                        )
                    }
                }
                state.lastAction?.let { action ->
                    item {
                        Text(
                            text = stringResource(R.string.wear_last_action, action),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .transformedHeight(this, transformationSpec),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionStatusPanel(
    sessionState: WorkoutSessionState,
    statusColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = stringResource(R.string.wear_status_label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(sessionStatusLabelRes(sessionState)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = statusColor,
            )
        }
    }
}

@Composable
private fun TimerPanel(
    elapsedTimeMs: Long,
    sessionState: WorkoutSessionState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.wear_timer_label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = formatElapsed(elapsedTimeMs),
            style = MaterialTheme.typography.displaySmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (sessionState == WorkoutSessionState.ACTIVE) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            },
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DailyGoalPanel(
    dailyGoalMinutes: Int,
    elapsedTimeMs: Long,
    modifier: Modifier = Modifier,
) {
    val elapsedMinutes = (elapsedTimeMs / 60_000L).toInt()
    val progress = if (dailyGoalMinutes > 0) {
        elapsedMinutes.toFloat() / dailyGoalMinutes
    } else {
        0f
    }.coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.wear_goal_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.wear_goal_value, dailyGoalMinutes),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        ProgressTrack(progress = progress)
        Text(
            text = stringResource(
                R.string.wear_progress,
                elapsedMinutes,
                dailyGoalMinutes,
                (progress * 100).roundToInt(),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProgressTrack(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.tertiary),
        )
    }
}

@Composable
private fun commandFeedbackMessage(feedback: CommandFeedback, lastCommand: WorkoutAction?): String =
    when (feedback) {
        CommandFeedback.NONE -> ""
        CommandFeedback.SENDING -> stringResource(
            R.string.wear_command_sending,
            lastCommand?.payload.orEmpty(),
        )
        CommandFeedback.SENT -> stringResource(
            R.string.wear_command_sent,
            lastCommand?.payload.orEmpty(),
        )
        CommandFeedback.NO_NODE -> stringResource(R.string.wear_command_no_node)
        CommandFeedback.FAILED -> stringResource(
            R.string.wear_command_failed,
            lastCommand?.payload.orEmpty(),
        )
    }

@Composable
private fun commandFeedbackColor(feedback: CommandFeedback): Color =
    when (feedback) {
        CommandFeedback.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
        CommandFeedback.SENDING, CommandFeedback.SENT -> MaterialTheme.colorScheme.tertiary
        CommandFeedback.NO_NODE, CommandFeedback.FAILED -> MaterialTheme.colorScheme.error
    }

@Composable
private fun sessionStatusColor(sessionState: WorkoutSessionState): Color =
    when (sessionState) {
        WorkoutSessionState.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
        WorkoutSessionState.ACTIVE -> MaterialTheme.colorScheme.primary
        WorkoutSessionState.PAUSED -> MaterialTheme.colorScheme.secondary
        WorkoutSessionState.STOPPED -> MaterialTheme.colorScheme.error
    }

private fun sessionStatusLabelRes(sessionState: WorkoutSessionState): Int =
    when (sessionState) {
        WorkoutSessionState.IDLE -> R.string.wear_status_idle
        WorkoutSessionState.ACTIVE -> R.string.wear_status_active
        WorkoutSessionState.PAUSED -> R.string.wear_status_paused
        WorkoutSessionState.STOPPED -> R.string.wear_status_stopped
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

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun WearWorkoutIdlePreview() {
    FitnessTrackerWorkoutSessionCompanionTheme {
        WearWorkoutScreen(
            state = WearWorkoutUiState(),
            onStart = {},
            onPause = {},
            onStop = {},
        )
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun WearWorkoutActivePreview() {
    FitnessTrackerWorkoutSessionCompanionTheme {
        WearWorkoutScreen(
            state = WearWorkoutUiState(
                sessionState = WorkoutSessionState.ACTIVE,
                elapsedTimeMs = 12 * 60_000L + 34_000L,
                dailyGoalMinutes = 30,
                lastAction = "START",
                commandFeedback = CommandFeedback.SENT,
                lastCommand = WorkoutAction.START,
            ),
            onStart = {},
            onPause = {},
            onStop = {},
        )
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun WearWorkoutNoNodePreview() {
    FitnessTrackerWorkoutSessionCompanionTheme {
        WearWorkoutScreen(
            state = WearWorkoutUiState(
                commandFeedback = CommandFeedback.NO_NODE,
                lastCommand = WorkoutAction.START,
            ),
            onStart = {},
            onPause = {},
            onStop = {},
        )
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun WearWorkoutPausedPreview() {
    FitnessTrackerWorkoutSessionCompanionTheme {
        WearWorkoutScreen(
            state = WearWorkoutUiState(
                sessionState = WorkoutSessionState.PAUSED,
                elapsedTimeMs = 12 * 60_000L + 34_000L,
                dailyGoalMinutes = 30,
                lastAction = "PAUSE",
            ),
            onStart = {},
            onPause = {},
            onStop = {},
        )
    }
}
