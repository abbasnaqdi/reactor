package com.dfmabbas.reactor.engine.helper

import android.content.Context
import java.io.*


internal class FileHandler(private val context: Context) {

    internal fun writeJSON(file: File, value: String): Boolean {
        var bw: BufferedWriter? = null
        var fw: FileWriter? = null

        fw = FileWriter(file);
        bw = BufferedWriter(fw);

        bw.write(value);

        bw.close();
        fw.close();

//        val fileOutputStream = FileOutputStream(file)
//
//        fileOutputStream.write(value.toByteArray())
//        fileOutputStream.close()

        return true
    }

    internal fun readJSON(file: File): String {
        var result = ""

        val fileInputStream = FileInputStream(file)
        val myReader = BufferedReader(InputStreamReader(fileInputStream))
        val aBuffer = StringBuffer()

        myReader.forEachLine {
            aBuffer.append(it)
        }

        myReader.close()

        result = aBuffer.toString()

        return result
    }
}