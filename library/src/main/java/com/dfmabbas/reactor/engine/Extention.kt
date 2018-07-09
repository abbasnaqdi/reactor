package com.dfmabbas.reactor.engine

import android.content.Context

internal fun <T> getType(value: T): String {
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

internal fun String.convertToAny(type: Any): Any {
    return when (type) {
        is Boolean -> this.toBoolean()
        is Int -> this.toInt()
        is Long -> this.toLong()
        is Double -> this.toDouble()
        is Float -> this.toFloat()
        is String -> this
        else -> this
    }
}

internal fun Context.getPath(): String {
    return this.filesDir?.path + "/"
}