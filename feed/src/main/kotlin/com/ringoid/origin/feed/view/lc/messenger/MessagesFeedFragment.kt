package com.ringoid.origin.feed.view.lc.messenger

import android.os.Bundle
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.image.IImage
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.adapter.lmm.BaseLmmAdapter
import com.ringoid.origin.feed.adapter.lmm.MessengerFeedAdapter
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.lc.base.BaseLcFeedFragment
import com.ringoid.origin.messenger.model.ChatPayload
import com.ringoid.origin.messenger.view.ChatFragment
import com.ringoid.origin.navigation.RequestCode
import com.ringoid.origin.navigation.navigate
import com.ringoid.origin.navigation.noConnection
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.main.IBaseMainActivity
import com.ringoid.origin.view.main.LcNavTab
import com.ringoid.utility.communicator
import com.ringoid.utility.image.ImageRequest

class MessagesFeedFragment : BaseLcFeedFragment<MessagesFeedViewModel>() {

    companion object {
        fun newInstance(): MessagesFeedFragment = MessagesFeedFragment()
    }

    override fun getVmClass(): Class<MessagesFeedViewModel> = MessagesFeedViewModel::class.java

    override fun createFeedAdapter(): BaseLmmAdapter =
        MessengerFeedAdapter(ImageRequest(context!!)).apply {
            messageClickListener = { model: FeedItem, position: Int, positionOfImage: Int ->
                openChat(position = position, peerId = model.id, image = model.images[positionOfImage])
            }
            onImageToOpenChatClickListener = { model: ProfileImageVO, feedItemPosition: Int ->
                openChat(position = feedItemPosition, peerId = model.profileId, image = model.image)
            }
        }

    override fun getEmptyStateInput(mode: Int): EmptyFragment.Companion.Input? =
        when (mode) {
            ViewState.CLEAR.MODE_EMPTY_DATA -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_messages_empty_no_data)
            ViewState.CLEAR.MODE_NEED_REFRESH -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.common_pull_to_refresh)
            else -> null
        }

    override fun getSourceFeed(): LcNavTab = LcNavTab.MESSAGES

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(viewLifecycleOwner) {
            observe(vm.count) { communicator(IBaseMainActivity::class.java)?.showCountOnMessages(count = it) }
        }
    }

    // --------------------------------------------------------------------------------------------
    private fun openChat(position: Int, peerId: String, image: IImage? = null, tag: String = ChatFragment.TAG) {
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
