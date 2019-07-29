package com.ringoid.origin.feed.view.lc.base

import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.image.IImage
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.view.FeedFragment
import com.ringoid.origin.messenger.model.ChatPayload
import com.ringoid.origin.messenger.view.ChatFragment
import com.ringoid.origin.navigation.RequestCode
import com.ringoid.origin.navigation.navigate
import com.ringoid.origin.navigation.noConnection
import com.ringoid.origin.view.main.LmmNavTab

abstract class BaseLcFeedFragment<VM : BaseLcFeedViewModel> : FeedFragment<VM>() {

    protected abstract fun getSourceFeed(): LmmNavTab

    override fun getAddPhotoDialogDescriptionResId(): Int = OriginR_string.feed_lmm_dialog_no_user_photo_description

    // --------------------------------------------------------------------------------------------
    protected fun openChat(position: Int, peerId: String, image: IImage? = null, tag: String = ChatFragment.TAG) {
        if (!connectionManager.isNetworkAvailable()) {
            noConnection(this)
            return
        }

        childFragmentManager.let {
            it.findFragmentByTag(tag)
                ?: run {
                    val payload = ChatPayload(
                        position = position,
                        peerId = peerId,
                        peerImageId = image?.id ?: DomainUtil.BAD_ID,
                        peerImageUri = image?.uri,
                        peerThumbnailUri = image?.thumbnailUri,
                        sourceFeed = getSourceFeed())
                    vm.onChatOpen(profileId = peerId, imageId = image?.id ?: DomainUtil.BAD_ID)
                    navigate(this, path = "/chat?peerId=$peerId&payload=${payload.toJson()}&tag=$tag", rc = RequestCode.RC_CHAT)
                }
        }
    }
}
