package com.ringoid.origin.feed.view.lmm.base

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.DomainUtil.BAD_ID
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.base.FeedViewHolderHideControls
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowControls
import com.ringoid.origin.feed.adapter.lmm.BaseLmmAdapter
import com.ringoid.origin.feed.view.FeedFragment
import com.ringoid.origin.feed.view.lmm.ILmmFragment
import com.ringoid.origin.messenger.ChatPayload
import com.ringoid.origin.messenger.view.ChatFragment
import com.ringoid.origin.messenger.view.IChatHost
import com.ringoid.origin.navigation.noConnection
import com.ringoid.origin.view.dialog.IDialogCallback
import com.ringoid.origin.view.main.IBaseMainActivity
import com.ringoid.utility.communicator
import kotlinx.android.synthetic.main.fragment_feed.*

abstract class BaseLmmFeedFragment<VM : BaseLmmFeedViewModel> : FeedFragment<VM, FeedItem>(), IChatHost, IDialogCallback  {

    abstract fun instantiateFeedAdapter(): BaseLmmAdapter

    override fun createFeedAdapter(): BaseLmmAdapter =
        instantiateFeedAdapter().apply {
            messageClickListener = { model: FeedItem, position: Int, positionOfImage: Int ->
                communicator(ILmmFragment::class.java)?.showTabs(isVisible = false)
                openChat(position = position, peerId = model.id, imageId = model.images[positionOfImage].id)
            }
        }

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
                if (it.position == 1) {
                    scrollToTopOfItemAtPosition(0)
                }

                when (tag) {
                    ChatFragment.TAG -> {
                        communicator(ILmmFragment::class.java)?.showTabs(isVisible = true)
                        vm.onChatClose(profileId = it.peerId, imageId = it.peerImageId)
                        // supply first message from user to FeedItem to change in on bind
                        it.firstUserMessage?.let { message -> feedAdapter.getModel(it.position).messages.add(message) }
                        getRecyclerView().post {
                            feedAdapter.notifyItemChanged(it.position, FeedViewHolderShowControls)
                        }
                    }
                }
                true  // no-op value
            }
    }

    protected fun openChat(position: Int, peerId: String, imageId: String = BAD_ID, tag: String = ChatFragment.TAG) {
        if (!connectionManager.isNetworkAvailable()) {
            noConnection(this)
            return
        }

        childFragmentManager.let {
            it.findFragmentByTag(tag)
                ?: run {
                    val payload = ChatPayload(
                        position = position,
                        peerId = peerId,
                        peerImageId = imageId
                    )
                    vm.onChatOpen(profileId = peerId, imageId = imageId)
                    scrollToTopOfItemAtPositionAndPost(position).post {
                        feedAdapter.notifyItemChanged(position, FeedViewHolderHideControls)
                    }
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
        /**
         * Parent's [Fragment.onActivityCreated] is called before this method on any child [Fragment],
         * so it's safe to access parent's [ViewModel] here, because it's already initialized.
         */
        communicator(ILmmFragment::class.java)?.accessViewModel()
            ?.let { parentVm -> viewLifecycleOwner.observe(parentVm.listScrolls, ::scrollListToPosition) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_items.addOnScrollListener(topScrollListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rv_items.removeOnScrollListener(topScrollListener)
    }

    // --------------------------------------------------------------------------------------------
    override fun onRefresh() {
        communicator(IBaseMainActivity::class.java)?.onRefreshFeed()
        vm.onRefresh()
    }
}
