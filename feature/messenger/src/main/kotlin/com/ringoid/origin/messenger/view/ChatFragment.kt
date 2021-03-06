package com.ringoid.origin.messenger.view

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import com.ringoid.base.observe
import com.ringoid.base.observeOneShot
import com.ringoid.base.view.BaseDialogFragment
import com.ringoid.base.view.IBaseActivity
import com.ringoid.base.view.ViewState
import com.ringoid.debug.DebugLogUtil
import com.ringoid.debug.timer.TimeKeeper
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.DomainUtil.BAD_ID
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.origin.AppRes
import com.ringoid.origin.error.handleOnView
import com.ringoid.origin.messenger.OriginR_string
import com.ringoid.origin.messenger.R
import com.ringoid.origin.messenger.WidgetR_style
import com.ringoid.origin.messenger.adapter.ChatAdapter
import com.ringoid.origin.messenger.model.ChatPayload
import com.ringoid.origin.model.BlockReportPayload
import com.ringoid.origin.model.OnlineStatus
import com.ringoid.origin.navigation.Extras
import com.ringoid.origin.navigation.RequestCode
import com.ringoid.origin.navigation.navigate
import com.ringoid.origin.navigation.noConnection
import com.ringoid.origin.view.dialog.IDialogCallback
import com.ringoid.origin.view.main.LcNavTab
import com.ringoid.utility.*
import com.uber.autodispose.lifecycle.autoDisposable
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

    private var inputText: CharSequence = ""
    private var peerId: String = BAD_ID
    private var payload: ChatPayload? = null

    private lateinit var chatAdapter: ChatAdapter
    private val timeKeeper = TimeKeeper()

    override fun getVmClass(): Class<ChatViewModel> = ChatViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_chat

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        fun onIdleState() {
            pb_chat?.changeVisibility(isVisible = false, soft = true)
        }

        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.IDLE -> onIdleState()
            is ViewState.LOADING -> pb_chat?.changeVisibility(isVisible = true)
            is ViewState.ERROR -> newState.e.handleOnView(this, ::onIdleState)
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        peerId = arguments?.getString(BUNDLE_KEY_PEER_ID, BAD_ID) ?: BAD_ID
        payload = arguments?.getParcelable(BUNDLE_KEY_PAYLOAD)
        ChatInMemoryCache.addProfileIfNotExists(profileId = peerId)

        chatAdapter = ChatAdapter().apply {
            itemDoubleClickListener = { _, _ -> closeChat() }
            onMessageInsertListener = { _ -> scrollToLastItem() }
            onMessageLongClickListener = { message ->
                context?.let {
                    it.copyToClipboard(key = DomainUtil.CLIPBOARD_KEY_CHAT_MESSAGE, value = message.text)
                    it.toast(OriginR_string.common_clipboard, gravity = Gravity.CENTER)
                }
            }
        }

        timeKeeper.registerCallback { runOnUiThread { context?.toast(OriginR_string.time_keeper_interval_alert_load) } }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        Dialog(activity!!, WidgetR_style.ChatDialog)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(viewLifecycleOwner) {
            observe(vm.messages(), chatAdapter::submitList)
            observe(vm.sentMessage(), chatAdapter::prepend)
            observe(vm.onlineStatus(), ::showOnlineStatus)
            observe(vm.peerName()) { peerName -> tv_peer_name?.text = peerName }
            observeOneShot(vm.notifyOnMessagesLoadOneShot()) { started ->
                if (started) timeKeeper.start() else timeKeeper.stop()
            }
            observeOneShot(vm.updateReadStatusOnMessagesOneShot(), chatAdapter::updateReadStatusOnMessages)
        }
        communicator(IBaseActivity::class.java)?.keyboard()
            ?.autoDisposable(scopeProvider)
            ?.subscribe({ scrollToTopOfItemAtPosition(0) }, DebugLogUtil::e)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RequestCode.RC_BLOCK_DIALOG -> {
                if (resultCode == Activity.RESULT_OK) {
                    val tag = arguments?.getString(BUNDLE_KEY_TAG, TAG) ?: TAG
                    if (data?.hasExtra(Extras.OUT_EXTRA_REPORT_REASON) != null) {
                        val reasonNumber = data.getIntExtra(Extras.OUT_EXTRA_REPORT_REASON, 0)
                        communicator(IChatHost::class.java)?.onReportFromChat(tag = tag, payload = payload!!, reasonNumber = reasonNumber)
                    } else {
                        communicator(IChatHost::class.java)?.onBlockFromChat(tag = tag, payload = payload!!)
                    }
                    closeChat()
                } else {
                    rv_chat_messages.apply { setPadding(paddingLeft, paddingTop, paddingRight, 0) }
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
            textChanges().compose(inputDebounce()).subscribe {
                if (it.isNotEmpty() && it.last() == '\n') {  // user has typed newline character
                    scrollToLastItemIfNearBottom()  // avoid input box overlapping list
                }
                inputText = it
            }
        }
        ibtn_message_send.clicks().compose(clickDebounce()).subscribe {
            if (!connectionManager.isNetworkAvailable()) {
                noConnection(this)
                return@subscribe
            }

            val imageId = payload?.peerImageId ?: BAD_ID
            vm.sendMessage(peerId = peerId, imageId = imageId, text = et_message.text.toString(),
                           sourceFeed = payload?.sourceFeed ?: LcNavTab.MESSAGES)
            clearEditText()
        }
        ibtn_chat_close.clicks().compose(clickDebounce()).subscribe { closeChat() }
        ibtn_settings.clicks().compose(clickDebounce()).subscribe {
            showChatControls(isVisible = false)
            val blockPayload = BlockReportPayload(profileImageUri = payload?.peerImageUri, profileThumbnailUri = payload?.peerThumbnailUri)
            navigate(this@ChatFragment, path = "/block_dialog?payload=${blockPayload.toJson()}", rc = RequestCode.RC_BLOCK_DIALOG)
            rv_chat_messages.apply { setPadding(paddingLeft, paddingTop, paddingRight, AppRes.BLOCK_BOTTOM_SHEET_DIALOG_HEIGHT) }
        }
        ll_status_container.clicks().compose(clickDebounce()).subscribe { closeChat() }
        rv_chat_messages.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(context)
                .apply {
                    reverseLayout = true
                    stackFromEnd = true
                }
//            itemAnimator = SlideUpAlphaAnimator()
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.getMessages(profileId = peerId)
        et_message.apply {
            val text = ChatInMemoryCache.getInputMessage(profileId = peerId)
            setText(text)
            setSelection(text?.length ?:0)
            requestFocus()
        }

        ChatInMemoryCache.onChatOpen(chatId = peerId)  // Chat screen is visible to user
        dialog?.window?.showKeyboard()
    }

    override fun onPause() {
        super.onPause()
        ChatInMemoryCache.setInputMessage(profileId = peerId, text = buildString { append(inputText) })
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val tag = arguments?.getString(BUNDLE_KEY_TAG, TAG) ?: TAG
        communicator(IDialogCallback::class.java)?.onDialogDismiss(tag = tag, payload = payload)
    }

    override fun onDestroy() {
        super.onDestroy()
        timeKeeper.unregisterCallback()
    }

    // --------------------------------------------------------------------------------------------
    private fun closeChat() {
        DebugLogUtil.clear()  // clear debug log on back from Chat to avoid app freezes
        ChatInMemoryCache.onChatClose()
        rv_chat_messages.linearLayoutManager()?.let {
            val position = it.findFirstVisibleItemPosition()
            val scroll = it.findViewByPosition(position)?.let { rv_chat_messages.bottom - it.top } ?: 0
            ChatInMemoryCache.addProfileWithPosition(profileId = peerId, position = position to scroll)
        }
        et_message.hideKeyboard()
        dismissAllowingStateLoss()
    }

    private fun scrollListToPosition(position: Int) {
        rv_chat_messages?.post { rv_chat_messages?.scrollToPosition(position) }
    }

    private fun scrollToTopOfItemAtPosition(position: Int) {
        rv_chat_messages?.post {
            rv_chat_messages?.linearLayoutManager()?.scrollToPositionWithOffset(position, 0)
        }
    }

    private fun scrollToLastItem() {
        scrollToTopOfItemAtPosition(0)  // scroll to last message
    }

    /**
     * If vertical position is near the last message in Chat - scroll to the bottom of
     * each newly inserted message, otherwise - remain on the current vertical position.
     */
    private fun scrollToLastItemIfNearBottom() {
        rv_chat_messages?.linearLayoutManager()
            ?.takeIf { it.findFirstVisibleItemPosition() <= 1 }
            ?.let { scrollToTopOfItemAtPosition(0) }  // scroll to last message
    }

    private fun scrollToItemAtCachedPosition() {
        val position = ChatInMemoryCache.getProfilePosition(profileId = peerId)
        rv_chat_messages?.post {
            rv_chat_messages?.linearLayoutManager()?.scrollToPositionWithOffset(position.first, position.second)
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
    }

    private fun showOnlineStatus(onlineStatus: OnlineStatus) {
        payload?.onlineStatus = onlineStatus
        with (label_online_status) {
            alpha = if (onlineStatus == OnlineStatus.UNKNOWN) 0.0f else 1.0f
            setIcon(onlineStatus.resId)
            setText(onlineStatus.label)
        }
    }

    // ------------------------------------------
    private fun clearEditText() {
        ChatInMemoryCache.setInputMessage(profileId = peerId, text = "")
        et_message.setText("")  // clear text input
    }

    // --------------------------------------------------------------------------------------------
    private val gestureDetector = GestureDetectorCompat(context, ListTouchCallback())

    private inner class ListTouchCallback : GestureDetector.SimpleOnGestureListener() {

        override fun onDoubleTapEvent(event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_UP) {
                closeChat()
            }
            return true
        }
    }
}
