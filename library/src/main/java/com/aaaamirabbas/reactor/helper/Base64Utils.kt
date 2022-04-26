package com.aaaamirabbas.reactor.helper

import android.os.Build
import android.util.Base64
import java.util.Base64.getDecoder
import java.util.Base64.getEncoder

object Base64Utils {
    fun encode(text: String): String {
        val data: ByteArray = text.toByteArray(Charsets.UTF_8)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            getEncoder().encodeToString(data)
        else Base64.encodeToString(data, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    fun decode(base64: String): String {
        val decodeString = Base64.decode(base64, Base64.DEFAULT)

        return String(decodeString, Charsets.UTF_8)
    }
}