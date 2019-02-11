package com.ringoid.origin.feed.view.lmm.like

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.view.ViewState
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.origin.feed.view.lmm.base.BaseLmmFeedViewModel
import io.reactivex.Observable
import javax.inject.Inject

class LikesFeedViewModel @Inject constructor(
    getLmmUseCase: GetLmmUseCase, clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase, app: Application)
    : BaseLmmFeedViewModel(getLmmUseCase, clearCachedAlreadySeenProfileIdsUseCase, cacheBlockedProfileIdUseCase, countUserImagesUseCase, app) {

    val badgeLikes by lazy { MutableLiveData<Boolean>() }

    private val numberOfLikes = mutableMapOf<String, MutableSet<String>>()

    override fun doOnSuccess(lmm: Lmm) {
        super.doOnSuccess(lmm)
        badgeLikes.value = lmm.newLikesCount() > 0
    }

    override fun isLmmEmpty(lmm: Lmm): Boolean = lmm.isLikesEmpty()

    override fun sourceFeed(): Observable<List<FeedItem>> = getLmmUseCase.repository.feedLikes

    override fun getFeedName(): String = "who_liked_me"

    fun onLike(profileId: String, imageId: String, isLiked: Boolean, feedItemPosition: Int) {
        if (!numberOfLikes.containsKey(profileId)) {
            numberOfLikes[profileId] = mutableSetOf()
        }
        numberOfLikes[profileId]?.let {
            if (isLiked) it.add(imageId) else it.remove(imageId)
            viewState.value = if (it.isEmpty()) ViewState.DONE(NO_LIKES_ON_PROFILE(feedItemPosition))
                              else ViewState.DONE(HAS_LIKES_ON_PROFILE(feedItemPosition))
        }
        onLike(profileId, imageId, isLiked)
    }

    override fun onRefresh() {
        badgeLikes.value = false  // discard badge on refresh - it will be set properly after refresh
        super.onRefresh()
    }
}
