package com.ringoid.origin.messenger.view

import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.BaseDialogFragment
import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.messenger.R
import com.ringoid.origin.messenger.R.id.ibtn_chat_close
import com.ringoid.origin.view.main.IMainActivity
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.communicator
import kotlinx.android.synthetic.main.fragment_chat.*

class ChatFragment : BaseDialogFragment<ChatViewModel>() {

    companion object {
        private const val BUNDLE_KEY_PEER_ID = "bundle_key_peer_id"

        fun newInstance(peerId: String): ChatFragment =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(BUNDLE_KEY_PEER_ID, peerId)
                }
            }
    }

    override fun getVmClass(): Class<ChatViewModel> = ChatViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_chat

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ibtn_chat_close.clicks().compose(clickDebounce()).subscribe {
            communicator(IMainActivity::class.java)?.popScreen()
        }
    }
}
