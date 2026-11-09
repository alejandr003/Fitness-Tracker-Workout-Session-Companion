package com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared

/**
 * Contrato compartido de comunicación Wear OS ↔ Mobile (Play Services Wearable Data Layer).
 *
 * - [MessageClient]: comandos de control desde el reloj al teléfono.
 * - [DataClient]: publicación/consumo reactivo de métricas y estado.
 *
 * Importante: mantener estos valores centralizados para evitar strings mágicos dispersos.
 */
object WearableConfig {
    /** Path para comandos de entrenamiento enviados por MessageClient (wear -> mobile). */
    const val WORKOUT_ACTION_PATH = "/workout/action"

    /** Path para métricas/estado publicados por DataClient (mobile -> wear). */
    const val FITNESS_METRICS_PATH = "/fitness/metrics"

    /** Keys del DataMap publicado en [FITNESS_METRICS_PATH]. */
    object MetricKeys {
        /** Estado de la sesión (ver [WorkoutSessionState]). */
        const val SESSION_STATE = "session_state"

        /** Tiempo transcurrido en milisegundos. */
        const val ELAPSED_TIME_MS = "elapsed_time_ms"

        /** Meta diaria configurada (unidad definida por la app, p. ej. minutos o pasos). */
        const val DAILY_GOAL = "daily_goal"

        /** Marca de tiempo de la última actualización (epoch millis) para forzar frescura del DataItem. */
        const val UPDATED_AT_MS = "updated_at_ms"

        /** Acción/comando asociado al último cambio (opcional, para depuración/UI). */
        const val LAST_ACTION = "last_action"
    }

    /** Payloads de comando enviados por MessageClient en [WORKOUT_ACTION_PATH]. */
    object ActionPayloads {
        const val START = "START"
        const val PAUSE = "PAUSE"
        const val STOP = "STOP"
    }
}
