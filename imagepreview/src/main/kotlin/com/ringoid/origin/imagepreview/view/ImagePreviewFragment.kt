package com.ringoid.origin.imagepreview.view

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.imagepreview.R
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.randomString
import com.steelkiwi.cropiwa.config.CropIwaSaveConfig
import kotlinx.android.synthetic.main.fragment_image_preview.*
import java.io.File

class ImagePreviewFragment : BaseFragment<ImagePreviewViewModel>() {

    companion object {
        const val TAG = "ImagePreviewFragment_tag"

        private const val BUNDLE_KEY_IMAGE_URI = "bundle_key_image_uri"

        fun newInstance(uri: String): ImagePreviewFragment =
            ImagePreviewFragment().apply {
                arguments = Bundle().apply { putString(BUNDLE_KEY_IMAGE_URI, uri) }
            }
    }

    protected var uri: String? = null

    override fun getVmClass(): Class<ImagePreviewViewModel> = ImagePreviewViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_image_preview

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uri = arguments?.getString(BUNDLE_KEY_IMAGE_URI)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_done.clicks().compose(clickDebounce()).subscribe { cropImage() }

        uri?.let { crop_view.setImageUri(Uri.parse(it)) }
    }

    // --------------------------------------------------------------------------------------------
    private fun cropImage() {
        context?.let {
            val destinationFile = File(it.filesDir, "${randomString()}.png")
            crop_view.crop(
                CropIwaSaveConfig.Builder(Uri.fromFile(destinationFile))
                    .setCompressFormat(Bitmap.CompressFormat.PNG)
                    .setQuality(100) // hint for lossy compression formats
                    .build()
            )
        }
        // TODO: send broadcast on image cropped - and set that image on profile screen
        // TODO: close this ImagePreview screen immediately, doing cropping in background
    }
}
