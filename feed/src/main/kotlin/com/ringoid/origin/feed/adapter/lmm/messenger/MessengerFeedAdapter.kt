package com.ringoid.origin.feed.adapter.lmm.messenger

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.origin.feed.adapter.lmm.like.BaseLikeFeedAdapter
import com.ringoid.origin.feed.model.ProfileImageVO

class MessengerFeedAdapter(imagesViewPool: RecyclerView.RecycledViewPool? = null)
    : BaseLikeFeedAdapter<OriginFeedViewHolder<FeedItem>>(imagesViewPool) {

    var onImageToOpenChatClickListener: ((model: ProfileImageVO, position: Int) -> Unit)? = null

    override fun getLayoutId(): Int = R.layout.rv_item_messenger_feed_profile

    override fun instantiateViewHolder(view: View): OriginFeedViewHolder<FeedItem> =
        MessengerViewHolder(view, viewPool = imagesViewPool).also { vh ->
            vh.profileImageAdapter.also { adapter ->
                adapter.isLikeEnabled = false  // hide like button on messenger feed items
                adapter.itemClickListener = wrapOnImageClickListener(vh, onImageToOpenChatClickListener)
            }
        }

    override fun instantiateHeaderViewHolder(view: View) = HeaderMessengerViewHolder(view)
}
