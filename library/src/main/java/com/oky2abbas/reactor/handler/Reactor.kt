package com.oky2abbas.reactor.handler

import android.content.Context
import com.oky2abbas.reactor.helper.SerializationHelper
import com.oky2abbas.reactor.security.SecurityController
import java.io.Serializable

class Reactor @JvmOverloads constructor(
    appContext: Context,
    isEncrypt: Boolean = true
) {
    val securityController = SecurityController(appContext, isEncrypt)
    val serializationHelper = SerializationHelper()

    inline fun <reified T : Serializable> put(key: String, value: T?): Boolean {
        if (value == null) {
            remove<T>(key)
            return true
        }

        val typeName = T::class.java.simpleName
        val serializeValue = serializationHelper.serialize(value)
        return securityController.put(key, serializeValue, typeName)
    }

    inline fun <reified T : Serializable> get(key: String): T? {
        val typeName = T::class.java.simpleName
        val value = securityController.get(key, typeName) ?: return null
        return serializationHelper.deserialize(value)
    }

    inline fun <reified T : Serializable> get(key: String, default: T): T {
        val typeName = T::class.java.simpleName
        val value = securityController.get(key, typeName) ?: return default
        return serializationHelper.deserialize(value)
    }

    inline fun <reified T : Serializable> remove(vararg keys: String): Boolean {
        val typeName = T::class.java.simpleName
        return securityController.remove(keys.toList(), typeName)
    }

    fun eraseAllData() {
        securityController.eraseAllData()
    }
}