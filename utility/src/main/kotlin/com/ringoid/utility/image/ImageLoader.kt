package com.ringoid.utility.image

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.ringoid.utility.R
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import timber.log.Timber
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

object ImageLoader {

    private const val IMAGE_LOAD_RETRY_COUNT = 4
    private const val IMAGE_LOAD_RETRY_DELAY = 55L  // in ms

    /**
     * @see https://proandroiddev.com/progressive-image-loading-with-rxjava-64bd2b973690
     */
    fun load(uri: String?, thumbnailUri: String? = null, imageView: ImageView, options: RequestOptions? = null) {
        loadRequest(uri, thumbnailUri, imageView.context, options)
            ?.listener(AutoRetryImageListener(uri, imageView, withThumbnail = !thumbnailUri.isNullOrBlank(), options = optimalOptions(imageView.context, uri, options)))
            ?.into(imageView)
    }

    fun loadRequest(uri: String?, thumbnailUri: String? = null, context: Context, options: RequestOptions? = null): RequestBuilder<Drawable>? {
        if (uri.isNullOrBlank()) {
            return null
        }

        val thumbnailRequest =
            thumbnailUri?.let {
                Glide.with(context)
                    .load(it)
                    .apply(optimalOptions(context, thumbnailUri, options))
            }

        return Glide.with(context)
            .load(uri)
            .apply(optimalOptions(context, uri, options))
            .let { request -> thumbnailRequest?.let { request.thumbnail(it) } ?: request.thumbnail(0.1f) }
    }

    @Suppress("CheckResult")
    internal fun load(uri: String?, imageView: ImageView, withThumbnail: Boolean = false,options: RequestOptions? = null) {
        fun getDrawableFuture() = getDrawableFuture(uri, imageView, options)

        if (uri.isNullOrBlank()) {
            return
        }

        Single.just(getDrawableFuture())
            .flatMap { Single.fromFuture(it) }
            .retryWhen {
                it.zipWith<Int, Pair<Int, Throwable>>(Flowable.range(1, IMAGE_LOAD_RETRY_COUNT), BiFunction { e: Throwable, i -> i to e })
                    .flatMap { errorWithAttempt ->
                        val attemptNumber = errorWithAttempt.first
                        val error = errorWithAttempt.second
                        val delayTime = IMAGE_LOAD_RETRY_DELAY * Math.pow(5.0, attemptNumber.toDouble()).toLong()
                        Flowable.timer(delayTime, TimeUnit.MILLISECONDS)
                            .doOnSubscribe { Timber.v("Retry to load image, attempt [$attemptNumber / $IMAGE_LOAD_RETRY_COUNT] after error: $error") }
                            .doOnComplete { if (attemptNumber >= IMAGE_LOAD_RETRY_COUNT) throw error }
                    }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ imageView.post { imageView.setImageDrawable(it) } },
                       {
                           Timber.e(it)
                           if (!withThumbnail) {
                               imageView.post { imageView.setImageResource(R.drawable.ic_no_photo_placeholder_grey_96dp) }
                           }
                       })
    }

    // ------------------------------------------
    private fun getDrawableFuture(uri: String?, imageView: ImageView, options: RequestOptions? = null): Future<Drawable> =
        Glide.with(imageView).load(uri).apply(wrapOptions(options)).submit()

    private fun getDrawableProgress(context: Context) =
        CircularProgressDrawable(context)
            .apply {
                setColorSchemeColors(ContextCompat.getColor(context, R.color.util_grass))
                strokeWidth = 5f
                centerRadius = 30f
                start()
            }

    // ------------------------------------------
    private fun optimalOptions(context: Context, uri: String?, options: RequestOptions?): RequestOptions {
        fun isLocalUri(uri: String?): Boolean = uri?.startsWith("file") ?: false

        if (uri.isNullOrBlank()) {
            return wrapOptions(options)
        }

        val cacheStrategy = if (isLocalUri(uri)) DiskCacheStrategy.NONE
                            else DiskCacheStrategy.AUTOMATIC

        return wrapOptions(options)
            .placeholder(getDrawableProgress(context))
            .diskCacheStrategy(cacheStrategy)
    }

    private fun wrapOptions(options: RequestOptions?): RequestOptions =
        RequestOptions().apply { options?.let { apply(it) } }.error(R.drawable.ic_no_photo_placeholder_grey_96dp)
}
