package com.example.transform

import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Mechanism for transform sessions to start a monitor of global snapshot state writes in order to
 * schedule periodic dispatch of apply notifications. Sessions should call [ensureStarted] during
 * setup to initialize periodic global snapshot notifications (which are necessary in order for
 * recompositions to be scheduled in response to state changes). These will be sent on
 * Dispatchers.Default. This is based on [androidx.compose.ui.platform.GlobalSnapshotManager].
 */
object GlobalSnapshotManager {
    private val started = AtomicBoolean(false)
    private val sent = AtomicBoolean(false)

    fun ensureStarted() {
        if (started.compareAndSet(false, true)) {
            val channel = Channel<Unit>(1)
            CoroutineScope(Dispatchers.Main).launch {
                channel.consumeEach {
                    sent.set(false)
                    Snapshot.sendApplyNotifications()
                }
            }
            Snapshot.registerGlobalWriteObserver {
                if (sent.compareAndSet(false, true)) {
                    channel.trySend(Unit)
                }
            }
        }
    }
}
