package com.aaaamirabbas.reactor.handler

import android.content.Context
import com.aaaamirabbas.reactor.datastore.DataStoreManager // New import
import com.aaaamirabbas.reactor.migration.MigrationManager // New import
import com.aaaamirabbas.reactor.helper.SerializationHelper
import java.io.Serializable

// Removed isEncrypt parameter, DataStoreManager will always use EncryptedSharedPreferences
class Reactor(appContext: Context) {

    // Instantiate DataStoreManager and MigrationManager
    private val dataStoreManager = DataStoreManager(appContext)
    private val migrationManager = MigrationManager(appContext)

    // SerializationHelper is still needed
    private val serializationHelper = SerializationHelper()

    init {
        // Perform migration check when Reactor is initialized
        migrationManager.migrateIfNeeded()
    }

    inline fun <reified T : Serializable> put(key: String, value: T?): Boolean {
        if (value == null) {
            remove<T>(key) // This already returns Boolean
            return true // Consistent with remove's Boolean, or decide on overall strategy
        }

        val typeName = T::class.java.simpleName
        val serializeValue = serializationHelper.serialize(value)

        return if (serializeValue != null) {
            dataStoreManager.put(originalKey = key, typeName = typeName, value = serializeValue)
            true // Assuming success from DataStoreManager.put (which is void)
        } else {
            false // Indicate serialization failure
        }
    }

    // Re-implement putString using the generic put, or directly if simpler
    // For now, let's use the generic version for consistency.
    @Deprecated("Use generic put<String>(key, value) instead", ReplaceWith("put(key, value)"))
    fun putString(key: String, value: String?): Boolean {
         return put(key, value) // String is Serializable, value can be null
    }

    inline fun <reified T : Serializable> get(key: String, default: T): T {
        val typeName = T::class.java.simpleName
        val value = dataStoreManager.get(originalKey = key, typeName = typeName) ?: return default
        return serializationHelper.deserialize(value) ?: default
    }

    inline fun <reified T : Serializable> get(key: String): T? {
        val typeName = T::class.java.simpleName
        val value = dataStoreManager.get(originalKey = key, typeName = typeName) ?: return null
        return serializationHelper.deserialize(value)
    }

    // Re-implement getString using the generic get
    @Deprecated("Use generic get<String>(key, default) instead", ReplaceWith("get(key, default)"))
    fun getString(key: String, default: String): String {
        return get(key, default) // Generic get handles default
    }

    @Deprecated("Use generic get<String>(key) instead", ReplaceWith("get(key)"))
    fun getString(key: String): String? {
        return get(key) // Generic get handles nullable
    }

    inline fun <reified T : Serializable> remove(vararg keys: String): Boolean {
        val typeName = T::class.java.simpleName
        keys.forEach { key ->
            dataStoreManager.remove(originalKey = key, typeName = typeName)
        }
        return true // Assuming success
    }

    fun eraseAllData() {
        dataStoreManager.clearAll()
        // Consider if migration flag should be reset here. Probably not,
        // as eraseAllData is about current data, not past migration status.
    }
}