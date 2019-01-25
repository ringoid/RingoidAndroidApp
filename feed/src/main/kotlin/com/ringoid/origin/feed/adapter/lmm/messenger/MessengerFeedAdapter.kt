package com.ringoid.origin.feed.adapter.lmm.messenger

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.domain.model.feed.EmptyFeedItem
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.BaseFeedAdapter
import com.ringoid.origin.feed.adapter.base.FeedItemDiffCallback
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.rv_item_messenger_feed_profile.view.*

class MessengerFeedAdapter(imagesViewPool: RecyclerView.RecycledViewPool? = null)
    : BaseFeedAdapter<FeedItem, OriginFeedViewHolder<FeedItem>>(imagesViewPool, FeedItemDiffCallback()) {

    var messageClickListener: ((model: FeedItem, position: Int, positionOfImage: Int) -> Unit)? = null

    override fun getLayoutId(): Int = R.layout.rv_item_messenger_feed_profile

    override fun instantiateViewHolder(view: View): OriginFeedViewHolder<FeedItem> =
        MessengerViewHolder(view, viewPool = imagesViewPool).also { vh ->
            vh.profileImageAdapter.isLikeButtonVisible = false
            val wrapMessageClickListener: ((model: FeedItem, position: Int) -> Unit)? =
                { model: FeedItem, position: Int ->
                    messageClickListener?.invoke(model, position, vh.getCurrentImagePosition())
                }
            vh.itemView.ibtn_message.clicks().compose(clickDebounce())
                .subscribe { wrapOnItemClickListener(vh, wrapMessageClickListener).onClick(vh.itemView.ibtn_message) }
        }

    override fun instantiateHeaderViewHolder(view: View) = HeaderMessengerViewHolder(view)

    // ------------------------------------------
    override fun getHeaderItem(): FeedItem = EmptyFeedItem
}
