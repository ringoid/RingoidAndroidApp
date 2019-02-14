package com.ringoid.utility.image

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
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
        fun isLocalUri(uri: String?): Boolean = uri?.startsWith("file") ?: false

        if (uri.isNullOrBlank()) {
            return
        }

        val cacheStrategy = if (isLocalUri(uri)) DiskCacheStrategy.NONE
                            else DiskCacheStrategy.AUTOMATIC
        val thumbnailCacheStrategy = if (isLocalUri(thumbnailUri)) DiskCacheStrategy.NONE
                                     else DiskCacheStrategy.AUTOMATIC

        val xOptions = wrapOptions(imageView.context, options).diskCacheStrategy(cacheStrategy)
        val xThumbnailOptions = RequestOptions()
            .placeholder(getDrawableProgress(imageView.context))
            .diskCacheStrategy(thumbnailCacheStrategy)

        val thumbnailRequest =
            thumbnailUri?.let {
                Glide.with(imageView)
                    .load(it)
                    .apply(xThumbnailOptions)
            }

        Glide.with(imageView)
            .load(uri)
            .apply(xOptions)
            .listener(AutoRetryImageListener(uri, imageView, xOptions))
            .let { request -> thumbnailRequest?.let { request.thumbnail(it) } ?: request.thumbnail(0.1f) }
            .into(imageView)
    }

    @Suppress("CheckResult")
    internal fun load(uri: String?, imageView: ImageView, options: RequestOptions? = null) {
        fun getDrawableFuture() = getDrawableFuture(uri, imageView, options)

        if (uri.isNullOrBlank()) {
            return
        }

        Single.fromFuture(getDrawableFuture())
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
                       { imageView.post { imageView.setImageResource(R.drawable.ic_no_photo_placeholder_grey_96dp) } ; Timber.e(it) })
    }

    // ------------------------------------------
    private fun getDrawableFuture(uri: String?, imageView: ImageView, options: RequestOptions? = null): Future<Drawable> =
        Glide.with(imageView).load(uri).apply(wrapOptions(imageView.context, options)).submit()

    private fun getDrawableProgress(context: Context) =
        CircularProgressDrawable(context)
            .apply {
                setColorSchemeColors(ContextCompat.getColor(context, R.color.util_grass))
                strokeWidth = 5f
                centerRadius = 30f
                start()
            }

    private fun wrapOptions(context: Context, options: RequestOptions?): RequestOptions =
        RequestOptions().apply { options?.let { apply(it) } }
            .error(R.drawable.ic_no_photo_placeholder_grey_96dp)
            .placeholder(getDrawableProgress(context))
}
