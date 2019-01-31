package com.ringoid.origin.view.common

import android.content.Context
import android.net.Uri
import com.ringoid.base.IImagePreviewReceiver
import com.steelkiwi.cropiwa.image.CropIwaResultReceiver
import timber.log.Timber

class ImagePreviewReceiver(private val receiver: CropIwaResultReceiver) : IImagePreviewReceiver {

    private var isRegistered: Boolean = false
    private var lastCropError: Throwable? = null
    private var lastCropResult: Uri? = null

    override fun register(context: Context?) {
        if (context == null) {
            return
        }

        clear()  // forget any previous result in order not to receive it again
        receiver.apply {
            setListener(object : CropIwaResultReceiver.Listener {
                override fun onCropFailed(e: Throwable) {
                    Timber.e(e, "Image crop has failed")
                    lastCropError = e
                }

                override fun onCropSuccess(croppedUri: Uri) {
                    Timber.v("Image cropping has succeeded, uri: $croppedUri")
                    lastCropResult = croppedUri
                }
            })
            register(context)
            isRegistered = true
        }
    }

    override fun unregister(context: Context?) {
        if (context == null) {
            return
        }

        if (isRegistered) {
            try {
                receiver.unregister(context)
            } catch (e: IllegalArgumentException) {
                // it's a shame that Android FW doesn't provide receiver.isRegistered() method.
                Timber.w(e)
            } finally {
                isRegistered = false
            }
        }
    }

    override fun clear() {
        lastCropError = null
        lastCropResult = null
    }
    override fun hasResult(): Boolean = lastCropError != null || lastCropResult != null
    override fun getLastError(): Throwable? = lastCropError
    override fun getLastResult(): Uri? = lastCropResult
}
