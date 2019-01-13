package com.ringoid.origin.imagepreview.view

import android.app.Application
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.origin.navigation.ExternalNavigator
import javax.inject.Inject

class ImagePreviewViewModel @Inject constructor(app: Application) : BaseViewModel(app) {

    fun onNavigateBack() {
        navigation.value = ExternalNavigator::openGalleryToGetImage
    }
}
