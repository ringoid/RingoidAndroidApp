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
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.rv_item_messenger_feed_profile.view.*

class MessengerFeedAdapter(imagesViewPool: RecyclerView.RecycledViewPool? = null)
    : BaseFeedAdapter<FeedItem, OriginFeedViewHolder<FeedItem>>(imagesViewPool, FeedItemDiffCallback(), headerRows = 1) {

    var messageClickListener: ((model: FeedItem, position: Int, positionOfImage: Int) -> Unit)? = null
    var onImageToOpenChatClickListener: ((model: ProfileImageVO, position: Int) -> Unit)? = null

    override fun getLayoutId(): Int = R.layout.rv_item_messenger_feed_profile

    override fun instantiateViewHolder(view: View): OriginFeedViewHolder<FeedItem> =
        MessengerViewHolder(view, viewPool = imagesViewPool).also { vh ->
            vh.profileImageAdapter.also { adapter ->
                adapter.isLikeEnabled = false  // hide like button on messenger feed items
                adapter.itemClickListener = wrapOnImageClickListener(vh, onImageToOpenChatClickListener)
            }
            val wrapMessageClickListener = wrapMessageClickListener(vh)
            vh.itemView.ibtn_message.clicks().compose(clickDebounce())
                .subscribe { wrapOnItemClickListener(vh, wrapMessageClickListener).onClick(vh.itemView.ibtn_message) }
        }

    override fun instantiateHeaderViewHolder(view: View) = HeaderMessengerViewHolder(view)

    // ------------------------------------------
    override fun getStubItem(): FeedItem = EmptyFeedItem

    // ------------------------------------------
    private fun wrapMessageClickListener(vh: MessengerViewHolder): ((model: FeedItem, position: Int) -> Unit)? =
        { model: FeedItem, position: Int ->
            messageClickListener?.invoke(model, position, vh.getCurrentImagePosition())
        }
}
