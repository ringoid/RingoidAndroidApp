package com.ringoid.origin.feed.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.model.feed.Profile
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.model.ProfileImageVO

open class FeedAdapter(imagesViewPool: RecyclerView.RecycledViewPool? = null)
    : BaseFeedAdapter<Profile, FeedViewHolder>(imagesViewPool, ProfileDiffCallback()) {

    var onLikeImageListener: ((model: ProfileImageVO, position: Int) -> Unit)? = null

    override fun getLayoutId(): Int = R.layout.rv_item_feed_profile

    override fun instantiateViewHolder(view: View): FeedViewHolder =
        FeedViewHolder(view, viewPool = imagesViewPool).apply {
            onLikeImageListener = this@FeedAdapter.onLikeImageListener
        }

    fun submit(feed: Feed) {
        submitList(feed.profiles)
    }
}

// ------------------------------------------------------------------------------------------------
class ProfileDiffCallback : BaseDiffCallback<Profile>() {

    override fun areItemsTheSame(oldItem: Profile, newItem: Profile): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Profile, newItem: Profile): Boolean =
        oldItem == newItem  // as 'data class'
}
