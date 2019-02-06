package com.ringoid.origin.imagepreview.view

import android.app.Application
import androidx.fragment.app.Fragment
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.origin.navigation.ExternalNavigator
import com.ringoid.origin.navigation.NavigateFrom
import com.ringoid.origin.navigation.navigateAndClose
import javax.inject.Inject

class ImagePreviewViewModel @Inject constructor(app: Application) : BaseViewModel(app) {

    object InternalNavigator {
        fun openProfileScreen(fragment: Fragment) {
            navigateAndClose(fragment, path = "/main?tab=${NavigateFrom.MAIN_TAB_PROFILE}")
        }
    }

    // --------------------------------------------------------------------------------------------
    fun onNavigateBack() {
        navigation.value = ExternalNavigator::openGalleryToGetImageFragment
    }

    fun onInvalidImageUriAfterLogin() {
        navigation.value = InternalNavigator::openProfileScreen
    }
}
