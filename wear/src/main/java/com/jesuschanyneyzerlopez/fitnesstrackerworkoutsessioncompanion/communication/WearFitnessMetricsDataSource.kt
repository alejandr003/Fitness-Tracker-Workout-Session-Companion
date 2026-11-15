package com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.communication

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.WearableConfig
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.WorkoutSessionState
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Partial snapshot received from `/fitness/metrics`.
 *
 * Every field is nullable so incomplete/partial payloads can update only the values they contain
 * instead of overwriting the rest of the UI with defaults.
 */
data class WearMetricsUpdate(
    val sessionState: WorkoutSessionState? = null,
    val elapsedTimeMs: Long? = null,
    val dailyGoal: Int? = null,
    val lastAction: String? = null,
    val updatedAtMs: Long? = null,
) {
    val hasAnyUiValue: Boolean
        get() = sessionState != null ||
            elapsedTimeMs != null ||
            dailyGoal != null ||
            lastAction != null
}

/**
 * Listens for `/fitness/metrics` changes published by the phone (Issue #6).
 *
 * Uses [DataClient.OnDataChangedListener] and exposes the parsed values as a [Flow] so the
 * ViewModel can apply them on its own coroutine scope and Compose can render them reactively.
 */
class WearFitnessMetricsDataSource(context: Context) : DataClient.OnDataChangedListener {
    private val dataClient = Wearable.getDataClient(context.applicationContext)
    private val updates = MutableSharedFlow<WearMetricsUpdate>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val metricsUpdates: Flow<WearMetricsUpdate> = updates.asSharedFlow()

    fun start() {
        dataClient.addListener(this).addOnFailureListener { error ->
            Log.w(TAG, "Could not register DataClient listener", error)
        }
    }

    fun stop() {
        dataClient.removeListener(this).addOnFailureListener { error ->
            Log.w(TAG, "Could not unregister DataClient listener", error)
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        try {
            for (event in dataEvents) {
                if (event.type != DataEvent.TYPE_CHANGED) continue
                val dataItem = event.dataItem ?: continue
                if (dataItem.uri.path != WearableConfig.FITNESS_METRICS_PATH) continue

                val update = parseMetrics(dataItem) ?: continue
                Log.d(TAG, "Received metrics: $update")
                updates.tryEmit(update)
            }
        } catch (error: Exception) {
            Log.w(TAG, "Failed to process DataClient event", error)
        } finally {
            dataEvents.release()
        }
    }

    private fun parseMetrics(dataItem: DataItem): WearMetricsUpdate? {
        val dataMap = try {
            DataMapItem.fromDataItem(dataItem).dataMap
        } catch (error: Exception) {
            Log.w(TAG, "Could not read DataMap for ${dataItem.uri}", error)
            return null
        }
        return parseMetrics(dataMap)
    }

    private fun parseMetrics(dataMap: DataMap): WearMetricsUpdate? {
        val update = WearMetricsUpdate(
            sessionState = parseSessionState(dataMap),
            elapsedTimeMs = dataMap.longOrNull(WearableConfig.MetricKeys.ELAPSED_TIME_MS)
                ?.takeIf { it >= 0L },
            dailyGoal = dataMap.intOrNull(WearableConfig.MetricKeys.DAILY_GOAL)
                ?.coerceIn(0, MAX_DAILY_GOAL_MINUTES),
            lastAction = dataMap.stringOrNull(WearableConfig.MetricKeys.LAST_ACTION)
                ?.takeIf { it.isNotBlank() },
            updatedAtMs = dataMap.longOrNull(WearableConfig.MetricKeys.UPDATED_AT_MS)
                ?.takeIf { it >= 0L },
        )
        if (!update.hasAnyUiValue) {
            Log.w(TAG, "Ignoring metrics payload without known UI fields")
            return null
        }
        return update
    }

    private fun parseSessionState(dataMap: DataMap): WorkoutSessionState? {
        val rawState = dataMap.stringOrNull(WearableConfig.MetricKeys.SESSION_STATE) ?: return null
        return WorkoutSessionState.entries.firstOrNull {
            it.wireValue == rawState.trim().uppercase()
        } ?: run {
            Log.w(TAG, "Ignoring unknown session state '$rawState'")
            null
        }
    }

    private fun DataMap.stringOrNull(key: String): String? =
        if (!containsKey(key)) null else runCatching { getString(key) }.getOrNull()

    private fun DataMap.longOrNull(key: String): Long? =
        if (!containsKey(key)) null else runCatching { getLong(key) }.getOrNull()

    private fun DataMap.intOrNull(key: String): Int? =
        if (!containsKey(key)) null else runCatching { getInt(key) }.getOrNull()

    private companion object {
        const val TAG = "WearMetricsListener"
        const val MAX_DAILY_GOAL_MINUTES = 240
    }
}
