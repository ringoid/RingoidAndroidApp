package com.ringoid.origin.feed.view.lmm.base

import android.os.Bundle
import android.os.Parcelable
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.DomainUtil.BAD_ID
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.base.FeedViewHolderHideControls
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowControls
import com.ringoid.origin.feed.adapter.base.IFeedViewHolder
import com.ringoid.origin.feed.view.FeedFragment
import com.ringoid.origin.messenger.view.ChatFragment
import com.ringoid.origin.messenger.ChatPayload
import com.ringoid.origin.messenger.view.IChatHost
import com.ringoid.origin.view.dialog.IDialogCallback

abstract class BaseLmmFeedFragment<VM : BaseLmmFeedViewModel, VH>
    : FeedFragment<VM, FeedItem, VH>(), IChatHost, IDialogCallback
    where VH : BaseViewHolder<FeedItem>, VH : IFeedViewHolder {

    // --------------------------------------------------------------------------------------------
    override fun onBlockFromChat(payload: ChatPayload) {
        vm.onBlock(profileId = payload.peerId, imageId = payload.peerImageId, sourceFeed = "chat")
    }

    override fun onReportFromChat(payload: ChatPayload, reasonNumber: Int) {
        vm.onReport(profileId = payload.peerId, imageId = payload.peerImageId, reasonNumber = reasonNumber, sourceFeed = "chat")
    }

    override fun onDialogDismiss(tag: String, payload: Parcelable?) {
        (payload as? ChatPayload)
            ?.let {
                if (it.position == DomainUtil.BAD_POSITION) {
                    return
                }

                when (tag) {
                    ChatFragment.TAG -> {
                        vm.onChatClose(profileId = it.peerId, imageId = it.peerImageId)
                        feedAdapter.notifyItemChanged(it.position, FeedViewHolderShowControls)
                    }
                }
            }
    }

    protected fun openChat(position: Int, peerId: String, imageId: String = BAD_ID, tag: String = ChatFragment.TAG) {
        childFragmentManager.let {
            it.findFragmentByTag(tag)
                ?: run {
                    val payload = ChatPayload(
                        position = position,
                        peerId = peerId,
                        peerImageId = imageId
                    )
                    vm.onChatOpen(profileId = peerId, imageId = imageId)
                    scrollToTopOfItemAtPosition(position)
                    feedAdapter.notifyItemChanged(position, FeedViewHolderHideControls)
                    ChatFragment.newInstance(peerId = peerId, payload = payload, tag = tag).show(it, tag)
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
