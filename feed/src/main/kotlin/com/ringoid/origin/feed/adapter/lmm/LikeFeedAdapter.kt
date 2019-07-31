package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.image.ImageRequest
import kotlinx.android.synthetic.main.rv_item_feed_profile_content.view.*
import kotlinx.android.synthetic.main.rv_item_lmm_profile.view.*

class LikeFeedAdapter(imageLoader: ImageRequest) : BaseLmmAdapter(imageLoader) {

    var onLikeImageListener: ((model: ProfileImageVO, feedItemPosition: Int) -> Unit)? = null

    override fun instantiateViewHolder(view: View): LmmViewHolder =
        LikeFeedViewHolder(view, viewPool = imagesViewPool, imageLoader = imageLoader)
            .also { vh ->
                vh.itemView.ibtn_like.clicks().compose(clickDebounce())
                    .subscribe {
                        vh.adapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                            ?.let { feedItemPosition ->
                                val imagePosition = vh.getCurrentImagePosition()
                                val image = vh.profileImageAdapter.getModel(imagePosition)
                                onLikeImageListener?.invoke(image, feedItemPosition)
                            }
                    }
                vh.itemView.iv_message.changeVisibility(isVisible = false)
                vh.profileImageAdapter.itemDoubleClickListener = { model, position ->
                    wrapOnImageClickListenerByFeedItem(vh, onLikeImageListener)?.invoke(model, position)
                }
            }
}
