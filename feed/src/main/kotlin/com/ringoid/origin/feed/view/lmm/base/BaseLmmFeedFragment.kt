package com.ringoid.origin.feed.view.lmm.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.image.IImage
import com.ringoid.origin.AppRes
import com.ringoid.origin.feed.adapter.base.FeedViewHolderHideControls
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowControls
import com.ringoid.origin.feed.adapter.lmm.BaseLmmAdapter
import com.ringoid.origin.feed.view.FeedFragment
import com.ringoid.origin.feed.view.lmm.ILmmFragment
import com.ringoid.origin.messenger.ChatPayload
import com.ringoid.origin.messenger.view.ChatFragment
import com.ringoid.origin.messenger.view.IChatHost
import com.ringoid.origin.navigation.RequestCode
import com.ringoid.origin.navigation.noConnection
import com.ringoid.origin.view.dialog.IDialogCallback
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.communicator
import com.ringoid.utility.isVisible
import com.ringoid.utility.linearLayoutManager
import kotlinx.android.synthetic.main.fragment_feed.*
import timber.log.Timber

abstract class BaseLmmFeedFragment<VM : BaseLmmFeedViewModel> : FeedFragment<VM, FeedItem>(), IChatHost, IDialogCallback  {

    abstract fun instantiateFeedAdapter(): BaseLmmAdapter

    override fun createFeedAdapter(): BaseLmmAdapter =
        instantiateFeedAdapter().apply {
            messageClickListener = { model: FeedItem, position: Int, positionOfImage: Int ->
                communicator(ILmmFragment::class.java)?.showTabs(isVisible = false)
                openChat(position = position, peerId = model.id, image = model.images[positionOfImage])
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
                scrollToTopOfItemAtPosition(it.position, offset = AppRes.BUTTON_HEIGHT)

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

    protected fun openChat(position: Int, peerId: String, image: IImage? = null, tag: String = ChatFragment.TAG) {
        if (!connectionManager.isNetworkAvailable()) {
            noConnection(this)
            return
        }

        communicator(ILmmFragment::class.java)?.showTabs(isVisible = false)
        childFragmentManager.let {
            it.findFragmentByTag(tag)
                ?: run {
                    val payload = ChatPayload(
                        position = position,
                        peerId = peerId,
                        peerImageId = image?.id ?: DomainUtil.BAD_ID,
                        peerImageUri = image?.uri
                    )
                    vm.onChatOpen(profileId = peerId, imageId = image?.id ?: DomainUtil.BAD_ID)
                    scrollToTopOfItemAtPositionAndPost(position).post {
                        feedAdapter.notifyItemChanged(position, FeedViewHolderHideControls)
                    }
                    ChatFragment.newInstance(peerId = peerId, payload = payload, tag = tag).show(it, tag)
//                    navigate(this, path = "/chat?peerId=$peerId&payload=${payload.toJson()}&tag=$tag", rc = RequestCode.RC_CHAT)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RequestCode.RC_CHAT -> {
                if (resultCode != Activity.RESULT_OK) {
                    return
                }

                if (data == null) {
                    val e = NullPointerException("No output from Chat dialog - this is an error!")
                    Timber.e(e); throw e
                }

                when (data.getStringExtra("action")) {
                    "block" -> onBlockFromChat(payload = data.getParcelableExtra("payload"))
                    "report" -> onReportFromChat(payload = data.getParcelableExtra("payload"), reasonNumber = data.getIntExtra("reason", 0))
                    else -> onDialogDismiss(tag = data.getStringExtra("tag"), payload = data.getParcelableExtra("payload"))
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_items.addOnScrollListener(topScrollListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rv_items.removeOnScrollListener(topScrollListener)
    }

    /* Scroll listeners */
    // --------------------------------------------------------------------------------------------
    private val topScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(rv, dx, dy)
            rv.linearLayoutManager()?.let {
                if (dy > 0) {  // scroll list down - to see new items
                    if (scroll_fab.isVisible()) {
                        scroll_fab.changeVisibility(isVisible = false)
                    }
                } else {  // scroll list up - to see previous items
                    val offset = rv.computeVerticalScrollOffset()
                    if (scroll_fab.isVisible()) {
                        if (offset <= 0) {
                            scroll_fab.changeVisibility(isVisible = false)
                        }
                    } else {
                        if (offset > 0) {
                            scroll_fab.changeVisibility(isVisible = true)
                        }
                    }
                }
            }
        }
    }
}
