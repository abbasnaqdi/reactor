package com.dfmabbas.reactor.handler

import android.content.Context
import com.dfmabbas.reactor.engine.EngineController

class Reactor {
    private var appContext: Context? = null;
    private var securityLevel: SecurityLevel? = null
    private var dbName: String? = null;
    private var engineController: EngineController? = null

    fun setContext(context: Context): Reactor {
        if (this.appContext == null)
            this.appContext = context

        return this
    }

    fun setDatabaseName(name: String): Reactor {
        if (this.dbName == null)
            this.dbName = name

        return this
    }

    fun setSecurityLevel(level: SecurityLevel): Reactor {
        if (securityLevel == null)
            securityLevel = level

        return this
    }

    fun build(): Reactor? {
        if (appContext == null)
            return null

        if (dbName == null)
            dbName = appContext?.packageName?.length.toString()

        if (securityLevel == null)
            securityLevel = SecurityLevel.POWERFUL

        dbName += securityLevel.toString()

        if (engineController == null)
            engineController = EngineController(appContext!!, dbName!!, securityLevel!!)

        return this
    }

    fun put(key: String, value: Any): Boolean {
        return engineController?.put(key, value)!!
    }

    fun <T> get(key: String, default: T): T {
        return engineController?.get<T>(key, default) ?: return default
    }

    fun remove(key: String, type: Any): Boolean {
        return engineController?.remove(key, type)!!
    }

    fun clearAll() {
        engineController?.clearAll()!!
    }
}