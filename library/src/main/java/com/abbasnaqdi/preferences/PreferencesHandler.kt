package com.abbasnaqdi.preferences // Updated package

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataMigration // New import
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
// Make sure these imports are present for key creation
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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

interface PrefsOperations {
    suspend fun <T : Any> putValue(name: String, value: T) // Type T of value used to determine Preferences.Key<T>
    suspend fun <T : Any> removeValue(name: String, type: Class<T>) // Explicit type for removal
    fun <T : Any> getValueFlow(name: String, type: Class<T>, defaultValue: T?): Flow<T?>
    suspend fun <T : Any> readValueOnce(name: String, type: Class<T>, defaultValue: T?): Result<T?>
    suspend fun clearAll()
}

class PreferencesHandler(
    appContext: Context,
    name: String,
    encrypted: Boolean,
    migrations: List<DataMigration<Preferences>> = emptyList() // New parameter
) {
    private val operations: PrefsOperations by lazy {
        if (encrypted) {
            // EncryptedSharedPreferences does not directly support DataStore migrations in the same way.
            // Migrations for EncryptedSharedPreferences would need to be manual before initialization
            // or by reading from an old source and writing to new EncryptedSharedPreferences.
            // For now, the migrations parameter will be ignored for the encrypted path.
            // A warning or documentation should reflect this.
            val masterKey = MasterKey.Builder(appContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val encSharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
                appContext,
                name,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            EncryptedPrefsOperations(encSharedPreferences)
        } else {
            val dataStoreInstance: DataStore<Preferences> = PreferenceDataStoreFactory.create(
                produceFile = { appContext.preferencesDataStoreFile(name) },
                migrations = migrations // Pass migrations here
            )
            DataStorePrefsOperations(dataStoreInstance)
        }
    }

    suspend inline fun <reified T : Any> put(name: String, value: T) {
        // The implementation of PrefsOperations.putValue will handle type checking and key creation.
        operations.putValue(name, value)
    }

    suspend inline fun <reified T : Any> remove(name: String) {
        operations.removeValue(name, T::class.java)
    }

    inline fun <reified T : Any> get(name: String, defaultValue: T? = null): Flow<T?> {
        return operations.getValueFlow(name, T::class.java, defaultValue)
    }

    suspend inline fun <reified T : Any> readOnce(name: String, defaultValue: T? = null): Result<T?> {
        return operations.readValueOnce(name, T::class.java, defaultValue)
    }

    // clear() method remains the same:
    suspend fun clear() = operations.clearAll()
}

// Now internal and takes DataStore as a constructor parameter
internal class DataStorePrefsOperations(
    private val dataStore: DataStore<Preferences>
) : PrefsOperations {

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> getKeyForType(name: String, type: Class<T>): Preferences.Key<T> {
        return when (type) {
            Integer::class.java, Int::class.java -> intPreferencesKey(name) as Preferences.Key<T>
            java.lang.Long::class.java, Long::class.java -> longPreferencesKey(name) as Preferences.Key<T>
            java.lang.Float::class.java, Float::class.java -> floatPreferencesKey(name) as Preferences.Key<T>
            java.lang.Double::class.java, Double::class.java -> doublePreferencesKey(name) as Preferences.Key<T>
            java.lang.Boolean::class.java, Boolean::class.java -> booleanPreferencesKey(name) as Preferences.Key<T>
            String::class.java -> stringPreferencesKey(name) as Preferences.Key<T>
            Set::class.java -> stringSetPreferencesKey(name) as Preferences.Key<T> // Assumes Set<String>
            else -> throw IllegalArgumentException("Unsupported type for DataStore Preferences: ${type.name}")
        }
    }

    // Helper for putValue where type is inferred from value
    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> getKeyForValue(name: String, value: T): Preferences.Key<T> {
         return when (value) {
            is Int -> intPreferencesKey(name) as Preferences.Key<T>
            is Long -> longPreferencesKey(name) as Preferences.Key<T>
            is Float -> floatPreferencesKey(name) as Preferences.Key<T>
            is Double -> doublePreferencesKey(name) as Preferences.Key<T>
            is Boolean -> booleanPreferencesKey(name) as Preferences.Key<T>
            is String -> stringPreferencesKey(name) as Preferences.Key<T>
            is Set<*> -> {
                // Ensure all elements are String for Set, as DataStore Preferences only supports Set<String>
                if (value.all { it is String }) {
                    stringSetPreferencesKey(name) as Preferences.Key<T>
                } else {
                    throw IllegalArgumentException("Only Set<String> is supported for DataStore Preferences. Found: Set<${value.firstOrNull()?.javaClass?.simpleName}>")
                }
            }
            else -> throw IllegalArgumentException("Unsupported type for DataStore Preferences: ${value::class.java.name}")
        }
    }


    override suspend fun <T : Any> putValue(name: String, value: T) {
        val key = getKeyForValue(name, value)
        dataStore.edit { prefs -> prefs[key] = value }
    }

    override suspend fun <T : Any> removeValue(name: String, type: Class<T>) {
        val key = getKeyForType(name, type)
        dataStore.edit { prefs -> prefs.remove(key) }
    }

    override fun <T : Any> getValueFlow(name: String, type: Class<T>, defaultValue: T?): Flow<T?> {
        val key = getKeyForType(name, type)
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { preferences -> preferences[key] ?: defaultValue }
    }

    override suspend fun <T : Any> readValueOnce(name: String, type: Class<T>, defaultValue: T?): Result<T?> {
        return try {
            val key = getKeyForType(name, type)
            Result.success(dataStore.data.first()[key] ?: defaultValue)
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
    private fun getKeyNameFromString(name: String): String = name // Simplified, was getKeyName(key: Preferences.Key<T>)

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Any> putValue(name: String, value: T) { // name is already String
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().apply {
                val keyName = getKeyNameFromString(name)
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
                    else -> throw IllegalArgumentException("Unsupported type for SharedPreferences: ${value::class.java.name}")
                }
            }.apply()
        }
    }

    override suspend fun <T : Any> removeValue(name: String, type: Class<T>) { // Added 'type' but it's not strictly needed for remove by name
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().remove(getKeyNameFromString(name)).apply()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getValueFlow(name: String, type: Class<T>, defaultValue: T?): Flow<T?> {
        return callbackFlow {
            withContext(Dispatchers.IO) {
                val keyName = getKeyNameFromString(name)
                val value: Any? = if (sharedPreferences.contains(keyName)) {
                    when (type) { // Use the provided 'type' parameter
                        Integer::class.java, Int::class.java -> sharedPreferences.getInt(keyName, defaultValue as? Int ?: 0)
                        java.lang.Long::class.java, Long::class.java -> sharedPreferences.getLong(keyName, defaultValue as? Long ?: 0L)
                        java.lang.Float::class.java, Float::class.java -> sharedPreferences.getFloat(keyName, defaultValue as? Float ?: 0f)
                        java.lang.Boolean::class.java, Boolean::class.java -> sharedPreferences.getBoolean(keyName, defaultValue as? Boolean ?: false)
                        String::class.java -> sharedPreferences.getString(keyName, defaultValue as? String)
                        Set::class.java -> sharedPreferences.getStringSet(keyName, defaultValue as? Set<String>) // Assumes Set<String>
                        else -> {
                            if (defaultValue != null && type.isAssignableFrom(defaultValue.javaClass)) {
                                defaultValue // Fallback to defaultValue if type is unknown but defaultValue matches
                            } else {
                                // Cannot infer type if defaultValue is null and type is exotic
                                // Or throw: throw IllegalArgumentException("Unsupported type for SharedPreferences: ${type.name}")
                                null
                            }
                        }
                    }
                } else {
                    defaultValue
                }
                trySend(value as? T)
            }
            awaitClose { /* No resources to free */ }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Any> readValueOnce(name: String, type: Class<T>, defaultValue: T?): Result<T?> {
        return withContext(Dispatchers.IO) {
            try {
                val keyName = getKeyNameFromString(name)
                val value: Any? = if (sharedPreferences.contains(keyName)) {
                     when (type) { // Use the provided 'type' parameter
                        Integer::class.java, Int::class.java -> sharedPreferences.getInt(keyName, defaultValue as? Int ?: 0)
                        java.lang.Long::class.java, Long::class.java -> sharedPreferences.getLong(keyName, defaultValue as? Long ?: 0L)
                        java.lang.Float::class.java, Float::class.java -> sharedPreferences.getFloat(keyName, defaultValue as? Float ?: 0f)
                        java.lang.Boolean::class.java, Boolean::class.java -> sharedPreferences.getBoolean(keyName, defaultValue as? Boolean ?: false)
                        String::class.java -> sharedPreferences.getString(keyName, defaultValue as? String)
                        Set::class.java -> sharedPreferences.getStringSet(keyName, defaultValue as? Set<String>) // Assumes Set<String>
                        else -> {
                            if (defaultValue != null && type.isAssignableFrom(defaultValue.javaClass)) {
                                defaultValue
                            } else {
                                // Or throw: throw IllegalArgumentException("Unsupported type for SharedPreferences: ${type.name}")
                                null
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
