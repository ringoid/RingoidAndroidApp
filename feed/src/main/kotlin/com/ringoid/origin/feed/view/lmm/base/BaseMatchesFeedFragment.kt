package com.ringoid.origin.feed.view.lmm.base

import android.view.View
import com.ringoid.origin.AppRes
import com.ringoid.origin.feed.adapter.base.FeedViewHolderHideChatBtnOnScroll
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowChatBtnOnScroll

abstract class BaseMatchesFeedFragment<VM : BaseLmmFeedViewModel> : BaseLmmFeedFragment<VM>() {

    // --------------------------------------------------------------------------------------------
    override fun onRefresh() {
        super.onRefresh()
        chatBtnHide = false
        chatBtnShow = false
    }

    /* Scroll listeners */
    // --------------------------------------------------------------------------------------------
    protected val CHAT_BTN_BOTTOM = AppRes.FEED_ITEM_BIAS_BTN_TOP_OFFSET
    protected var chatBtnHide: Boolean = false
    protected var chatBtnShow: Boolean = false

    override fun processItemViewControlVisibility(position: Int, view: View) {
        super.processItemViewControlVisibility(position, view)
        if (Math.abs(BB_TOP - view.top) >= CHAT_BTN_BOTTOM) {
            chatBtnHide = false
            if (!chatBtnShow) {
                chatBtnShow = true
                feedAdapter.notifyItemChanged(position, FeedViewHolderShowChatBtnOnScroll)
            }
        } else {
            chatBtnShow = false
            if (!chatBtnHide) {
                chatBtnHide = true
                feedAdapter.notifyItemChanged(position, FeedViewHolderHideChatBtnOnScroll)
            }
        }
    }
}
