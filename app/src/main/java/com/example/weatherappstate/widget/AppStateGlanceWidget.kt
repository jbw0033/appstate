package com.example.weatherappstate.widget

import android.content.Context
import androidx.appstate.transform.transform
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.EmptyCoroutineContext

abstract class AppStateGlanceWidget<T: Any> : GlanceAppWidget() {
    val map = mutableMapOf<GlanceId, MutableState<T>>()
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    internal fun startTransform(context: Context) {
        transform(context = EmptyCoroutineContext, scope = scope, defaultValue = Unit, onUpdate = {
            val glanceIds by produceState(listOf()) {
                value = GlanceAppWidgetManager(context).getGlanceIds(this@AppStateGlanceWidget::class.java)
            }
            glanceIds.forEach { id ->
                val data = provideData(context, id)
                val state = map.getOrPut(id) { mutableStateOf(data) }
                state.value = data
            }
        })
    }

    @Composable
    abstract fun provideData(context: Context, id: GlanceId) : T

    abstract suspend fun provideGlance(context: Context, data: MutableState<T>)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = map[id] ?: return
        provideGlance(context, state)
    }
}
