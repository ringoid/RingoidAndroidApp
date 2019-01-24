package com.ringoid.origin.feed.view.lmm.base

import android.os.Bundle
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.base.IFeedViewHolder
import com.ringoid.origin.feed.view.FeedFragment
import com.ringoid.origin.messenger.view.ChatFragment
import com.ringoid.origin.view.dialog.IDialogCallback

abstract class BaseLmmFeedFragment<VM : BaseLmmFeedViewModel, VH>
    : FeedFragment<VM, FeedItem, VH>(), IDialogCallback
    where VH : BaseViewHolder<FeedItem>, VH : IFeedViewHolder {

    // --------------------------------------------------------------------------------------------
    override fun onDialogDismiss(tag: String) {
        when (tag) {
            ChatFragment.TAG -> {}  // TODO: restore feed item controls visibility
        }
    }

    protected fun openChat(peerId: String, tag: String = ChatFragment.TAG) {
        childFragmentManager.let {
            it.findFragmentByTag(tag)
                ?: ChatFragment.newInstance(peerId = peerId, tag = tag).showNow(it, tag)
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewLifecycleOwner.observe(vm.feed, feedAdapter::submitList)
        vm.clearScreen(mode = ViewState.CLEAR.MODE_NEED_REFRESH)
    }
}
