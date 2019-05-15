package com.ringoid.origin.feed.adapter.base

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.ViewPreloadSizeProvider
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.BuildConfig
import com.ringoid.origin.feed.adapter.profile.ProfileImageAdapter
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.view.common.visibility_tracker.TrackingBus
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.collection.EqualRange
import com.ringoid.utility.linearLayoutManager
import com.ringoid.widget.view.rv.EnhancedPagerSnapHelper
import kotlinx.android.synthetic.main.rv_item_feed_profile_content.view.*
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import timber.log.Timber

interface IFeedViewHolder {

    var onBeforeLikeListener: (() -> Boolean)?
    var onImageTouchListener: ((x: Float, y: Float) -> Unit)?
    var snapPositionListener: ((snapPosition: Int) -> Unit)?
    var trackingBus: TrackingBus<EqualRange<ProfileImageVO>>?

    fun getCurrentImagePosition(): Int
}

abstract class OriginFeedViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : BaseViewHolder<FeedItemVO>(view), IFeedViewHolder {

    override var onBeforeLikeListener: (() -> Boolean)? = null
    override var onImageTouchListener: ((x: Float, y: Float) -> Unit)? = null
    override var snapPositionListener: ((snapPosition: Int) -> Unit)? = null
    override var trackingBus: TrackingBus<EqualRange<ProfileImageVO>>? = null

    override fun getCurrentImagePosition(): Int = 0
}

abstract class BaseFeedViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : OriginFeedViewHolder(view, viewPool) {

    override var onBeforeLikeListener: (() -> Boolean)? = null
        set(value) {
            field = value
            profileImageAdapter.onBeforeLikeListener = value
        }

    override var onImageTouchListener: ((x: Float, y: Float) -> Unit)? = null
        set(value) {
            field = value
            profileImageAdapter.onImageTouchListener = value
        }

    internal val profileImageAdapter = ProfileImageAdapter(view.context)

    private val imagePreloadListener: RecyclerViewPreloader<ProfileImageVO>
    private val snapHelper = EnhancedPagerSnapHelper(duration = 30)

    init {
        itemView.rv_items.apply {
            adapter = profileImageAdapter
                .also {
                    it.onBeforeLikeListener = onBeforeLikeListener
                    it.onImageTouchListener = onImageTouchListener
                    it.tabsObserver = itemView.tabs2.adapterDataObserver
                }
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                .also { it.initialPrefetchItemCount = 4 }
            snapHelper.attachToRecyclerView(this)
            itemView.tabs2.attachToRecyclerView(this, snapHelper)
            setHasFixedSize(true)
            setRecycledViewPool(viewPool)
            setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING)
            OverScrollDecoratorHelper.setUpOverScroll(this, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(rv, dx, dy)
                    rv.linearLayoutManager()?.let {
                        val from = it.findFirstVisibleItemPosition()
                        val to = it.findLastVisibleItemPosition()
                        val items = profileImageAdapter.getItemsExposed(from = from, to = to)
                        Timber.v("Visible profile images [${items.size}] [$from, $to]: $items")
                        trackingBus?.postViewEvent(EqualRange(from = from, to = to, items = items))
                        snapPositionListener?.invoke(from)
                    }
                }
            })
            imagePreloadListener = RecyclerViewPreloader(Glide.with(this), profileImageAdapter, ViewPreloadSizeProvider<ProfileImageVO>(), 10)
            addOnScrollListener(imagePreloadListener)
        }
        itemView.tv_profile_id.changeVisibility(isVisible = BuildConfig.IS_STAGING)
    }

    override fun bind(model: FeedItemVO) {
        showControls()  // cancel any effect caused by applied payloads
        profileImageAdapter.apply {
            clear()  // clear old items, preventing animator to animate change upon async diff calc finishes
            submitList(model.images.map { ProfileImageVO(profileId = model.id, image = it, isLiked = model.isLiked(imageId = it.id)) })
            itemView.rv_items.post { itemView.rv_items.linearLayoutManager()?.scrollToPosition(model.positionOfImage) }
            itemView.tabs2.alpha = if (model.images.size < 2) 0.0f else 1.0f
        }

        if (BuildConfig.IS_STAGING) {
            itemView.tv_profile_id.text = "Profile: ${model.idWithFirstN()}"
        }
    }

    override fun bind(model: FeedItemVO, payloads: List<Any>) {
        if (payloads.contains(FeedViewHolderHideControls)) {
            hideControls()
        }
        if (payloads.contains(FeedViewHolderShowControls)) {
            showControls()
        }

        // scroll affected
        if (payloads.contains(FeedViewHolderHideSettingsBtnOnScroll)) {
            itemView.ibtn_settings.changeVisibility(isVisible = false)
        }
        if (payloads.contains(FeedViewHolderShowSettingsBtnOnScroll)) {
            itemView.ibtn_settings.changeVisibility(isVisible = true)
        }
//        if (payloads.contains(FeedViewHolderHideTabsIndicatorOnScroll)) {
//            itemView.tabs.changeVisibility(isVisible = false)
//        }
//        if (payloads.contains(FeedViewHolderShowTabsIndicatorOnScroll)) {
//            itemView.tabs.changeVisibility(isVisible = true)
//        }
        if (payloads.contains(FeedViewHolderHideTabs2IndicatorOnScroll)) {
            itemView.tabs2.changeVisibility(isVisible = false)
        }
        if (payloads.contains(FeedViewHolderShowTabs2IndicatorOnScroll)) {
            itemView.tabs2.changeVisibility(isVisible = true)
        }
        if (payloads.contains(FeedViewHolderHideLikeBtnOnScroll)) {
            profileImageAdapter.notifyItemChanged(getCurrentImagePosition(), FeedViewHolderHideControls)
        }
        if (payloads.contains(FeedViewHolderShowLikeBtnOnScroll)) {
            profileImageAdapter.notifyItemChanged(getCurrentImagePosition(), FeedViewHolderShowControls)
        }
    }

    // ------------------------------------------------------------------------
    protected open fun hideControls() {
        itemView.apply {
            tabs2.changeVisibility(isVisible = false)
            ibtn_settings.changeVisibility(isVisible = false)
        }
        profileImageAdapter.notifyItemChanged(getCurrentImagePosition(), FeedViewHolderHideControls)
    }

    protected open fun showControls() {
        itemView.apply {
            tabs2.changeVisibility(isVisible = true)
            ibtn_settings.changeVisibility(isVisible = true)
        }
        profileImageAdapter.notifyItemChanged(getCurrentImagePosition(), FeedViewHolderShowControls)
    }

    override fun getCurrentImagePosition(): Int =
        itemView.rv_items.linearLayoutManager()?.findFirstVisibleItemPosition() ?: 0
}

class FeedViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : BaseFeedViewHolder(view, viewPool)

class HeaderFeedViewHolder(view: View) : OriginFeedViewHolder(view), IFeedViewHolder {

    override fun bind(model: FeedItemVO) {
        // no-op
    }
}

class FooterFeedViewHolder(view: View) : OriginFeedViewHolder(view), IFeedViewHolder {

    override fun bind(model: FeedItemVO) {
        showControls()
    }

    override fun bind(model: FeedItemVO, payloads: List<Any>) {
        if (payloads.contains(FeedFooterViewHolderHideControls)) {
            hideControls()
        }
        if (payloads.contains(FeedFooterViewHolderShowControls)) {
            showControls()
        }
    }

    // ------------------------------------------
    private fun hideControls() {
//        itemView.tv_end_item.changeVisibility(isVisible = false)
    }

    private fun showControls() {
//        itemView.tv_end_item.changeVisibility(isVisible = true)
    }
}
