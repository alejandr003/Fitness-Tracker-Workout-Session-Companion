package com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.presentation.workout

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.communication.CommandDispatchResult
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.communication.WorkoutCommandDispatcher
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.WorkoutAction
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.WorkoutSessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Owns the Wear UI state and dispatches Start/Pause/Stop to the phone (Issue #5). */
class WearWorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val dispatcher = WorkoutCommandDispatcher(application)

    private val _uiState = MutableStateFlow(WearWorkoutUiState())
    val uiState: StateFlow<WearWorkoutUiState> = _uiState.asStateFlow()

    fun onStart() = dispatch(WorkoutAction.START)
    fun onPause() = dispatch(WorkoutAction.PAUSE)
    fun onStop() = dispatch(WorkoutAction.STOP)

    private fun dispatch(action: WorkoutAction) {
        if (_uiState.value.commandFeedback == CommandFeedback.SENDING) return
        _uiState.update {
            it.copy(
                commandFeedback = CommandFeedback.SENDING,
                lastCommand = action,
            )
        }
        viewModelScope.launch {
            val result = dispatcher.dispatch(action)
            Log.d(TAG, "Dispatch result for ${action.payload}: $result")
            _uiState.update { state ->
                val sent = result is CommandDispatchResult.Sent
                state.copy(
                    sessionState = if (sent) {
                        optimisticSessionState(state.sessionState, action)
                    } else {
                        state.sessionState
                    },
                    commandFeedback = when (result) {
                        is CommandDispatchResult.Sent -> CommandFeedback.SENT
                        CommandDispatchResult.NoNode -> CommandFeedback.NO_NODE
                        is CommandDispatchResult.Failed -> CommandFeedback.FAILED
                    },
                )
            }
        }
    }

    private fun optimisticSessionState(
        current: WorkoutSessionState,
        action: WorkoutAction,
    ): WorkoutSessionState =
        when (action) {
            WorkoutAction.START -> WorkoutSessionState.ACTIVE
            WorkoutAction.PAUSE -> if (current == WorkoutSessionState.ACTIVE) {
                WorkoutSessionState.PAUSED
            } else {
                current
            }
            WorkoutAction.STOP -> if (
                current == WorkoutSessionState.ACTIVE ||
                current == WorkoutSessionState.PAUSED
            ) {
                WorkoutSessionState.STOPPED
            } else {
                current
            }
        }

    private companion object {
        const val TAG = "WearWorkoutVM"
    }
}
