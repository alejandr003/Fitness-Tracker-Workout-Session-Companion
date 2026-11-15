package com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.communication

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.WearableConfig
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared.WorkoutAction
import java.util.concurrent.TimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

sealed interface CommandDispatchResult {
    data class Sent(val nodeId: String, val requestId: Int) : CommandDispatchResult
    data object NoNode : CommandDispatchResult
    data class Failed(val cause: Throwable) : CommandDispatchResult
}

/**
 * Sends workout commands from Wear OS to the phone via MessageClient (`/workout/action`).
 * Issue #5: node discovery with NodeClient, payloads from the shared contract and basic error handling.
 */
class WorkoutCommandDispatcher(context: Context) {
    private val appContext = context.applicationContext
    private val nodeClient = Wearable.getNodeClient(appContext)
    private val messageClient = Wearable.getMessageClient(appContext)

    suspend fun dispatch(action: WorkoutAction): CommandDispatchResult {
        val nodes = try {
            withTimeoutOrNull(NODE_DISCOVERY_TIMEOUT_MS) {
                nodeClient.connectedNodes.await()
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.w(TAG, "Node discovery failed", error)
            return CommandDispatchResult.Failed(error)
        }

        if (nodes == null) {
            Log.w(TAG, "Node discovery timed out after ${NODE_DISCOVERY_TIMEOUT_MS}ms")
            return CommandDispatchResult.Failed(TimeoutException("Node discovery timed out"))
        }
        Log.d(
            TAG,
            "Connected nodes: ${nodes.joinToString { "${it.displayName}(${it.id}), nearby=${it.isNearby}" }}",
        )
        if (nodes.isEmpty()) {
            Log.w(TAG, "No connected nodes; cannot send ${action.payload}")
            return CommandDispatchResult.NoNode
        }

        val target = selectPhoneNode(nodes)
        val payload = action.payload.toByteArray(Charsets.UTF_8)
        val requestId = try {
            withTimeoutOrNull(SEND_TIMEOUT_MS) {
                messageClient
                    .sendMessage(target.id, WearableConfig.WORKOUT_ACTION_PATH, payload)
                    .await()
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.w(TAG, "Failed to send ${action.payload} to node=${target.id}", error)
            return CommandDispatchResult.Failed(error)
        }

        if (requestId == null) {
            Log.w(TAG, "Sending ${action.payload} timed out after ${SEND_TIMEOUT_MS}ms")
            return CommandDispatchResult.Failed(TimeoutException("Send timed out"))
        }
        Log.d(
            TAG,
            "Sent ${action.payload} to ${target.displayName}(${target.id}) requestId=$requestId",
        )
        return CommandDispatchResult.Sent(target.id, requestId)
    }

    private fun selectPhoneNode(nodes: List<Node>): Node =
        nodes.firstOrNull { it.isNearby } ?: nodes.first()

    private companion object {
        const val TAG = "WearCommandDispatcher"
        const val NODE_DISCOVERY_TIMEOUT_MS = 5_000L
        const val SEND_TIMEOUT_MS = 5_000L
    }
}
