package com.dfmabbas.reactor.engine

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

internal class EngineModel(private val appContext: Context,
                           private val scope: String) {

    private val path = appContext.getPath()

    internal fun makeDatabase(): Boolean {
        return File(path + scope).mkdir()
    }

    internal fun makeDocument(name: String): Boolean {
        val file = File("$path$scope/$name.json")
        return writeJSON(file, JSONObject("{is:true}"))
    }

    internal fun isDatabase(): Boolean {
        return File(path + scope).exists()
    }

    internal fun isDocument(name: String): Boolean {
        return File("$path$scope/$name.json").exists()
    }

    internal fun clearAll(): Boolean {
        return File(path + scope).deleteRecursively()
    }

    internal fun fetchJSON(name: String): JSONObject {
        val file = File("$path$scope/$name.json")
        return readJSON(file)
    }

    internal fun saveJSON(name: String, jsonObject: JSONObject): Boolean {
        val file = File("$path$scope/$name.json")
        return writeJSON(file, jsonObject)
    }

    private fun writeJSON(file: File, jsonObject: JSONObject): Boolean {
        if (jsonObject.length() <= 0)
            return false

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

        val value = String(buffer, Charsets.UTF_8)

        fileInputStream.close()

        return JSONObject(value)
    }
}