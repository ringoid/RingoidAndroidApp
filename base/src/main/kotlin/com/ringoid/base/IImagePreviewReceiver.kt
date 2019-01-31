package com.ringoid.base

import android.net.Uri

interface IImagePreviewReceiver {

    fun register()
    fun unregister()

    fun clear()
    fun hasResult(): Boolean
    fun getLastError(): Throwable?
    fun getLastResult(): Uri?
}
