package com.ringoid.origin.feed.view.dialog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.view.SimpleBaseActivity
import com.ringoid.origin.navigation.Extras

@AppNav("block_dialog", "report_dialog")
class BlockBottomSheetActivity : SimpleBaseActivity(), IBlockBottomSheetActivity {

    private var blockDialog: BlockBottomSheetDialog? = null
    private var reportDialog: ReportBottomSheetDialog? = null

    // --------------------------------------------------------------------------------------------
    override fun onClose() {
        setResultExposed(currentResult, Intent().putExtra("out", intent.extras))
        finish()
    }

    // ------------------------------------------
    override fun onBlock() {
        setResultExposed(Activity.RESULT_OK, Intent().putExtra("out", intent.extras))
    }

    override fun onReport(reason: Int) {
        setResultExposed(Activity.RESULT_OK, Intent().putExtra(Extras.OUT_EXTRA_REPORT_REASON, reason).putExtra("out", intent.extras))
    }

    override fun onReportSheetOpen() {
        reportDialog?.showNow(supportFragmentManager, ReportBottomSheetDialog.TAG)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState ?: run {
            if (intent.dataString?.contains("block_dialog") == true) {
                blockDialog = BlockBottomSheetDialog.newInstance()
                    .also { it.showNow(supportFragmentManager, BlockBottomSheetDialog.TAG) }
            }
            if (intent.dataString?.contains("report_dialog") == true) {
                reportDialog = ReportBottomSheetDialog.newInstance()
                    .also { it.showNow(supportFragmentManager, ReportBottomSheetDialog.TAG) }
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
}
