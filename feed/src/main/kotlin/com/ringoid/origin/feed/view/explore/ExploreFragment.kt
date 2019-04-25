package com.ringoid.origin.feed.view.explore

import android.os.Bundle
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.exception.ThresholdExceededException
import com.ringoid.origin.AppRes
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.adapter.FeedAdapter
import com.ringoid.origin.feed.adapter.base.BaseFeedAdapter
import com.ringoid.origin.feed.adapter.base.FeedViewHolderHideLikeBtnOnScroll
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowLikeBtnOnScroll
import com.ringoid.origin.feed.misc.OffsetScrollStrategy
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.FeedFragment
import com.ringoid.origin.navigation.Payload
import com.ringoid.origin.navigation.noConnection
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.utility.debugToast
import timber.log.Timber

class ExploreFragment : FeedFragment<ExploreViewModel>() {

    companion object {
        fun newInstance(): ExploreFragment = ExploreFragment()
    }

    override fun createFeedAdapter(): BaseFeedAdapter =
        FeedAdapter().apply {
            onLikeImageListener = { model: ProfileImageVO, _ ->
                if (!connectionManager.isNetworkAvailable()) {
                    noConnection(this@ExploreFragment)
                } else {
                    Timber.v("${if (model.isLiked) "L" else "Unl"}iked image: ${model.image}")
                    vm.onLike(profileId = model.profileId, imageId = model.image.id, isLiked = model.isLiked)
                }
            }
        }

    override fun getVmClass(): Class<ExploreViewModel> = ExploreViewModel::class.java

    override fun getEmptyStateInput(mode: Int): EmptyFragment.Companion.Input? =
        when (mode) {
            ViewState.CLEAR.MODE_EMPTY_DATA -> EmptyFragment.Companion.Input(emptyTitleResId = OriginR_string.feed_explore_empty_title, emptyTextResId = OriginR_string.feed_explore_empty_no_data)
            ViewState.CLEAR.MODE_NEED_REFRESH -> EmptyFragment.Companion.Input(emptyTitleResId = OriginR_string.feed_explore_empty_title, emptyTextResId = OriginR_string.common_pull_to_refresh)
            else -> null
        }

    override fun getAddPhotoDialogDescriptionResId(): Int = OriginR_string.feed_explore_dialog_no_user_photo_description

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        when (newState) {
            is ViewState.ERROR -> {
                when (newState.e) {
                    is ThresholdExceededException -> {
                        activity?.debugToast("Repeat after delay exceeded time threshold")
                        vm.clearScreen(ViewState.CLEAR.MODE_EMPTY_DATA)  // purge Explore feed if fetching has failed with timeout
                        return
                    }
                    else -> feedAdapter.error()
                }
            }
        }
        super.onViewStateChange(newState)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        // don't call 'super', completely overridden method
    }

    // ------------------------------------------
    private var postponedTabTransaction = false

    override fun onTabTransaction(payload: String?) {
        super.onTabTransaction(payload)
        if (!isViewModelInitialized) {
            postponedTabTransaction = true
            return
        }

        payload?.let {
            when (it) {
                Payload.PAYLOAD_FEED_NEED_REFRESH -> permissionManager.askForLocationPermission(this)  //vm.onRefresh(withLoading = true)
                else -> { /* no-op */ }
            }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewLifecycleOwner.observe(vm.feed) { feedAdapter.append(it.profiles.map { FeedItemVO(it) }) { !isEmpty() } }
        vm.clearScreen(mode = ViewState.CLEAR.MODE_NEED_REFRESH)  // Explore feed is initially purged

        if (postponedTabTransaction) {
            doPostponedTabTransaction()
            postponedTabTransaction = false
        }
    }

    /* Scroll listeners */
    // --------------------------------------------------------------------------------------------
    override fun getOffsetScrollStrategies(): List<OffsetScrollStrategy> =
        mutableListOf<OffsetScrollStrategy>()
            .apply {
                addAll(super.getOffsetScrollStrategies())
                add(OffsetScrollStrategy(type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_BIAS_BTN_BOTTOM, hide = FeedViewHolderHideLikeBtnOnScroll, show = FeedViewHolderShowLikeBtnOnScroll))
            }
}
