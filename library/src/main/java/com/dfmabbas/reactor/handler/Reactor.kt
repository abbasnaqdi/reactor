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

    fun put(key: String, value: Any): Boolean {
        return engineController?.put(key, value)!!
    }

    fun get(key: String, default: Any): Any {
        return engineController?.get(key, default) ?: return default
    }

    fun getString(key: String, default: Any): String {
        return get(key, default).toString()
    }

    fun getBoolean(key: String, default: Any): Boolean {
        return get(key, default).toString().toBoolean()
    }

    fun getInt(key: String, default: Any): Int {
        return get(key, default).toString().toInt()
    }

    fun getFloat(key: String, default: Any): Float {
        return get(key, default).toString().toFloat()
    }

    fun getLong(key: String, default: Any): Long {
        return get(key, default).toString().toLong()
    }

    fun getDouble(key: String, default: Any): Double {
        return get(key, default).toString().toDouble()
    }

    fun remove(key: String, type: Any): Boolean {
        return engineController?.remove(key, type)!!
    }

    fun clearAll() {
        engineController?.clearAll()!!
    }
}