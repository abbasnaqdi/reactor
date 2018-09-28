package com.dfmabbas.reactor.engine

import android.content.Context
import java.io.Serializable

internal class EngineController(private val appContext: Context,
                                private val scope: String) {

    private val model = EngineModel(appContext, scope)

    init {
        initEngine()
    }

    private fun initEngine() {
        if (!model.isDatabase()) {
            model.makeDatabase()
        }
    }

    internal fun <T : Serializable> put(key: String, value: String, type: T): Boolean {
        val typeName = type.getTypeName()
        if (!model.isDocument(typeName))
            model.makeDocument(typeName)

        val fetchObject = model.fetchJSON(typeName)
        fetchObject.put(key, value)

        return model.saveJSON(typeName, fetchObject)
    }

    internal fun <T : Serializable> get(key: String, default: T): String? {
        val typeName = default.getTypeName()
        if (!model.isDocument(typeName))
            model.makeDocument(typeName)

        val fetchObject = model.fetchJSON(typeName)
        return fetchObject.optString(key, null)
    }

    internal fun <T : Serializable> remove(key: String, type: T): Boolean {
        val typeName = type.getTypeName()
        if (!model.isDocument(typeName))
            model.makeDocument(typeName)

        val fetchObject = model.fetchJSON(typeName)
        if (!fetchObject.has(key))
            return true

        fetchObject.remove(key)

        return model.saveJSON(typeName, fetchObject)
    }

    internal fun clearAll(): Boolean {
        val result = model.clearAll()
        initEngine()
        return result
    }
}