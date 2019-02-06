package com.ringoid.origin.imagepreview.view

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.GlideApp
import com.ringoid.origin.imagepreview.OriginR_string
import com.ringoid.origin.imagepreview.R
import com.ringoid.origin.navigation.ExternalNavigator
import com.ringoid.origin.navigation.NavigateFrom
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.communicator
import com.ringoid.utility.randomString
import com.ringoid.utility.snackbar
import com.steelkiwi.cropiwa.OnImageLoadListener
import com.steelkiwi.cropiwa.config.CropIwaSaveConfig
import kotlinx.android.synthetic.main.fragment_image_preview.*
import timber.log.Timber
import java.io.File

class ImagePreviewFragment : BaseFragment<ImagePreviewViewModel>(), OnImageLoadListener {

    companion object {
        internal const val TAG = "ImagePreviewFragment_tag"

        private const val BUNDLE_KEY_IMAGE_URI = "bundle_key_image_uri"
        private const val BUNDLE_KEY_NAVIGATE_FROM = "bundle_key_navigate_from"

        fun newInstance(uri: Uri?, navigateFrom: String? = null): ImagePreviewFragment =
            ImagePreviewFragment().apply {
                arguments = Bundle().apply {
                    Timber.v("ImagePreview: uri=$uri, navigateFrom=$navigateFrom")
                    putParcelable(BUNDLE_KEY_IMAGE_URI, uri)
                    putString(BUNDLE_KEY_NAVIGATE_FROM, navigateFrom)
                }
            }
    }

    private var uri: Uri? = null

    override fun getVmClass(): Class<ImagePreviewViewModel> = ImagePreviewViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_image_preview

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uri = arguments?.getParcelable<Uri>(BUNDLE_KEY_IMAGE_URI)?.also {
            activity?.contentResolver?.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ExternalNavigator.RC_GALLERY_GET_IMAGE -> {
                when (resultCode) {
                    Activity.RESULT_CANCELED -> onClose()
                    Activity.RESULT_OK -> uri = data?.data?.also { setImageUri(it) }
                }
            }
        }
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (toolbar as Toolbar).apply {
            inflateMenu(com.ringoid.origin.R.menu.menu_done)
            setBackgroundColor(ContextCompat.getColor(context, com.ringoid.origin.R.color.semitransparent_soft))
            setOnMenuItemClickListener {
                when (it.itemId) {
                    com.ringoid.origin.R.id.menu_done -> { cropImage() ; true }
                    else -> false
                }
            }
            setNavigationOnClickListener { onNavigateBack() }
        }

        crop_view.setOnImageLoadListener(this)

        uri?.let { setImageUri(it) }
           ?: run {
               Timber.w("No image uri supplied on ImagePreview screen.")
               arguments?.getString(BUNDLE_KEY_NAVIGATE_FROM)
                             ?.takeIf { it == NavigateFrom.SCREEN_LOGIN }
                             ?.let { vm.onInvalidImageUriAfterLogin() }
           }
    }

    override fun onDestroyView() {
        crop_view.apply {
            setCropSaveCompleteListener(null)
            setOnImageLoadListener(null)
        }
        super.onDestroyView()
    }

    // --------------------------------------------------------------------------------------------
    private fun cropImage() {
        context?.let {
            val context = it
            val destinationFile = File(it.filesDir, "${randomString()}.jpg")
            crop_view.let {
                val imageConfig = CropIwaSaveConfig.Builder(Uri.fromFile(destinationFile))
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .setQuality(100) // hint for lossy compression formats
                    .build()
                it.setCropSaveCompleteListener { GlideApp.with(context).load(destinationFile).preload() }
                it.crop(imageConfig)
            }
        }

        activity?.setResult(Activity.RESULT_OK)  // close with result
        onClose(true)  // close this ImagePreview screen immediately, doing cropping in background
    }

    private fun setImageUri(uri: Uri) {
        crop_view.apply {
            clear()
            pb_image_preview.changeVisibility(isVisible = true)
            setImageUri(uri)
        }
    }

    // ------------------------------------------
    override fun onSuccess(uri: Uri) {
        pb_image_preview.changeVisibility(isVisible = false)
    }

    override fun onFailure(e: Throwable) {
        pb_image_preview.changeVisibility(isVisible = false)
        snackbar(view, OriginR_string.error_crop_image)
        onClose()  // failed to load image to crop - close without retry
    }

    // --------------------------------------------------------------------------------------------
    private fun onClose(withImageAdded: Boolean = false) {
        communicator(IImagePreviewActivity::class.java)?.onClose(withImageAdded)
    }

    internal fun onNavigateBack() {
        vm.onNavigateBack()
    }
}
