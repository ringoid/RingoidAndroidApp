package com.ringoid.origin.feed.view.dialog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.view.SimpleBaseActivity

@AppNav("block_dialog", "report_dialog")
class BlockBottomSheetActivity : SimpleBaseActivity(), IBlockBottomSheetActivity {

    companion object {
        const val OUT_EXTRA_REPORT_REASON = "out_extra_report_reason"
    }

    private var blockDialog: BlockBottomSheetDialog? = null
    private var reportDialog: ReportBottomSheetDialog? = null

    // --------------------------------------------------------------------------------------------
    override fun onClose() {
        finish()
    }

    // ------------------------------------------
    override fun onBlock() {
        setResult(Activity.RESULT_OK)
    }

    override fun onReport(reason: Int) {
        setResult(Activity.RESULT_OK, Intent().putExtra(OUT_EXTRA_REPORT_REASON, reason))
    }

    override fun onReportSheetOpen() {
        reportDialog?.showNow(supportFragmentManager, ReportBottomSheetDialog.TAG)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState ?: run {
            when (intent.action) {
                "block_dialog" -> {
                    blockDialog = BlockBottomSheetDialog.newInstance()
                    blockDialog
                }
                "report_dialog" -> {
                    reportDialog = ReportBottomSheetDialog.newInstance()
                    reportDialog
                }
                else -> null
            }
            ?.showNow(supportFragmentManager, BlockBottomSheetDialog.TAG)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        blockDialog = null
        reportDialog = null
    }
}
