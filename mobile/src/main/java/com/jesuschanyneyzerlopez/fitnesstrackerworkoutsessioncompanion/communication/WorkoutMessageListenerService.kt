package com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.communication

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.WearableConfig
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.WorkoutAction

/**
 * Receives workout commands from Wear OS via MessageClient (`/workout/action`).
 * Issue #3: forwards START/PAUSE/STOP to the session controller through [WorkoutCommandBus].
 */
class WorkoutMessageListenerService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path != WearableConfig.WORKOUT_ACTION_PATH) {
            super.onMessageReceived(messageEvent)
            return
        }
        val payload = runCatching { String(messageEvent.data, Charsets.UTF_8) }.getOrNull()
        val action = WorkoutAction.fromPayload(payload)
        if (action == null) {
            Log.w(TAG, "Ignoring unknown workout payload='$payload' from node=${messageEvent.sourceNodeId}")
            return
        }
        val emitted = WorkoutCommandBus.tryEmit(action)
        Log.d(TAG, "Received $action (payload='$payload', emitted=$emitted) from node=${messageEvent.sourceNodeId}")
    }

    private companion object {
        const val TAG = "WorkoutMsgListener"
    }
}
