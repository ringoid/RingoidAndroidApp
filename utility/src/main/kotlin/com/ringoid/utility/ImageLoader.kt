package com.ringoid.utility

import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

object ImageLoader {

    fun load(uri: String, imageView: ImageView) {
        val progress = CircularProgressDrawable(imageView.context)
            .apply {
                strokeWidth = 5f
                centerRadius = 30f
                start()
            }

        // TODO: add retry logic
        Glide.with(imageView)
            .load(uri)
            .apply(RequestOptions().placeholder(progress))
            .into(imageView)
    }
}
