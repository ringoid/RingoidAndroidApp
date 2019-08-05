package com.ringoid.origin.feed.adapter.explore

import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.touches
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.*
import com.ringoid.origin.feed.model.EmptyFeedItemVO
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.image.ImageRequest
import kotlinx.android.synthetic.main.rv_item_feed_profile_content.view.*

class ExploreFeedAdapter(imageLoader: ImageRequest) : BaseFeedAdapter(imageLoader, FeedItemVODiffCallback()) {

    var onLikeImageListener: ((model: ProfileImageVO, position: Int) -> Unit)? = null

    override fun getLayoutId(): Int = R.layout.rv_item_feed_profile

    override fun instantiateViewHolder(view: View): OriginFeedViewHolder =
        ExploreFeedViewHolder(view, viewPool = imagesViewPool, imageLoader = imageLoader)
            .also { vh ->
                with (vh.itemView.ibtn_like) {
                    clicks()
                        .compose(clickDebounce())
                        .subscribe { _ /** feedItemPosition */ ->
                            vh.adapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                                ?.let {
                                    if (onBeforeLikeListener?.invoke() != false) {
                                        val imagePosition = vh.getCurrentImagePosition()
                                        val image = vh.profileImageAdapter.getModel(imagePosition)
                                        onLikeImageListener?.invoke(image, imagePosition)
                                        notifyItemChanged(vh.adapterPosition, FeedItemViewHolderAnimateLike)
                                    }
                                }
                        }
                    touches().filter { it.action == MotionEvent.ACTION_DOWN }
                             .compose(clickDebounce())
                             .subscribe { onImageTouchListener?.invoke(it.rawX, it.rawY) }
                }
                vh.profileImageAdapter.itemDoubleClickListener = { model, position ->
                    onLikeImageListener?.invoke(model, position)
                }
            }

    override fun instantiateHeaderViewHolder(view: View) = HeaderFeedViewHolder(view)
    override fun instantiateFooterViewHolder(view: View) = FooterFeedViewHolder(view)

    // ------------------------------------------
    override fun getStubItem(): FeedItemVO = EmptyFeedItemVO

    override fun getFooterLayoutResId(): Int = R.layout.rv_item_feed_footer
}
