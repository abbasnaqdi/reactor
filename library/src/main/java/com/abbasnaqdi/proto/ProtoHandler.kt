package com.abbasnaqdi.proto // Updated package

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler // Added import
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch
import java.io.IOException
import kotlin.Result // Ensure this is kotlin.Result

class ProtoHandler<T : Any>(
    private val appContext: Context,
    private val fileName: String, // e.g., "user_prefs.pb"
    private val userSerializer: Serializer<T>, // Renamed for clarity
    private val encrypted: Boolean
) {
    private val actualSerializer: Serializer<T> by lazy {
        if (encrypted) {
            // MasterKey alias could be derived from fileName or be a constant for the app
            val masterKeyAlias = "master_key_for_${fileName.replace(".", "_")}"
            // TODO: The EncryptedProtoSerializer currently uses PLACEHOLDER encryption.
            // Replace with a secure implementation before production use for encrypted Proto.
            EncryptedProtoSerializer(appContext, userSerializer, masterKeyAlias)
        } else {
            userSerializer
        }
    }

    internal val dataStore: DataStore<T> by lazy { // Made dataStore internal for test observation if needed (as per prompt, though test workarounds might be better)
        DataStoreFactory.create(
            serializer = actualSerializer, // Use the potentially wrapped serializer
            produceFile = { appContext.dataStoreFile(fileName) },
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { actualSerializer.defaultValue }
            )
        )
    }

    open val data: Flow<T> // Made open for test workaround in ProtoHandlerTest.kt
        get() = dataStore.data
            .catch { exception ->
                // Handle specific exceptions like CorruptionException or IOException
                // For simplicity, rethrow or emit a default/error state if applicable
                // Depending on T, emitting a default might require more context or a factory
                if (exception is IOException || exception is CorruptionException) {
                    // Log the error, perhaps provide a way to recover or signal error state
                    // For now, rethrowing to let the collector handle it, which is common.
                    throw DataStoreReadException("Error reading Proto DataStore: ${exception.message}", exception)
                }
                throw exception
            }

    suspend fun updateData(transform: suspend (t: T) -> T): Result<T> {
        return try {
            val updatedData = dataStore.updateData(transform)
            Result.success(updatedData)
        } catch (e: Exception) {
            Result.failure(DataStoreWriteException("Error updating Proto DataStore: ${e.message}", e))
        }
    }

    suspend fun readData(): Result<T> {
        return try {
            val currentData = dataStore.data.first() // This can throw if the initial read fails
            Result.success(currentData)
        } catch (e: Exception) {
            Result.failure(DataStoreReadException("Error reading Proto DataStore snapshot: ${e.message}", e))
        }
    }
}

// Custom exceptions for more specific error handling by the client
class DataStoreReadException(message: String, cause: Throwable? = null) : IOException(message, cause)
class DataStoreWriteException(message: String, cause: Throwable? = null) : IOException(message, cause)
