package com.ringoid.origin.view.common

import android.content.Context
import android.net.Uri
import com.ringoid.base.IImagePreviewReceiver
import com.ringoid.report.log.Report
import com.steelkiwi.cropiwa.image.CropIwaResultReceiver
import com.steelkiwi.cropiwa.util.CropException
import timber.log.Timber

class ImagePreviewReceiver(private val applicationContext: Context, private val receiver: CropIwaResultReceiver)
    : IImagePreviewReceiver {

    private var isRegistered: Boolean = false
    private var lastCropError: Throwable? = null
    private var lastCropResult: Uri? = null

    private var onError: ((e: Throwable) -> Unit)? = null
    private var onSuccess: ((uri: Uri) -> Unit)? = null

    // ------------------------------------------
    override fun register() {
        Timber.v("Register global image preview receiver")
        clear()  // forget any previous result in order not to receive it again
        receiver.apply {
            setListener(object : CropIwaResultReceiver.Listener {
                override fun onCropFailed(e: Throwable) {
                    val extras = if (e is CropException) listOf("crop log" to e.breadcrumbs) else emptyList()
                    Report.capture(e, "Image crop has failed", extras = extras)
                    lastCropError = e
                    onError?.invoke(e)
                }

                override fun onCropSuccess(croppedUri: Uri) {
                    Timber.v("Image cropping has succeeded, uri: $croppedUri")
                    lastCropResult = croppedUri
                    onSuccess?.invoke(croppedUri)
                }
            })
            register(applicationContext)
            isRegistered = true
        }
    }

    override fun unregister() {
        Timber.v("Unregister global image preview receiver")
        if (isRegistered) {
            try {
                receiver.unregister(applicationContext)
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

    // ------------------------------------------
    override fun doOnError(l: (e: Throwable) -> Unit): IImagePreviewReceiver {
        onError = l
        return this
    }

    override fun doOnSuccess(l: (uri: Uri) -> Unit): IImagePreviewReceiver {
        onSuccess = l
        return this
    }

    override fun dispose() {
        onError = null
        onSuccess = null
    }

    override fun subscribe() {
        lastCropError
            ?.let { callOnError(it) }
            ?: run { lastCropResult?.let { callOnSuccess(it) } }
    }

    // --------------------------------------------------------------------------------------------
    private fun callOnError(lastCropError: Throwable) {
        onError?.invoke(lastCropError)
        clear()  // value must not be reusable to emulate cold observable source
    }

    private fun callOnSuccess(lastCropResult: Uri) {
        onSuccess?.invoke(lastCropResult)
        clear()  // value must not be reusable to emulate cold observable source
    }
}
