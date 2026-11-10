package com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.communication

import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.WorkoutAction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Simple in-process bridge for commands received by [WorkoutMessageListenerService].
 * Issue #3 -> consumed by [com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.workout.WorkoutSessionViewModel].
 */
object WorkoutCommandBus {
    private val _actions = MutableSharedFlow<WorkoutAction>(extraBufferCapacity = 16)
    val actions: SharedFlow<WorkoutAction> = _actions.asSharedFlow()

    fun tryEmit(action: WorkoutAction): Boolean = _actions.tryEmit(action)
}
