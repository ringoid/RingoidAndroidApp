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
import com.ringoid.utility.isNotFoundNetworkError
import com.ringoid.utility.loge
import com.ringoid.utility.logv
import timber.log.Timber
import java.lang.ref.WeakReference

object ImageLoader {

    const val RETRY_COUNT = 5

    /**
     * @see https://proandroiddev.com/progressive-image-loading-with-rxjava-64bd2b973690
     */
    fun load(uri: String?, thumbnailUri: String? = null, iv: ImageView) {
        if (uri.isNullOrBlank()) {
            return  // no image data to load
        }
        val imageViewRef = WeakReference(iv)
        imageViewRef.get()
            ?.let { it as? SimpleDraweeView }
            ?.let {
                it.tag = 0  // depth of retry recursion
                it.hierarchy.setProgressBarImage(CircularImageProgressBarDrawable())
                it.controller = createRecursiveImageController(uri, thumbnailUri, imageViewRef).build()
            } ?: run { Timber.e("ImageLoader: Either ImageView is not Fresco DraweeView or it's GC'ed (ref is null)") }
    }

    // --------------------------------------------------------------------------------------------
    private fun createRecursiveImageController(uri: String, thumbnailUri: String?, imageViewRef: WeakReference<ImageView>)
            : PipelineDraweeControllerBuilder =
        imageViewRef.get()?.let { it as? SimpleDraweeView }?.let { imageView ->
            createFlatImageController(uri, thumbnailUri)
                .setOldController(imageView.controller)
                .setControllerListener(object : BaseControllerListener<ImageInfo>() {
//                    override fun onFinalImageSet(id: String, imageInfo: ImageInfo?, animatable: Animatable?) {
//                        super.onFinalImageSet(id, imageInfo, animatable)
//                        Timber.v("Image has loaded: $id [${imageInfo?.width}, ${imageInfo?.height}]")
//                    }

                    override fun onFailure(id: String, throwable: Throwable) {
                        super.onFailure(id, throwable)
                        val depth = imageView.tag as Int
                        imageView.loge(throwable, "ImageLoader: Failed to load image [$uri], retry ${depth + 1} / $RETRY_COUNT")
                        if (throwable.isNotFoundNetworkError()) {
                            return  // resource at uri not found, don't retry
                        }

                        if (depth >= RETRY_COUNT) {
                            imageView.logv("ImageLoader: All retries have exhausted, fallback to manual retry")
                            val controller = createFlatImageController(uri, thumbnailUri)
                                .setOldController(imageView.controller)
                                .setTapToRetryEnabled(true)  // enable manual retry on tap
                                .build()
                            imageView.let { it.post { it.controller = controller } }
                        } else {
                            imageView.logv("ImageLoader: Retry load image: [$depth / $RETRY_COUNT]")
                            imageView.tag = depth + 1
                            delay(2000L) {
                                val controller = createRecursiveImageController(uri, thumbnailUri, imageViewRef)
                                    .setOldController(imageView.controller)
                                    .build()
                                imageView.controller = controller
                            }
                        }
                    }
                })
        } ?: createFlatImageController(uri, thumbnailUri)

    private fun createFlatImageController(uri: String, thumbnailUri: String?)
            : PipelineDraweeControllerBuilder =
        Fresco.newDraweeControllerBuilder()
            .setLowResImageRequest(ImageRequest.fromUri(thumbnailUri))
            .setImageRequest(ImageRequest.fromUri(Uri.parse(uri)))
            .setRetainImageOnFailure(true)
}
