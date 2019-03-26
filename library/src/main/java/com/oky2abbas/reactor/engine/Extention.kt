package com.oky2abbas.reactor.engine

import android.content.Context
import java.io.Serializable

internal fun <T : Serializable> T.getTypeName(): String {
    return this::class.java.simpleName
}

internal fun Context.getPath(): String {
    return this.filesDir?.path + "/"
}