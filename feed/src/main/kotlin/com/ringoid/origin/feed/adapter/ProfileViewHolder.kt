package com.ringoid.origin.feed.adapter

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.model.feed.Profile
import com.ringoid.origin.feed.model.ProfileImageVO
import kotlinx.android.synthetic.main.rv_item_feed_profile.view.*
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper

class ProfileViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : BaseViewHolder<Profile>(view) {

    internal var onLikeImageListener: ((model: ProfileImageVO, position: Int) -> Unit)? = null
        set(value) {
            field = value
            profileImageAdapter.itemClickListener = value
        }

    internal val profileImageAdapter = ProfileImageAdapter().apply { itemClickListener = onLikeImageListener }

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
        }
    }

    override fun bind(model: Profile) {
        profileImageAdapter.submitList(model.images.map { ProfileImageVO(profileId = model.id, image = it) })
    }

    internal fun getCurrentImagePosition(): Int =
        (itemView.rv_items.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
}
