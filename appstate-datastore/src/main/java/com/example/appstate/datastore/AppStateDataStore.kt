package com.example.appstate.datastore

import androidx.appstate.AppState
import androidx.appstate.AppStateKey
import androidx.appstate.transform.listener
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.okio.OkioSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okio.BufferedSink
import okio.BufferedSource
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.createType
import kotlin.reflect.typeOf

/**
 * Marks an [AppStateKey] to be persisted to [DataStore].
 *
 * Keys annotated with this will have their state automatically saved and restored.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
public annotation class PersistToDataStore

/** Preferences map for [AppState] backed by [DataStore]. */
public abstract class AppStatePreferences internal constructor() {
    /**
     * Returns an immutable map of the preferences.
     *
     * @return map of key names to serialized string values
     */
    public abstract fun asMap(): Map<String, String>

    /**
     * Gets a typed value for the given [AppStateKey].
     *
     * @param key the [AppStateKey] to retrieve
     * @return the deserialized value, or null if the key is not set.
     * @throws CorruptionException if the key cannot be deserialized.
     */
    public inline operator fun <reified T : Any?> get(key: AppStateKey<T>): T? {
        val keyName = key::class.qualifiedName ?: return null
        val valueJson = asMap()[keyName] ?: return null
        return try {
            val serializer = serializer(typeOf<T>())
            Json.decodeFromString(serializer, valueJson) as T
        } catch (e: SerializationException) {
            throw CorruptionException("Unable to deserialize JSON from String.", e)
        } catch (e: Exception) {
            throw CorruptionException("Unexpected error restoring state for key: $keyName", e)
        }
    }

    /**
     * Returns a mutable copy of the preferences.
     *
     * @return mutable preferences
     */
    public fun toMutablePreferences(): MutableAppStatePreferences {
        return MutableAppStatePreferences(asMap().toMutableMap())
    }
}

/** Mutable version of [AppStatePreferences]. */
public class MutableAppStatePreferences
internal constructor(
    @PublishedApi internal val preferencesMap: MutableMap<String, String> = mutableMapOf()
) : AppStatePreferences() {

    override fun asMap(): Map<String, String> {
        return preferencesMap.toMap()
    }

    /**
     * Sets a typed value for the given [AppStateKey].
     *
     * @param key the [AppStateKey] to set
     * @param value the value to set for the key
     * @throws CorruptionException if the key cannot be serialized.
     */
    public inline operator fun <reified T : Any?> set(key: AppStateKey<T>, value: T) {
        val keyName = key::class.qualifiedName ?: return
        try {
            val serializer = serializer(typeOf<T>())
            preferencesMap[keyName] = Json.encodeToString(serializer, value)
        } catch (e: SerializationException) {
            throw CorruptionException("Unable to serialize String to JSON.", e)
        } catch (e: Exception) {
            throw CorruptionException("Unexpected error saving state for key: $keyName", e)
        }
    }
}

/**
 * Edits the [AppStatePreferences] in [DataStore] transactionally.
 *
 * @param transform block to mutate the [MutableAppStatePreferences]
 * @return the updated [AppStatePreferences]
 */
public suspend fun DataStore<AppStatePreferences>.edit(
    transform: suspend (MutableAppStatePreferences) -> Unit
): AppStatePreferences {
    return this.updateData { current -> current.toMutablePreferences().apply { transform(this) } }
}

/**
 * Serializer for [AppStatePreferences] using [DataStore] and Okio.
 *
 * Serializes the preferences to a JSON string backed by a UTF-8 file.
 */
public object AppStateSerializer : OkioSerializer<AppStatePreferences> {
    override val defaultValue: AppStatePreferences = MutableAppStatePreferences()

    override suspend fun readFrom(source: BufferedSource): AppStatePreferences {
        try {
            val string = source.readUtf8()
            if (string.isEmpty()) {
                return defaultValue
            }
            val map = Json.decodeFromString<Map<String, String>>(string)
            return MutableAppStatePreferences(map.toMutableMap())
        } catch (e: SerializationException) {
            throw CorruptionException("Unable to deserialize JSON from String.", e)
        } catch (e: Exception) {
            throw CorruptionException("Unexpected error reading state from DataStore", e)
        }
    }

    override suspend fun writeTo(t: AppStatePreferences, sink: BufferedSink) {
        try {
            val string = Json.encodeToString<Map<String, String>>(t.asMap())
            sink.writeUtf8(string)
        } catch (e: SerializationException) {
            throw CorruptionException("Unable to serialize String to JSON.", e)
        }
    }
}

/**
 * Listens to [AppState] changes and persists annotated keys to [DataStore].
 *
 * Keys must be annotated with [PersistToDataStore] to be saved.
 *
 * @param dataStore the [DataStore] used to save and restore state
 * @return an [AppStateToken] to manage the listener's lifecycle
 */
public suspend fun AppState.addAppStateToDataStoreListener(
    dataStore: DataStore<AppStatePreferences>
) = listener {
    val activeKeys = keys
    for (key in activeKeys) {
        if (key::class.hasAnnotation<PersistToDataStore>()) {
            val restored = remember(key) { mutableStateOf(false) }
            val restoredValue = remember(key) { mutableStateOf<Any?>(null) }
            @Suppress("UNCHECKED_CAST")
            val state = getState(key as AppStateKey<Any?>, null)
            val value = state.value

            LaunchedEffect(key, value) {
                if (!restored.value) {
                    val preferences = dataStore.data.first()
                    val keyName = checkNotNull(key::class.qualifiedName) {
                        "Keys annotated with @PersistToDataStore must have a qualified name."
                    }
                    val valueJson = preferences.asMap()[keyName]
                    if (valueJson != null) {
                        try {
                            val valueType = key.getValueType()
                            val serializer = serializer(valueType)
                            val decoded = Json.decodeFromString(serializer, valueJson)
                            @Suppress("UNCHECKED_CAST")
                            this@addAppStateToDataStoreListener.setState(
                                key as AppStateKey<Any>,
                                decoded as Any,
                            )
                            restoredValue.value = decoded
                        } catch (e: SerializationException) {
                            throw CorruptionException("Unable to deserialize JSON from String.", e)
                        } catch (e: Exception) {
                            throw CorruptionException(
                                "Unexpected error restoring state for key: $keyName",
                                e,
                            )
                        }
                    }
                    restored.value = true
                } else {
                    if (value != null && value != restoredValue.value) {
                        val keyName = checkNotNull(key::class.qualifiedName) {
                            "Keys annotated with @PersistToDataStore must have a qualified name."
                        }
                        withContext(Dispatchers.IO) {
                            dataStore.edit { settings ->
                                try {
                                    val valueType = key.getValueType()
                                    val serializer = serializer(valueType)
                                    val jsonValue = Json.encodeToString(serializer, value)
                                    settings.preferencesMap[keyName] = jsonValue
                                    restoredValue.value = value
                                } catch (e: SerializationException) {
                                    throw CorruptionException("Unable to serialize String to JSON.", e)
                                } catch (e: Exception) {
                                    throw CorruptionException(
                                        "Unexpected error saving state for key: $keyName",
                                        e,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun AppStateKey<*>.getValueType(): KType {
    val appStateKeySupertype = this::class.allSupertypes
        .firstOrNull { it.classifier == AppStateKey::class }
    return appStateKeySupertype?.arguments?.firstOrNull()?.type
        ?: error("Could not find AppStateKey supertype for ${this::class}")
}

private inline fun <reified T : Annotation> KClass<*>.hasAnnotation(): Boolean {
    return this.annotations.any { it is T }
}

@Suppress("UNCHECKED_CAST")
private fun <T> AppState.getUntypedState(key: AppStateKey<*>, defaultValue: T?): State<T?> {
    return this.getState(key as AppStateKey<T?>, defaultValue)
}

