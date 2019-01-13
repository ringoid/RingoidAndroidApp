package com.ringoid.origin.feed.explore

import com.ringoid.base.view.ViewState
import com.ringoid.origin.R
import com.ringoid.origin.feed.FeedFragment
import com.ringoid.origin.view.dialog.Dialogs

class ExploreFragment : FeedFragment<ExploreViewModel>() {

    companion object {
        fun newInstance(): ExploreFragment = ExploreFragment()
    }

    override fun getVmClass(): Class<ExploreViewModel> = ExploreViewModel::class.java

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        fun onIdleState() {
            //
        }

        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.IDLE -> onIdleState()
            is ViewState.LOADING -> {}
            is ViewState.ERROR -> {
                // TODO: analyze: newState.e
                Dialogs.showTextDialog(activity, titleResId = R.string.error_common, description = "DL TEXT FROM URL")
                onIdleState()
            }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
}
