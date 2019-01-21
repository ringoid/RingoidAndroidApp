package com.ringoid.origin.feed.view.lmm.like

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.origin.feed.view.lmm.base.BaseLmmFeedViewModel
import io.reactivex.Observable
import javax.inject.Inject

class LikesFeedViewModel @Inject constructor(
    getLmmUseCase: GetLmmUseCase, cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase, app: Application)
    : BaseLmmFeedViewModel(getLmmUseCase, cacheBlockedProfileIdUseCase, countUserImagesUseCase, app) {

    val badgeLikes by lazy { MutableLiveData<Boolean>() }

    override fun doOnSuccess(lmm: Lmm) {
        super.doOnSuccess(lmm)
        badgeLikes.value = lmm.newLikesCount() > 0
    }

    override fun isLmmEmpty(lmm: Lmm): Boolean = lmm.isLikesEmpty()

    override fun sourceFeed(): Observable<List<FeedItem>> = getLmmUseCase.repository.feedLikes

    override fun getFeedName(): String = "who_liked_me"
}
