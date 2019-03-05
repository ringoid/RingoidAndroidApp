package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.BaseFeedAdapter
import com.ringoid.origin.feed.adapter.base.FeedItemVODiffCallback
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.origin.feed.model.EmptyFeedItemVO
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.rv_item_lmm_profile.view.*

abstract class BaseLmmAdapter(headerRows: Int = 1) : BaseFeedAdapter(FeedItemVODiffCallback(), headerRows = headerRows) {

    var messageClickListener: ((model: FeedItem, position: Int, positionOfImage: Int) -> Unit)? = null

    override fun getLayoutId(): Int = R.layout.rv_item_lmm_profile

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OriginFeedViewHolder {
        val viewHolder = super.onCreateViewHolder(parent, viewType)
        return viewHolder  // perform additional initialization only for VIEW_TYPE_NORMAL view holders
            .takeIf { viewType == VIEW_TYPE_NORMAL }
            ?.also { vh ->
                val wrapMessageClickListener = wrapMessageClickListener(vh)
                vh.itemView.ibtn_message.clicks().compose(clickDebounce())
                    .subscribe { wrapOnItemClickListener(vh, wrapMessageClickListener).onClick(vh.itemView.ibtn_message) }
            } ?: viewHolder  // don't apply additional initializations on non-VIEW_TYPE_NORMAL view holders
    }

    override fun instantiateViewHolder(view: View): LmmViewHolder = LmmViewHolder(view, viewPool = imagesViewPool)

    override fun instantiateHeaderViewHolder(view: View) = HeaderLmmViewHolder(view)
    override fun instantiateFooterViewHolder(view: View) = FooterLmmViewHolder(view)

    // ------------------------------------------
    override fun getStubItem(): FeedItemVO = EmptyFeedItemVO
    override fun getHeaderLayoutResId(): Int = R.layout.rv_item_feed_lmm_header

    // ------------------------------------------
    private fun wrapMessageClickListener(vh: OriginFeedViewHolder): ((model: FeedItemVO, position: Int) -> Unit)? =
        { model: FeedItemVO, position: Int ->
            messageClickListener?.invoke(model.feedItem(), position, vh.getCurrentImagePosition())
        }
}
