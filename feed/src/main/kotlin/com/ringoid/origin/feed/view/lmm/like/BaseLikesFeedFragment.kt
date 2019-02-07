package com.ringoid.origin.feed.view.lmm.like

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.ringoid.base.observe
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.lmm.BaseLmmAdapter
import com.ringoid.origin.feed.view.lmm.ILmmFragment
import com.ringoid.origin.feed.view.lmm.base.BaseLmmFeedFragment
import com.ringoid.origin.feed.view.lmm.base.BaseLmmFeedViewModel
import com.ringoid.origin.messenger.ChatPayload
import com.ringoid.origin.messenger.view.ChatFragment
import com.ringoid.utility.communicator

abstract class BaseLikesFeedFragment<VM : BaseLmmFeedViewModel> : BaseLmmFeedFragment<VM>() {

    abstract fun instantiateFeedAdapter(): BaseLmmAdapter

    override fun createFeedAdapter(): BaseLmmAdapter =
        instantiateFeedAdapter().apply {
            messageClickListener = { model: FeedItem, position: Int, positionOfImage: Int ->
                communicator(ILmmFragment::class.java)?.showTabs(isVisible = false)
                openChat(position = position, peerId = model.id, imageId = model.images[positionOfImage].id)
            }
        }

    // --------------------------------------------------------------------------------------------
    override fun onDialogDismiss(tag: String, payload: Parcelable?) {
        super.onDialogDismiss(tag, payload)
        when (tag) {
            ChatFragment.TAG -> communicator(ILmmFragment::class.java)?.showTabs(isVisible = true)
        }
        (payload as? ChatPayload)?.let {
            if (it.position == 1) {
                scrollToTopOfItemAtPosition(0)
            }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /**
         * Parent's [Fragment.onActivityCreated] is called before this method on any child [Fragment],
         * so it's safe to access parent's [ViewModel] here, because it's already initialized.
         */
        communicator(ILmmFragment::class.java)?.accessViewModel()
            ?.let { parentVm -> viewLifecycleOwner.observe(parentVm.listScrolls, ::scrollListToPosition) }
    }
}
