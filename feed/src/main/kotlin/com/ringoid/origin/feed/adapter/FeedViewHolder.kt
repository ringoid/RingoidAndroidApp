package com.ringoid.origin.feed.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.feed.Profile
import com.ringoid.origin.feed.adapter.base.BaseFeedViewHolder
import com.ringoid.origin.feed.model.ProfileImageVO

open class FeedViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : BaseFeedViewHolder<Profile>(view, viewPool), IFeedViewHolder {

    internal var onLikeImageListener: ((model: ProfileImageVO, position: Int) -> Unit)? = null
        set(value) {
            field = value
            profileImageAdapter.itemClickListener = value
        }
}
