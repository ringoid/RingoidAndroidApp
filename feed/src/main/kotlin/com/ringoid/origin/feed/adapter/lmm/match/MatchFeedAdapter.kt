package com.ringoid.origin.feed.adapter.lmm.match

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.origin.feed.adapter.ProfileViewHolder
import com.ringoid.origin.feed.adapter.lmm.like.LikeFeedAdapter
import com.ringoid.utility.changeVisibility
import kotlinx.android.synthetic.main.rv_item_profile_image.view.*

class MatchFeedAdapter(imagesViewPool: RecyclerView.RecycledViewPool? = null) : LikeFeedAdapter(imagesViewPool) {

    override fun instantiateViewHolder(view: View): ProfileViewHolder =
        super.instantiateViewHolder(view).apply {
            itemView.ibtn_like.changeVisibility(isVisible = false, soft = true)
        }
}
