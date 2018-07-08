package com.dfmabbas.reactor.handler

import android.content.Context
import com.dfmabbas.reactor.engine.EngineController

class Reactor(context: Context, dbName: String, securityLevel: SecurityLevel) {
    private var appContext: Context? = null;
    private var engineController: EngineController? = null

    private var dbName: String? = null;

    init {
        if (this.appContext == null) this.appContext = context
        if (this.dbName == null) this.dbName = dbName

        if (engineController == null)
            engineController = EngineController(context, dbName, securityLevel)
    }

    fun put(key: String, value: Any): Boolean? {
        return engineController?.append(key, value)
    }

    fun get(key: String, default: Any): Any? {
        return engineController?.get(key, default)
    }

    fun getString(key: String, default: Any): String? {
        return engineController?.get(key, default) as String?
    }

    fun getBoolean(key: String, default: Any): Boolean? {
        return engineController?.get(key, default) as Boolean?
    }

    fun getInt(key: String, default: Any): Int? {
        return engineController?.get(key, default) as Int?
    }

    fun getFloat(key: String, default: Any): Float? {
        return engineController?.get(key, default) as Float?
    }

    fun getLong(key: String, default: Any): Long? {
        return engineController?.get(key, default) as Long?
    }

    fun getDouble(key: String, default: Any): Double? {
        return engineController?.get(key, default) as Double?
    }

    fun remove(key: String, type: Any): Boolean {
        return engineController?.remove(key, type)!!
    }

    fun clearAll() {
        engineController?.clearAll()!!
    }
}