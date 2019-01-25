package com.ringoid.origin.messenger.view

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.observe
import com.ringoid.base.view.BaseDialogFragment
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.DomainUtil.BAD_ID
import com.ringoid.domain.model.messenger.Message
import com.ringoid.origin.messenger.OriginR_string
import com.ringoid.origin.messenger.R
import com.ringoid.origin.messenger.WidgetR_style
import com.ringoid.origin.messenger.adapter.ChatAdapter
import com.ringoid.origin.navigation.RequestCode
import com.ringoid.origin.navigation.navigate
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.origin.view.dialog.IDialogCallback
import com.ringoid.utility.*
import kotlinx.android.synthetic.main.fragment_chat.*

class ChatFragment : BaseDialogFragment<ChatViewModel>() {

    companion object {
        const val TAG = "ChatFragment_tag"

        private const val BUNDLE_KEY_PEER_ID = "bundle_key_peer_id"
        private const val BUNDLE_KEY_POSITION_IN_FEED = "bundle_key_position_in_feed"
        private const val BUNDLE_KEY_TAG = "bundle_key_tag"

        fun newInstance(peerId: String, position: Int = DomainUtil.BAD_POSITION, tag: String = TAG): ChatFragment =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(BUNDLE_KEY_PEER_ID, peerId)
                    putInt(BUNDLE_KEY_POSITION_IN_FEED, position)
                    putString(BUNDLE_KEY_TAG, tag)
                }
            }
    }

    private var peerId: String = BAD_ID
    private lateinit var chatAdapter: ChatAdapter

    override fun getVmClass(): Class<ChatViewModel> = ChatViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_chat

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        fun onIdleState() {
            pb_chat.changeVisibility(isVisible = false, soft = true)
        }

        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.IDLE -> onIdleState()
            is ViewState.LOADING -> pb_chat.changeVisibility(isVisible = true)
            is ViewState.ERROR -> {
                // TODO: analyze: newState.e
                Dialogs.errorDialog(this, newState.e)
                onIdleState()
            }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        peerId = arguments?.getString(BUNDLE_KEY_PEER_ID, BAD_ID) ?: BAD_ID

        chatAdapter = ChatAdapter().apply {
            itemClickListener = { _, _ -> closeChat() }
            onMessageClickListener = { model: Message, _ ->
                context?.copyToClipboard(DomainUtil.CLIPBOARD_KEY_CHAT_MESSAGE, model.text)
                context?.toast(OriginR_string.common_clipboard)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        Dialog(activity!!, WidgetR_style.ChatDialog)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        super.onCreateView(inflater, container, savedInstanceState)
            ?.apply { setOnClickListener { closeChat() } }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewLifecycleOwner.apply {
            observe(vm.messages, chatAdapter::submitList) { scrollToTopOfItemAtPosition(0) }
            observe(vm.sentMessage, ::putMyMessage)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RequestCode.RC_BLOCK_DIALOG -> {
                if (resultCode == Activity.RESULT_OK) {
                    // TODO: close chat
                } else {
                    showChatControls(isVisible = true)
                }
            }
        }
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        et_message.apply {
            requestFocus()
            setOnKeyPreImeListener { keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                    closeChat()
                }
                false
            }
        }
        ibtn_message_send.clicks().compose(clickDebounce()).subscribe { vm.sendMessage(peerId, et_message.text.toString()) }
        ibtn_chat_close.clicks().compose(clickDebounce()).subscribe { closeChat() }
        ibtn_settings.clicks().compose(clickDebounce()).subscribe {
            showChatControls(isVisible = false)
            navigate(this@ChatFragment, path = "/block_dialog", rc = RequestCode.RC_BLOCK_DIALOG)
        }
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val position = arguments?.getInt(BUNDLE_KEY_POSITION_IN_FEED, DomainUtil.BAD_POSITION) ?: DomainUtil.BAD_POSITION
        val tag = arguments?.getString(BUNDLE_KEY_TAG, TAG) ?: TAG
        communicator(IDialogCallback::class.java)?.onDialogDismiss(position = position, tag = tag)
    }

    // --------------------------------------------------------------------------------------------
    private fun closeChat() {
        et_message.hideKeyboard()
        dismiss()
    }

    private fun scrollListToPosition(position: Int) {
        rv_chat_messages.smoothScrollToPosition(position)
    }

    private fun scrollToTopOfItemAtPosition(position: Int) {
        (rv_chat_messages.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
    }

    private fun showChatControls(isVisible: Boolean) {
        if (isVisible) {
            et_message.showKeyboard()
        } else {
            et_message.hideKeyboard()
        }
        ibtn_chat_close.changeVisibility(isVisible, soft = true)
        ibtn_settings.changeVisibility(isVisible, soft = true)
        ll_text_input.changeVisibility(isVisible, soft = true)
        rv_chat_messages.changeVisibility(isVisible, soft = true)
    }

    // ------------------------------------------
    private fun putMyMessage(message: Message) {
        et_message.setText("")  // clear text input
        scrollListToPosition(0)
        chatAdapter.prepend(message)
    }
}
