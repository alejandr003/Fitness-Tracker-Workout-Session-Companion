package com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.presentation.workout

import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.FitnessMetrics
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.WorkoutAction
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.WorkoutSessionState

/** Feedback for the command dispatched from Wear OS to the phone (Issue #5). */
enum class CommandFeedback {
    NONE,
    SENDING,
    SENT,
    NO_NODE,
    FAILED,
}

/** UI state rendered by the Wear OS workout screen (Issues #4 and #5). */
data class WearWorkoutUiState(
    val sessionState: WorkoutSessionState = WorkoutSessionState.IDLE,
    val elapsedTimeMs: Long = 0L,
    val dailyGoalMinutes: Int = 30,
    val lastAction: String? = null,
    val commandFeedback: CommandFeedback = CommandFeedback.NONE,
    val lastCommand: WorkoutAction? = null,
) {
    companion object {
        fun fromMetrics(metrics: FitnessMetrics): WearWorkoutUiState =
            WearWorkoutUiState(
                sessionState = metrics.sessionState,
                elapsedTimeMs = metrics.elapsedTimeMs,
                dailyGoalMinutes = metrics.dailyGoal,
                lastAction = metrics.lastAction,
            )
    }
}
