package com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.presentation.workout

import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.FitnessMetrics
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.WorkoutSessionState

/** UI state rendered by the Wear OS workout screen (Issue #4). */
data class WearWorkoutUiState(
    val sessionState: WorkoutSessionState = WorkoutSessionState.IDLE,
    val elapsedTimeMs: Long = 0L,
    val dailyGoalMinutes: Int = 30,
    val lastAction: String? = null,
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
