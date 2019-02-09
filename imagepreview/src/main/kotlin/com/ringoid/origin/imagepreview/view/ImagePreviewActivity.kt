package com.ringoid.origin.imagepreview.view

import androidx.fragment.app.Fragment
import com.ringoid.base.BuildConfig
import com.ringoid.base.deeplink.AppNav
import com.ringoid.origin.navigation.*
import com.ringoid.origin.view.base.BaseHostActivity

@AppNav("imagepreview")
class ImagePreviewActivity : BaseHostActivity(), IImagePreviewActivity {

    override fun getFragmentTag(): String = ImagePreviewFragment.TAG
    override fun instantiateFragment(): Fragment =
        ImagePreviewFragment.newInstance(
            uri = intent.getParcelableExtra(CONTENT_URI),
            navigateFrom = intent.getStringExtra(Extras.EXTRA_NAVIGATE_FROM))

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onBackPressed() {
        if (BuildConfig.BACK_TO_GALLERY_FROM_IMAGE_PREVIEW) {
            (supportFragmentManager.findFragmentByTag(ImagePreviewFragment.TAG) as? ImagePreviewFragment)
                ?.onNavigateBack()
        } else {
            onClose()
        }
    }

    // --------------------------------------------------------------------------------------------
    override fun onClose(withImageAdded: Boolean) {
        intent.getStringExtra(Extras.EXTRA_NAVIGATE_FROM)
              ?.takeIf { it == NavigateFrom.SCREEN_LOGIN }
              ?.let {
                  val payload = if (withImageAdded) "&tabPayload=${Payload.PAYLOAD_PROFILE_LOGIN_IMAGE_ADDED}"
                                else "&tabPayload=${Payload.PAYLOAD_PROFILE_LOGIN}"
                  navigate(this, path = "/main?tab=${NavigateFrom.MAIN_TAB_PROFILE}$payload")
              }
        super.onBackPressed()  // close ImagePreview screen
    }
}
