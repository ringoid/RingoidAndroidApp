package com.ringoid.origin.feed.view

import android.app.Application
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.model.actions.BlockActionObject
import com.ringoid.domain.model.actions.LikeActionObject
import com.ringoid.domain.model.actions.UnlikeActionObject
import com.ringoid.origin.view.common.visibility_tracker.VisibilityState

abstract class FeedViewModel(app: Application) : BaseViewModel(app) {

    abstract fun getFeedName(): String

    fun onAddImage() {
        navigation.value = FeedFragment.InternalNavigator::openProfileScreen
    }

    fun onLike(profileId: String, imageId: String, isLiked: Boolean) {
        val aobj = if (isLiked) LikeActionObject(sourceFeed = getFeedName(), targetImageId = imageId, targetUserId = profileId)
                   else UnlikeActionObject(sourceFeed = getFeedName(), targetImageId = imageId, targetUserId = profileId)
        actionObjectPool.put(aobj)
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

    fun onView(state: VisibilityState) {
        // TODO: post VIEW aobj properly
    }
}
