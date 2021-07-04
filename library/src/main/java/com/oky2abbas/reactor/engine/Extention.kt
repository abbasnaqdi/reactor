package com.oky2abbas.reactor.engine

import android.content.Context
import java.io.Serializable

internal fun Context.getPath(): String {
    return this.filesDir?.path + "/"
}