package com.ringoid.origin.view.messenger

import com.ringoid.base.view.BaseFragment

class ChatFragment : BaseFragment<ChatViewModel>() {

    override fun getVmClass(): Class<ChatViewModel> = ChatViewModel::class.java

    override fun getLayoutId(): Int = 0
}
