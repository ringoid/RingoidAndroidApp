package com.ringoid.utility.image

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ringoid.utility.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

object ImageLoader {

    private lateinit var progress: Drawable

    fun init(context: Context) {
        progress = CircularProgressDrawable(context)
            .apply {
                setColorSchemeColors(ContextCompat.getColor(context, R.color.util_grass))
                strokeWidth = 5f
                centerRadius = 30f
                start()
            }
    }

    fun load(uri: String?, thumbnailUri: String? = null, imageView: ImageView, options: RequestOptions? = null) {
        if (uri.isNullOrBlank()) {
            return
        }

        val thumbnailRequest =
            thumbnailUri?.let {
                Glide.with(imageView)
                    .load(it)
                    .apply(RequestOptions().placeholder(progress))
            }

        Glide.with(imageView)
            .load(uri)
            .apply(RequestOptions().apply { options?.let { apply(it) } })
//            .listener(AutoRetryImageListener(uri, imageView))
            .let { request -> thumbnailRequest?.let { request.thumbnail(it) } ?: request.thumbnail(0.1f) }
            .into(imageView)
    }

    @Suppress("CheckResult")
    fun load(uri: String?, imageView: ImageView, listener: RetryImageListener) {
        if (uri.isNullOrBlank()) {
            return
        }

        val drawableFuture = Glide.with(imageView)
            .load(uri)
            .listener(listener)
            .submit()

        Observable.fromFuture(drawableFuture)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ imageView.post { imageView.setImageDrawable(it) } }, Timber::e)
    }
}
