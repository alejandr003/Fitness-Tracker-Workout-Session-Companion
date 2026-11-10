package com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.workout

import android.app.Application
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.communication.FitnessMetricsPublisher
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.communication.WorkoutCommandBus
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.FitnessMetrics
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
 * Screen state holder for the mobile workout session (Issues #2, #3).
 * Owns timer state and transitions; consumes Wear OS commands and publishes /fitness/metrics.
 */
class WorkoutSessionViewModel(application: Application) : AndroidViewModel(application) {
    private val metricsPublisher = FitnessMetricsPublisher(application)

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null
    private var accumulatedMs: Long = 0L
    private var activeStartElapsedRealtime: Long = 0L
    private var lastPublishedAtElapsedRealtime: Long = 0L

    init {
        viewModelScope.launch {
            WorkoutCommandBus.actions.collect { action ->
                Log.d(TAG, "Applying command from Wear OS: $action")
                handleAction(action)
            }
        }
    }

    fun onStart() = handleAction(WorkoutAction.START)
    fun onPause() = handleAction(WorkoutAction.PAUSE)
    fun onStop() = handleAction(WorkoutAction.STOP)

    fun onDailyGoalChange(minutes: Int) {
        _uiState.update { it.copy(dailyGoalMinutes = minutes.coerceIn(1, 240)) }
        publishMetrics(force = true)
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
        publishMetrics(force = true)
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
        publishMetrics(force = true)
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
        publishMetrics(force = true)
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
                delay(TICK_INTERVAL_MS)
                val now = SystemClock.elapsedRealtime()
                val elapsed = accumulatedMs + (now - activeStartElapsedRealtime)
                var shouldPublish = false
                _uiState.update { state ->
                    if (state.sessionState == WorkoutSessionState.ACTIVE) {
                        shouldPublish = (now - lastPublishedAtElapsedRealtime) >= PUBLISH_INTERVAL_MS
                        state.copy(elapsedTimeMs = elapsed)
                    } else {
                        state
                    }
                }
                if (shouldPublish) {
                    publishMetrics(force = false)
                }
            }
        }
    }

    private fun publishMetrics(force: Boolean) {
        val now = SystemClock.elapsedRealtime()
        if (!force && (now - lastPublishedAtElapsedRealtime) < PUBLISH_INTERVAL_MS) return
        lastPublishedAtElapsedRealtime = now
        val state = _uiState.value
        val metrics = FitnessMetrics(
            sessionState = state.sessionState,
            elapsedTimeMs = state.elapsedTimeMs,
            dailyGoal = state.dailyGoalMinutes,
            updatedAtMs = System.currentTimeMillis(),
            lastAction = state.lastAction,
        )
        viewModelScope.launch {
            metricsPublisher.publish(metrics)
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

    private companion object {
        const val TAG = "WorkoutSessionVM"
        const val TICK_INTERVAL_MS = 250L
        const val PUBLISH_INTERVAL_MS = 3_000L
    }
}
