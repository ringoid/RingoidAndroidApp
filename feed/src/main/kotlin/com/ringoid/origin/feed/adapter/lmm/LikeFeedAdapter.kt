package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class LikeFeedAdapter : BaseLmmAdapter() {

    override fun instantiateViewHolder(view: View): LmmViewHolder =
        LikeFeedViewHolder(view, viewPool = imagesViewPool).also { vh ->
            vh.profileImageAdapter.itemClickListener = { model, _ ->
                if (vh.adapterPosition != RecyclerView.NO_POSITION) {
                    getModel(vh.adapterPosition).likedImages[model.image.id] = model.isLiked
                }
            }
        }
}
