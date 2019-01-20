package com.ringoid.origin.feed.view.common

import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.origin.feed.view.NO_IMAGES_IN_PROFILE
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber

class FeedControllerDelegate<T>(
    private val controller: T, private val countUserImagesUseCase: CountUserImagesUseCase)
    where T : BaseViewModel, T : IFeedController {

    fun clearScreen(mode: Int) {
        controller.viewState.value = ViewState.CLEAR(mode)
    }

    fun onRefresh() {
        countUserImagesUseCase.source()
            .map { it > 0 }  // user has images in profile
            .autoDisposable(controller)
            .subscribe({
                if (it) {
                    controller.actionObjectPool.trigger()
                    clearScreen(mode = ViewState.CLEAR.MODE_DEFAULT)
                    controller.getFeed()
                } else {
                    controller.viewState.value = ViewState.DONE(NO_IMAGES_IN_PROFILE)
                }
            }, Timber::e)
    }
}
