package com.ringoid.origin.feed.adapter.lmm.like

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class LikeFeedAdapter(imagesViewPool: RecyclerView.RecycledViewPool? = null)
    : BaseLikeFeedAdapter<LikeFeedViewHolder>(imagesViewPool) {

    override fun instantiateViewHolder(view: View): LikeFeedViewHolder = LikeFeedViewHolder(view)
}
