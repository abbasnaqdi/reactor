package com.oky2abbas.reactor.security

import android.content.Context
import com.oky2abbas.reactor.engine.EngineController

class SecurityController(appContext: Context, isEncrypt: Boolean) {
    private var engineController = EngineController(appContext, if (isEncrypt) "AES" else "NONE")
    private val securityModel = SecurityModel(appContext, isEncrypt)

    fun put(key: String, value: String, typeName: String): Boolean {
        val encryptValue = securityModel.encryptValue(value)
        return engineController.put(key, encryptValue, typeName)
    }

    fun get(key: String, typeName: String): String? {
        val value: String = engineController.get(key, typeName) ?: return null

        return try {
            securityModel.decryptValue(value)
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun remove(keys: List<String>, typeName: String): Boolean {
        return engineController.remove(keys, typeName)
    }

    fun eraseAllData() {
        engineController.eraseAllData()
    }
}