package com.ringoid.origin.messenger.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.origin.feed.adapter.FeedAdapter
import com.ringoid.origin.feed.adapter.ProfileViewHolder
import com.ringoid.origin.messenger.R

class MessengerFeedAdapter(imagesViewPool: RecyclerView.RecycledViewPool? = null) : FeedAdapter(imagesViewPool) {

    override fun getLayoutId(): Int = R.layout.rv_item_messenger_feed_profile

    override fun instantiateViewHolder(view: View): ProfileViewHolder {
        return super.instantiateViewHolder(view)
    }
}
