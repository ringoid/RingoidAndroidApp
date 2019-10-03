package com.ringoid.origin.feed.view.explore

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.ringoid.base.observe
import com.ringoid.base.observeOneShot
import com.ringoid.base.view.ViewState
import com.ringoid.origin.AppRes
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.WidgetR_attrs
import com.ringoid.origin.feed.WidgetR_color
import com.ringoid.origin.feed.adapter.base.*
import com.ringoid.origin.feed.adapter.explore.ExploreFeedAdapter
import com.ringoid.origin.feed.exception.LoadMoreFailedException
import com.ringoid.origin.feed.misc.OffsetScrollStrategy
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.FeedFragment
import com.ringoid.origin.navigation.noConnection
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.filters.BaseFiltersFragment
import com.ringoid.report.exception.ThresholdExceededException
import com.ringoid.utility.debugToast
import com.ringoid.utility.getAttributeColor
import com.ringoid.utility.runOnUiThread

class ExploreFeedFragment : FeedFragment<ExploreFeedViewModel>() {

    companion object {
        fun newInstance(): ExploreFeedFragment = ExploreFeedFragment()
    }

    override fun createFeedAdapter(): BaseFeedAdapter =
        ExploreFeedAdapter().apply {
            onErrorLabelClickListener = { vm.onRetryLoadMore() }
            onFooterLabelClickListener = { onEmptyLabelClick() }
            onLikeImageListener = { model: ProfileImageVO, _ /** image position */ ->
                if (!connectionManager.isNetworkAvailable()) {
                    noConnection(this@ExploreFeedFragment)
                } else {
                    vm.onLike(profileId = model.profileId, imageId = model.image.id)
                }
            }
        }

    override fun createFiltersFragment(): BaseFiltersFragment<*> = ExploreFeedFiltersFragment.newInstance()

    override fun getVmClass(): Class<ExploreFeedViewModel> = ExploreFeedViewModel::class.java

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

    override fun contextMenuActions(): String = "like"

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        when (newState) {
            is ViewState.ERROR -> {
                when (newState.e) {
                    is LoadMoreFailedException -> {
                        feedAdapter.error()  // set feed items list to error state
                        return  // override superclass behavior
                    }
                    is ThresholdExceededException -> {
                        activity?.debugToast("Repeat after delay exceeded time threshold")
                        onClearState(ViewState.CLEAR.MODE_EMPTY_DATA)  // purge Explore feed if fetching has failed with timeout
                        return  // override superclass behavior
                    }
                }
            }
            is ViewState.PAGING -> {
                timeKeeper.start()
                runOnUiThread { feedAdapter.loading() }  // avoid inconsistency in RV
            }
        }
        super.onViewStateChange(newState)
    }

    // ------------------------------------------
    override fun onDiscardAllProfiles() {
        if (vm.hasFiltersApplied()) {
            onClearState(ViewState.CLEAR.MODE_CHANGE_FILTERS)  // discard all filtered profiles in Feed
        } else {
            super.onDiscardAllProfiles()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        // don't call 'super', completely overridden method
    }

    override fun onNoImagesInUserProfile(redirectBackOnFeedScreen: Boolean) {
        super.onNoImagesInUserProfile(redirectBackOnFeedScreen)
        if (feedAdapter.isEmpty()) {
            onClearState(mode = ViewState.CLEAR.MODE_NEED_REFRESH)  // no images in Profile
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with (viewLifecycleOwner) {
            observe(vm.feed()) {
                if (feedAdapter.getModelsCount() + it.profiles.size > 0) {
                    toolbarWidget?.restoreScrollFlags()
                }
                feedAdapter.append(it.profiles.map { FeedItemVO(it) }) { !isEmpty() }
            }
            observeOneShot(vm.discardProfilesOneShot()) { onDiscardMultipleProfilesState(profileIds = it) }
        }
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClearState(mode = ViewState.CLEAR.MODE_NEED_REFRESH)  // Explore feed is initially purged

        filtersPopupWidget?.setOnClickListener_applyFilters {
            it.hide()
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
                add(OffsetScrollStrategy(tag = "total likes bottom", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_BIAS_BTN_LABEL_BOTTOM, hide = FeedViewHolderHideTotalLikesCountOnScroll, show = FeedViewHolderShowTotalLikesCountOnScroll))
            }
}
