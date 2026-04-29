package com.example.transform

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.launch
import java.lang.System.nanoTime
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <R> transform(
    context: CoroutineContext = EmptyCoroutineContext,
    scope: CoroutineScope = CoroutineScope(context),
    defaultValue: R,
    transform: @Composable () -> R
) : State<R> {
    GlobalSnapshotManager.ensureStarted()
    val clockContext = GatedFrameClock(scope, context)
    val finalContext = context + clockContext

    val recomposer = Recomposer(finalContext)
    val composition = Composition(UnitApplier, recomposer)
    scope.launch(finalContext, start = CoroutineStart.UNDISPATCHED) {
        try {
            recomposer.runRecomposeAndApplyChanges()
        } finally {
            composition.dispose()
        }
    }

    val state = mutableStateOf(defaultValue)

    composition.setContent {
        state.value = transform()
    }

    return state
}

internal class GatedFrameClock(
    scope: CoroutineScope,
    context: CoroutineContext,
) : MonotonicFrameClock {
    private val frameSends = Channel<Unit>(CONFLATED)

    init {
        scope.launch(context) {
            for (send in frameSends) sendFrame()
        }
    }

    var isRunning: Boolean = true
        set(value) {
            val started = value && !field
            field = value
            if (started) {
                sendFrame()
            }
        }

    private var lastNanos = 0L
    private var lastOffset = 0

    private fun sendFrame() {
        val timeNanos = nanoTime()

        // Since we only have millisecond resolution, ensure the nanos form always increases by
        // incrementing a nano offset if we collide with the previous timestamp.
        val offset = if (timeNanos == lastNanos) {
            lastOffset + 1
        } else {
            lastNanos = timeNanos
            0
        }
        lastOffset = offset

        clock.sendFrame(timeNanos + offset)
    }

    private val clock = BroadcastFrameClock {
        if (isRunning) frameSends.trySend(Unit).getOrThrow()
    }

    override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
        return clock.withFrameNanos(onFrame)
    }
}

private object UnitApplier : AbstractApplier<Unit>(Unit) {
    override fun insertBottomUp(index: Int, instance: Unit) {}
    override fun insertTopDown(index: Int, instance: Unit) {}
    override fun move(from: Int, to: Int, count: Int) {}
    override fun remove(index: Int, count: Int) {}
    override fun onClear() {}
}