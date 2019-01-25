package com.ringoid.origin.feed.adapter.lmm.like

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.domain.model.feed.EmptyFeedItem
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.BaseFeedAdapter
import com.ringoid.origin.feed.adapter.base.FeedItemDiffCallback
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.rv_item_lmm_profile.view.*

abstract class BaseLikeFeedAdapter<VH : OriginFeedViewHolder<FeedItem>>(imagesViewPool: RecyclerView.RecycledViewPool? = null)
    : BaseFeedAdapter<FeedItem, VH>(imagesViewPool, FeedItemDiffCallback()) {

    var messageClickListener: ((model: FeedItem, position: Int, positionOfImage: Int) -> Unit)? = null

    override fun getLayoutId(): Int = R.layout.rv_item_lmm_profile

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        super.onCreateViewHolder(parent, viewType)
            .also { vh ->
                if (viewType != VIEW_TYPE_NORMAL) {
                    return@also
                }

                val wrapMessageClickListener: ((model: FeedItem, position: Int) -> Unit)? =
                    { model: FeedItem, position: Int ->
                        messageClickListener?.invoke(model, position, vh.getCurrentImagePosition())
                    }
                vh.itemView.ibtn_message.apply {
                    // TODO: show message button only after like tap
                    changeVisibility(isVisible = true)
                    clicks().compose(clickDebounce())
                        .subscribe { wrapOnItemClickListener(vh, wrapMessageClickListener).onClick(vh.itemView.ibtn_message) }
                }
            }

    // ------------------------------------------
    override fun getHeaderItem(): FeedItem = EmptyFeedItem
    override fun getHeaderLayoutResId(): Int = R.layout.rv_item_feed_lmm_header
}
