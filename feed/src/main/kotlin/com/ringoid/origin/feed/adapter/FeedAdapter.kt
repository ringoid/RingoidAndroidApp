package com.ringoid.origin.feed.adapter

import android.view.View
import com.ringoid.domain.model.debug.VerticalDebugBlob
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.*
import com.ringoid.origin.feed.model.EmptyFeedItemVO
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.randomString
import leakcanary.LeakSentry

class FeedAdapter : BaseFeedAdapter(FeedItemVODiffCallback()) {

    var onLikeImageListener: ((model: ProfileImageVO, position: Int) -> Unit)? = null

    override fun getLayoutId(): Int = R.layout.rv_item_feed_profile

    override fun instantiateViewHolder(view: View): OriginFeedViewHolder =
        FeedViewHolder(view, viewPool = imagesViewPool).also { vh ->
            vh.profileImageAdapter.itemClickListener = { model, position ->
                getModel(vh.adapterPosition).likedImages[model.image.id] = model.isLiked
                onLikeImageListener?.invoke(model, position)
            }
            vh.debugBlob = VerticalDebugBlob(tag = "${vh.adapterPosition}_${randomString(4)}")
        }

    override fun instantiateHeaderViewHolder(view: View) = HeaderFeedViewHolder(view)
    override fun instantiateFooterViewHolder(view: View) = FooterFeedViewHolder(view)

    override fun onViewRecycled(holder: OriginFeedViewHolder) {
        super.onViewRecycled(holder)
        holder.takeIf { it is FeedViewHolder }
              ?.let { it as FeedViewHolder }
              ?.let { LeakSentry.refWatcher.watch(it.debugBlob, it.debugBlob.tag) }
    }

    // ------------------------------------------
    override fun getStubItem(): FeedItemVO = EmptyFeedItemVO

    override fun getFooterLayoutResId(): Int = R.layout.rv_item_feed_footer
}
