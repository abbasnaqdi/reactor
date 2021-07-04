package com.oky2abbas.reactor.engine

import android.content.Context

internal class EngineController(
    appContext: Context, scope: String
) {

    private val model = EngineModel(appContext, scope)

    init {
        initEngine()
    }

    private fun initEngine() {
        if (!model.isDatabase()) {
            model.makeDatabase()
        }
    }

    internal fun put(key: String, value: String, typeName: String): Boolean {
        if (!model.isDocument(typeName))
            model.makeDocument(typeName)

        val fetchObject = model.fetchJSON(typeName)
        fetchObject.put(key, value)

        return model.saveJSON(typeName, fetchObject)
    }

    internal fun get(key: String, typeName: String): String? {
        if (!model.isDocument(typeName))
            model.makeDocument(typeName)

        val fetchObject = model.fetchJSON(typeName)
        return fetchObject.optString(key, null)
    }

    internal fun remove(keys: List<String>, typeName: String): Boolean {
        if (!model.isDocument(typeName))
            model.makeDocument(typeName)

        val fetchObject = model.fetchJSON(typeName)
        keys.forEach { key ->
            if (!fetchObject.has(key))
                fetchObject.remove(key)
        }

        return model.saveJSON(typeName, fetchObject)
    }

    internal fun eraseAllData(): Boolean {
        val result = model.eraseAllData()
        initEngine()
        return result
    }
}