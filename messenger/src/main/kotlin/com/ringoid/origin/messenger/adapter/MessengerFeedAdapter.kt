package com.ringoid.origin.messenger.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.feed.Profile
import com.ringoid.origin.feed.adapter.FeedAdapter
import com.ringoid.origin.feed.adapter.FeedViewHolder
import com.ringoid.origin.messenger.R

class MessengerFeedAdapter(imagesViewPool: RecyclerView.RecycledViewPool? = null) : FeedAdapter(imagesViewPool) {

    var openChatListener: ((model: Profile, position: Int) -> Unit)? = null

    override fun getLayoutId(): Int = R.layout.rv_item_messenger_feed_profile

    override fun instantiateViewHolder(view: View): FeedViewHolder =
        super.instantiateViewHolder(view).also { vh ->
            vh.profileImageAdapter.isLikeButtonVisible = false
            vh.setOnClickListener(wrapOnItemClickListener(vh, openChatListener))
        }
}
