package com.dfmabbas.reactor.engine

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

internal class EngineModel(context: Context, db_name: String) {

    private var appContext: Context? = null
    private var dbName: String? = null
    private var pathDir: String? = null

    init {
        if (this.appContext == null) this.appContext = context
        if (this.dbName == null) this.dbName = db_name
        if (pathDir == null) pathDir = appContext?.getPath()
    }

    internal fun makeDatabase(): Boolean {
        return File(pathDir + dbName).mkdir()
    }

    internal fun makeDocument(name: String): Boolean {
        val file = File("$pathDir$dbName/$name.json")

        if (file.createNewFile()) {
            return writeJSON(file, JSONObject())
        }

        return false
    }

    internal fun isDatabase(): Boolean {
        return File(pathDir + dbName).exists()
    }

    internal fun isDocument(name: String): Boolean {
        return File("$pathDir$dbName/$name.json").exists()
    }

    internal fun fetchJSON(name: String): JSONObject {
        val file = File("$pathDir$dbName/$name.json")
        return readJSON(file)
    }

    internal fun saveJSON(name: String, jsonObject: JSONObject): Boolean {
        val file = File("$pathDir$dbName/$name.json")
        return writeJSON(file, jsonObject)
    }

    internal fun clearAll(): Boolean {
        return File(pathDir + dbName).deleteRecursively()
    }

    private fun writeJSON(file: File, jsonObject: JSONObject): Boolean {
        val fileOutputStream = FileOutputStream(file)

        fileOutputStream.write(jsonObject.toString().toByteArray())
        fileOutputStream.close()

        return true
    }

    private fun readJSON(file: File): JSONObject {
        val fileInputStream = FileInputStream(file)
        val size = fileInputStream.available()
        val buffer = ByteArray(size)

        fileInputStream.read(buffer)
        fileInputStream.close()

        val value = String(buffer, Charsets.UTF_8)

        return JSONObject(value)
    }
}