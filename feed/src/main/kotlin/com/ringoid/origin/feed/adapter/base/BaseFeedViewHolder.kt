package com.ringoid.origin.feed.adapter.base

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.model.feed.IProfile
import com.ringoid.origin.feed.adapter.profile.ProfileImageAdapter
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.view.common.visibility_tracker.TrackingBus
import com.ringoid.utility.collection.EqualRange
import kotlinx.android.synthetic.main.rv_item_feed_profile_content.view.*
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import timber.log.Timber

interface IFeedViewHolder {

    var trackingBus: TrackingBus<EqualRange<ProfileImageVO>>?

    fun getCurrentImagePosition(): Int
}

abstract class OriginFeedViewHolder<T : IProfile>(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : BaseViewHolder<T>(view), IFeedViewHolder {

    override var trackingBus: TrackingBus<EqualRange<ProfileImageVO>>? = null

    override fun getCurrentImagePosition(): Int = 0
}

abstract class BaseFeedViewHolder<T : IProfile>(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : OriginFeedViewHolder<T>(view, viewPool) {

    internal val profileImageAdapter = ProfileImageAdapter()
    override var trackingBus: TrackingBus<EqualRange<ProfileImageVO>>? = null

    init {
        val snapHelper = PagerSnapHelper()
        itemView.rv_items.apply {
            adapter = profileImageAdapter.also { it.tabsObserver = itemView.tabs.adapterDataObserver }
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                .also { it.initialPrefetchItemCount = 2 }
            snapHelper.attachToRecyclerView(this)
            itemView.tabs.attachToRecyclerView(this, snapHelper)
            setHasFixedSize(true)
            setRecycledViewPool(viewPool)
            setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING)
            OverScrollDecoratorHelper.setUpOverScroll(this, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)

            setOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(rv, dx, dy)
                    (rv.layoutManager as? LinearLayoutManager)?.let {
                        val from = it.findFirstVisibleItemPosition()
                        val to = it.findLastVisibleItemPosition()
                        val items = profileImageAdapter.getItemsExposed(from = from, to = to)
                        Timber.v("Visible items [${items.size}] [$from, $to]: $items")
                        trackingBus?.postViewEvent(EqualRange(from = from, to = to, items = items))
                    }
                }
            })
        }
    }

    override fun bind(model: T) {
        profileImageAdapter.submitList(model.images.map { ProfileImageVO(profileId = model.id, image = it) })
    }

    override fun getCurrentImagePosition(): Int =
        (itemView.rv_items.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
}
