package com.ringoid.origin.messenger.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.BaseDialogFragment
import com.ringoid.origin.messenger.R
import com.ringoid.origin.view.main.IMainActivity
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.communicator
import com.ringoid.utility.showKeyboard
import kotlinx.android.synthetic.main.fragment_chat.*

class ChatFragment : BaseDialogFragment<ChatViewModel>() {

    companion object {
        const val TAG = "ChatFragment_tag"

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
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        Dialog(activity!!, R.style.AppDialog_FullScreen_Transparent)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = super.onCreateView(inflater, container, savedInstanceState)
        dialog?.window?.apply {
            showKeyboard()
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        return rootView//?.apply { setOnClickListener { dismiss() } }
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        et_message.apply {
            requestFocus()
//            setOnKeyPreImeListener { keyCode, event ->
//                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
//                    dismiss()
//                }
//                false
//            }
        }
        ibtn_chat_close.clicks().compose(clickDebounce()).subscribe {
            communicator(IMainActivity::class.java)?.popScreen()
        }
    }
}
