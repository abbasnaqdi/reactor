package com.dfmabbas.reactor.engine

import android.content.Context

internal fun <T> T.getKindScope(): String {
    return when (this) {
        is Boolean -> "bool"
        is String -> "string"
        is Int -> "int"
        is Long -> "long"
        is Double -> "double"
        is Float -> "float"
        else -> "object"
    }
}

internal fun Context.getPath(): String {
    return this.filesDir?.path + "/"
}