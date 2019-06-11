package com.ringoid.utility.image

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.ringoid.utility.thread
import timber.log.Timber
import java.io.FileNotFoundException
import java.lang.ref.WeakReference

/**
 * @see https://github.com/bumptech/glide/issues/1308
 */
class AutoRetryImageListener(
    private val uri: String?, private val imageView: WeakReference<ImageView>,
    private val options: RequestOptions? = null, private val withThumbnail: Boolean = false)
    : RequestListener<Drawable> {

    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
        Timber.v("Failed to load image: [$uri]")
        if (e?.rootCauses?.find { it is FileNotFoundException } != null) {
            Timber.e("Image file not found by url: $uri")
            return false  // handle with Glide
        }

        thread(2000L) { ImageLoader.load(uri, imageView, withThumbnail, options) }
        return true  // we handled the problem
    }

    override fun onResourceReady(resource: Drawable, model: Any?, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
        Timber.v("Successfully loaded image")
        return false  // handle with Glide
    }
}
