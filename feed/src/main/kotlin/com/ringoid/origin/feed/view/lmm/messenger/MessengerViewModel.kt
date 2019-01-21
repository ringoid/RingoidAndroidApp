package com.ringoid.origin.feed.view.lmm.messenger

import android.app.Application
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.origin.feed.view.FeedViewModel
import com.ringoid.origin.feed.view.common.FeedControllerDelegate
import com.ringoid.origin.feed.view.common.IFeedController
import javax.inject.Inject

class MessengerViewModel @Inject constructor(
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase, app: Application)
    : FeedViewModel(cacheBlockedProfileIdUseCase, app), IFeedController {

    private val delegate = FeedControllerDelegate(this, countUserImagesUseCase)

    override fun getFeed() {
        // TODO: get lmm messages
    }

    override fun getFeedName(): String = "messages"

    // ------------------------------------------
    override fun clearScreen(mode: Int) {
        delegate.clearScreen(mode)
    }

    override fun onRefresh() {
        delegate.onRefresh()
    }
}
