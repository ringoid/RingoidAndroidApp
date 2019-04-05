package com.ringoid.origin.feed.view.lmm.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.exception.ThresholdExceededException
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.image.IImage
import com.ringoid.origin.AppRes
import com.ringoid.origin.feed.adapter.base.FeedViewHolderHideControls
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowControls
import com.ringoid.origin.feed.adapter.lmm.BaseLmmAdapter
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.feed.view.FeedFragment
import com.ringoid.origin.feed.view.lmm.ILmmFragment
import com.ringoid.origin.feed.view.lmm.SEEN_ALL_FEED
import com.ringoid.origin.messenger.ChatPayload
import com.ringoid.origin.messenger.view.ChatFragment
import com.ringoid.origin.messenger.view.IChatHost
import com.ringoid.origin.navigation.RequestCode
import com.ringoid.origin.navigation.navigate
import com.ringoid.origin.navigation.noConnection
import com.ringoid.origin.view.dialog.IDialogCallback
import com.ringoid.utility.communicator
import com.ringoid.utility.debugToast
import timber.log.Timber

abstract class BaseLmmFeedFragment<VM : BaseLmmFeedViewModel> : FeedFragment<VM>(), IChatHost, IDialogCallback  {

    abstract fun instantiateFeedAdapter(): BaseLmmAdapter

    override fun createFeedAdapter(): BaseLmmAdapter =
        instantiateFeedAdapter().apply {
            messageClickListener = { model: FeedItem, position: Int, positionOfImage: Int ->
                communicator(ILmmFragment::class.java)?.showTabs(isVisible = false)
                openChat(position = position, peerId = model.id, image = model.images[positionOfImage])
            }
        }

    protected abstract fun getSourceFeed(): String

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.DONE -> {
                newState.residual
                    .takeIf { it is SEEN_ALL_FEED }
                    ?.let { it as SEEN_ALL_FEED }
                    ?.let { onSeenAllFeed(it.sourceFeed) }
            }
        }
    }

    // --------------------------------------------------------------------------------------------
    override fun onBlockFromChat(tag: String, payload: ChatPayload) {
        vm.onBlock(profileId = payload.peerId, imageId = payload.peerImageId, sourceFeed = payload.sourceFeed, fromChat = true)
    }

    override fun onReportFromChat(tag: String, payload: ChatPayload, reasonNumber: Int) {
        vm.onReport(profileId = payload.peerId, imageId = payload.peerImageId, reasonNumber = reasonNumber, sourceFeed = payload.sourceFeed, fromChat = true)
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
                        it.firstUserMessage?.let {
                            message -> feedAdapter.getModel(it.position).messages.add(message)
                            vm.onFirstUserMessageSent(profileId = it.peerId)
                        }
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
                        peerImageUri = image?.uri,
                        sourceFeed = getSourceFeed())
                    vm.onChatOpen(profileId = peerId, imageId = image?.id ?: DomainUtil.BAD_ID)
                    scrollToTopOfItemAtPositionAndPost(position).post {
                        feedAdapter.notifyItemChanged(position, FeedViewHolderHideControls)
                    }
//                    ChatFragment.newInstance(peerId = peerId, payload = payload, tag = tag).show(it, tag)
                    navigate(this, path = "/chat?peerId=$peerId&payload=${payload.toJson()}&tag=$tag", rc = RequestCode.RC_CHAT)
                }
        }
    }

    // ------------------------------------------
    internal fun clearScreen(mode: Int) {
        vm.clearScreen(mode)
    }

    private fun onSeenAllFeed(sourceFeed: Int) =
        when (sourceFeed) {
            SEEN_ALL_FEED.FEED_LIKES -> communicator(ILmmFragment::class.java)?.showBadgeOnLikes(false)
            SEEN_ALL_FEED.FEED_MATCHES -> communicator(ILmmFragment::class.java)?.showBadgeOnMatches(false)
            SEEN_ALL_FEED.FEED_MESSENGER -> communicator(ILmmFragment::class.java)?.showBadgeOnMessenger(false)
            else -> { /* no-op */ }
        }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(viewLifecycleOwner) {
            observe(vm.feed) { feedAdapter.submitList(it.map { FeedItemVO(it) }) }
            observe(vm.oneShot) {
                it.getContentIfNotHandled()
                    ?.takeIf { it is ThresholdExceededException }
                    ?.let { activity?.debugToast("Repeat after delay exceeded time threshold") }
            }
        }
        /**
         * Parent's [Fragment.onActivityCreated] is called before this method on any child [Fragment],
         * so it's safe to access parent's [ViewModel] here, because it's already initialized.
         */
        communicator(ILmmFragment::class.java)?.accessViewModel()
            ?.let {
                viewLifecycleOwner.observe(it.listScrolls, ::scrollListToPosition)
                vm.applyCachedFeed(it.cachedLmm)
            }
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

                val tag = data.getStringExtra("tag")
                val payload = data.getParcelableExtra<ChatPayload>("payload")
                when (data.getStringExtra("action")) {
                    "block" -> onBlockFromChat(tag = tag, payload = payload)
                    "report" -> onReportFromChat(tag = tag, payload = payload, reasonNumber = data.getIntExtra("reason", 0))
                }
                onDialogDismiss(tag = tag, payload = payload)
            }
        }
    }
}
