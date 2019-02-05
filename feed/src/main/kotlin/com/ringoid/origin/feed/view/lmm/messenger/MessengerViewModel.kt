package com.ringoid.origin.feed.view.lmm.messenger

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.origin.feed.view.lmm.base.BaseLmmFeedViewModel
import io.reactivex.Observable
import javax.inject.Inject

class MessengerViewModel @Inject constructor(
    getLmmUseCase: GetLmmUseCase, clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase, app: Application)
    : BaseLmmFeedViewModel(getLmmUseCase, clearCachedAlreadySeenProfileIdsUseCase, cacheBlockedProfileIdUseCase, countUserImagesUseCase, app) {

    val badgeMessenger by lazy { MutableLiveData<Boolean>() }

    override fun doOnSuccess(lmm: Lmm) {
        super.doOnSuccess(lmm)
        badgeMessenger.value = lmm.hasNewMessages
    }

    override fun isLmmEmpty(lmm: Lmm): Boolean = lmm.isMessagesEmpty()

    override fun sourceFeed(): Observable<List<FeedItem>> = getLmmUseCase.repository.feedMessages

    override fun getFeedName(): String = "messages"

    override fun onRefresh() {
        badgeMessenger.value = false  // discard badge on refresh - it will be set properly after refresh
        super.onRefresh()
    }
}
