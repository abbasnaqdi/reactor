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

    fun <T> get(key: String, default: T): T {
        return engineController?.get<T>(key, default) ?: return default
    }

//    fun <T> remove(key: String, type: T): Boolean {
//        return engineController?.remove(key, type)!!
//    }

    fun clearAll() {
        engineController?.clearAll()!!
    }
}