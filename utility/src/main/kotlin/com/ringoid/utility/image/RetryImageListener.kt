package com.ringoid.utility.image

import android.graphics.drawable.Drawable
import com.bumptech.glide.request.RequestListener

interface RetryImageListener : RequestListener<Drawable> {
    fun reset()
}
