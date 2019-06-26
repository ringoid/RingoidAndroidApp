package com.ringoid.origin.feed.view.lmm.messenger

import com.ringoid.base.view.ViewState
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.adapter.lmm.BaseLmmAdapter
import com.ringoid.origin.feed.adapter.lmm.MessengerFeedAdapter
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.lmm.ILmmFragment
import com.ringoid.origin.feed.view.lmm.base.BaseMatchesFeedFragment
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.main.LmmNavTab
import com.ringoid.utility.communicator
import com.ringoid.utility.image.ImageRequest

class MessagesFeedFragment : BaseMatchesFeedFragment<MessagesFeedViewModel>() {

    companion object {
        fun newInstance(): MessagesFeedFragment = MessagesFeedFragment()
    }

    override fun getVmClass(): Class<MessagesFeedViewModel> = MessagesFeedViewModel::class.java

    override fun instantiateFeedAdapter(): BaseLmmAdapter =
        MessengerFeedAdapter(ImageRequest(context!!)).apply {
            onImageToOpenChatClickListener = { model: ProfileImageVO, feedItemPosition: Int ->
                openChat(position = feedItemPosition, peerId = model.profileId, image = model.image)
            }
        }

    override fun getEmptyStateInput(mode: Int): EmptyFragment.Companion.Input? =
        when (mode) {
            ViewState.CLEAR.MODE_EMPTY_DATA -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_messages_empty_no_data)
            ViewState.CLEAR.MODE_NEED_REFRESH -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.common_pull_to_refresh)
            else -> null
        }

    override fun getSourceFeed(): LmmNavTab = LmmNavTab.MESSAGES

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.DONE -> {
                when (newState.residual) {
                    is PUSH_NEW_MESSAGES -> communicator(ILmmFragment::class.java)?.showBadgeOnMessenger(isVisible = true)
                }
            }
        }
    }
}
