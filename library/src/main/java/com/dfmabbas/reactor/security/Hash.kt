package com.dfmabbas.reactor.security

import java.security.MessageDigest

internal class Hash {
    internal fun getSHA256(value: String): String {
        val bytes = value.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)

        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}