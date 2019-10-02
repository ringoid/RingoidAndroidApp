package com.ringoid.origin.feed.view.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.BottomSheet
import com.ringoid.base.view.SimpleBaseDialogFragment
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.R
import com.ringoid.utility.ValueUtils
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.communicator
import kotlinx.android.synthetic.main.dialog_feed_item_context_menu.*
import timber.log.Timber

@BottomSheet(true)
class FeedItemContextMenuDialog : SimpleBaseDialogFragment() {

    companion object {
        const val TAG = "FeedItemContextMenuDialog_tag"

        private const val BUNDLE_KEY_CONTEXT_MENU_ACTIONS = "bundle_key_context_menu_actions"
        private const val BUNDLE_KEY_SOCIAL_INSTAGRAM = "bundle_key_social_instagram"
        private const val BUNDLE_KEY_SOCIAL_TIKTOK = "bundle_key_social_tiktok"

        fun newInstance(contextMenuActions: String? = null,
                        socialInstagram: String? = null,
                        socialTiktok: String? = null): FeedItemContextMenuDialog =
            FeedItemContextMenuDialog().apply {
                arguments = Bundle().apply {
                    contextMenuActions?.takeIf { it.isNotBlank() }
                        ?.let {
                            val actions = it.split(',')
                            putStringArray(BUNDLE_KEY_CONTEXT_MENU_ACTIONS, actions.toTypedArray())
                        }
                    putString(BUNDLE_KEY_SOCIAL_INSTAGRAM, socialInstagram)
                    putString(BUNDLE_KEY_SOCIAL_TIKTOK, socialTiktok)
                }
            }
    }

    override fun getLayoutId(): Int = R.layout.dialog_feed_item_context_menu

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_block.clicks().compose(clickDebounce()).subscribe { onBlock() }
        btn_report.clicks().compose(clickDebounce()).subscribe { onReportSheetOpen() }
        btn_open_chat.clicks().compose(clickDebounce()).subscribe { onOpenChat() }
        btn_send_like.clicks().compose(clickDebounce()).subscribe { onSendLike() }
        btn_send_match.clicks().compose(clickDebounce()).subscribe { onSendMatch() }
        btn_open_social_instagram.clicks().compose(clickDebounce()).subscribe { openSocialInstagram() }
        btn_open_social_tiktok.clicks().compose(clickDebounce()).subscribe { openSocialTiktok() }

        val contextMenuActions = arguments?.getStringArray(BUNDLE_KEY_CONTEXT_MENU_ACTIONS) ?: emptyArray()
        Timber.v("List of context menu actions: ${contextMenuActions.joinToString()}")
        btn_open_chat.changeVisibility(isVisible = contextMenuActions.contains("chat"))
        btn_send_like.changeVisibility(isVisible = contextMenuActions.contains("like"))
        btn_send_match.changeVisibility(isVisible = contextMenuActions.contains("match"))

        arguments?.getString(BUNDLE_KEY_SOCIAL_INSTAGRAM)?.takeIf { it.isNotBlank() }?.let { instagramUserId ->
            btn_open_social_instagram.text = String.format(resources.getString(OriginR_string.profile_button_open_social_instagram, ValueUtils.atCharSocialId(instagramUserId)))
        } ?: run { btn_open_social_instagram.changeVisibility(isVisible = false) }

        arguments?.getString(BUNDLE_KEY_SOCIAL_TIKTOK)?.takeIf { it.isNotBlank() }?.let { tiktokUserId ->
            btn_open_social_tiktok.text = String.format(resources.getString(OriginR_string.profile_button_open_social_tiktok, ValueUtils.atCharSocialId(tiktokUserId)))
        } ?: run { btn_open_social_tiktok.changeVisibility(isVisible = false) }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        close()
    }

    // ------------------------------------------
    private fun close() {
        communicator(IFeedItemContextMenuActivity::class.java)?.onClose()
    }

    // ------------------------------------------
    private fun onBlock() {
        communicator(IFeedItemContextMenuActivity::class.java)?.onBlock()
        close()
    }

    private fun onOpenChat() {
        communicator(IFeedItemContextMenuActivity::class.java)?.openChat()
        close()
    }

    private fun onReportSheetOpen() {
        dismiss()
        communicator(IFeedItemContextMenuActivity::class.java)?.onReportSheetOpen()
    }

    private fun onSendLike() {
        communicator(IFeedItemContextMenuActivity::class.java)?.onSendLike()
        close()
    }

    private fun onSendMatch() {
        communicator(IFeedItemContextMenuActivity::class.java)?.onSendMatch()
        close()
    }

    private fun openSocialInstagram() {
        communicator(IFeedItemContextMenuActivity::class.java)?.openSocialInstagram()
        close()
    }

    private fun openSocialTiktok() {
        communicator(IFeedItemContextMenuActivity::class.java)?.openSocialTiktok()
        close()
    }
}