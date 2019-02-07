package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import com.ringoid.origin.feed.adapter.MessengerViewHolder

class MessengerFeedAdapter : MatchFeedAdapter() {

    override fun instantiateViewHolder(view: View): LmmViewHolder =
        MessengerViewHolder(view, viewPool = imagesViewPool)
}
