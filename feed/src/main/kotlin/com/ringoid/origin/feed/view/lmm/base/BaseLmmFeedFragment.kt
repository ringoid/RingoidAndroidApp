package com.ringoid.origin.feed.view.lmm.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.image.IImage
import com.ringoid.domain.model.messenger.userMessage
import com.ringoid.origin.AppRes
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.adapter.base.*
import com.ringoid.origin.feed.adapter.lmm.BaseLmmAdapter
import com.ringoid.origin.feed.misc.OffsetScrollStrategy
import com.ringoid.origin.feed.view.FeedFragment
import com.ringoid.origin.feed.view.lmm.ILmmFragment
import com.ringoid.origin.feed.view.lmm.RESTORE_CACHED_LIKES
import com.ringoid.origin.feed.view.lmm.RESTORE_CACHED_USER_MESSAGES
import com.ringoid.origin.feed.view.lmm.SEEN_ALL_FEED
import com.ringoid.origin.messenger.ChatPayload
import com.ringoid.origin.messenger.view.ChatFragment
import com.ringoid.origin.messenger.view.IChatHost
import com.ringoid.origin.navigation.RequestCode
import com.ringoid.origin.navigation.navigate
import com.ringoid.origin.navigation.noConnection
import com.ringoid.origin.view.dialog.IDialogCallback
import com.ringoid.utility.communicator
import com.ringoid.utility.runOnUiThread
import timber.log.Timber

abstract class BaseLmmFeedFragment<VM : BaseLmmFeedViewModel> : FeedFragment<VM>(), IChatHost, IDialogCallback  {

    abstract fun instantiateFeedAdapter(): BaseLmmAdapter

    override fun createFeedAdapter(): BaseLmmAdapter =
        instantiateFeedAdapter().apply {
            messageClickListener = { model: FeedItem, position: Int, positionOfImage: Int ->
                communicator(ILmmFragment::class.java)?.showTabs(isVisible = false)
                openChat(position = position, peerId = model.id, image = model.images[positionOfImage])
            }
        }

    protected abstract fun getSourceFeed(): String

    override fun getAddPhotoDialogDescriptionResId(): Int = OriginR_string.feed_lmm_dialog_no_user_photo_description

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.DONE -> {
                when (newState.residual) {
                    /**
                     * When Lmm feed has been restored from a cache, there could be some liked images
                     * on some feed items, that were liked by user in between first successful fetch
                     * for feed and the following unsuccessful fetch, when time threshold has been hit.
                     * In that case, cache Lmm is restored and those likes should also be restored.
                     */
                    is RESTORE_CACHED_LIKES -> (newState.residual as RESTORE_CACHED_LIKES)
                        .let {
                            it.likedFeedItemIds.let { map ->
                                map.keys.forEach { id ->
                                    feedAdapter.findModelAndPosition { it.id == id }
                                        ?.also { model -> map[id]?.forEach { model.second.likedImages[it] = true } }
                                        ?.also { feedAdapter.notifyItemChanged(it.first) }
                                }
                            }
                        }
                    /**
                     * When Lmm feed has been restored from a cache, there could be some profiles that
                     * user has sent a single message to. This affect appearance of feed item in list
                     * (in particular, a chat icon changes). That messages could be sent by user in
                     * between first successful fetch for feed and the following unsuccessful fetch,
                     * when time threshold has been hit. In that case, cache Lmm is restored and fictive
                     * messages should be applied to such feed items to apply changes on their appearance.
                     */
                    is RESTORE_CACHED_USER_MESSAGES -> (newState.residual as RESTORE_CACHED_USER_MESSAGES)
                        .let {
                            it.messagedFeedItemIds.forEach { id ->
                                feedAdapter.findModelAndPosition { it.id == id }
                                    ?.also { it.second.messages.add(userMessage(chatId = it.second.id)) }
                                    ?.also { feedAdapter.notifyItemChanged(it.first) }
                            }
                        }
                    /**
                     * All feed items on a particular Lmm feed, specified by [SEEN_ALL_FEED.sourceFeed],
                     * have been seen by user, so it's time to hide red badge on a corresponding Lmm tab.
                     */
                    is SEEN_ALL_FEED -> {
                        (newState.residual as SEEN_ALL_FEED)
                            .let {
                                when (it.sourceFeed) {
                                    SEEN_ALL_FEED.FEED_LIKES -> communicator(ILmmFragment::class.java)?.showBadgeOnLikes(false)
                                    SEEN_ALL_FEED.FEED_MATCHES -> communicator(ILmmFragment::class.java)?.showBadgeOnMatches(false)
                                    SEEN_ALL_FEED.FEED_MESSENGER -> communicator(ILmmFragment::class.java)?.showBadgeOnMessenger(false)
                                    else -> { /* no-op */ }
                                }
                            }
                    }
                }
            }
        }
    }

    // --------------------------------------------------------------------------------------------
    override fun onBlockFromChat(tag: String, payload: ChatPayload) {
        vm.onBlock(profileId = payload.peerId, imageId = payload.peerImageId, sourceFeed = payload.sourceFeed, fromChat = true)
    }

    override fun onReportFromChat(tag: String, payload: ChatPayload, reasonNumber: Int) {
        vm.onReport(profileId = payload.peerId, imageId = payload.peerImageId, reasonNumber = reasonNumber, sourceFeed = payload.sourceFeed, fromChat = true)
    }

    override fun onDialogDismiss(tag: String, payload: Parcelable?) {
        (payload as? ChatPayload)
            ?.let {
                if (it.position == DomainUtil.BAD_POSITION) {
                    return
                }

                when (tag) {
                    ChatFragment.TAG -> {
                        communicator(ILmmFragment::class.java)?.showTabs(isVisible = true)
                        vm.onChatClose(profileId = it.peerId, imageId = it.peerImageId)
                        // supply first message from user to FeedItem to change in on bind
                        it.firstUserMessage?.let { _ -> vm.onFirstUserMessageSent(profileId = it.peerId) }
                        getRecyclerView().post { feedAdapter.notifyItemChanged(it.position, FeedViewHolderShowControls) }
                    }
                }
                true  // no-op value
            }
    }

    protected fun openChat(position: Int, peerId: String, image: IImage? = null, tag: String = ChatFragment.TAG) {
        if (!connectionManager.isNetworkAvailable()) {
            noConnection(this)
            return
        }

        communicator(ILmmFragment::class.java)?.showTabs(isVisible = false)
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

    // ------------------------------------------
    internal fun clearScreen(mode: Int) {
        vm.clearScreen(mode)
    }

    override fun onRefresh() {
        super.onRefresh()
        Bus.post(event = BusEvent.RefreshOnLmm)
    }

    internal fun transferProfile(profileId: String, destinationFeed: String, payload: Bundle? = null) {
        vm.prependProfileOnTransfer(profileId, destinationFeed, payload)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(viewLifecycleOwner) {
            observe(vm.feed) {
                feedAdapter.submitList(it)
                runOnUiThread { scrollListToPosition(0) }
            }
        }
        /**
         * Parent's [Fragment.onActivityCreated] is called before this method on any child [Fragment],
         * so it's safe to access parent's [ViewModel] here, because it's already initialized.
         */
        communicator(ILmmFragment::class.java)?.accessViewModel()
            ?.let {
                it.viewState.value?.let { state ->
                    when (state) {
                        ViewState.LOADING -> showLoading(isVisible = true)
                        else -> {
                            showLoading(isVisible = false)
                            vm.applyCachedFeed(it.cachedLmm)
                        }
                    }
                }
                viewLifecycleOwner.observe(it.listScrolls, ::scrollListToPosition)
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
                }
                onDialogDismiss(tag = tag, payload = payload)
            }
        }
    }

    /* Scroll listeners */
    // --------------------------------------------------------------------------------------------
    override fun getOffsetScrollStrategies(): List<OffsetScrollStrategy> =
        mutableListOf<OffsetScrollStrategy>()
            .apply {
                addAll(super.getOffsetScrollStrategies())
                add(OffsetScrollStrategy(type = OffsetScrollStrategy.Type.UP, deltaOffset = AppRes.STD_MARGIN_16, hide = FeedViewHolderHideOnlineStatusOnScroll, show = FeedViewHolderShowOnlineStatusOnScroll))
                add(OffsetScrollStrategy(type = OffsetScrollStrategy.Type.UP, deltaOffset = AppRes.FEED_ITEM_AGE_TOP, hide = FeedViewHolderHideAgeOnScroll, show = FeedViewHolderShowAgeOnScroll))
                add(OffsetScrollStrategy(type = OffsetScrollStrategy.Type.UP, deltaOffset = AppRes.FEED_ITEM_DISTANCE_TOP, hide = FeedViewHolderHideDistanceOnScroll, show = FeedViewHolderShowDistanceOnScroll))
                add(OffsetScrollStrategy(type = OffsetScrollStrategy.Type.UP, deltaOffset = AppRes.FEED_ITEM_TABS_INDICATOR_TOP2, hide = FeedViewHolderHideTabsIndicatorOnScroll, show = FeedViewHolderShowTabsIndicatorOnScroll))
                add(OffsetScrollStrategy(type = OffsetScrollStrategy.Type.TOP, deltaOffset = AppRes.FEED_ITEM_SETTINGS_BTN_TOP, hide = FeedViewHolderHideSettingsBtnOnScroll, show = FeedViewHolderShowSettingsBtnOnScroll))
            }
}
