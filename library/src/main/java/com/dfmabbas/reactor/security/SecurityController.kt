package com.dfmabbas.reactor.security

import android.content.Context
import com.dfmabbas.reactor.engine.EngineController
import com.dfmabbas.reactor.handler.Algorithm

internal class SecurityController(appContext: Context, algorithm: Algorithm) {
    private var engineController = EngineController(appContext, algorithm.name)
    private val securityModel = SecurityModel(appContext, algorithm)

    internal fun <T> put(key: String, value: String, type: T): Boolean {
        val encryptValue = securityModel.encryptValue(value.toString())
        return engineController.put(key, encryptValue, type)
    }

    internal fun <T> get(key: String, default: T): String? {
        val value: String = engineController.get(key, default) ?: return null

        return try {
            securityModel.decryptValue(value)
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun <T> remove(key: String, type: T): Boolean {
        return engineController.remove(key, type)
    }

    fun clearAll() {
        engineController.clearAll()
    }
}