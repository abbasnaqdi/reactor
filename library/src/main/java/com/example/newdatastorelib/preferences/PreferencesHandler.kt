package com.example.newdatastorelib.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.Result

interface PrefsOperations { // Keep as is
    suspend fun <T> putValue(key: Preferences.Key<T>, value: T)
    suspend fun <T> removeValue(key: Preferences.Key<T>)
    fun <T> getValueFlow(key: Preferences.Key<T>, defaultValue: T?): Flow<T?>
    suspend fun <T> readValueOnce(key: Preferences.Key<T>, defaultValue: T?): Result<T?>
    suspend fun clearAll()
}

class PreferencesHandler(
    appContext: Context, // Made appContext public for operations classes if needed, or pass selectively
    name: String,
    encrypted: Boolean
) {
    private val operations: PrefsOperations by lazy {
        if (encrypted) {
            val masterKey = MasterKey.Builder(appContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val encSharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
                appContext,
                name, // Filename for EncryptedSharedPreferences
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            EncryptedPrefsOperations(encSharedPreferences) // Inject dependency
        } else {
            val dataStoreInstance: DataStore<Preferences> = PreferenceDataStoreFactory.create(
                produceFile = { appContext.preferencesDataStoreFile(name) }
            )
            DataStorePrefsOperations(dataStoreInstance) // Inject dependency
        }
    }

    suspend fun <T> put(key: Preferences.Key<T>, value: T) = operations.putValue(key, value)
    suspend fun <T> remove(key: Preferences.Key<T>) = operations.removeValue(key)
    fun <T> get(key: Preferences.Key<T>, defaultValue: T? = null): Flow<T?> = operations.getValueFlow(key, defaultValue)
    suspend fun <T> readOnce(key: Preferences.Key<T>, defaultValue: T? = null): Result<T?> = operations.readValueOnce(key, defaultValue)
    suspend fun clear() = operations.clearAll()
}

// Now internal and takes DataStore as a constructor parameter
internal class DataStorePrefsOperations(
    private val dataStore: DataStore<Preferences>
) : PrefsOperations {
    override suspend fun <T> putValue(key: Preferences.Key<T>, value: T) {
        dataStore.edit { prefs -> prefs[key] = value }
    }

    override suspend fun <T> removeValue(key: Preferences.Key<T>) {
        dataStore.edit { prefs -> prefs.remove(key) }
    }

    override fun <T> getValueFlow(key: Preferences.Key<T>, defaultValue: T?): Flow<T?> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { preferences -> preferences[key] ?: defaultValue }
    }

    override suspend fun <T> readValueOnce(key: Preferences.Key<T>, defaultValue: T?): Result<T?> {
        return try {
            // Make sure to catch exceptions on data.first() itself if flow is cold and might fail on collection
            val preferences = dataStore.data.catch { exception ->
                 if (exception is IOException) emit(emptyPreferences()) else throw exception
            }.first()
            Result.success(preferences[key] ?: defaultValue)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}

// Now internal and takes SharedPreferences as a constructor parameter
internal class EncryptedPrefsOperations(
    private val sharedPreferences: SharedPreferences
) : PrefsOperations {
    private fun <T> getKeyName(key: Preferences.Key<T>): String = key.name // Keep as is

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> putValue(key: Preferences.Key<T>, value: T) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().apply {
                val keyName = getKeyName(key)
                when (value) {
                    is Int -> putInt(keyName, value)
                    is Long -> putLong(keyName, value)
                    is Float -> putFloat(keyName, value)
                    is Boolean -> putBoolean(keyName, value)
                    is String -> putString(keyName, value)
                    is Set<*> -> {
                        if (value.all { it is String }) {
                            putStringSet(keyName, value as Set<String>)
                        } else {
                            throw IllegalArgumentException("Only Set<String> is supported for SharedPreferences.")
                        }
                    }
                    else -> throw IllegalArgumentException("Unsupported type for SharedPreferences: ${value!!::class.java.name}")
                }
            }.apply()
        }
    }

    override suspend fun <T> removeValue(key: Preferences.Key<T>) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().remove(getKeyName(key)).apply()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getValueFlow(key: Preferences.Key<T>, defaultValue: T?): Flow<T?> {
        return callbackFlow {
            withContext(Dispatchers.IO) {
                val keyName = getKeyName(key)
                val value: Any? = if (sharedPreferences.contains(keyName)) {
                     when (defaultValue) {
                        is Int -> sharedPreferences.getInt(keyName, defaultValue)
                        is Long -> sharedPreferences.getLong(keyName, defaultValue)
                        is Float -> sharedPreferences.getFloat(keyName, defaultValue)
                        is Boolean -> sharedPreferences.getBoolean(keyName, defaultValue)
                        is String -> sharedPreferences.getString(keyName, defaultValue)
                        is Set<*> -> sharedPreferences.getStringSet(keyName, defaultValue as? Set<String>)
                        else -> {
                            val tempDefault = key.getDefaultValueBasedOnKeyNameOrHeuristic(defaultValue) // Pass defaultValue
                            when (tempDefault) {
                                is Int -> sharedPreferences.getInt(keyName, tempDefault)
                                is Long -> sharedPreferences.getLong(keyName, tempDefault)
                                is Float -> sharedPreferences.getFloat(keyName, tempDefault)
                                is Boolean -> sharedPreferences.getBoolean(keyName, tempDefault)
                                is String -> sharedPreferences.getString(keyName, tempDefault)
                                // Set<String> case is tricky without a non-null Set default
                                else -> if (defaultValue == null) null else throw IllegalArgumentException("Unsupported type for key $keyName without a typed defaultValue")
                            }
                        }
                    }
                } else {
                    defaultValue
                }
                trySend(value as? T)
            }
            awaitClose { }
        }
    }

    // Adjusted helper to make a more informed guess, though still limited.
    private fun <T> Preferences.Key<T>.getDefaultValueBasedOnKeyNameOrHeuristic(currentDefaultValue: T?): Any? {
        if (currentDefaultValue != null) return currentDefaultValue // Prefer explicit default
        // Example heuristic (very basic, expand as needed):
        // This is where knowing the *expected type* of T for this key is crucial if currentDefaultValue is null.
        // Since Preferences.Key is generic and T is erased, this is a fundamental challenge.
        // If this key is known to be, e.g., an Int key by convention even if `defaultValue` is `null`,
        // then `0` could be returned. Without such conventions, it's hard.
        // For this example, we'll stick to null if currentDefaultValue is null.
        return null
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> readValueOnce(key: Preferences.Key<T>, defaultValue: T?): Result<T?> {
        return withContext(Dispatchers.IO) {
            try {
                val keyName = getKeyName(key)
                val value: Any? = if (sharedPreferences.contains(keyName)) {
                    when (defaultValue) {
                        is Int -> sharedPreferences.getInt(keyName, defaultValue)
                        is Long -> sharedPreferences.getLong(keyName, defaultValue)
                        is Float -> sharedPreferences.getFloat(keyName, defaultValue)
                        is Boolean -> sharedPreferences.getBoolean(keyName, defaultValue)
                        is String -> sharedPreferences.getString(keyName, defaultValue)
                        is Set<*> -> sharedPreferences.getStringSet(keyName, defaultValue as? Set<String>)
                         else -> {
                            val tempDefault = key.getDefaultValueBasedOnKeyNameOrHeuristic(defaultValue)
                             when (tempDefault) {
                                is Int -> sharedPreferences.getInt(keyName, tempDefault)
                                is Long -> sharedPreferences.getLong(keyName, tempDefault)
                                is Float -> sharedPreferences.getFloat(keyName, tempDefault)
                                is Boolean -> sharedPreferences.getBoolean(keyName, tempDefault)
                                is String -> sharedPreferences.getString(keyName, tempDefault)
                                else -> if (defaultValue == null) null else throw IllegalArgumentException("Unsupported type for key $keyName without a typed defaultValue")
                            }
                        }
                    }
                } else {
                    defaultValue
                }
                Result.success(value as? T)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    override suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().clear().apply()
        }
    }
}
