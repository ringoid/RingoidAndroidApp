package com.ringoid.origin.feed.view.lmm.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.image.IImage
import com.ringoid.origin.AppRes
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.adapter.base.*
import com.ringoid.origin.feed.adapter.lmm.BaseLmmAdapter
import com.ringoid.origin.feed.misc.OffsetScrollStrategy
import com.ringoid.origin.feed.view.DISCARD_PROFILE
import com.ringoid.origin.feed.view.FeedFragment
import com.ringoid.origin.feed.view.lc.SEEN_ALL_FEED
import com.ringoid.origin.feed.view.lmm.ILmmFragment
import com.ringoid.origin.messenger.model.ChatPayload
import com.ringoid.origin.messenger.view.ChatFragment
import com.ringoid.origin.messenger.view.IChatHost
import com.ringoid.origin.navigation.RequestCode
import com.ringoid.origin.navigation.navigate
import com.ringoid.origin.navigation.noConnection
import com.ringoid.origin.view.dialog.IDialogCallback
import com.ringoid.origin.view.filters.BaseFiltersFragment
import com.ringoid.origin.view.main.LmmNavTab
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.communicator
import com.ringoid.utility.runOnUiThread
import kotlinx.android.synthetic.main.fragment_feed.*
import timber.log.Timber

@Deprecated("LMM -> LC")
abstract class BaseLmmFeedFragment<VM : BaseLmmFeedViewModel> : FeedFragment<VM>(), IChatHost, IDialogCallback  {

    abstract fun instantiateFeedAdapter(): BaseLmmAdapter

    override fun createFeedAdapter(): BaseLmmAdapter =
        instantiateFeedAdapter().apply {
            messageClickListener = { model: FeedItem, position: Int, positionOfImage: Int ->
                openChat(position = position, peerId = model.id, image = model.images[positionOfImage])
            }
        }

    override fun createFiltersFragment(): BaseFiltersFragment<*> = LmmFeedFiltersFragment.newInstance()

    protected abstract fun getSourceFeed(): LmmNavTab

    override fun getAddPhotoDialogDescriptionResId(): Int = OriginR_string.feed_lmm_dialog_no_user_photo_description

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.DONE -> {
                when (newState.residual) {
                    is DISCARD_PROFILE -> {
                        // Deprecated, method 'decrementCountOnLmm()' was removed
                        // communicator(IBaseMainActivity::class.java)?.decrementCountOnLmm()
                        communicator(ILmmFragment::class.java)?.changeCountOnTopTab(tab = getSourceFeed(), delta = -1)
                    }
//                    /**
//                     * Since on refresh on Lmm each other Lmm's Feed except the current one goes into
//                     * [ViewState.DONE] with [CLEAR_AND_REFRESH_EXCEPT] residual payload, and the
//                     * current Feed stops refreshing if there is no images in user's profile, it is
//                     * also needed to stop refreshing for all the other Lmm's Feeds as well.
//                     */
//                    is NO_IMAGES_IN_USER_PROFILE ->
//                        communicator(ILmmFragment::class.java)?.accessViewModel()
//                            ?.let {
//                                // DEPRECATED and commented
////                                it.viewState.value = ViewState.CLEAR(mode = ViewState.CLEAR.MODE_NEED_REFRESH)
////                                it.viewState.value = ViewState.IDLE
//                            }
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
                        if (!it.isChatEmpty && it.sourceFeedCompat == LmmNavTab.MATCHES) {
                            vm.transferProfile(profileId = it.peerId)
                        } else {
                            feedAdapter.findModel(it.position)?.setOnlineStatus(it.onlineStatus)
                            getRecyclerView().post {
                                feedAdapter.notifyItemChanged(it.position, FeedViewHolderShowControls)
                                trackScrollOffsetForPosition(it.position)
                            }
                        }
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

        childFragmentManager.let {
            it.findFragmentByTag(tag)
                ?: run {
                    val payload = ChatPayload(
                        position = position,
                        peerId = peerId,
                        peerImageId = image?.id ?: DomainUtil.BAD_ID,
                        peerImageUri = image?.uri,
                        peerThumbnailUri = image?.thumbnailUri,
                        sourceFeed = getSourceFeed().slice(),  // compatibility mode
                        sourceFeedCompat = getSourceFeed())
                    vm.onChatOpen(profileId = peerId, imageId = image?.id ?: DomainUtil.BAD_ID)
                    navigate(this, path = "/chat?peerId=$peerId&payload=${payload.toJson()}&tag=$tag", rc = RequestCode.RC_CHAT)
                }
        }
    }

    // ------------------------------------------
    internal fun clearScreen(mode: Int) {
        invalidateScrollCaches()  // clear cached positions in offset scrolls strategies on clear feed
        onClearState(mode)
    }

    internal fun transferProfile(profileId: String, destinationFeed: LmmNavTab, payload: Bundle? = null) {
        vm.prependProfileOnTransfer(profileId, destinationFeed, payload)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(viewLifecycleOwner) {
            observe(vm.count()) { communicator(ILmmFragment::class.java)?.showCountOnTopTab(tab = getSourceFeed(), count = it) }
            observe(vm.feed()) {
                feedAdapter.submitList(it)
                runOnUiThread { rv_items?.let { scrollListToPosition(0) } }
            }
            observe(vm.refreshOnPush()) { showRefreshPopup(isVisible = it) }
        }
        /**
         * Parent's [Fragment.onActivityCreated] is called before this method on any child [Fragment],
         * so it's safe to access parent's [ViewModel] here, because it's already initialized.
         */
        communicator(ILmmFragment::class.java)?.accessViewModel()
            ?.let {
                // DEPRECATED and commented
//                it.viewState.value?.let { state ->
//                    when (state) {
//                        ViewState.LOADING -> showLoading(isVisible = true)
//                        else -> {
//                            showLoading(isVisible = false)
//                            vm.applyCachedFeed(it.cachedLmm)
//                        }
//                    }
//                }
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
                    else -> onDialogDismiss(tag = tag, payload = payload)
                }

                payload?.let {
                    vm.onChatClose(profileId = it.peerId, imageId = it.peerImageId)
                }
            }
        }
    }

    @Suppress("AutoDispose", "CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // refresh by click on 'tap to refresh' popup
        btn_refresh_popup.clicks().compose(clickDebounce()).subscribe { vm.onTapToRefreshClick() }
    }

    /* Scroll listeners */
    // --------------------------------------------------------------------------------------------
    override fun getOffsetScrollStrategies(): List<OffsetScrollStrategy> =
        mutableListOf<OffsetScrollStrategy>()
            .apply {
                addAll(super.getOffsetScrollStrategies())
                add(OffsetScrollStrategy(tag = "online top", type = OffsetScrollStrategy.Type.TOP, deltaOffset = AppRes.STD_MARGIN_16, hide = FeedViewHolderHideOnlineStatusOnScroll, show = FeedViewHolderShowOnlineStatusOnScroll))
                add(OffsetScrollStrategy(tag = "dot tabs top", type = OffsetScrollStrategy.Type.TOP, deltaOffset = AppRes.FEED_ITEM_TABS_INDICATOR_TOP2, hide = FeedViewHolderHideTabsIndicatorOnScroll, show = FeedViewHolderShowTabsIndicatorOnScroll))
                add(OffsetScrollStrategy(tag = "settings top", type = OffsetScrollStrategy.Type.TOP, deltaOffset = AppRes.FEED_ITEM_SETTINGS_BTN_TOP, hide = FeedViewHolderHideSettingsBtnOnScroll, show = FeedViewHolderShowSettingsBtnOnScroll))
                add(OffsetScrollStrategy(tag = "prop about top", type = OffsetScrollStrategy.Type.TOP, deltaOffset = AppRes.FEED_ITEM_PROPERTY_ABOUT_TOP, hide = FeedViewHolderHideAboutOnScroll, show = FeedViewHolderShowAboutOnScroll))
                add(OffsetScrollStrategy(tag = "prop 0 top", type = OffsetScrollStrategy.Type.TOP, deltaOffset = AppRes.FEED_ITEM_PROPERTY_TOP_0, hide = FeedViewHolderHideOnScroll(1), show = FeedViewHolderShowOnScroll(1)))
                add(OffsetScrollStrategy(tag = "prop 1 top", type = OffsetScrollStrategy.Type.TOP, deltaOffset = AppRes.FEED_ITEM_PROPERTY_TOP_1, hide = FeedViewHolderHideOnScroll(0), show = FeedViewHolderShowOnScroll(0)))
                add(OffsetScrollStrategy(tag = "prop name 0 top", type = OffsetScrollStrategy.Type.TOP, deltaOffset = AppRes.FEED_ITEM_PROPERTY_TOP_0_X, hide = FeedViewHolderHideNameOnScroll(0, type = OffsetScrollStrategy.Type.TOP), show = FeedViewHolderShowNameOnScroll(0, type = OffsetScrollStrategy.Type.TOP)))
                add(OffsetScrollStrategy(tag = "prop name 1 top", type = OffsetScrollStrategy.Type.TOP, deltaOffset = AppRes.FEED_ITEM_PROPERTY_TOP_1, hide = FeedViewHolderHideNameOnScroll(1, type = OffsetScrollStrategy.Type.TOP), show = FeedViewHolderShowNameOnScroll(1, type = OffsetScrollStrategy.Type.TOP)))
                add(OffsetScrollStrategy(tag = "prop name 2 top", type = OffsetScrollStrategy.Type.TOP, deltaOffset = AppRes.FEED_ITEM_PROPERTY_TOP_2, hide = FeedViewHolderHideNameOnScroll(2, type = OffsetScrollStrategy.Type.TOP), show = FeedViewHolderShowNameOnScroll(2, type = OffsetScrollStrategy.Type.TOP)))
            }

    override fun getTopBorderForOffsetScroll(): Int = AppRes.LMM_TOP_TAB_BAR_HIDE_AREA_HEIGHT
}
