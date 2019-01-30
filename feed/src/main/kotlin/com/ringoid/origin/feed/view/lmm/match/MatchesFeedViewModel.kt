package com.ringoid.origin.feed.view.lmm.match

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

class MatchesFeedViewModel @Inject constructor(
    getLmmUseCase: GetLmmUseCase, clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase, app: Application)
    : BaseLmmFeedViewModel(getLmmUseCase, clearCachedAlreadySeenProfileIdsUseCase, cacheBlockedProfileIdUseCase, countUserImagesUseCase, app) {

    val badgeMatches by lazy { MutableLiveData<Boolean>() }

    override fun doOnSuccess(lmm: Lmm) {
        super.doOnSuccess(lmm)
        badgeMatches.value = lmm.newLikesCount() > 0
    }

    override fun isLmmEmpty(lmm: Lmm): Boolean = lmm.isMatchesEmpty()

    override fun sourceFeed(): Observable<List<FeedItem>> = getLmmUseCase.repository.feedMatches

    override fun getFeedName(): String = "matches"
}
