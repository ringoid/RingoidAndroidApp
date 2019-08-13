package com.ringoid.origin.feed.view.explore

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.exception.ThresholdExceededException
import com.ringoid.origin.AppRes
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.WidgetR_attrs
import com.ringoid.origin.feed.WidgetR_color
import com.ringoid.origin.feed.adapter.base.BaseFeedAdapter
import com.ringoid.origin.feed.adapter.base.FeedViewHolderHideLikeBtnOnScroll
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowLikeBtnOnScroll
import com.ringoid.origin.feed.adapter.explore.ExploreFeedAdapter
import com.ringoid.origin.feed.misc.OffsetScrollStrategy
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.DISCARD_PROFILES
import com.ringoid.origin.feed.view.FeedFragment
import com.ringoid.origin.feed.view.NO_IMAGES_IN_USER_PROFILE
import com.ringoid.origin.navigation.Payload
import com.ringoid.origin.navigation.noConnection
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.filters.BaseFiltersFragment
import com.ringoid.utility.debugToast
import com.ringoid.utility.getAttributeColor
import com.ringoid.utility.image.ImageRequest

class ExploreFragment : FeedFragment<ExploreViewModel>() {

    companion object {
        fun newInstance(): ExploreFragment = ExploreFragment()
    }

    override fun createFeedAdapter(): BaseFeedAdapter =
        ExploreFeedAdapter(ImageRequest(context!!)).apply {
            onFooterLabelClickListener = { onEmptyLabelClick() }
            onLikeImageListener = { model: ProfileImageVO, _ /** image position */ ->
                if (!connectionManager.isNetworkAvailable()) {
                    noConnection(this@ExploreFragment)
                } else {
                    vm.onLike(profileId = model.profileId, imageId = model.image.id)
                }
            }
        }

    override fun createFiltersFragment(): BaseFiltersFragment<*> = ExploreFiltersFragment.newInstance()

    override fun getVmClass(): Class<ExploreViewModel> = ExploreViewModel::class.java

    override fun getEmptyStateInput(mode: Int): EmptyFragment.Companion.Input? =
        when (mode) {
            ViewState.CLEAR.MODE_EMPTY_DATA -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_explore_empty_no_data)
            ViewState.CLEAR.MODE_NEED_REFRESH -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.common_pull_to_refresh)
            ViewState.CLEAR.MODE_CHANGE_FILTERS ->
                EmptyFragment.Companion.Input(
                    emptyTextResId = OriginR_string.feed_empty_no_data_filters,
                    labelTextColor = context?.getAttributeColor(WidgetR_attrs.refTextColorPrimary) ?: ContextCompat.getColor(context!!, WidgetR_color.primary_text),
                    isLabelClickable = true)
            else -> null
        }

    override fun getAddPhotoDialogDescriptionResId(): Int = OriginR_string.feed_explore_dialog_no_user_photo_description

    override fun getToolbarTitleResId(): Int = OriginR_string.feed_explore_title

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        when (newState) {
            is ViewState.DONE -> {
                when (newState.residual) {
                    is DISCARD_PROFILES -> onDiscardMultipleProfilesState(profileIds = (newState.residual as DISCARD_PROFILES).profileIds)
                    is NO_IMAGES_IN_USER_PROFILE -> {
                        if (feedAdapter.isEmpty()) {
                            onClearState(mode = ViewState.CLEAR.MODE_NEED_REFRESH)
                        }
                    }
                }
            }
            is ViewState.ERROR -> {
                when (newState.e) {
                    is ThresholdExceededException -> {
                        activity?.debugToast("Repeat after delay exceeded time threshold")
                        onClearState(ViewState.CLEAR.MODE_EMPTY_DATA)  // purge Explore feed if fetching has failed with timeout
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
                Payload.PAYLOAD_FEED_NEED_REFRESH -> vm.refresh()
                else -> { /* no-op */ }
            }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewLifecycleOwner.observe(vm.feed) {
            if (feedAdapter.getModelsCount() + it.profiles.size > 0) {
                toolbarWidget?.restoreScrollFlags()
            }
            feedAdapter.append(it.profiles.map { FeedItemVO(it) }) { !isEmpty() }
        }

        if (postponedTabTransaction) {
            doPostponedTabTransaction()
            postponedTabTransaction = false
        }
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClearState(mode = ViewState.CLEAR.MODE_NEED_REFRESH)  // Explore feed is initially purged

        filtersPopupWidget?.setOnClickListener_applyFilters {
            filtersPopupWidget?.hide()
            vm.onApplyFilters()
        }
        filtersPopupWidget?.hideShowAllButton()
    }

    /* Scroll listeners */
    // --------------------------------------------------------------------------------------------
    override fun getOffsetScrollStrategies(): List<OffsetScrollStrategy> =
        mutableListOf<OffsetScrollStrategy>()
            .apply {
                addAll(super.getOffsetScrollStrategies())
                add(OffsetScrollStrategy(tag = "like btn bottom", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_BIAS_BTN_BOTTOM, hide = FeedViewHolderHideLikeBtnOnScroll, show = FeedViewHolderShowLikeBtnOnScroll))
            }
}
