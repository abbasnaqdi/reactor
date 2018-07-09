package com.dfmabbas.reactor.engine

import android.content.Context
import com.dfmabbas.reactor.handler.SecurityLevel
import com.dfmabbas.reactor.security.SecurityController

internal class EngineController(context: Context, db_name: String, security_level: SecurityLevel) {

    private var appContext: Context? = null;
    private var securityController: SecurityController? = null
    private var engineModel: EngineModel? = null

    private var dbName: String? = null;
    private var securityLevel: SecurityLevel? = null

    init {
        if (this.appContext == null) this.appContext = context

        if (securityController == null)
            securityController = SecurityController(appContext?.packageName!!)

        if (this.dbName == null)
            this.dbName = db_name
        if (engineModel == null)
            engineModel = EngineModel(appContext!!, this.dbName!!)

        if (this.securityLevel == null) this.securityLevel = security_level

        configEngine()
    }

    private fun configEngine() {
        if (!engineModel?.isDatabase()!!) {
            engineModel?.makeDatabase()

            if (!engineModel?.isDocument("string")!!)
                engineModel?.makeDocument("string")

            if (!engineModel?.isDocument("bool")!!)
                engineModel?.makeDocument("bool")

            if (!engineModel?.isDocument("int")!!)
                engineModel?.makeDocument("int")

            if (!engineModel?.isDocument("long")!!)
                engineModel?.makeDocument("long")

            if (!engineModel?.isDocument("double")!!)
                engineModel?.makeDocument("double")

            if (!engineModel?.isDocument("float")!!)
                engineModel?.makeDocument("float")

            if (!engineModel?.isDocument("unk")!!)
                engineModel?.makeDocument("unk")
        }
    }

    internal fun put(key: String, value: Any): Boolean {
        val name = getType(value)
        val secureValue = securityController?.encryptValue(key, value.toString(), securityLevel!!)

        val fetchObject = engineModel?.fetchJSON(name)
        fetchObject?.putOpt(key, secureValue)

        return engineModel?.saveJSON(name, fetchObject!!)!!
    }

    internal fun <T> get(key: String, default: T): T {
        val name = getType(default)
        val fetchObject = engineModel?.fetchJSON(name)!!

        if (!fetchObject.has(key))
            return default

        val value = securityController?.decryptValue(key,
                fetchObject.opt(key).toString(),
                securityLevel!!)

        return value?.convertToAny(default!!) as T
    }

    internal fun remove(key: String, type: Any): Boolean {
        val name = getType(type)
        val fetchObject = engineModel?.fetchJSON(name)!!

        if (!fetchObject.has(key))
            return true

        fetchObject.remove(key)

        return engineModel?.saveJSON(name, fetchObject)!!
    }

    internal fun clearAll(): Boolean {
        val result = engineModel?.clearAll()!!
        configEngine()

        return result
    }
}