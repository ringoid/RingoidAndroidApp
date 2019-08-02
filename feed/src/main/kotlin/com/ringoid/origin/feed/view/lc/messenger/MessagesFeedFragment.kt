package com.ringoid.origin.feed.view.lc.messenger

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.image.IImage
import com.ringoid.domain.model.messenger.peerMessage
import com.ringoid.domain.model.messenger.userMessage
import com.ringoid.origin.AppRes
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.adapter.base.FeedViewHolderHideChatBtnOnScroll
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowChatBtnOnScroll
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowControls
import com.ringoid.origin.feed.adapter.lmm.BaseLmmAdapter
import com.ringoid.origin.feed.adapter.lmm.MessagesFeedAdapter
import com.ringoid.origin.feed.misc.OffsetScrollStrategy
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.DISCARD_PROFILE
import com.ringoid.origin.feed.view.lc.base.BaseLcFeedFragment
import com.ringoid.origin.feed.view.lmm.base.PUSH_NEW_MATCHES_TOTAL
import com.ringoid.origin.feed.view.lmm.base.PUSH_NEW_MESSAGES
import com.ringoid.origin.feed.view.lmm.base.PUSH_NEW_MESSAGES_TOTAL
import com.ringoid.origin.messenger.model.ChatPayload
import com.ringoid.origin.messenger.view.ChatFragment
import com.ringoid.origin.messenger.view.IChatHost
import com.ringoid.origin.navigation.RequestCode
import com.ringoid.origin.navigation.navigate
import com.ringoid.origin.navigation.noConnection
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.dialog.IDialogCallback
import com.ringoid.origin.view.main.IBaseMainActivity
import com.ringoid.origin.view.main.LcNavTab
import com.ringoid.origin.view.particles.PARTICLE_TYPE_MATCH
import com.ringoid.origin.view.particles.PARTICLE_TYPE_MESSAGE
import com.ringoid.utility.communicator
import com.ringoid.utility.image.ImageRequest
import timber.log.Timber

class MessagesFeedFragment : BaseLcFeedFragment<MessagesFeedViewModel>(), IChatHost, IDialogCallback {

    companion object {
        fun newInstance(): MessagesFeedFragment = MessagesFeedFragment()
    }

    override fun getVmClass(): Class<MessagesFeedViewModel> = MessagesFeedViewModel::class.java

    override fun createFeedAdapter(): BaseLmmAdapter =
        MessagesFeedAdapter(ImageRequest(context!!)).apply {
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

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.DONE -> {
                when (newState.residual) {
                    is DISCARD_PROFILE -> communicator(IBaseMainActivity::class.java)?.decrementCountOnMessages()
                    is PUSH_NEW_MESSAGES -> {
                        val profileId = (newState.residual as PUSH_NEW_MESSAGES).profileId
                        feedAdapter.findPosition { it.id == profileId }
                            .takeIf { it != DomainUtil.BAD_POSITION }
                            ?.let { feedAdapter.notifyItemChanged(it, FeedViewHolderShowControls) }
                    }
                    is PUSH_NEW_MATCHES_TOTAL,
                    is PUSH_NEW_MESSAGES_TOTAL -> communicator(IBaseMainActivity::class.java)?.showBadgeOnMessages(isVisible = true)
                }
            }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(viewLifecycleOwner) {
            observe(vm.count) { communicator(IBaseMainActivity::class.java)?.showCountOnMessages(count = it) }
            observe(vm.pushNewMatch) { communicator(IBaseMainActivity::class.java)?.showParticleAnimation(PARTICLE_TYPE_MATCH) }
            observe(vm.pushNewMessage) { communicator(IBaseMainActivity::class.java)?.showParticleAnimation(PARTICLE_TYPE_MESSAGE) }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RequestCode.RC_CHAT -> {
                if (resultCode != Activity.RESULT_OK) {
                    return
                }

                if (data == null) {
                    val e = NullPointerException("No output from Chat dialog - this is an error!")
                    Timber.e(e); throw e
                }

                val tag = data.getStringExtra("tag")
                val payload = data.getParcelableExtra<ChatPayload>("payload")
                when (data.getStringExtra("action")) {
                    "block" -> onBlockFromChat(tag = tag, payload = payload)
                    "report" -> onReportFromChat(tag = tag, payload = payload, reasonNumber = data.getIntExtra("reason", 0))
                    else -> onDialogDismiss(tag = tag, payload = payload)
                }

                payload?.let {
                    vm.onChatClose(profileId = it.peerId, imageId = it.peerImageId)
                }
            }
        }
    }

    // --------------------------------------------------------------------------------------------
    override fun onBlockFromChat(tag: String, payload: ChatPayload) {
        vm.onBlock(profileId = payload.peerId, imageId = payload.peerImageId, sourceFeed = payload.sourceFeedCompat.feedName, fromChat = true)
    }

    override fun onReportFromChat(tag: String, payload: ChatPayload, reasonNumber: Int) {
        vm.onReport(profileId = payload.peerId, imageId = payload.peerImageId, reasonNumber = reasonNumber, sourceFeed = payload.sourceFeedCompat.feedName, fromChat = true)
    }

    override fun onDialogDismiss(tag: String, payload: Parcelable?) {
        (payload as? ChatPayload)
            ?.let {
                if (it.position == DomainUtil.BAD_POSITION) {
                    return
                }

                when (tag) {
                    ChatFragment.TAG -> {
                        scrollToTopOfItemAtPosition(it.position)
                        getRecyclerView().post {
                            feedAdapter.findModel(it.position)?.let { feedItem ->
                                with (feedItem.messagesReflection) {
                                    clear()
                                    val peerMessagesCount = ChatInMemoryCache.getPeerMessagesCount(profileId = feedItem.id)
                                    val userMessagesCount = ChatInMemoryCache.getUserMessagesCount(chatId = feedItem.id)
                                    addAll(Array(peerMessagesCount) { peerMessage(chatId = feedItem.id) })
                                    addAll(Array(userMessagesCount) { userMessage(chatId = feedItem.id) })
                                }
                                feedItem.setOnlineStatus(it.onlineStatus)
                            }
                            feedAdapter.notifyItemChanged(it.position, FeedViewHolderShowControls)
                            trackScrollOffsetForPosition(it.position)
                        }
                    }
                }
                true  // no-op value
            }
    }

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

    /* Scroll listeners */
    // --------------------------------------------------------------------------------------------
    override fun getOffsetScrollStrategies(): List<OffsetScrollStrategy> =
        mutableListOf<OffsetScrollStrategy>()
            .apply {
                addAll(super.getOffsetScrollStrategies())
                add(OffsetScrollStrategy(tag = "chat btn bottom", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_BIAS_BTN_BOTTOM, hide = FeedViewHolderHideChatBtnOnScroll, show = FeedViewHolderShowChatBtnOnScroll))
            }
}
