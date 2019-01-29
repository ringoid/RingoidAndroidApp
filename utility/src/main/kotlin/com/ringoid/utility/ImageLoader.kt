package com.ringoid.utility

import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

object ImageLoader {

    fun load(uri: String?, imageView: ImageView, options: RequestOptions? = null) {
        if (uri.isNullOrBlank()) {
            return
        }

        val progress = CircularProgressDrawable(imageView.context)
            .apply {
                setColorSchemeColors(ContextCompat.getColor(imageView.context, R.color.util_grass))
                strokeWidth = 5f
                centerRadius = 30f
                start()
            }

        // TODO: add retry logic
        Glide.with(imageView)
            .load(uri)
            .apply(RequestOptions().placeholder(progress).apply { options?.let { apply(it) } })
            .into(imageView)
    }
}
