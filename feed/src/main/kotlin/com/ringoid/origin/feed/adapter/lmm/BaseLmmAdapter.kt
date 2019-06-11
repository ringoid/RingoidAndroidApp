package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.BaseFeedAdapter
import com.ringoid.origin.feed.adapter.base.FeedItemVODiffCallback
import com.ringoid.origin.feed.model.EmptyFeedItemVO
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.utility.image.ImageRequest

abstract class BaseLmmAdapter(imageLoader: ImageRequest, headerRows: Int = 1)
    : BaseFeedAdapter(imageLoader, FeedItemVODiffCallback(), headerRows = headerRows) {

    var messageClickListener: ((model: FeedItem, position: Int, positionOfImage: Int) -> Unit)? = null

    override fun getLayoutId(): Int = R.layout.rv_item_lmm_profile

    override fun instantiateViewHolder(view: View): LmmViewHolder = LmmViewHolder(view, viewPool = imagesViewPool, imageLoader = imageLoader)

    override fun instantiateHeaderViewHolder(view: View) = HeaderLmmViewHolder(view)
    override fun instantiateFooterViewHolder(view: View) = FooterLmmViewHolder(view)

    // ------------------------------------------
    override fun getStubItem(): FeedItemVO = EmptyFeedItemVO
    override fun getHeaderLayoutResId(): Int = R.layout.rv_item_feed_lmm_header
}
