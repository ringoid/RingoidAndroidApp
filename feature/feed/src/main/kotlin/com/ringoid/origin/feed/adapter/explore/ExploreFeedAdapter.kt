package com.ringoid.origin.feed.adapter.explore

import android.view.View
import android.widget.TextView
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.origin.feed.OriginR_id
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.BaseFeedAdapter
import com.ringoid.origin.feed.adapter.base.FeedItemVODiffCallback
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.origin.feed.model.EmptyFeedItemVO
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.utility.clickDebounce

class ExploreFeedAdapter : BaseFeedAdapter(FeedItemVODiffCallback()) {

    var onErrorLabelClickListener: (() -> Unit)?= null
    var onFooterLabelClickListener: (() -> Unit)?= null

    override fun getLayoutId(): Int = R.layout.rv_item_feed_profile

    override fun instantiateViewHolder(view: View): OriginFeedViewHolder =
        ExploreFeedViewHolder(view, viewPool = imagesViewPool)
            .also { vh ->
                vh.profileImageAdapter.itemDoubleClickListener = { model, position ->
                    onLikeImageListener?.invoke(model, position)
                }
            }

    override fun instantiateHeaderViewHolder(view: View) = HeaderFeedViewHolder(view)
    override fun instantiateFooterViewHolder(view: View) =
        FooterFeedViewHolder(view).also { vh ->
            vh.itemView.findViewById<TextView>(OriginR_id.tv_end_item)?.let {
                it.clicks().compose(clickDebounce()).subscribe {
                    onFooterLabelClickListener?.invoke()
                }
            }
        }

    override fun instantiateErrorViewHolder(view: View): OriginFeedViewHolder =
        ErrorFeedViewHolder(view).also { vh ->
            vh.itemView.findViewById<TextView>(OriginR_id.tv_error_end_item)?.let {
                it.clicks().compose(clickDebounce()).subscribe {
                    onErrorLabelClickListener?.invoke()
                }
            }
        }

    // ------------------------------------------
    override fun getStubItem(): FeedItemVO = EmptyFeedItemVO

    override fun getFooterLayoutResId(): Int = R.layout.rv_item_feed_footer
}
