package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.*
import com.ringoid.utility.changeVisibility
import kotlinx.android.synthetic.main.rv_item_lmm_footer.view.*
import kotlinx.android.synthetic.main.rv_item_lmm_profile.view.*

open class LmmViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : BaseFeedViewHolder<FeedItem>(view, viewPool) {

    init {
        // TODO: improve UX for fling
//        itemView.ibtn_message.setOnFlingListener {
//            val rv = itemView.rv_items
//            when (it) {
//                Direction.left -> {  // next page
//                    rv.smoothScrollToPosition(adapterPosition + 1)
//                }
//                Direction.right -> {  // previous page
//                    rv.smoothScrollToPosition(adapterPosition - 1)
//                }
//            }
//        }
    }

    override fun bind(model: FeedItem) {
        super.bind(model)
        setMessengerIcon(model)
    }

    override fun bind(model: FeedItem, payloads: List<Any>) {
        super.bind(model, payloads)
        setMessengerIcon(model)

        // scroll affected
        if (payloads.contains(FeedViewHolderHideChatBtnOnScroll)) {
            itemView.ibtn_message.changeVisibility(isVisible = false)
        }
        if (payloads.contains(FeedViewHolderShowChatBtnOnScroll)) {
            itemView.ibtn_message.changeVisibility(isVisible = true)
        }
    }

    // ------------------------------------------------------------------------
    override fun hideControls() {
        super.hideControls()
        itemView.ibtn_message.changeVisibility(isVisible = false)
    }

    override fun showControls() {
        super.showControls()
        itemView.ibtn_message.changeVisibility(isVisible = true)
    }

    // ------------------------------------------
    private fun setMessengerIcon(model: FeedItem) {
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
        itemView.ibtn_message.setImageSrcResource(resId = iconResId)
    }
}

class HeaderLmmViewHolder(view: View) : OriginFeedViewHolder<FeedItem>(view) {

    override fun bind(model: FeedItem) {
        // no-op
    }
}

class FooterLmmViewHolder(view: View) : OriginFeedViewHolder<FeedItem>(view) {

    override fun bind(model: FeedItem) {
        showControls()
    }

    override fun bind(model: FeedItem, payloads: List<Any>) {
        if (payloads.contains(FeedFooterViewHolderHideControls)) {
            hideControls()
        }
        if (payloads.contains(FeedFooterViewHolderShowControls)) {
            showControls()
        }
    }

    // ------------------------------------------
    private fun hideControls() {
        itemView.iv_end_item.changeVisibility(isVisible = false)
    }

    private fun showControls() {
        itemView.iv_end_item.changeVisibility(isVisible = true)
    }
}
