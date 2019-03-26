package com.oky2abbas.reactor.handler

import android.content.Context
import com.oky2abbas.reactor.helper.SerializationHelper
import com.oky2abbas.reactor.security.SecurityController
import java.io.Serializable

class Reactor @JvmOverloads constructor(
    private val appContext: Context,
    isEncrypt: Boolean = true
) {

    private val securityController = SecurityController(appContext, isEncrypt)
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