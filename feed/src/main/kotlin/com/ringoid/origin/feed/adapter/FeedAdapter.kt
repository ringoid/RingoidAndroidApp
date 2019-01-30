package com.ringoid.origin.feed.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.feed.EmptyProfile
import com.ringoid.domain.model.feed.Profile
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.BaseFeedAdapter
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.origin.feed.adapter.base.ProfileDiffCallback
import com.ringoid.origin.feed.model.ProfileImageVO

class FeedAdapter(imagesViewPool: RecyclerView.RecycledViewPool? = null)
    : BaseFeedAdapter<Profile, OriginFeedViewHolder<Profile>>(imagesViewPool, ProfileDiffCallback()) {

    var onLikeImageListener: ((model: ProfileImageVO, position: Int) -> Unit)? = null

    override fun getLayoutId(): Int = R.layout.rv_item_feed_profile

    override fun instantiateViewHolder(view: View): OriginFeedViewHolder<Profile> =
        FeedViewHolder(view, viewPool = imagesViewPool).also { vh ->
            vh.onLikeImageListener = onLikeImageListener
        }

    override fun instantiateHeaderViewHolder(view: View) = HeaderFeedViewHolder(view)

    // ------------------------------------------
    override fun getStubItem(): Profile = EmptyProfile

    override fun getFooterLayoutResId(): Int = R.layout.rv_item_feed_footer
}
