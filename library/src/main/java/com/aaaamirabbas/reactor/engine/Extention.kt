package com.aaaamirabbas.reactor.engine

import android.content.Context

internal fun Context.getPath(): String {
    return this.filesDir?.path + "/"
}