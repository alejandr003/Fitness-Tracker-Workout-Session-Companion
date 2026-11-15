package com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.presentation.workout

import android.app.Application
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.communication.CommandDispatchResult
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.communication.WearFitnessMetricsDataSource
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.communication.WearMetricsUpdate
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.communication.WorkoutCommandDispatcher
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.WorkoutAction
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.WorkoutSessionState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Owns the Wear UI state, dispatches commands (#5) and consumes phone metrics (#6). */
class WearWorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val dispatcher = WorkoutCommandDispatcher(application)
    private val metricsDataSource = WearFitnessMetricsDataSource(application)

    private val _uiState = MutableStateFlow(WearWorkoutUiState())
    val uiState: StateFlow<WearWorkoutUiState> = _uiState.asStateFlow()

    private var metricsTickerJob: Job? = null
    private var baselineElapsedMs = 0L
    private var baselineReceivedAtElapsedRealtime = 0L
    private var lastMetricsUpdatedAtMs = 0L

    init {
        metricsDataSource.start()
        viewModelScope.launch {
            metricsDataSource.metricsUpdates.collect { update -> applyMetrics(update) }
        }
    }

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

    private fun applyMetrics(update: WearMetricsUpdate) {
        if (!update.hasAnyUiValue) return

        val updatedAtMs = update.updatedAtMs
        if (updatedAtMs != null) {
            if (updatedAtMs < lastMetricsUpdatedAtMs) {
                Log.d(
                    TAG,
                    "Ignoring stale metrics updatedAt=$updatedAtMs last=$lastMetricsUpdatedAtMs",
                )
                return
            }
            lastMetricsUpdatedAtMs = updatedAtMs
        }

        val now = SystemClock.elapsedRealtime()
        update.elapsedTimeMs?.let { elapsed ->
            baselineElapsedMs = elapsed
            baselineReceivedAtElapsedRealtime = now
        }

        _uiState.update { state ->
            val nextSessionState = update.sessionState ?: state.sessionState
            state.copy(
                sessionState = nextSessionState,
                elapsedTimeMs = update.elapsedTimeMs
                    ?: if (nextSessionState == WorkoutSessionState.IDLE) 0L else state.elapsedTimeMs,
                dailyGoalMinutes = update.dailyGoal ?: state.dailyGoalMinutes,
                lastAction = update.lastAction
                    ?: if (update.sessionState == WorkoutSessionState.IDLE) null else state.lastAction,
            )
        }

        if (_uiState.value.sessionState == WorkoutSessionState.ACTIVE) {
            if (baselineReceivedAtElapsedRealtime == 0L) {
                baselineElapsedMs = _uiState.value.elapsedTimeMs
                baselineReceivedAtElapsedRealtime = now
            }
            startMetricsTicker()
        } else {
            stopMetricsTicker()
        }
    }

    /**
     * Keeps the watch timer moving smoothly between periodic phone publications. Each incoming
     * snapshot resets the baseline, correcting any drift.
     */
    private fun startMetricsTicker() {
        if (metricsTickerJob?.isActive == true) return
        metricsTickerJob = viewModelScope.launch {
            while (true) {
                delay(METRICS_TICK_INTERVAL_MS)
                if (_uiState.value.sessionState != WorkoutSessionState.ACTIVE) break
                val elapsed = baselineElapsedMs +
                    (SystemClock.elapsedRealtime() - baselineReceivedAtElapsedRealtime)
                _uiState.update { state ->
                    if (state.sessionState == WorkoutSessionState.ACTIVE) {
                        state.copy(elapsedTimeMs = elapsed.coerceAtLeast(0L))
                    } else {
                        state
                    }
                }
            }
        }
    }

    private fun stopMetricsTicker() {
        metricsTickerJob?.cancel()
        metricsTickerJob = null
    }

    override fun onCleared() {
        stopMetricsTicker()
        metricsDataSource.stop()
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
        const val METRICS_TICK_INTERVAL_MS = 1_000L
    }
}
