package com.ringoid.origin.messenger.view

import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.R

class ChatFragment : BaseFragment<ChatViewModel>() {

    companion object {
        fun newInstance(): ChatFragment = ChatFragment()
    }

    override fun getVmClass(): Class<ChatViewModel> = ChatViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_chat

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
}
