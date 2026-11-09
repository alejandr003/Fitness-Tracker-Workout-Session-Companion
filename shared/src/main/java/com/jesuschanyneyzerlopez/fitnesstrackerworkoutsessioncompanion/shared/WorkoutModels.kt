package com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared

/**
 * Acciones de control de sesión enviadas desde Wear OS al teléfono.
 * Se transportan como payload de [WearableConfig.WORKOUT_ACTION_PATH].
 */
enum class WorkoutAction(val payload: String) {
    START(WearableConfig.ActionPayloads.START),
    PAUSE(WearableConfig.ActionPayloads.PAUSE),
    STOP(WearableConfig.ActionPayloads.STOP);

    companion object {
        fun fromPayload(value: String?): WorkoutAction? =
            entries.firstOrNull { it.payload == value?.trim()?.uppercase() }
    }
}

/** Estado lógico de la sesión de entrenamiento. */
enum class WorkoutSessionState(val wireValue: String) {
    IDLE("IDLE"),
    ACTIVE("ACTIVE"),
    PAUSED("PAUSED"),
    STOPPED("STOPPED");

    companion object {
        fun fromWireValue(value: String?): WorkoutSessionState =
            entries.firstOrNull { it.wireValue == value } ?: IDLE
    }
}

/**
 * Modelo compartido de métricas/estado publicado por [DataClient] y consumido por Wear OS.
 *
 * Este DTO es "serializable" en el sentido práctico del proyecto: puede mapearse
 * de forma determinística a/desde un `DataMap` usando [WearableConfig.MetricKeys].
 */
data class FitnessMetrics(
    val sessionState: WorkoutSessionState = WorkoutSessionState.IDLE,
    val elapsedTimeMs: Long = 0L,
    val dailyGoal: Int = 0,
    val updatedAtMs: Long = 0L,
    val lastAction: String? = null,
)
