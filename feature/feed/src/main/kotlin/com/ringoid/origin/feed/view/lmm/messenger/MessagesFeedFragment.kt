package com.ringoid.origin.feed.view.lmm.messenger

import com.ringoid.base.view.ViewState
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.adapter.lmm.BaseLmmAdapter
import com.ringoid.origin.feed.adapter.lmm.MessagesFeedAdapter
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.lmm.base.BaseMatchesFeedFragment
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.main.LmmNavTab

@Deprecated("LMM -> LC")
class MessagesFeedFragment : BaseMatchesFeedFragment<MessagesFeedViewModel>() {

    companion object {
        fun newInstance(): MessagesFeedFragment = MessagesFeedFragment()
    }

    override fun getVmClass(): Class<MessagesFeedViewModel> = MessagesFeedViewModel::class.java

    override fun instantiateFeedAdapter(): BaseLmmAdapter =
        MessagesFeedAdapter().apply {
            onImageToOpenChatClickListener = { model: ProfileImageVO, feedItemPosition: Int ->
                openChat(position = feedItemPosition, peerId = model.profileId, image = model.image)
            }
        }

    override fun getEmptyStateInput(mode: Int): EmptyFragment.Companion.Input? =
        when (mode) {
            ViewState.CLEAR.MODE_EMPTY_DATA -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_messages_empty_no_data)
            ViewState.CLEAR.MODE_NEED_REFRESH -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.common_pull_to_refresh)
            ViewState.CLEAR.MODE_CHANGE_FILTERS -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_empty_no_data_filters)
            else -> null
        }

    override fun getSourceFeed(): LmmNavTab = LmmNavTab.MESSAGES

    override fun getToolbarTitleResId(): Int = OriginR_string.feed_messages_title

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
//            is ViewState.DONE -> {
//                when (newState.residual) {
//                    is PUSH_NEW_MESSAGES_TOTAL -> communicator(ILmmFragment::class.java)?.showBadgeOnMessenger(isVisible = true)
//                }
//            }
        }
    }
}
