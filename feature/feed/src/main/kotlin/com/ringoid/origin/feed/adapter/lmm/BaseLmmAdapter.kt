package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.BaseFeedAdapter
import com.ringoid.origin.feed.adapter.base.FeedItemVODiffCallback
import com.ringoid.origin.feed.model.EmptyFeedItemVO
import com.ringoid.origin.feed.model.FeedItemVO

abstract class BaseLmmAdapter : BaseFeedAdapter(FeedItemVODiffCallback(), headerRows = 1) {

    var messageClickListener: ((model: FeedItem, position: Int, positionOfImage: Int) -> Unit)? = null

    override fun getLayoutId(): Int = R.layout.rv_item_lmm_profile

    override fun instantiateViewHolder(view: View): LmmViewHolder = LmmViewHolder(view, viewPool = imagesViewPool)

    override fun instantiateHeaderViewHolder(view: View) = HeaderLmmViewHolder(view)
    override fun instantiateFooterViewHolder(view: View) = FooterLmmViewHolder(view)

    // ------------------------------------------
    override fun getStubItem(): FeedItemVO = EmptyFeedItemVO
}
