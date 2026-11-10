package com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.communication

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.FitnessMetrics
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.WearableConfig
import kotlinx.coroutines.tasks.await

/**
 * Publishes workout metrics/state to the Wearable Data Layer (`/fitness/metrics`).
 * Issue #3: event-driven + periodic publishing (driven by the session controller).
 */
class FitnessMetricsPublisher(context: Context) {
    private val appContext = context.applicationContext
    private val dataClient = Wearable.getDataClient(appContext)

    suspend fun publish(metrics: FitnessMetrics): Result<Unit> = runCatching {
        val request = PutDataMapRequest.create(WearableConfig.FITNESS_METRICS_PATH).apply {
            dataMap.putString(WearableConfig.MetricKeys.SESSION_STATE, metrics.sessionState.wireValue)
            dataMap.putLong(WearableConfig.MetricKeys.ELAPSED_TIME_MS, metrics.elapsedTimeMs)
            dataMap.putInt(WearableConfig.MetricKeys.DAILY_GOAL, metrics.dailyGoal)
            dataMap.putLong(WearableConfig.MetricKeys.UPDATED_AT_MS, metrics.updatedAtMs)
            metrics.lastAction?.let { dataMap.putString(WearableConfig.MetricKeys.LAST_ACTION, it) }
            // Force change detection even when payload is similar.
            setUrgent()
        }.asPutDataRequest()
        dataClient.putDataItem(request).await()
        Log.d(TAG, "Published metrics: state=${metrics.sessionState} elapsed=${metrics.elapsedTimeMs} goal=${metrics.dailyGoal}")
        Unit
    }.onFailure { error ->
        Log.w(TAG, "Failed to publish /fitness/metrics", error)
    }

    private companion object {
        const val TAG = "FitnessMetricsPub"
    }
}
