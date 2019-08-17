package com.ringoid.origin.imagepreview.view

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.ringoid.base.navigation.AppScreen
import com.ringoid.base.view.BaseFragment
import com.ringoid.domain.log.SentryUtil
import com.ringoid.origin.imagepreview.OriginR_id
import com.ringoid.origin.imagepreview.OriginR_menu
import com.ringoid.origin.imagepreview.OriginR_string
import com.ringoid.origin.imagepreview.R
import com.ringoid.origin.navigation.ExternalNavigator
import com.ringoid.origin.navigation.NavigateFrom
import com.ringoid.origin.navigation.navigateAndClose
import com.ringoid.origin.navigation.noConnection
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.communicator
import com.ringoid.utility.randomString
import com.ringoid.utility.toast
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
    private var isLoading: Boolean = false

    override fun getVmClass(): Class<ImagePreviewViewModel> = ImagePreviewViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_image_preview

    override fun appScreen(): AppScreen = AppScreen.IMAGE_PREVIEW

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uri = arguments?.getParcelable<Uri>(BUNDLE_KEY_IMAGE_URI)?.also {
            with(activity!!) {
                // @see: https://stackoverflow.com/questions/37993762/java-lang-securityexception-on-takepersistableuripermission-saf
                try {
                    try {
                        grantUriPermission(packageName, it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                    } catch (e: IllegalArgumentException) {
                        grantUriPermission(packageName, it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: SecurityException) {
                    SentryUtil.capture(e, "No persistable permission grants found")
                    uri = null  // preventing from further getting content by input uri
                }
            }
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
            inflateMenu(OriginR_menu.menu_done)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    OriginR_id.menu_done -> { cropImage() ; true }
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
                         ?.let { navigateAndClose(this, path = "/main?tab=${NavigateFrom.MAIN_TAB_PROFILE}") }
                         ?: run { onFailure(NullPointerException("Uri is null")) }
           }
    }

    override fun onDestroyView() {
        crop_view.apply {
            setCropSaveCompleteListener(null)
            setOnImageLoadListener(null)
        }
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        uri?.let { activity?.revokeUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    }

    // --------------------------------------------------------------------------------------------
    private fun cropImage() {
        if (isLoading) {
            return
        }
        if (!connectionManager.isNetworkAvailable()) {
            noConnection(this)
            return
        }

        context?.let {
            val destinationFile = File(it.filesDir, "${randomString()}.jpg")
            crop_view.let { view ->
                val imageConfig = CropIwaSaveConfig.Builder(Uri.fromFile(destinationFile))
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .setQuality(100) // hint for lossy compression formats
                    .build()
                view.crop(imageConfig)
            }
        }

        activity?.setResult(Activity.RESULT_OK)  // close with result
        onClose(true)  // close this ImagePreview screen immediately, doing cropping in background
    }

    private fun setImageUri(uri: Uri) {
        crop_view.apply {
            isLoading = true
            clear()
            pb_image_preview.changeVisibility(isVisible = true)
            setImageUri(uri)
        }
    }

    // ------------------------------------------
    override fun onSuccess(uri: Uri) {
        isLoading = false
        pb_image_preview.changeVisibility(isVisible = false)
    }

    override fun onFailure(e: Throwable) {
        isLoading = false
        pb_image_preview.changeVisibility(isVisible = false)
        context?.toast(OriginR_string.error_crop_image)
        onClose()  // failed to load image to crop - close without retry
    }

    // --------------------------------------------------------------------------------------------
    private fun onClose(withImageAdded: Boolean = false) {
        communicator(IImagePreviewActivity::class.java)?.onClose(withImageAdded)
    }

    internal fun onNavigateBack() {
        ExternalNavigator.openGalleryToGetImageFragment(this)
    }
}
