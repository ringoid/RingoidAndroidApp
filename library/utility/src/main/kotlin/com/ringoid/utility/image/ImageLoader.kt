package com.ringoid.utility.image

import android.net.Uri
import android.widget.ImageView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequest
import timber.log.Timber

object ImageLoader {

    const val RETRY_COUNT = 5

    /**
     * @see https://proandroiddev.com/progressive-image-loading-with-rxjava-64bd2b973690
     */
    fun load(uri: String?, thumbnailUri: String? = null, imageView: ImageView) {
        if (uri.isNullOrBlank()) {
            return
        }

        if (imageView is SimpleDraweeView) {
            thumbnailUri?.let { thumbUri ->
                imageView.tag = 0  // depth of retry recursion
                imageView.controller = createRecursiveImageController(uri, thumbUri, imageView).build()
            } ?: run { imageView.setImageURI(Uri.parse(uri)) }
        } else {
            throw UnsupportedOperationException("Only Fresco is available")
        }
    }

    // --------------------------------------------------------------------------------------------
    private fun createRecursiveImageController(uri: String, thumbnailUri: String, imageView: SimpleDraweeView)
            : PipelineDraweeControllerBuilder =
        createFlatImageController(uri, thumbnailUri)
              .setOldController(imageView.controller)
              .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                  override fun onFailure(id: String, throwable: Throwable) {
                      super.onFailure(id, throwable)
                      val depth = imageView.tag as Int
                      Timber.e(throwable, "Failed to load image (depth=$depth), retrying [$id]...")

                      val controller = if (depth > RETRY_COUNT) {
                          createFlatImageController(uri, thumbnailUri)
                              .setOldController(imageView.controller)
                              .setTapToRetryEnabled(true)  // enable manual retry on tap
                              .build()
                      } else {
                          imageView.tag = depth + 1
                          createRecursiveImageController(uri, thumbnailUri, imageView)
                              .setOldController(imageView.controller)
                              .build()
                      }
                      imageView.controller = controller
                  }
              })

    private fun createFlatImageController(uri: String, thumbnailUri: String)
            : PipelineDraweeControllerBuilder =
        Fresco.newDraweeControllerBuilder()
            .setLowResImageRequest(ImageRequest.fromUri(thumbnailUri))
            .setImageRequest(ImageRequest.fromUri(Uri.parse(uri)))
            .setRetainImageOnFailure(true)
}
