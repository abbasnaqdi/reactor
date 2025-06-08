package com.example.newdatastorelib.core

import android.content.Context
import androidx.datastore.core.Serializer
import com.example.newdatastorelib.preferences.PreferencesHandler
import com.example.newdatastorelib.proto.ProtoHandler
import java.util.concurrent.ConcurrentHashMap

class NewDataStore(private val appContext: Context) {

    // Cache for PreferencesHandler instances
    // Key: "name_encrypted" (e.g., "default_prefs_false", "user_settings_true")
    private val preferencesHandlerCache = ConcurrentHashMap<String, PreferencesHandler>()

    // Cache for ProtoHandler instances
    // Key: "fileName_serializerClass_encrypted" (e.g., "user_prefs.pb_com.example.UserSerializer_false")
    // Using serializer.javaClass.name helps differentiate if multiple protos use the same filename (though not ideal)
    // A more robust key might involve the type T, but that's harder with erasure.
    // For now, filename and serializer class name should be reasonably unique.
    private val protoHandlerCache = ConcurrentHashMap<String, ProtoHandler<out Any>>()


    fun preferences(name: String = "default_prefs", encrypted: Boolean = false): PreferencesHandler {
        val cacheKey = "${name}_${encrypted}"
        return preferencesHandlerCache.getOrPut(cacheKey) {
            PreferencesHandler(appContext, name, encrypted)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> proto(
        serializer: Serializer<T>,
        fileName: String,
        encrypted: Boolean = false
    ): ProtoHandler<T> {
        // Using serializer.javaClass.name in the key. This assumes that for a given T,
        // the same serializer instance or class is always passed.
        val cacheKey = "${fileName}_${serializer.javaClass.name}_${encrypted}"

        // getOrPut is not ideal here if different T are accidentally used with the same key.
        // A more type-safe cache would be complex. This relies on correct usage.
        return protoHandlerCache.getOrPut(cacheKey) {
            ProtoHandler(appContext, fileName, serializer, encrypted)
        } as ProtoHandler<T>
        // The cast is safe if the keying strategy + usage is consistent.
    }

    // Optional: Method to clear caches if re-configuration or testing needs it
    fun clearCaches() {
        preferencesHandlerCache.clear()
        protoHandlerCache.clear()
    }
}
