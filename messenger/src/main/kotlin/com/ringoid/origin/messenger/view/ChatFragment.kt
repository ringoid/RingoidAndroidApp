package com.ringoid.origin.messenger.view

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.observe
import com.ringoid.base.view.BaseDialogFragment
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.DomainUtil.BAD_ID
import com.ringoid.domain.model.messenger.Message
import com.ringoid.origin.error.handleOnView
import com.ringoid.origin.messenger.OriginR_string
import com.ringoid.origin.messenger.R
import com.ringoid.origin.messenger.WidgetR_style
import com.ringoid.origin.messenger.adapter.ChatAdapter
import com.ringoid.origin.navigation.Extras
import com.ringoid.origin.navigation.RequestCode
import com.ringoid.origin.navigation.navigate
import com.ringoid.origin.view.dialog.IDialogCallback
import com.ringoid.utility.*
import com.ringoid.widget.decor.TopBottomDividerItemDecoration
import kotlinx.android.synthetic.main.fragment_chat.*

class ChatFragment : BaseDialogFragment<ChatViewModel>() {

    companion object {
        const val TAG = "ChatFragment_tag"

        private const val BUNDLE_KEY_PEER_ID = "bundle_key_peer_id"
        private const val BUNDLE_KEY_PAYLOAD = "bundle_key_payload"
        private const val BUNDLE_KEY_TAG = "bundle_key_tag"

        fun newInstance(peerId: String, payload: ChatPayload = ChatPayload(peerId = peerId), tag: String = TAG): ChatFragment =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(BUNDLE_KEY_PEER_ID, peerId)
                    putParcelable(BUNDLE_KEY_PAYLOAD, payload)
                    putString(BUNDLE_KEY_TAG, tag)
                }
            }
    }

    private var peerId: String = BAD_ID
    private var payload: Parcelable? = null
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
            is ViewState.DONE -> {
                when (newState.residual) {
                    is CHAT_MESSAGE_SENT -> {
                        onIdleState()
                        context?.toast(OriginR_string.chat_message_sent, gravity = Gravity.CENTER)
                        if (chatAdapter.isEmpty()) {
                            closeChat()
                        }
                    }
                }
            }
            is ViewState.IDLE -> onIdleState()
            is ViewState.LOADING -> pb_chat.changeVisibility(isVisible = true)
            is ViewState.ERROR -> newState.e.handleOnView(this, ::onIdleState)
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        peerId = arguments?.getString(BUNDLE_KEY_PEER_ID, BAD_ID) ?: BAD_ID
        payload = arguments?.getParcelable(BUNDLE_KEY_PAYLOAD)

        chatAdapter = ChatAdapter().apply {
            itemClickListener = { _, _ -> closeChat() }
            onMessageClickListener = { model: Message, _ ->
                context?.copyToClipboard(DomainUtil.CLIPBOARD_KEY_CHAT_MESSAGE, model.text)
                context?.toast(OriginR_string.common_clipboard, gravity = Gravity.CENTER)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        Dialog(activity!!, WidgetR_style.ChatDialog)

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
                    if (data?.hasExtra(Extras.OUT_EXTRA_REPORT_REASON) != null) {
                        val reasonNumber = (data.getIntExtra(Extras.OUT_EXTRA_REPORT_REASON, 0) + 1) * 10
                        communicator(IChatHost::class.java)?.onReportFromChat(payload as ChatPayload, reasonNumber = reasonNumber)
                    } else {
                        communicator(IChatHost::class.java)?.onBlockFromChat(payload as ChatPayload)
                    }
                    closeChat()
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
        ibtn_message_send.clicks().compose(clickDebounce()).subscribe {
            val imageId = (payload as? ChatPayload)?.peerImageId ?: BAD_ID
            vm.sendMessage(peerId = peerId, imageId = imageId, text = et_message.text.toString())
        }
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
            addItemDecoration(TopBottomDividerItemDecoration(context, R.dimen.chat_blob_divider_height))
            setOnTouchListener(ChatTouchListener())
        }
    }

    override fun onStart() {
        super.onStart()
        vm.getMessages()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val tag = arguments?.getString(BUNDLE_KEY_TAG, TAG) ?: TAG
        communicator(IDialogCallback::class.java)?.onDialogDismiss(tag = tag, payload = payload)
    }

    // --------------------------------------------------------------------------------------------
    private fun closeChat() {
        et_message.hideKeyboard()
        dismiss()
    }

    private fun scrollListToPosition(position: Int) {
        rv_chat_messages.post { rv_chat_messages.smoothScrollToPosition(position) }
    }

    private fun scrollToTopOfItemAtPosition(position: Int) {
        rv_chat_messages.post {
            (rv_chat_messages.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
        }
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
        chatAdapter.prepend(message)
        scrollListToPosition(0)
    }

    // ------------------------------------------
    private inner class ChatTouchListener : View.OnTouchListener {
        private var lastAction: Int = MotionEvent.ACTION_DOWN

        @Suppress("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean =
            if (event.action == MotionEvent.ACTION_UP &&
                (lastAction == MotionEvent.ACTION_DOWN ||
                    (v.takeIf { it is RecyclerView }
                      ?.let { it as? RecyclerView }
                      ?.let { lastAction == MotionEvent.ACTION_MOVE && it.adapter?.itemCount == 0 }) == true)) {
                closeChat()
                true
            } else {
                lastAction = event.action
                false
            }
    }
}
