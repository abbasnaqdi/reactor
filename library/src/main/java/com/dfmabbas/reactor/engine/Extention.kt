package com.dfmabbas.reactor.engine

import android.content.Context

internal fun <T> getKindScope(value: T): String {
    return when (value) {
        is Boolean -> "bool"
        is String -> "string"
        is Int -> "int"
        is Long -> "long"
        is Double -> "double"
        is Float -> "float"
        else -> "unk"
    }
}

internal fun <T> Any.toT(kind: T): T {
    val value = this.toString()

    return when (kind) {
        is Boolean -> value.toBoolean()
        is Int -> value.toInt()
        is Long -> value.toLong()
        is Double -> value.toDouble()
        is Float -> value.toFloat()
        else -> this
    } as T
}

internal fun Context.getPath(): String {
    return this.filesDir?.path + "/"
}