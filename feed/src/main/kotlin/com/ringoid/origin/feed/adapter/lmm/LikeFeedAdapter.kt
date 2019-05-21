package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.changeVisibility
import kotlinx.android.synthetic.main.rv_item_lmm_profile.view.*

class LikeFeedAdapter : BaseLmmAdapter() {

    var onLikeImageListener: ((model: ProfileImageVO, feedItemPosition: Int) -> Unit)? = null

    override fun instantiateViewHolder(view: View): LmmViewHolder =
        LikeFeedViewHolder(view, viewPool = imagesViewPool).also { vh ->
            vh.profileImageAdapter.itemClickListener = { model, position ->
                if (vh.adapterPosition != RecyclerView.NO_POSITION) {
                    getModel(vh.adapterPosition).likedImages[model.image.id] = model.isLiked
                    wrapOnImageClickListenerByFeedItem(vh, onLikeImageListener)?.invoke(model, position)
                }
            }
        }
}
