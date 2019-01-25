package com.ringoid.origin.feed.adapter.base

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.model.feed.IProfile
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.view.common.visibility_tracker.TrackingBus
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.collection.EqualRange
import kotlinx.android.synthetic.main.rv_item_feed_profile_content.view.*

abstract class BaseFeedAdapter<T : IProfile, VH>(
    imagesViewPool: RecyclerView.RecycledViewPool? = null, diffCb: BaseDiffCallback<T>)
    : BaseListAdapter<T, VH>(diffCb) where VH : BaseViewHolder<T>, VH : IFeedViewHolder {

    var settingsClickListener: ((model: T, position: Int, positionOfImage: Int) -> Unit)? = null
    internal var trackingBus: TrackingBus<EqualRange<ProfileImageVO>>? = null

    protected var imagesViewPool: RecyclerView.RecycledViewPool? = null
        private set

    init {
        this.imagesViewPool = imagesViewPool ?: RecyclerView.RecycledViewPool()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        super.onCreateViewHolder(parent, viewType).also { vh ->
            if (viewType != VIEW_TYPE_NORMAL) {
                return@also
            }

            vh.trackingBus = this@BaseFeedAdapter.trackingBus
            val wrapSettingsClickListener: ((model: T, position: Int) -> Unit)? =
                { model: T, position: Int ->
                    settingsClickListener?.invoke(model, position, vh.getCurrentImagePosition())
                }
            vh.itemView.ibtn_settings.clicks().compose(clickDebounce())
                .subscribe { wrapOnItemClickListener(vh, wrapSettingsClickListener).onClick(vh.itemView.ibtn_settings) }
            vh.setOnClickListener(null)  // clicks on the whole feed's item is no-op
        }

    fun clear() {
        submitList(emptyList())
    }
}
