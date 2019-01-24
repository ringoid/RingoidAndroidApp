package com.ringoid.origin.messenger.view

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.observe
import com.ringoid.base.view.BaseDialogFragment
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.messenger.Message
import com.ringoid.origin.messenger.OriginR_string
import com.ringoid.origin.messenger.R
import com.ringoid.origin.messenger.adapter.ChatAdapter
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.copyToClipboard
import com.ringoid.utility.toast
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

    private lateinit var chatAdapter: ChatAdapter

    override fun getVmClass(): Class<ChatViewModel> = ChatViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_chat

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatAdapter = ChatAdapter().apply {
            itemClickListener = { model: Message, _ ->
                context?.copyToClipboard(DomainUtil.CLIPBOARD_KEY_CHAT_MESSAGE, model.text)
                context?.toast(OriginR_string.common_clipboard)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        Dialog(activity!!, R.style.ChatDialog)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        super.onCreateView(inflater, container, savedInstanceState)
            ?.apply { setOnClickListener { dismiss() } }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewLifecycleOwner.observe(vm.messages, chatAdapter::submitList) {
            rv_chat_messages.scrollToPosition(it.size - 1)
        }
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        et_message.apply {
            requestFocus()
            setOnKeyPreImeListener { keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                    dismiss()
                }
                false
            }
        }
        ibtn_chat_close.clicks().compose(clickDebounce()).subscribe { dismiss() }
        rv_chat_messages.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(context)
                .apply {
                    reverseLayout = true
                    stackFromEnd = true
                }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.getMessages()
    }
}
