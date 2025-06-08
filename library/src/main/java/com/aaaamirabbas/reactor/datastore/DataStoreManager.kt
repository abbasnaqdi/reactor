package com.aaaamirabbas.reactor.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class DataStoreManager(appContext: Context) {

    private val MIGRATION_COMPLETED_KEY = "reactor_migration_completed"

    private val masterKeyAlias = MasterKey.Builder(appContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = EncryptedSharedPreferences.create(
            appContext,
            "reactor_secure_prefs", // filename for the encrypted shared preferences
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Placeholder for put/get/remove methods
    private fun getStoreKey(originalKey: String, typeName: String): String = "${typeName}_${originalKey}"

    fun put(originalKey: String, typeName: String, value: String) {
        val storeKey = getStoreKey(originalKey, typeName)
        sharedPreferences.edit().putString(storeKey, value).apply()
    }

    fun get(originalKey: String, typeName: String): String? {
        val storeKey = getStoreKey(originalKey, typeName)
        return sharedPreferences.getString(storeKey, null)
    }

    fun remove(originalKey: String, typeName: String) {
        val storeKey = getStoreKey(originalKey, typeName)
        sharedPreferences.edit().remove(storeKey).apply()
    }

    fun clearAll() {
        // TODO: Implement actual clear all logic
        sharedPreferences.edit().clear().apply()
    }

    fun isMigrationCompleted(): Boolean {
        return sharedPreferences.getBoolean(MIGRATION_COMPLETED_KEY, false)
    }

    fun setMigrationCompleted() {
        sharedPreferences.edit().putBoolean(MIGRATION_COMPLETED_KEY, true).apply()
    }
}
