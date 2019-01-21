package com.ringoid.origin.feed.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.model.feed.Profile
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.view.common.visibility_tracker.TrackingBus
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.collection.EqualRange
import kotlinx.android.synthetic.main.rv_item_feed_profile_content.view.*

open class FeedAdapter(private var imagesViewPool: RecyclerView.RecycledViewPool? = null)
    : BaseListAdapter<Profile, FeedViewHolder>(ProfileDiffCallback()) {

    companion object {
        const val VIEW_TYPE_NORMAL = 0
        const val VIEW_TYPE_LOADING = 1
        const val VIEW_TYPE_FEED_END = 2
    }

    var onLikeImageListener: ((model: ProfileImageVO, position: Int) -> Unit)? = null
    var settingsClickListener: ((model: Profile, position: Int, positionOfImage: Int) -> Unit)? = null
    internal var trackingBus: TrackingBus<EqualRange<ProfileImageVO>>? = null

    init {
        imagesViewPool = imagesViewPool ?: RecyclerView.RecycledViewPool()
    }

    override fun getLayoutId(): Int = R.layout.rv_item_feed_profile

    override fun instantiateViewHolder(view: View): FeedViewHolder =
        FeedViewHolder(view, viewPool = imagesViewPool).apply {
            onLikeImageListener = this@FeedAdapter.onLikeImageListener
            trackingBus = this@FeedAdapter.trackingBus
            val wrapSettingsClickListener: ((model: Profile, position: Int) -> Unit)? =
                { model: Profile, position: Int ->
                    settingsClickListener?.invoke(model, position, getCurrentImagePosition())
                }
            itemView.ibtn_settings.clicks().compose(clickDebounce())
                .subscribe { wrapOnItemClickListener(this, wrapSettingsClickListener).onClick(itemView.ibtn_settings) }
            setOnClickListener(null)  // clicks on the whole feed's item is no-op
        }

    fun submit(feed: Feed) {
        submitList(feed.profiles)
    }

    fun clear() {
        submitList(emptyList())
    }
}

// ------------------------------------------------------------------------------------------------
class ProfileDiffCallback : BaseDiffCallback<Profile>() {

    override fun areItemsTheSame(oldItem: Profile, newItem: Profile): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Profile, newItem: Profile): Boolean =
        oldItem == newItem  // as 'data class'
}
