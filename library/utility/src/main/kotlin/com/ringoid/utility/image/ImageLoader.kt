package com.ringoid.utility.image

import android.net.Uri
import android.widget.ImageView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.ImageRequest

object ImageLoader {
    /**
     * @see https://proandroiddev.com/progressive-image-loading-with-rxjava-64bd2b973690
     */
    fun load(uri: String?, thumbnailUri: String? = null, imageView: ImageView) {
        if (uri.isNullOrBlank()) {
            return
        }

        if (imageView is SimpleDraweeView) {
            thumbnailUri?.let {
                val controller = Fresco.newDraweeControllerBuilder()
                    .setLowResImageRequest(ImageRequest.fromUri(it))
                    .setImageRequest(ImageRequest.fromUri(Uri.parse(uri)))
                    .setOldController(imageView.controller)
                    .setTapToRetryEnabled(true)
                    .build()
                imageView.controller = controller
            } ?: run { imageView.setImageURI(Uri.parse(uri)) }
        } else {
            throw UnsupportedOperationException("Only Fresco is available")
        }
    }
}
