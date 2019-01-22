package com.ringoid.origin.imagepreview.view

import androidx.fragment.app.Fragment
import com.ringoid.base.BuildConfig
import com.ringoid.base.deeplink.AppNav
import com.ringoid.origin.navigation.CONTENT_URI
import com.ringoid.origin.navigation.Extras
import com.ringoid.origin.navigation.NavigateFrom
import com.ringoid.origin.navigation.navigate
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
            super.onBackPressed()
        }
    }

    // --------------------------------------------------------------------------------------------
    override fun onClose() {
        intent.getStringExtra(Extras.EXTRA_NAVIGATE_FROM)
            ?.takeIf { it == NavigateFrom.SCREEN_LOGIN }
            ?.let { navigate(this, path = "/main?tab=${NavigateFrom.MAIN_TAB_PROFILE}") }
        super.onBackPressed()  // close ImagePreview screen
    }
}
