package com.ringoid.origin.feed.view

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.model.actions.BlockActionObject
import com.ringoid.domain.model.actions.LikeActionObject
import com.ringoid.domain.model.actions.UnlikeActionObject
import com.ringoid.domain.model.feed.Feed

abstract class FeedViewModel(app: Application) : BaseViewModel(app) {

    val feed by lazy { MutableLiveData<Feed>() }

    abstract fun getFeed()
    abstract fun getFeedName(): String

    fun onLike(profileId: String, imageId: String, isLiked: Boolean) {
        if (isLiked) LikeActionObject(sourceFeed = getFeedName(), targetImageId = imageId, targetUserId = profileId)
        else UnlikeActionObject(sourceFeed = getFeedName(), targetImageId = imageId, targetUserId = profileId)
        .also { actionObjectPool.put(it) }
    }

    fun onBlock(profileId: String, imageId: String) {
        onReport(profileId = profileId, imageId = imageId, reasonNumber = 0)
    }

    fun onReport(profileId: String, imageId: String, reasonNumber: Int) {
        BlockActionObject(
            numberOfBlockReason = reasonNumber, sourceFeed = getFeedName(),
            targetImageId = imageId, targetUserId = profileId)
            .also { actionObjectPool.put(it) }
    }
}
