package com.example.transform

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.launch
import java.lang.System.nanoTime
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Executes a Composable [transform] block in a headless Composition and returns its result as a [State].
 *
 * This function bridges reactive Compose state (or Flows collected as state) into a continuously 
 * updated [State] object outside a traditional Compose UI hierarchy. The [transform] block is 
 * automatically recomposed whenever any Compose state it reads is updated, and the newly 
 * returned value is pushed into the resulting [State].
 *
 * @param context The [CoroutineContext] to use for the composition's recomposer. Defaults to [EmptyCoroutineContext].
 * @param scope The [CoroutineScope] in which the recomposer and frame clock will run. Defaults to a scope using [context].
 * @param defaultValue The initial value to populate the returned [State] before the first composition completes.
 * @param onUpdate The [Composable] block that computes the value of type [R]. It will be recomposed when its state dependencies change.
 * @return A [State] containing the latest result of the [transform] block.
 */
fun <R> transform(
    context: CoroutineContext = EmptyCoroutineContext,
    scope: CoroutineScope = CoroutineScope(context),
    defaultValue: R,
    onUpdate: @Composable () -> R
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
        state.value = onUpdate()
    }

    return state
}

/**
 * A Composable [transform] that remembers the resulting [State] and ties the
 * headless composition to the current [CoroutineScope] provided by the Compose lifecycle.
 *
 * @param defaultValue The initial value to populate the returned [State] before the first composition completes.
 * @param onUpdate The [Composable] block that computes the value of type [R].
 * @return A [State] containing the latest result of the [transform] block.
 */
@Composable
fun <R> transform(
    defaultValue: R,
    onUpdate: @Composable () -> R
): State<R> {
    val scope = rememberCoroutineScope()
    return remember(scope) {
        transform(scope = scope, defaultValue = defaultValue, onUpdate = onUpdate)
    }
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