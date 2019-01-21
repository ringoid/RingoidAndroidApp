package com.ringoid.origin.feed.view.lmm.messenger

import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.feed.R

class MessengerFragment : BaseFragment<MessengerViewModel>() {

    companion object {
        fun newInstance(): MessengerFragment =
            MessengerFragment()
    }

    override fun getVmClass(): Class<MessengerViewModel> = MessengerViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_messenger

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
}
