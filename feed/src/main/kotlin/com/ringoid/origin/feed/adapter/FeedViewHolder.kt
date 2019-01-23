package com.ringoid.origin.feed.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.feed.Profile
import com.ringoid.origin.feed.adapter.base.BaseFeedViewHolder
import com.ringoid.origin.feed.adapter.base.IFeedViewHolder
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.view.common.visibility_tracker.TrackingBus
import com.ringoid.utility.collection.EqualRange

class FeedViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : BaseFeedViewHolder<Profile>(view, viewPool), IFeedViewHolder {

    internal var onLikeImageListener: ((model: ProfileImageVO, position: Int) -> Unit)? = null
        set(value) {
            field = value
            profileImageAdapter.itemClickListener = value
        }
}

class HeaderFeedViewHolder(view: View) : OriginFeedViewHolder<Profile>(view), IFeedViewHolder {

    override var trackingBus: TrackingBus<EqualRange<ProfileImageVO>>? = null

    override fun bind(model: Profile) {
        // no-op
    }

    override fun getCurrentImagePosition(): Int = 0
}
