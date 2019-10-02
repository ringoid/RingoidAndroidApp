package com.ringoid.origin.feed.view.dialog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.gson.Gson
import com.ringoid.base.deeplink.AppNav
import com.ringoid.imageloader.ImageLoader
import com.ringoid.origin.feed.R
import com.ringoid.origin.model.FeedItemContextMenuPayload
import com.ringoid.origin.navigation.ExternalNavigator
import com.ringoid.origin.navigation.Extras
import com.ringoid.origin.view.base.theme.ThemedSimpleBaseActivity
import kotlinx.android.synthetic.main.activity_bottom_sheet_block.*
import timber.log.Timber

@AppNav("feed_item_context_menu", "block_dialog", "report_dialog")
class FeedItemContextMenuActivity : ThemedSimpleBaseActivity(), IFeedItemContextMenuActivity {

    companion object {
        private const val BUNDLE_KEY_OUTPUT_DATA = "bundle_key_output_data"
    }

    private var blockDialog: BlockBottomSheetDialog? = null
    private var reportDialog: ReportBottomSheetDialog? = null
    private var feedItemContextMenuDialog: FeedItemContextMenuDialog? = null

    private lateinit var outputData: Intent
    private var instagramUserId: String? = null
    private var tiktokUserId: String? = null

    override fun getLayoutId(): Int = R.layout.activity_bottom_sheet_block

    // --------------------------------------------------------------------------------------------
    override fun onClose() {
        setResultExposed(currentResult, outputData)
        finish()
    }

    // ------------------------------------------
    override fun onBlock() {
        outputData.putExtra(Extras.OUT_EXTRA_REPORT_REASON, 0)
        setResultExposed(Activity.RESULT_OK, outputData)
    }

    override fun onReport(reason: Int) {
        outputData.putExtra(Extras.OUT_EXTRA_REPORT_REASON, reason)
        setResultExposed(Activity.RESULT_OK, outputData)
    }

    override fun onReportSheetOpen() {
        createReportDialogIfNeed()
        reportDialog?.showNow(supportFragmentManager, ReportBottomSheetDialog.TAG)
    }

    override fun onSendLike() {
        outputData.putExtra(Extras.OUT_EXTRA_LIKE_SENT, true)
        setResultExposed(Activity.RESULT_OK, outputData)
    }

    override fun onSendMatch() {
        onSendLike()  // sending match is the same as sending like
    }

    override fun openChat() {
        outputData.putExtra(Extras.OUT_EXTRA_OPEN_CHAT, true)
        setResultExposed(Activity.RESULT_OK, outputData)
    }

    override fun openSocialInstagram() {
        ExternalNavigator.openSocialInstagram(this, instagramUserId = instagramUserId)
    }

    override fun openSocialTiktok() {
        ExternalNavigator.openSocialTiktok(this, tiktokUserId = tiktokUserId)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        outputData = savedInstanceState?.getParcelable(BUNDLE_KEY_OUTPUT_DATA)
            ?: intent.extras?.let { extras -> Intent().putExtras(extras) }
            ?: intent

        intent.extras?.let {
            val payloadJson = it.getString("payload") ?: "{}"
            val payload = Gson().fromJson(payloadJson, FeedItemContextMenuPayload::class.java)
            ImageLoader.load(uri = payload.profileImageUri, thumbnailUri = payload.profileThumbnailUri,
                             iv = iv_profile_image)

            instagramUserId = payload.socialInstagram
            tiktokUserId = payload.socialTiktok
        }

        savedInstanceState ?: run {
            if (intent.dataString?.contains("feed_item_context_menu") == true) {
                createFeedItemContextMenuDialogIfNeed()
                feedItemContextMenuDialog?.showNow(supportFragmentManager, FeedItemContextMenuDialog.TAG)
            } else if (intent.dataString?.contains("block_dialog") == true) {
                createBlockDialogIfNeed()
                blockDialog?.showNow(supportFragmentManager, BlockBottomSheetDialog.TAG)
            } else if (intent.dataString?.contains("report_dialog") == true) {
                createReportDialogIfNeed()
                reportDialog?.showNow(supportFragmentManager, ReportBottomSheetDialog.TAG)
            } else {
                Timber.d("No dialog to open for context menu")
            }
        }
    }

    override fun onBackPressed() {
        onClose()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(BUNDLE_KEY_OUTPUT_DATA, outputData)
    }

    override fun onDestroy() {
        super.onDestroy()
        blockDialog = null
        reportDialog = null
    }

    // --------------------------------------------------------------------------------------------
    private fun createFeedItemContextMenuDialogIfNeed() {
        if (feedItemContextMenuDialog == null) {
            val contextMenuActions = intent.extras?.getString("actions")
            feedItemContextMenuDialog = FeedItemContextMenuDialog.newInstance(contextMenuActions, socialInstagram = instagramUserId, socialTiktok = tiktokUserId)
        }
    }

    private fun createBlockDialogIfNeed() {
        if (blockDialog == null) {
            blockDialog = BlockBottomSheetDialog.newInstance()
        }
    }

    private fun createReportDialogIfNeed() {
        if (reportDialog == null) {
            val excludedReasons = intent.extras?.getString("excludedReasons")
            reportDialog = ReportBottomSheetDialog.newInstance(excludedReasons)
        }
    }
}
