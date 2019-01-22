package com.ringoid.origin.messenger.view

import android.os.Bundle
import android.view.View
import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.messenger.R

class ChatFragment : BaseFragment<ChatViewModel>() {

    override fun getVmClass(): Class<ChatViewModel> = ChatViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_chat

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
