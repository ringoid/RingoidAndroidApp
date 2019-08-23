package com.ringoid.utility.image

import android.net.Uri
import android.widget.ImageView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequest
import com.ringoid.utility.delay
import timber.log.Timber
import java.lang.ref.WeakReference

object ImageLoader {

    const val RETRY_COUNT = 5

    /**
     * @see https://proandroiddev.com/progressive-image-loading-with-rxjava-64bd2b973690
     */
    fun load(uri: String?, thumbnailUri: String? = null, iv: ImageView) {
        if (uri.isNullOrBlank()) {
            return
        }
        val imageViewRef = WeakReference(iv)
        imageViewRef.get()?.let { imageView ->
            if (imageView is SimpleDraweeView) {
                thumbnailUri?.let { thumbUri ->
                    imageView.tag = 0  // depth of retry recursion
                    imageView.controller = createRecursiveImageController(uri, thumbUri, imageViewRef).build()
                } ?: run { imageView.setImageURI(Uri.parse(uri)) }
            } else {
                throw UnsupportedOperationException("Only Fresco is available")
            }
        } ?: run { Timber.e("Reference to ImageView is null") }
    }

    // --------------------------------------------------------------------------------------------
    private fun createRecursiveImageController(uri: String, thumbnailUri: String, imageViewRef: WeakReference<ImageView>)
            : PipelineDraweeControllerBuilder =
        imageViewRef.get()?.let { it as? SimpleDraweeView }?.let { imageView ->
            createFlatImageController(uri, thumbnailUri)
                .setOldController(imageView.controller)
                .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                    override fun onFailure(id: String, throwable: Throwable) {
                        super.onFailure(id, throwable)
                        val depth = imageView.tag as Int
                        Timber.e(throwable, "Failed to load image (depth=$depth), retrying [$id]...")

                        if (depth > RETRY_COUNT) {
                            Timber.v("All retries have exhausted, fallback to manual retry")
                            val controller = createFlatImageController(uri, thumbnailUri)
                                .setOldController(imageView.controller)
                                .setTapToRetryEnabled(true)  // enable manual retry on tap
                                .build()
                            imageView.let { it.post { it.controller = controller } }
                        } else {
                            imageView.tag = depth + 1
                            delay(2000L) {
                                Timber.v("Retry load image: [$depth / $RETRY_COUNT]")
                                val controller = createRecursiveImageController(uri, thumbnailUri, imageViewRef)
                                    .setOldController(imageView.controller)
                                    .build()
                                imageView.controller = controller
                            }
                        }
                    }
                })
        } ?: createFlatImageController(uri, thumbnailUri)

    private fun createFlatImageController(uri: String, thumbnailUri: String)
            : PipelineDraweeControllerBuilder =
        Fresco.newDraweeControllerBuilder()
            .setLowResImageRequest(ImageRequest.fromUri(thumbnailUri))
            .setImageRequest(ImageRequest.fromUri(Uri.parse(uri)))
            .setRetainImageOnFailure(true)
}
