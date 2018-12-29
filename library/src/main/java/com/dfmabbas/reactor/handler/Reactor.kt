package com.dfmabbas.reactor.handler

import android.content.Context
import com.dfmabbas.reactor.helper.SerializationHelper
import com.dfmabbas.reactor.security.SecurityController
import java.io.Serializable

class Reactor(private val appContext: Context, isCryptography: Boolean) {
    private val securityController = SecurityController(appContext, isCryptography)
    private val serializationHelper = SerializationHelper()

    fun <T : Serializable> put(key: String, value: T): Boolean {
        val serializeValue = serializationHelper.serialize(value)
        return securityController.put(key, serializeValue, value)
    }

    fun <T : Serializable> get(key: String, default: T): T {
        val value = securityController.get(key, default)

        if (value == null) {
            put(key, default)
            return default
        }

        return serializationHelper.deserialize(value)
    }

    fun <T : Serializable> remove(key: String, type: T): Boolean {
        return securityController.remove(key, type)
    }

    fun clearAll() {
        securityController.clearAll()
    }
}