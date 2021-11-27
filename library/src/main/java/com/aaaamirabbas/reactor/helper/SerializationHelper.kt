package com.aaaamirabbas.reactor.helper

import android.util.Base64
import android.util.Base64InputStream
import android.util.Base64OutputStream
import java.io.*


class SerializationHelper {
    fun <T : Serializable> serialize(data: T): String {
        val outputStream = ByteArrayOutputStream()
        val objectOutput = ObjectOutputStream(
            Base64OutputStream(outputStream, Base64.NO_PADDING or Base64.NO_WRAP)
        )
        objectOutput.writeObject(data)
        objectOutput.close()

        return outputStream.toString("UTF-8")
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Serializable> deserialize(data: String): T {
        return ObjectInputStream(
            Base64InputStream(
                ByteArrayInputStream(data.toByteArray()),
                Base64.NO_PADDING or Base64.NO_WRAP
            )
        ).readObject() as T
    }
}