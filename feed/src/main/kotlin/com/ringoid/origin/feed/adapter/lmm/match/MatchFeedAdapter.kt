package com.ringoid.origin.feed.adapter.lmm.match

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.origin.feed.adapter.ProfileViewHolder
import com.ringoid.origin.feed.adapter.lmm.like.LikeFeedAdapter
import kotlinx.android.synthetic.main.rv_item_feed_profile.view.*

class MatchFeedAdapter(imagesViewPool: RecyclerView.RecycledViewPool? = null) : LikeFeedAdapter(imagesViewPool) {

    override fun instantiateViewHolder(view: View): ProfileViewHolder =
        super.instantiateViewHolder(view).also { vh ->
            vh.profileImageAdapter.isLikeButtonVisible = false
            (vh.itemView.ibtn_message.layoutParams as? ConstraintLayout.LayoutParams)
                ?.apply { horizontalBias = 0.28f }?.let { vh.itemView.ibtn_message.layoutParams = it }
        }
}
