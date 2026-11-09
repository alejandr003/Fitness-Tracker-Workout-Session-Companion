package com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.workout

import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.WorkoutSessionState

/** Immutable UI state for the mobile workout dashboard (Issue #2). */
data class WorkoutUiState(
    val sessionState: WorkoutSessionState = WorkoutSessionState.IDLE,
    val elapsedTimeMs: Long = 0L,
    val dailyGoalMinutes: Int = 30,
    val lastAction: String? = null,
)
