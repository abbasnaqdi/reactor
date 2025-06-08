package com.aaaamirabbas.reactor.migration

import android.content.Context
import android.util.Log
import com.aaaamirabbas.reactor.datastore.DataStoreManager
import com.aaaamirabbas.reactor.engine.EngineModel_Old
import com.aaaamirabbas.reactor.helper.SerializationHelper
import com.aaaamirabbas.reactor.security.SecurityModel_Old

class MigrationManager(private val appContext: Context) {

    private val newDataStoreManager = DataStoreManager(appContext)
    private val serializationHelper = SerializationHelper() // Assuming this is available and compatible

    companion object {
        private const val TAG = "ReactorMigration"
    }

    fun migrateIfNeeded() {
        if (newDataStoreManager.isMigrationCompleted()) {
            Log.i(TAG, "Migration already completed. Skipping.")
            return
        }

        Log.i(TAG, "Starting data migration...")

        // Old versions for encrypted data
        val oldSecurityModelEncrypted = SecurityModel_Old(appContext, true)
        val oldEngineModelEncrypted = EngineModel_Old(appContext, "AES") // Scope for encrypted

        // Old versions for non-encrypted data (if Reactor supported saving without encryption)
        val oldSecurityModelUnencrypted = SecurityModel_Old(appContext, false)
        val oldEngineModelUnencrypted = EngineModel_Old(appContext, "NONE") // Scope for unencrypted

        var migratedValuesCount = 0

        // --- Migrate Encrypted Data ---
        Log.i(TAG, "Attempting to migrate encrypted data (AES scope)...")
        val encryptedTypeNames = oldEngineModelEncrypted.discoverTypeNames()
        Log.i(TAG, "Found encrypted typeNames: ${'$'}encryptedTypeNames")
        for (typeName in encryptedTypeNames) {
            val keys = oldEngineModelEncrypted.getAllKeysForType(typeName)
            for (key in keys) {
                val encryptedValue = oldEngineModelEncrypted.fetchJSON(typeName).optString(key, null)
                if (encryptedValue != null) {
                    try {
                        val decryptedValue = oldSecurityModelEncrypted.decryptValue(encryptedValue)
                        // The decrypted value is the Base64 string from SerializationHelper
                        // It needs to be directly stored by DataStoreManager, which will re-encrypt it.
                        // The DataStoreManager already expects a String value.
                        newDataStoreManager.put(originalKey = key, typeName = typeName, value = decryptedValue)
                        migratedValuesCount++
                        Log.d(TAG, "Migrated AES/$typeName/$key")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error migrating AES/$typeName/$key: ${'$'}{e.message}", e)
                    }
                }
            }
        }

        // --- Migrate Unencrypted Data ---
        Log.i(TAG, "Attempting to migrate unencrypted data (NONE scope)...")
        val unencryptedTypeNames = oldEngineModelUnencrypted.discoverTypeNames()
        Log.i(TAG, "Found unencrypted typeNames: ${'$'}unencryptedTypeNames")
        for (typeName in unencryptedTypeNames) {
            val keys = oldEngineModelUnencrypted.getAllKeysForType(typeName)
            for (key in keys) {
                // Value from NONE scope is already decrypted (it was never encrypted)
                // but it's still the Base64 string from SerializationHelper
                val plainValue = oldEngineModelUnencrypted.fetchJSON(typeName).optString(key, null)
                if (plainValue != null) {
                    try {
                        newDataStoreManager.put(originalKey = key, typeName = typeName, value = plainValue)
                        migratedValuesCount++
                        Log.d(TAG, "Migrated NONE/$typeName/$key")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error migrating NONE/$typeName/$key: ${'$'}{e.message}", e)
                    }
                }
            }
        }

        Log.i(TAG, "Migration attempt finished. Migrated $migratedValuesCount values.")

        // Optional: Delete old files after successful migration
        // For now, we'll skip this to be safe. It can be added later.
        // Example:
        // encryptedTypeNames.forEach { oldEngineModelEncrypted.getOldDataFile(it).delete() }
        // unencryptedTypeNames.forEach { oldEngineModelUnencrypted.getOldDataFile(it).delete() }
        // val reactorOldDir = oldEngineModelEncrypted.getOldDataDirectory()
        // if (reactorOldDir.listFiles()?.isEmpty() == true) { reactorOldDir.delete() }


        newDataStoreManager.setMigrationCompleted()
        Log.i(TAG, "Migration flag set.")
    }
}
