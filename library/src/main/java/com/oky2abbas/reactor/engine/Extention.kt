package com.oky2abbas.reactor.engine

import android.content.Context

internal fun Context.getPath(): String {
    return this.filesDir?.path + "/"
}