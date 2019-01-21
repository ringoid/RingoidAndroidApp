package com.ringoid.origin.messenger.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.base.BaseFeedAdapter
import com.ringoid.origin.feed.adapter.base.FeedItemDiffCallback
import com.ringoid.origin.messenger.R

class MessengerFeedAdapter(imagesViewPool: RecyclerView.RecycledViewPool? = null)
    : BaseFeedAdapter<FeedItem, MessengerViewHolder>(imagesViewPool, FeedItemDiffCallback()) {

    var openChatListener: ((model: FeedItem, position: Int) -> Unit)? = null

    override fun getLayoutId(): Int = R.layout.rv_item_messenger_feed_profile

    override fun instantiateViewHolder(view: View): MessengerViewHolder =
        MessengerViewHolder(view, viewPool = imagesViewPool).also { vh ->
            vh.profileImageAdapter.isLikeButtonVisible = false
            vh.setOnClickListener(wrapOnItemClickListener(vh, openChatListener))
        }
}
