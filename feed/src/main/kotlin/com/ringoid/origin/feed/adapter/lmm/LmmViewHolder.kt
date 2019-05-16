package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.*
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.utility.changeVisibility
import kotlinx.android.synthetic.main.rv_item_lmm_profile.view.*

open class LmmViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : BaseFeedViewHolder(view, viewPool) {

    init {
        itemView.tv_seen_status.changeVisibility(isVisible = BuildConfig.IS_STAGING)
    }

    override fun bind(model: FeedItemVO) {
        super.bind(model)
        setMessengerIcon(model)

        itemView.tv_seen_status.text = if (model.isNotSeen) "Not Seen" else "Seen"
    }

    override fun bind(model: FeedItemVO, payloads: List<Any>) {
        super.bind(model, payloads)
        setMessengerIcon(model)

        // scroll affected
        if (payloads.contains(FeedViewHolderHideChatBtnOnScroll)) {
//            itemView.ibtn_message.changeVisibility(isVisible = false)
        }
        if (payloads.contains(FeedViewHolderShowChatBtnOnScroll)) {
//            itemView.ibtn_message.changeVisibility(isVisible = true)
        }
    }

    // ------------------------------------------------------------------------
//    override fun hideControls() {
//        super.hideControls()
//        itemView.ibtn_message.changeVisibility(isVisible = false)
//    }
//
//    override fun showControls() {
//        super.showControls()
//        itemView.ibtn_message.changeVisibility(isVisible = true)
//    }

    // ------------------------------------------
    private fun setMessengerIcon(model: FeedItemVO) {
        var isVisible = false
        val iconResId = if (model.messages.isEmpty()) {
            isVisible = false
            R.drawable.ic_chat_bubble_outline_white
        } else {
            val peerMessagesCount = model.countOfPeerMessages()
            if (peerMessagesCount > 0) {
                if (peerMessagesCount == ChatInMemoryCache.getPeerMessagesCount(model.id)) {
                    isVisible = false
                    R.drawable.ic_messenger_outline_white
                } else {  // has unread messages from peer
                    isVisible = true
                    R.drawable.ic_messenger_fill_lgreen
                }
            } else {  // contains only current user's messages
                isVisible = false
                R.drawable.ic_chat_bubble_white
            }
        }
        itemView.ibtn_message.changeVisibility(isVisible = isVisible)
        itemView.ibtn_message.setImageSrcResource(resId = iconResId)
    }
}

class HeaderLmmViewHolder(view: View) : OriginFeedViewHolder(view) {

    override fun bind(model: FeedItemVO) {
        // no-op
    }
}

class FooterLmmViewHolder(view: View) : OriginFeedViewHolder(view) {

    override fun bind(model: FeedItemVO) {
        showControls()
    }

    override fun bind(model: FeedItemVO, payloads: List<Any>) {
        if (payloads.contains(FeedFooterViewHolderHideControls)) {
            hideControls()
        }
        if (payloads.contains(FeedFooterViewHolderShowControls)) {
            showControls()
        }
    }

    // ------------------------------------------
    private fun hideControls() {
//        itemView.iv_end_item.changeVisibility(isVisible = false)
    }

    private fun showControls() {
//        itemView.iv_end_item.changeVisibility(isVisible = true)
    }
}
