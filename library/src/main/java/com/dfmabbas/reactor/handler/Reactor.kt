package com.dfmabbas.reactor.handler

import android.content.Context
import com.dfmabbas.reactor.security.SecurityController

class Reactor(private val appContext: Context, algorithm: Algorithm) {

    private val securityController = SecurityController(appContext, algorithm)

    fun put(key: String, value: Any): Boolean {
        return securityController.put(key, value)
    }

    fun <T> get(key: String, default: T): T {
        return securityController.get(key, default) ?: return default
    }

    fun remove(key: String, type: Any): Boolean {
        return securityController.remove(key, type)
    }

    fun clearAll() {
        securityController.clearAll()
    }
}