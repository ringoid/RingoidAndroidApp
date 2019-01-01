package com.ringoid.utility

import android.widget.ImageView
import com.bumptech.glide.Glide

object ImageLoader {

    fun load(uri: String, imageView: ImageView) {
        // TODO: add retry logic
        Glide.with(imageView).load(uri).into(imageView)
    }
}
