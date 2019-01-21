package com.ringoid.origin.feed.view.lmm.like

import android.app.Application
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.origin.feed.view.FeedViewModel
import javax.inject.Inject

class LikesFeedViewModel @Inject constructor(
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    app: Application) : FeedViewModel(cacheBlockedProfileIdUseCase, app) {

    override fun getFeedName(): String = "who_liked_me"
}
