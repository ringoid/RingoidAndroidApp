package com.ringoid.origin.feed.view

import android.app.Application
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.model.actions.BlockActionObject
import com.ringoid.domain.model.actions.LikeActionObject
import com.ringoid.domain.model.actions.UnlikeActionObject
import com.ringoid.domain.model.actions.ViewActionObject
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.collection.EqualRange

abstract class FeedViewModel(app: Application) : BaseViewModel(app) {

    private val viewActionObjectBuffer = mutableMapOf<Pair<String, String>, ViewActionObject>()

    abstract fun getFeedName(): String

    // --------------------------------------------------------------------------------------------
    fun onAddImage() {
        navigation.value = FeedFragment.InternalNavigator::openProfileScreen
    }

    /* Action Objects */
    // --------------------------------------------------------------------------------------------
    fun onLike(profileId: String, imageId: String, isLiked: Boolean) {
        advanceAndPushViewObject(imageId to profileId, recreate = true)
        val aobj = if (isLiked) LikeActionObject(sourceFeed = getFeedName(), targetImageId = imageId, targetUserId = profileId)
                   else UnlikeActionObject(sourceFeed = getFeedName(), targetImageId = imageId, targetUserId = profileId)
        actionObjectPool.put(aobj)
    }

    fun onBlock(profileId: String, imageId: String) {
        onReport(profileId = profileId, imageId = imageId, reasonNumber = 0)
    }

    fun onReport(profileId: String, imageId: String, reasonNumber: Int) {
        advanceAndPushViewObject(imageId to profileId)
        BlockActionObject(numberOfBlockReason = reasonNumber,
            sourceFeed = getFeedName(), targetImageId = imageId, targetUserId = profileId)
            .also { actionObjectPool.put(it) }
    }

    fun onView(items: EqualRange<ProfileImageVO>) {
        items.forEach {
            addViewObjectToBuffer(ViewActionObject(
                timeInMillis = 0L, sourceFeed = getFeedName(),
                targetImageId = it.image.id, targetUserId = it.profileId))
        }
    }

    // --------------------------------------------------------------------------------------------
    private fun advanceAndPushViewObject(key: Pair<String, String>, recreate: Boolean = false) {
        viewActionObjectBuffer.let { val aobj = it[key]?.advance() ; it.remove(key) ; aobj }
            ?.takeIf { recreate }
            ?.let { addViewObjectToBuffer(it) }
    }

    private fun advanceAndPushViewObjects() {
        viewActionObjectBuffer.apply {
            values.forEach { it.advance() ; actionObjectPool.put(it) }
            clear()
        }
    }

    private fun addViewObjectToBuffer(aobj: ViewActionObject) {
        viewActionObjectBuffer[aobj.targetImageId to aobj.targetUserId] = aobj
    }

    // TODO: advance, push & remove VIEW when left from visibility roi
}
