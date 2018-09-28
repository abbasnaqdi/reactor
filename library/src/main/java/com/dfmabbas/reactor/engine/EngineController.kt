package com.dfmabbas.reactor.engine

import android.content.Context

internal class EngineController(private val appContext: Context,
                                private val scope: String) {

    private val model = EngineModel(appContext, scope)

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

            if (!model.isDocument("object"))
                model.makeDocument("object")
        }
    }

    internal fun <T> put(key: String, value: String, type: T): Boolean {
        val kind = type.getKindScope()

        val fetchObject = model.fetchJSON(kind)
        fetchObject.put(key, value)

        return model.saveJSON(kind, fetchObject)
    }

    internal fun <T> get(key: String, default: T): String? {
        val kind = default.getKindScope()
        val fetchObject = model.fetchJSON(kind)
        return fetchObject.optString(key, null)
    }

    internal fun <T> remove(key: String, type: T): Boolean {
        val kind = type.getKindScope()

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