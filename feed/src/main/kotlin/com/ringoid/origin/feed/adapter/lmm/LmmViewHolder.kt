package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.memory.ChatInMemoryCache
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
        showUnreadIcon(model)

        itemView.tv_seen_status.text = if (model.isNotSeen) "Not Seen" else "Seen"
    }

    override fun bind(model: FeedItemVO, payloads: List<Any>) {
        super.bind(model, payloads)
        showUnreadIcon(model)

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
    private fun showUnreadIcon(model: FeedItemVO) {
        val isVisible = if (!model.messages.isEmpty()) {
            val peerMessagesCount = model.countOfPeerMessages()
            if (peerMessagesCount > 0) {
                peerMessagesCount != ChatInMemoryCache.getPeerMessagesCount(model.id)
            } else {
                false
            }
        } else false
        itemView.iv_message.alpha = if (isVisible) 1f else 0f
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
