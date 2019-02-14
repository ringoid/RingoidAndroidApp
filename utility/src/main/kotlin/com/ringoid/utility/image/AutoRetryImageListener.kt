package com.ringoid.utility.image

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.Target
import com.ringoid.utility.delay
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 * @see https://github.com/bumptech/glide/issues/1308
 */
class AutoRetryImageListener(
    private val uri: String?, private val imageView: ImageView,
    private val onSuccess: (() -> Unit)? = null, private val onFailure: (() -> Unit)? = null)
    : RetryImageListener {

    companion object {
        /**
         * Number of retries, remember: the first load is not a retry, so a value of 2 means 3 Glide loads.
         */
        const val RETRY_COUNT = 20
    }

    private var retried = 0

    override fun reset() {
        retried = 0
    }

    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
        Timber.v("Failed to load image [$uri]: $retried")
        onFailure?.invoke()
        Thread { delay(1000L, scheduler = Schedulers.trampoline()) {
                if (retried++ < AutoRetryImageListener.RETRY_COUNT) {  // async recursion's stop condition
                    ImageLoader.load(uri, imageView,this)  // async recursion to try loading image again
                }
            }
        }.start()
        return true  // we handled the problem
    }

    override fun onResourceReady(resource: Drawable, model: Any?, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
        Timber.v("Successfully loaded image")
        onSuccess?.invoke()
        return false  // handle with Glide
    }
}