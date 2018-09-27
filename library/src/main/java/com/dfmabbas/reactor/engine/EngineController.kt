package com.dfmabbas.reactor.engine

import android.content.Context

internal class EngineController(private val appContext: Context,
                                private val scope: String) {

    private var model = EngineModel(appContext, scope)

    init {
        initEngine()
    }

    private fun initEngine() {
        if (!model.isDatabase()) {
            model.makeDatabase()

            if (!model.isDocument("string"))
                model.makeDocument("string")

            if (!model.isDocument("bool"))
                model.makeDocument("bool")

            if (!model.isDocument("int"))
                model.makeDocument("int")

            if (!model.isDocument("long"))
                model.makeDocument("long")

            if (!model.isDocument("double"))
                model.makeDocument("double")

            if (!model.isDocument("float"))
                model.makeDocument("float")

            if (!model.isDocument("unk"))
                model.makeDocument("unk")
        }
    }

    internal fun put(key: String, value: String, type: Any): Boolean {
        val kind = getKindScope(type)

        val fetchObject = model.fetchJSON(kind)
        fetchObject.putOpt(key, value)

        return model.saveJSON(kind, fetchObject)
    }

    internal fun get(key: String, default: Any): Any? {
        val kind = getKindScope(default)
        val fetchObject = model.fetchJSON(kind)

        if (!fetchObject.has(key))
            return null

        return fetchObject.opt(key)
    }

    internal fun remove(key: String, type: Any): Boolean {
        val kind = getKindScope(type)
        val fetchObject = model.fetchJSON(kind)

        if (!fetchObject.has(key))
            return true

        fetchObject.remove(key)

        return model.saveJSON(kind, fetchObject)
    }

    internal fun clearAll(): Boolean {
        val result = model.clearAll()

        initEngine()

        return result
    }
}