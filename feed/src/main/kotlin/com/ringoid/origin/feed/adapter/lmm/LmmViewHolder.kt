package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.BaseFeedViewHolder
import com.ringoid.origin.feed.adapter.base.FeedViewHolderHideChatBtnOnScroll
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowChatBtnOnScroll
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.image.ImageRequest
import kotlinx.android.synthetic.main.rv_item_lmm_profile.view.*

open class LmmViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null, imageLoader: ImageRequest)
    : BaseFeedViewHolder(view, viewPool, imageLoader) {

    init {
        itemView.tv_seen_status.changeVisibility(isVisible = BuildConfig.IS_STAGING)
    }

    override fun bind(model: FeedItemVO) {
        super.bind(model)
        setMessengerIcon(model)  // apply updates, if any

        itemView.tv_seen_status.text = if (model.isNotSeen) "Not Seen" else "Seen"
    }

    override fun bind(model: FeedItemVO, payloads: List<Any>) {
        super.bind(model, payloads)
        setMessengerIcon(model)  // apply updates, if any

        // scroll affected
        if (payloads.contains(FeedViewHolderHideChatBtnOnScroll)) {
            itemView.iv_message.changeVisibility(isVisible = false)
        }
        if (payloads.contains(FeedViewHolderShowChatBtnOnScroll)) {
            itemView.iv_message.changeVisibility(isVisible = true)
        }
    }

    // ------------------------------------------------------------------------
    override fun hideControls() {
        super.hideControls()
        itemView.iv_message.changeVisibility(isVisible = false)
    }

    override fun showControls() {
        super.showControls()
        itemView.iv_message.changeVisibility(isVisible = true)
    }

    // ------------------------------------------
    private fun setMessengerIcon(model: FeedItemVO) {
        val iconResId = if (model.messages.isEmpty()) {
            R.drawable.ic_chat_bubble_outline_white
        } else {
            val peerMessagesCount = model.countOfPeerMessages()
            if (peerMessagesCount > 0) {
                if (peerMessagesCount == ChatInMemoryCache.getPeerMessagesCount(model.id)) {
                        R.drawable.ic_messenger_outline_white
                    } else {  // has unread messages from peer
                        R.drawable.ic_messenger_fill_lgreen
                    }
            } else {  // contains only current user's messages
                R.drawable.ic_chat_bubble_white
            }
        }
        itemView.iv_message.setImageResource(iconResId)
    }
}

class HeaderLmmViewHolder(view: View) : OriginFeedViewHolder(view) {

    override fun bind(model: FeedItemVO) {
        // no-op
    }
}

class FooterLmmViewHolder(view: View) : OriginFeedViewHolder(view) {

    override fun bind(model: FeedItemVO) {
        // no-op
    }
}
