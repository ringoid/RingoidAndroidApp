package com.ringoid.origin.messenger.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import com.google.gson.Gson
import com.ringoid.base.deeplink.AppNav
import com.ringoid.debug.DebugLogUtil
import com.ringoid.domain.DomainUtil
import com.ringoid.imageloader.ImageLoader
import com.ringoid.origin.messenger.R
import com.ringoid.origin.messenger.model.ChatPayload
import com.ringoid.origin.view.base.theme.ThemedSimpleBaseActivity
import com.ringoid.origin.view.dialog.IDialogCallback
import kotlinx.android.synthetic.main.activity_chat.*

@AppNav("chat")
class ChatHostActivity : ThemedSimpleBaseActivity(), IChatHost, IDialogCallback {

    override fun getLayoutId(): Int = R.layout.activity_chat

    // --------------------------------------------------------------------------------------------
    override fun onBlockFromChat(tag: String, payload: ChatPayload) {
        val data = Intent().apply {
            putExtra("action", "block")
            putExtra("payload", payload)
            putExtra("tag", tag)
        }
        DebugLogUtil.v("Chat Host: onBlockFromChat")
        setResultExposed(Activity.RESULT_OK, data)
        finish()
    }

    override fun onReportFromChat(tag: String, payload: ChatPayload, reasonNumber: Int) {
        val data = Intent().apply {
            putExtra("action", "report")
            putExtra("payload", payload)
            putExtra("reason", reasonNumber)
            putExtra("tag", tag)
        }
        DebugLogUtil.v("Chat Host: onReportFromChat")
        setResultExposed(Activity.RESULT_OK, data)
        finish()
    }

    override fun onDialogDismiss(tag: String, payload: Parcelable?) {
        val data = Intent().apply {
            putExtra("payload", payload)
            putExtra("tag", tag)
        }
        DebugLogUtil.v("Chat Host: onDialogDismiss")
        setResultExposed(Activity.RESULT_OK, data)
        finish()
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.extras?.let {
            val peerId = it.getString("peerId") ?: DomainUtil.BAD_ID
            val payloadJson = it.getString("payload") ?: "{}"
            val payload = Gson().fromJson(payloadJson, ChatPayload::class.java)
            val tag = it.getString("tag") ?: ChatFragment.TAG
            savedInstanceState ?: run {
                ChatFragment.newInstance(peerId = peerId, payload = payload, tag = tag).show(supportFragmentManager, tag)
            }
            ImageLoader.load(uri = payload.peerImageUri, thumbnailUri = payload.peerThumbnailUri,
                             iv = iv_chat_image, extra = listOf("profileId" to payload.peerId))
        }
    }
}
