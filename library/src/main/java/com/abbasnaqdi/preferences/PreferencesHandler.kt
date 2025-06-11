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
import kotlinx.serialization.encodeToString // New import
import kotlinx.serialization.json.Json // New import
import kotlinx.serialization.KSerializer // New import
import kotlinx.serialization.serializer // New import
import kotlin.reflect.KClass // New import
import kotlin.reflect.full.createType // New import
import kotlin.reflect.full.isSubclassOf // New import
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

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> getKeyForType(name: String, type: Class<T>): Preferences.Key<T> {
        // This remains mostly the same for direct types.
        // For objects to be stored as JSON, they will be stored as String.
        return when (type) {
            Integer::class.java, Int::class.java -> intPreferencesKey(name) as Preferences.Key<T>
            java.lang.Long::class.java, Long::class.java -> longPreferencesKey(name) as Preferences.Key<T>
            java.lang.Float::class.java, Float::class.java -> floatPreferencesKey(name) as Preferences.Key<T>
            java.lang.Double::class.java, Double::class.java -> doublePreferencesKey(name) as Preferences.Key<T>
            java.lang.Boolean::class.java, Boolean::class.java -> booleanPreferencesKey(name) as Preferences.Key<T>
            String::class.java -> stringPreferencesKey(name) as Preferences.Key<T>
            Set::class.java -> {
                // This assumes if type is Set, it's Set<String> for direct storage.
                // Complex sets would go through JSON serialization.
                // This check might need refinement if we expect Set<CustomType> to be identified here.
                // For now, if type is exactly Set::class.java, assume Set<String>.
                stringSetPreferencesKey(name) as Preferences.Key<T>
            }
            else -> {
                // For any other type, we assume it will be stored as a JSON string.
                stringPreferencesKey(name) as Preferences.Key<T>
            }
        }
    }

    // Checks if a type is a directly supported Preferences primitive
    private fun <T: Any> isDirectlySupportedType(type: Class<T>): Boolean {
        return when (type) {
            Integer::class.java, Int::class.java,
            java.lang.Long::class.java, Long::class.java,
            java.lang.Float::class.java, Float::class.java,
            java.lang.Double::class.java, Double::class.java,
            java.lang.Boolean::class.java, Boolean::class.java,
            String::class.java -> true
            // Set::class.java for Set<String> is handled specially below
            else -> false
        }
    }
     private fun <T: Any> isStringSet(type: Class<T>, value: T? = null): Boolean {
        if (type == Set::class.java || value is Set<*>) {
            val set = value as? Set<*>
            // If value is null, we check if the type passed is literally Set::class.java,
            // implying the caller expects a Set<String> (as it's the only directly supported Set).
            // If value is not null, we check if all its elements are String.
            return set?.all { it is String } ?: (type == Set::class.java)
        }
        return false
    }


    override suspend fun <T : Any> putValue(name: String, value: T) {
        @Suppress("UNCHECKED_CAST")
        val valueClass = value::class.java as Class<T>
        if (isDirectlySupportedType(valueClass)) {
            val key = getKeyForType(name, valueClass) // Gets primitive key
            dataStore.edit { prefs -> prefs[key] = value }
        } else if (isStringSet(valueClass, value)) {
             val key = stringSetPreferencesKey(name) as Preferences.Key<Set<String>> // Explicitly Set<String>
             dataStore.edit { prefs -> prefs[key] = value as Set<String> }
        }
        else { // Assume custom object, serialize to JSON
            val key = stringPreferencesKey(name) // Store as string
            val jsonString = json.encodeToString(serializer(valueClass.kotlin.createType()) as KSerializer<T>, value)
            dataStore.edit { prefs -> prefs[key] = jsonString }
        }
    }

    override suspend fun <T : Any> removeValue(name: String, type: Class<T>) {
        // Removal needs to know if the original was a direct type or stored as JSON string
        val key = if (isDirectlySupportedType(type) || isStringSet(type)) {
            getKeyForType(name, type)
        } else { // Was stored as JSON string
            stringPreferencesKey(name) as Preferences.Key<T> // Cast is safe as it's for removal
        }
        dataStore.edit { prefs -> prefs.remove(key) }
    }

    override fun <T : Any> getValueFlow(name: String, type: Class<T>, defaultValue: T?): Flow<T?> {
        if (isDirectlySupportedType(type)) {
            val key = getKeyForType(name, type)
            return dataStore.data
                .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
                .map { preferences -> preferences[key] ?: defaultValue }
        } else if (isStringSet(type)) {
            // Ensure we are asking for Set<String> if using stringSetPreferencesKey
            // This path assumes T is Set<String> because isStringSet(type) was true.
            @Suppress("UNCHECKED_CAST")
            val key = stringSetPreferencesKey(name) as Preferences.Key<T>
             return dataStore.data
                .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
                .map { preferences -> preferences[key] ?: defaultValue }
        }
        else { // Assume custom object stored as JSON
            val key = stringPreferencesKey(name)
            return dataStore.data
                .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
                .map { preferences ->
                    val jsonString = preferences[key]
                    if (jsonString != null) {
                        try {
                            json.decodeFromString(serializer(type.kotlin.createType()) as KSerializer<T>, jsonString)
                        } catch (e: Exception) {
                            // Log error, return default or throw custom deserialization error
                            defaultValue
                        }
                    } else {
                        defaultValue
                    }
                }
        }
    }

    override suspend fun <T : Any> readValueOnce(name: String, type: Class<T>, defaultValue: T?): Result<T?> {
        return try {
            if (isDirectlySupportedType(type)) {
                val key = getKeyForType(name, type)
                Result.success(dataStore.data.first()[key] ?: defaultValue)
            } else if (isStringSet(type)) {
                @Suppress("UNCHECKED_CAST")
                val key = stringSetPreferencesKey(name) as Preferences.Key<T>
                Result.success(dataStore.data.first()[key] ?: defaultValue)
            }
            else { // Assume custom object stored as JSON
                val key = stringPreferencesKey(name)
                val jsonString = dataStore.data.first()[key]
                if (jsonString != null) {
                    try {
                       Result.success(json.decodeFromString(serializer(type.kotlin.createType()) as KSerializer<T>, jsonString))
                    } catch (e: Exception) {
                       Result.success(defaultValue) // Or failure, depending on desired strictness
                    }
                } else {
                    Result.success(defaultValue)
                }
            }
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

    // Reuse Json instance, potentially from a shared utility or companion object if desired
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private fun getKeyNameFromString(name: String): String = name

    // Helper to check if a type is a directly supported SharedPreferences primitive (excluding Set<String> here)
    private fun <T: Any> isDirectlySupportedSpType(type: Class<T>): Boolean {
        return when (type) {
            Integer::class.java, Int::class.java,
            java.lang.Long::class.java, Long::class.java,
            java.lang.Float::class.java, Float::class.java,
            java.lang.Boolean::class.java, Boolean::class.java,
            String::class.java -> true
            else -> false
        }
    }

    private fun <T: Any> isStringSet(type: Class<T>, value: T? = null): Boolean {
        if (type == Set::class.java || value is Set<*>) {
             val set = value as? Set<*>
             return set?.all { it is String } ?: true // Assume true if value is null but type is Set
        }
        return false
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Any> putValue(name: String, value: T) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().apply {
                val keyName = getKeyNameFromString(name)
                @Suppress("UNCHECKED_CAST") val valueClass = value::class.java as Class<T>

                if (isDirectlySupportedSpType(valueClass)) {
                    when (value) {
                        is Int -> putInt(keyName, value)
                        is Long -> putLong(keyName, value)
                        is Float -> putFloat(keyName, value)
                        is Boolean -> putBoolean(keyName, value)
                        is String -> putString(keyName, value)
                        // Should not reach here if isDirectlySupportedSpType is correct
                        else -> throw IllegalArgumentException("Type mismatch in putValue for directly supported SP types.")
                    }
                } else if (isStringSet(valueClass, value)) {
                     putStringSet(keyName, value as Set<String>)
                }
                else { // Custom object, serialize to JSON
                    val jsonString = json.encodeToString(serializer(valueClass.kotlin.createType()) as KSerializer<T>, value)
                    putString(keyName, jsonString)
                }
            }.apply()
        }
    }

    override suspend fun <T : Any> removeValue(name: String, type: Class<T>) { // Type not strictly needed for SP remove by name
        withContext(Dispatchers.IO) {
            // For SharedPreferences, remove by name is sufficient. Type is not needed for removal itself.
            sharedPreferences.edit().remove(getKeyNameFromString(name)).apply()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getValueFlow(name: String, type: Class<T>, defaultValue: T?): Flow<T?> {
        return callbackFlow {
            withContext(Dispatchers.IO) {
                val keyName = getKeyNameFromString(name)
                var resultValue: Any? = null
                var valueSet = false // Flag to check if a value was actively set (even if null)

                if (sharedPreferences.contains(keyName)) {
                    if (isDirectlySupportedSpType(type)) {
                        resultValue = when (type) {
                            Integer::class.java, Int::class.java -> sharedPreferences.getInt(keyName, defaultValue as? Int ?: 0)
                            java.lang.Long::class.java, Long::class.java -> sharedPreferences.getLong(keyName, defaultValue as? Long ?: 0L)
                            java.lang.Float::class.java, Float::class.java -> sharedPreferences.getFloat(keyName, defaultValue as? Float ?: 0f)
                            java.lang.Boolean::class.java, Boolean::class.java -> sharedPreferences.getBoolean(keyName, defaultValue as? Boolean ?: false)
                            String::class.java -> sharedPreferences.getString(keyName, defaultValue as? String)
                            else -> defaultValue // Should not be reached due to isDirectlySupportedSpType
                        }
                        valueSet = true
                    } else if (isStringSet(type)) {
                        resultValue = sharedPreferences.getStringSet(keyName, defaultValue as? Set<String>)
                        valueSet = true
                    }
                    else { // Custom object, attempt to deserialize from JSON
                        val jsonString = sharedPreferences.getString(keyName, null)
                        if (jsonString != null) {
                            try {
                                resultValue = json.decodeFromString(serializer(type.kotlin.createType()) as KSerializer<T>, jsonString)
                                valueSet = true
                            } catch (e: Exception) {
                                // Deserialization failed, log error, potentially use default or let it be null
                                // printError("JSON Deserialization failed for key '$keyName': $e") // Conceptual log
                                resultValue = defaultValue // Fallback to default if JSON is corrupt or type mismatch
                                valueSet = true
                            }
                        }
                    }
                }

                if (!valueSet) { // If not found or deserialization failed and no default was set from it
                    resultValue = defaultValue
                }
                trySend(resultValue as? T)
            }
            awaitClose { /* No resources to free */ }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Any> readValueOnce(name: String, type: Class<T>, defaultValue: T?): Result<T?> {
        return withContext(Dispatchers.IO) {
            try {
                val keyName = getKeyNameFromString(name)
                var resultValue: Any? = null
                var valueSet = false // Flag to check if a value was actively set

                if (sharedPreferences.contains(keyName)) {
                    if (isDirectlySupportedSpType(type)) {
                        resultValue = when (type) {
                            Integer::class.java, Int::class.java -> sharedPreferences.getInt(keyName, defaultValue as? Int ?: 0)
                            java.lang.Long::class.java, Long::class.java -> sharedPreferences.getLong(keyName, defaultValue as? Long ?: 0L)
                            java.lang.Float::class.java, Float::class.java -> sharedPreferences.getFloat(keyName, defaultValue as? Float ?: 0f)
                            java.lang.Boolean::class.java, Boolean::class.java -> sharedPreferences.getBoolean(keyName, defaultValue as? Boolean ?: false)
                            String::class.java -> sharedPreferences.getString(keyName, defaultValue as? String)
                             else -> defaultValue // Should not be reached
                        }
                        valueSet = true
                    } else if (isStringSet(type)) {
                        resultValue = sharedPreferences.getStringSet(keyName, defaultValue as? Set<String>)
                        valueSet = true
                    }
                    else { // Custom object, attempt to deserialize from JSON
                        val jsonString = sharedPreferences.getString(keyName, null)
                        if (jsonString != null) {
                            try {
                                resultValue = json.decodeFromString(serializer(type.kotlin.createType()) as KSerializer<T>, jsonString)
                                valueSet = true
                            } catch (e: Exception) {
                                // Fallback to default if JSON is corrupt or type mismatch
                                resultValue = defaultValue
                                valueSet = true
                                // Optionally, could return Result.failure(e) here if strictness is desired
                            }
                        }
                    }
                }

                if (!valueSet) {
                     resultValue = defaultValue
                }
                Result.success(resultValue as? T)
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
