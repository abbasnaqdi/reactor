package com.aaaamirabbas.reactor.helper

import android.util.Base64
import android.util.Base64InputStream
import android.util.Base64OutputStream
import java.io.*


@Suppress("UNCHECKED_CAST")
class SerializationHelper {
    @Synchronized
    fun <T : Serializable> serialize(data: T): String? {
        kotlin.runCatching {
            val outputStream = ByteArrayOutputStream()
            val objectOutput = ObjectOutputStream(
                Base64OutputStream(outputStream, Base64.NO_PADDING or Base64.NO_WRAP)
            )

            objectOutput.use {
                it.writeObject(data)
            }

            outputStream.use {
                it.toString("UTF-8")
            }
        }.onFailure {
            it.printStackTrace()
            return null
        }.onSuccess {
            return it
        }

        return null
    }

    @Synchronized
    fun <T : Serializable> deserialize(data: String): T? {
        kotlin.runCatching {
            val inputStream = ByteArrayInputStream(data.toByteArray(charset("UTF-8")))
            val objectInput = ObjectInputStream(
                Base64InputStream(inputStream, Base64.NO_PADDING or Base64.NO_WRAP)
            )

            objectInput.use { it.readObject() as T }
        }.onFailure {
            it.printStackTrace()
            return null
        }.onSuccess {
            return it
        }

        return null
    }
}