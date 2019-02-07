package com.ringoid.origin.feed.view.dialog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.ringoid.base.deeplink.AppNav
import com.ringoid.origin.navigation.Extras
import com.ringoid.origin.view.base.SimpleBaseDialogActivity

@AppNav("block_dialog", "report_dialog")
class BlockBottomSheetActivity : SimpleBaseDialogActivity(), IBlockBottomSheetActivity {

    private var blockDialog: BlockBottomSheetDialog? = null
    private var reportDialog: ReportBottomSheetDialog? = null

    // --------------------------------------------------------------------------------------------
    override fun onClose() {
        setResultExposed(currentResult, Intent().putExtras(intent.extras))
        finish()
    }

    // ------------------------------------------
    override fun onBlock() {
        setResultExposed(Activity.RESULT_OK, Intent().putExtras(intent.extras))
    }

    override fun onReport(reason: Int) {
        setResultExposed(Activity.RESULT_OK, Intent().putExtras(intent.extras).putExtra(Extras.OUT_EXTRA_REPORT_REASON, reason))
    }

    override fun onReportSheetOpen() {
        createReportDialogIfNeed()
        reportDialog?.showNow(supportFragmentManager, ReportBottomSheetDialog.TAG)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState ?: run {
            if (intent.dataString?.contains("block_dialog") == true) {
                createBlockDialogIfNeed()
                blockDialog?.showNow(supportFragmentManager, BlockBottomSheetDialog.TAG)
            }
            if (intent.dataString?.contains("report_dialog") == true) {
                createReportDialogIfNeed()
                reportDialog?.showNow(supportFragmentManager, ReportBottomSheetDialog.TAG)
            }
        }
    }

    override fun onBackPressed() {
        onClose()
    }

    override fun onDestroy() {
        super.onDestroy()
        blockDialog = null
        reportDialog = null
    }

    // --------------------------------------------------------------------------------------------
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
