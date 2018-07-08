package com.dfmabbas.reactor.engine.helper

import android.content.Context

internal fun Any.getType(): String {
    return when (this) {
        is Boolean -> "bool"
        is String -> "string"
        is Int -> "int"
        is Long -> "long"
        is Double -> "double"
        is Float -> "float"
        else -> "unk"
    }
}

internal fun Context.getPath(): String {
    return this.filesDir?.path + "/"
}