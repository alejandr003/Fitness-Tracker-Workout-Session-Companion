package com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.workout

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.WorkoutAction
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.WorkoutSessionState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Screen state holder for the mobile workout session (Issue #2).
 * Owns timer state and valid transitions; exposes a command entry point for Issue #3 (Wearable listener).
 */
class WorkoutSessionViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null
    private var accumulatedMs: Long = 0L
    private var activeStartElapsedRealtime: Long = 0L

    fun onStart() = handleAction(WorkoutAction.START)
    fun onPause() = handleAction(WorkoutAction.PAUSE)
    fun onStop() = handleAction(WorkoutAction.STOP)

    fun onDailyGoalChange(minutes: Int) {
        _uiState.update { it.copy(dailyGoalMinutes = minutes.coerceIn(1, 240)) }
    }

    /**
     * Entry point for commands coming from UI or from the Wearable MessageClient listener (Issue #3).
     */
    fun handleAction(action: WorkoutAction) {
        when (action) {
            WorkoutAction.START -> startSession()
            WorkoutAction.PAUSE -> pauseSession()
            WorkoutAction.STOP -> stopSession()
        }
    }

    /** Convenience for raw payloads received via MessageClient (`START`/`PAUSE`/`STOP`). */
    fun handleActionPayload(payload: String?) {
        WorkoutAction.fromPayload(payload)?.let(::handleAction)
    }

    private fun startSession() {
        val current = _uiState.value.sessionState
        if (current == WorkoutSessionState.ACTIVE) return
        if (current == WorkoutSessionState.IDLE || current == WorkoutSessionState.STOPPED) {
            accumulatedMs = 0L
        }
        activeStartElapsedRealtime = SystemClock.elapsedRealtime()
        _uiState.update {
            it.copy(
                sessionState = WorkoutSessionState.ACTIVE,
                elapsedTimeMs = accumulatedMs,
                lastAction = WorkoutAction.START.payload,
            )
        }
        startTicker()
    }

    private fun pauseSession() {
        if (_uiState.value.sessionState != WorkoutSessionState.ACTIVE) return
        accumulateElapsed()
        stopTicker()
        _uiState.update {
            it.copy(
                sessionState = WorkoutSessionState.PAUSED,
                elapsedTimeMs = accumulatedMs,
                lastAction = WorkoutAction.PAUSE.payload,
            )
        }
    }

    private fun stopSession() {
        val current = _uiState.value.sessionState
        if (current == WorkoutSessionState.IDLE) return
        if (current == WorkoutSessionState.ACTIVE) {
            accumulateElapsed()
        }
        stopTicker()
        _uiState.update {
            it.copy(
                sessionState = WorkoutSessionState.STOPPED,
                elapsedTimeMs = accumulatedMs,
                lastAction = WorkoutAction.STOP.payload,
            )
        }
    }

    private fun accumulateElapsed() {
        if (activeStartElapsedRealtime != 0L) {
            accumulatedMs += (SystemClock.elapsedRealtime() - activeStartElapsedRealtime)
            activeStartElapsedRealtime = 0L
        }
    }

    private fun startTicker() {
        stopTicker()
        tickerJob = viewModelScope.launch {
            while (true) {
                delay(250)
                val elapsed = accumulatedMs + (SystemClock.elapsedRealtime() - activeStartElapsedRealtime)
                _uiState.update { state ->
                    if (state.sessionState == WorkoutSessionState.ACTIVE) {
                        state.copy(elapsedTimeMs = elapsed)
                    } else {
                        state
                    }
                }
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    override fun onCleared() {
        stopTicker()
        super.onCleared()
    }
}
