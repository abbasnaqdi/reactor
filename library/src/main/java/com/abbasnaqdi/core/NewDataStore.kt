package com.abbasnaqdi.core

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.core.Preferences
import com.abbasnaqdi.preferences.PreferencesHandler
import com.abbasnaqdi.proto.ProtoHandler
import java.util.concurrent.ConcurrentHashMap

class NewDataStore(private val appContext: Context) { // appContext made internal for companion access if needed

    // Cache for PreferencesHandler instances (remains the same)
    private val preferencesHandlerCache = ConcurrentHashMap<String, PreferencesHandler>()

    // Cache for ProtoHandler instances (remains the same)
    private val protoHandlerCache = ConcurrentHashMap<String, ProtoHandler<out Any>>()

    // Instance methods (remain the same, but now also called by companion object methods)
    // Added migrations parameter to these methods in this step as well, anticipating next part of plan.
    fun preferences(
        name: String = "default_app_prefs", // Changed default name slightly
        encrypted: Boolean = false,
        migrations: List<DataMigration<Preferences>> = emptyList() // New parameter
    ): PreferencesHandler {
        val cacheKey = "${name}_${encrypted}" // Migrations don't alter the cache key for the handler itself
        return preferencesHandlerCache.getOrPut(cacheKey) {
            PreferencesHandler(appContext, name, encrypted, migrations) // Pass migrations
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> proto(
        serializer: Serializer<T>,
        fileName: String,
        encrypted: Boolean = false,
        migrations: List<DataMigration<T>> = emptyList() // New parameter
    ): ProtoHandler<T> {
        val cacheKey = "${fileName}_${serializer.javaClass.name}_${encrypted}"
        return protoHandlerCache.getOrPut(cacheKey) {
            ProtoHandler(appContext, fileName, serializer, encrypted, migrations = migrations) // Pass migrations
        } as ProtoHandler<T>
    }

    fun clearCaches() {
        preferencesHandlerCache.clear()
        protoHandlerCache.clear()
    }

    companion object {
        @Volatile
        private var defaultInstance: NewDataStore? = null
        private val LOCK = Any()

        /**
         * Initializes the default singleton instance of NewDataStore.
         * This should ideally be called once, for example, in your Application's onCreate().
         *
         * @param context The application context.
         */
        fun initializeDefaultInstance(context: Context) {
            synchronized(LOCK) {
                if (defaultInstance == null) {
                    defaultInstance = NewDataStore(context.applicationContext)
                }
            }
        }

        private fun getInstance(): NewDataStore {
            return defaultInstance ?: throw IllegalStateException(
                "Default NewDataStore instance has not been initialized. " +
                "Call NewDataStore.initializeDefaultInstance(context) in your Application class."
            )
        }

        /**
         * Gets the default PreferencesHandler.
         * Requires `initializeDefaultInstance` to have been called.
         */
        fun getDefaultPreferences(
            name: String = "default_app_prefs",
            encrypted: Boolean = false,
            migrations: List<DataMigration<Preferences>> = emptyList()
        ): PreferencesHandler {
            return getInstance().preferences(name, encrypted, migrations)
        }

        /**
         * Gets the default ProtoHandler for the given type.
         * Requires `initializeDefaultInstance` to have been called.
         */
        fun <T : Any> getDefaultProtoStore(
            serializer: Serializer<T>,
            fileName: String,
            encrypted: Boolean = false,
            migrations: List<DataMigration<T>> = emptyList()
        ): ProtoHandler<T> {
            return getInstance().proto(serializer, fileName, encrypted, migrations)
        }

        /**
         * Clears caches for the default instance. Useful for testing or specific scenarios.
         * Requires `initializeDefaultInstance` to have been called.
         */
        fun clearDefaultInstanceCaches() {
            getInstance().clearCaches()
        }
    }
}
