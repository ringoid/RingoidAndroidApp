package com.ringoid.origin.feed.view.lmm.messenger

import android.app.Application
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.view.lmm.base.BaseLmmFeedViewModel
import io.reactivex.Observable
import javax.inject.Inject

class MessengerViewModel @Inject constructor(
    getLmmUseCase: GetLmmUseCase, cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase, app: Application)
    : BaseLmmFeedViewModel(getLmmUseCase, cacheBlockedProfileIdUseCase, countUserImagesUseCase, app) {

    override fun sourceFeed(): Observable<List<FeedItem>> = getLmmUseCase.repository.feedMessages

    override fun getFeedName(): String = "messages"
}
