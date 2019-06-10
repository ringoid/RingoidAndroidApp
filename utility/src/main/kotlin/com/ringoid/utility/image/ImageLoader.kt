package com.ringoid.utility.image

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

object ImageLoader {

    private const val IMAGE_LOAD_RETRY_COUNT = 4
    private const val IMAGE_LOAD_RETRY_DELAY = 55L  // in ms

    /**
     * @see https://proandroiddev.com/progressive-image-loading-with-rxjava-64bd2b973690
     */
    fun load(uri: String?, thumbnailUri: String? = null, imageView: ImageView, options: RequestOptions? = null) {
        loadRequest(uri, thumbnailUri, imageView.context, options)
            ?.listener(AutoRetryImageListener(uri, imageView, withThumbnail = !thumbnailUri.isNullOrBlank(), options = null))
            ?.into(imageView)
    }

    fun simpleLoadRequest(uri: String?, context: Context, skipMemoryCache: Boolean = false, options: RequestOptions? = null): RequestBuilder<Drawable>? =
        uri?.let {
            Glide.with(context)
                .load(it)
                .diskCacheStrategy(cacheStrategy(uri))
                .skipMemoryCache(skipMemoryCache)
        }

    @Suppress("CheckResult")
    fun clear(context: Context?) {
        if (context == null) {
            return
        }

        Completable.fromCallable { Glide.get(context).clearDiskCache() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .andThen(Completable.fromCallable { Glide.get(context).clearMemory() })
            .subscribe({ Timber.v("Cleared Glide caches") }, Timber::e)
    }

    // ------------------------------------------
    private fun loadRequest(uri: String?, thumbnailUri: String? = null, context: Context, options: RequestOptions? = null): RequestBuilder<Drawable>? {
        if (uri.isNullOrBlank()) {
            return null
        }

        val thumbnailRequest = thumbnailUri?.let { Glide.with(context).load(it) }

        return Glide.with(context)
            .load(uri)
            .diskCacheStrategy(cacheStrategy(uri))
            .skipMemoryCache(true)  // don't keep original (large) image in memory cache
            .let { request -> thumbnailRequest?.let { request.thumbnail(it) } ?: request.thumbnail(0.1f) }
    }

    @Suppress("CheckResult")
    internal fun load(uri: String?, imageView: ImageView, withThumbnail: Boolean = false,options: RequestOptions? = null) {
        fun getDrawableFuture() = Glide.with(imageView).load(uri).submit()

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
            .subscribe({ imageView.post { imageView.setImageDrawable(it) } }, Timber::e)
    }

    private fun isLocalUri(uri: String?): Boolean = uri?.startsWith("file") ?: false

    private fun cacheStrategy(uri: String?): DiskCacheStrategy =
        if (isLocalUri(uri)) DiskCacheStrategy.NONE else DiskCacheStrategy.RESOURCE
}
