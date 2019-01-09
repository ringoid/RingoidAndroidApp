package com.ringoid.origin.view.feed

import android.app.Application
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.interactor.feed.GetNewFacesUseCase

open class FeedViewModel(private val getNewFacesUseCase: GetNewFacesUseCase, app: Application)
    : BaseViewModel(app) {

    fun getNewFaces() {
        // TODO
    }
}
