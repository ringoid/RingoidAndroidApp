package com.ringoid.imageloader

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequest
import com.ringoid.utility.delay
import com.ringoid.utility.isNotFoundNetworkError
import timber.log.Timber
import java.io.FileNotFoundException
import java.lang.ref.WeakReference

object ImageLoader {

    const val RETRY_COUNT = 5

    private var notFoundDrawable: Drawable? = null

    /**
     * @see https://proandroiddev.com/progressive-image-loading-with-rxjava-64bd2b973690
     */
    fun load(uri: String?, thumbnailUri: String? = null, iv: ImageView): ImageLoadRequestStatus {
        if (uri.isNullOrBlank()) {
            return ImageLoadRequestStatus.NoImageUri  // no image data to load
        }
        val imageViewRef = WeakReference(iv)
        return imageViewRef.get()
            ?.also { initResources(it.context) }
            ?.let { it as? SimpleDraweeView }
            ?.let {
                it.tag = 0  // depth of retry recursion
                it.hierarchy.setProgressBarImage(CircularImageProgressBarDrawable())
                it.controller = createRecursiveImageController(uri, thumbnailUri, imageViewRef).build()
                ImageLoadRequestStatus.Ok
            }
            ?: run {
                Timber.e("ImageLoader: Either ImageView is not Fresco DraweeView or it's GC'ed (ref is null)")
                ImageLoadRequestStatus.InvalidImageViewRef
            }
    }

    // ------------------------------------------
    private fun initResources(context: Context) {
        if (notFoundDrawable == null) {
            notFoundDrawable = ContextCompat.getDrawable(context, UtilityR_drawable.ic_not_found_photo_placeholder_grey_96dp)
        }
    }

    // --------------------------------------------------------------------------------------------
    private fun createRecursiveImageController(uri: String, thumbnailUri: String?, imageViewRef: WeakReference<ImageView>)
            : PipelineDraweeControllerBuilder =
        imageViewRef.get()?.let { it as? SimpleDraweeView }?.let { imageView ->
            createFlatImageController(uri, thumbnailUri)
                .setOldController(imageView.controller)
                .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                    // TODO: remove logs and debug drawable on PROD
                    override fun onFailure(id: String, throwable: Throwable) {
                        super.onFailure(id, throwable)
                        val depth = imageView.tag as Int
                        imageView.loge(throwable, "ImageLoader: Failed to load image [$uri], retry ${depth + 1} / $RETRY_COUNT")
                        imageView.hierarchy.setFailureImage(DebugImageLoadDrawable(cause = throwable))  // TODO: remove for prod
                        if (throwable is FileNotFoundException || throwable.isNotFoundNetworkError()) {
//                            imageView.hierarchy.setFailureImage(notFoundDrawable)  // TODO: uncomment for prod
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
                            imageView.logv("ImageLoader: Retry load image: [${depth + 1} / $RETRY_COUNT]")
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
