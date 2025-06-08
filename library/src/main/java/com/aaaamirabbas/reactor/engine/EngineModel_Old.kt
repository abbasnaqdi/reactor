package com.aaaamirabbas.reactor.engine

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.io.IOException

internal class EngineModel_Old(
    private val appContext: Context,
    private val scope: String // "AES" or "NONE"
) {

    private val mainDir: File = appContext.filesDir
    private val reactorDir: File = File(mainDir, "Reactor") // Assuming old files were in "Reactor" subdirectory

    // Helper to get the path for a specific document type
    private fun getDocumentPath(typeName: String): File {
        if (!reactorDir.exists()) {
            // If the directory doesn't exist, no old data to migrate from this path
            // This check can be refined in the MigrationManager
        }
        return File(reactorDir, "${scope}_${typeName}.json")
    }

    // Fetches the JSONObject from a given document type
    // This is critical for reading old data
    internal fun fetchJSON(typeName: String): JSONObject {
        val file = getDocumentPath(typeName)
        if (!file.exists()) {
            return JSONObject() // Return empty if no such file
        }
        return try {
            val jsonString = file.readText(Charsets.UTF_8)
            JSONObject(jsonString)
        } catch (e: IOException) {
            e.printStackTrace()
            JSONObject() // Return empty on error
        } catch (e: org.json.JSONException) {
            e.printStackTrace()
            JSONObject() // Return empty on error
        }
    }

    // Method to discover all possible typeName files for a given scope
    internal fun discoverTypeNames(): List<String> {
        if (!reactorDir.exists()) return emptyList()

        val typeNames = mutableListOf<String>()
        reactorDir.listFiles { _, name -> name.startsWith("${scope}_") && name.endsWith(".json") }
            ?.forEach { file ->
                val nameWithoutScope = file.name.removePrefix("${scope}_")
                val typeName = nameWithoutScope.removeSuffix(".json")
                typeNames.add(typeName)
            }
        return typeNames
    }

    // Method to get all keys from a specific typeName document
    internal fun getAllKeysForType(typeName: String): List<String> {
        val jsonObject = fetchJSON(typeName)
        return jsonObject.keys().asSequence().toList()
    }

    // Old isDatabase, makeDatabase, saveJSON, isDocument, makeDocument, eraseAllData are not needed for reading.
    // However, eraseAllData might be useful in MigrationManager after successful migration.
    // For now, focus on read capabilities.

    // Potentially useful for MigrationManager to clean up old files
    internal fun getOldDataFile(typeName: String): File {
        return getDocumentPath(typeName)
    }

    internal fun getOldDataDirectory(): File {
        return reactorDir
    }
}
