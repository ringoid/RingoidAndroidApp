package com.ringoid.base

import android.content.Context
import android.net.Uri

interface IImagePreviewReceiver {

    fun register(context: Context?)
    fun unregister(context: Context?)

    fun clear()
    fun hasResult(): Boolean
    fun getLastError(): Throwable?
    fun getLastResult(): Uri?
}
