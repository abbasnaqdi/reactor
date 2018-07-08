package com.dfmabbas.reactor.security

import android.util.Base64
import com.dfmabbas.reactor.handler.SecurityLevel

internal class SecurityController(scope: String) {
    private var crypt: AESCrypt? = null
    private var hash: Hash? = null
    private var appScope: String? = null

    init {
        if (crypt == null) crypt = AESCrypt()
        if (this.appScope == null) this.appScope = hash?.getSHA256(scope)
    }

    internal fun encryptValue(key: String, value: String, securityLevel: SecurityLevel): String {
        return when (securityLevel) {
            SecurityLevel.POWERFUL -> encryptPowerful(key, value)!!
            SecurityLevel.FAST -> encryptFast(value)!!
            SecurityLevel.NONE -> value
            else -> value
        }
    }

    internal fun decryptValue(key: String, value: String, securityLevel: SecurityLevel): String {
        return when (securityLevel) {
            SecurityLevel.POWERFUL -> decryptPowerful(key, value)!!
            SecurityLevel.FAST -> decryptFast(value)!!
            SecurityLevel.NONE -> value
            else -> value
        }
    }

    internal fun hashValue(value: String): String? {
        return hash?.getSHA256(value)
    }

    private fun encryptPowerful(key: String, value: String): String? {
        val password = hash?.getSHA256("$appScope$key")
        return crypt?.encrypt(password, value)
    }

    private fun decryptPowerful(key: String, value: String): String? {
        val password = hash?.getSHA256("$appScope$key")
        return crypt?.decrypt(password, value)
    }

    private fun encryptFast(value: String): String? {
        val data = value.toByteArray(Charsets.UTF_8)
        return Base64.encodeToString(data, Base64.DEFAULT)
    }

    private fun decryptFast(value: String): String? {
        val data = Base64.decode(value, Base64.DEFAULT)
        return String(data, Charsets.UTF_8)
    }

    private fun getSignature(): String {
        return ""
    }
}