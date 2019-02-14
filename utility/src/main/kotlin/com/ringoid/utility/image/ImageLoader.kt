package com.ringoid.utility.image

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
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

    /**
     * @see https://proandroiddev.com/progressive-image-loading-with-rxjava-64bd2b973690
     */
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

        val xOptions = wrapOptions(options)

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
            .doFinally { clearDrawableFuture() ; Timber.v("Clear resources used to load image") }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ imageView.post { imageView.setImageDrawable(it) } },
                       { imageView.post { imageView.setImageResource(R.drawable.ic_no_photo_placeholder_grey_96dp) } ; Timber.e(it) })
    }

    // ------------------------------------------
    private var prevRequest: Future<Drawable>? = null

    private fun clearDrawableFuture() {
        prevRequest?.cancel(true)
        prevRequest = null
    }

    private fun getDrawableFuture(uri: String?, imageView: ImageView, options: RequestOptions? = null): Future<Drawable> {
        clearDrawableFuture()
        prevRequest = Glide.with(imageView).load(uri).apply(wrapOptions(options)).submit()
        return prevRequest!!
    }

    private fun wrapOptions(options: RequestOptions?): RequestOptions =
        RequestOptions().apply { options?.let { apply(it) } }.error(R.drawable.ic_no_photo_placeholder_grey_96dp)
}
