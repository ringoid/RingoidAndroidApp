package com.ringoid.origin.feed.view.lmm.base

import android.os.Bundle
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.base.FeedViewHolderHideControls
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowControls
import com.ringoid.origin.feed.adapter.base.IFeedViewHolder
import com.ringoid.origin.feed.view.FeedFragment
import com.ringoid.origin.messenger.view.ChatFragment
import com.ringoid.origin.view.dialog.IDialogCallback

abstract class BaseLmmFeedFragment<VM : BaseLmmFeedViewModel, VH>
    : FeedFragment<VM, FeedItem, VH>(), IDialogCallback
    where VH : BaseViewHolder<FeedItem>, VH : IFeedViewHolder {

    // --------------------------------------------------------------------------------------------
    override fun onDialogDismiss(tag: String, position: Int) {
        if (position == DomainUtil.BAD_POSITION) {
            return
        }

        when (tag) {
            ChatFragment.TAG -> feedAdapter.notifyItemChanged(position, FeedViewHolderShowControls)
        }
    }

    protected fun openChat(peerId: String, position: Int, tag: String = ChatFragment.TAG) {
        childFragmentManager.let {
            it.findFragmentByTag(tag)
                ?: run {
                    feedAdapter.notifyItemChanged(position, FeedViewHolderHideControls)
                    ChatFragment.newInstance(peerId = peerId, position = position, tag = tag).showNow(it, tag)
                }
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
