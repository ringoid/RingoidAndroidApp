package com.ringoid.origin.feed.adapter.lmm.match

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.origin.feed.adapter.ProfileViewHolder
import com.ringoid.origin.feed.adapter.lmm.like.LikeFeedAdapter

class MatchFeedAdapter(imagesViewPool: RecyclerView.RecycledViewPool? = null) : LikeFeedAdapter(imagesViewPool) {

    override fun instantiateViewHolder(view: View): ProfileViewHolder =
        super.instantiateViewHolder(view).also { vh ->
            vh.profileImageAdapter.isLikeButtonVisible = false
        }
}
