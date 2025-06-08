package com.aaaamirabbas.reactor.security

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.aaaamirabbas.reactor.helper.AESUtils_Old // Use the restored AESUtils_Old
import java.nio.charset.Charset
import java.security.MessageDigest

@SuppressLint("HardwareIds")
internal class SecurityModel_Old(
    private val appContext: Context,
    private val isEncryptEnable: Boolean
) {

    private val mainKey: String by lazy {
        getHashKey(
            appContext.packageName + Settings.Secure.getString(
                appContext.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        )
    }

    // This is a common way to generate a consistent hash key.
    // The original might have been different.
    private fun getHashKey(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charset.defaultCharset()))
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    internal fun encryptValue(value: String?): String {
        if (value.isNullOrEmpty()) return ""
        if (!isEncryptEnable) return value

        return AESUtils_Old.encrypt(mainKey, value)
    }

    internal fun decryptValue(value: String?): String {
        if (value.isNullOrEmpty()) return ""
        if (!isEncryptEnable) return value

        return AESUtils_Old.decrypt(mainKey, value)
    }
}
