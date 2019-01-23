package com.ringoid.origin.feed.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.BaseDialogFragment
import com.ringoid.base.view.BottomSheet
import com.ringoid.origin.feed.R
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.communicator
import kotlinx.android.synthetic.main.dialog_bottom_sheet_report.*

@BottomSheet(true)
class ReportBottomSheetDialog : BaseDialogFragment() {

    companion object {
        const val TAG = "ReportBottomSheetDialog_tag"

        fun newInstance(): ReportBottomSheetDialog = ReportBottomSheetDialog()
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        dialog?.apply {
            setCanceledOnTouchOutside(true)
            setOnCancelListener { close() }
        }
        return inflater.inflate(R.layout.dialog_bottom_sheet_report, container, false)
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_report_1.clicks().compose(clickDebounce()).subscribe {
            communicator(IBlockBottomSheetActivity::class.java)?.onReport(reason = 0)
            close()
        }
        btn_report_2.clicks().compose(clickDebounce()).subscribe {
            communicator(IBlockBottomSheetActivity::class.java)?.onReport(reason = 1)
            close()
        }
        btn_report_3.clicks().compose(clickDebounce()).subscribe {
            communicator(IBlockBottomSheetActivity::class.java)?.onReport(reason = 2)
            close()
        }
        btn_report_4.clicks().compose(clickDebounce()).subscribe {
            communicator(IBlockBottomSheetActivity::class.java)?.onReport(reason = 3)
            close()
        }
        btn_report_5.clicks().compose(clickDebounce()).subscribe {
            communicator(IBlockBottomSheetActivity::class.java)?.onReport(reason = 4)
            close()
        }
        btn_report_6.clicks().compose(clickDebounce()).subscribe {
            communicator(IBlockBottomSheetActivity::class.java)?.onReport(reason = 5)
            close()
        }
    }

    // ------------------------------------------
    private fun close() {
        communicator(IBlockBottomSheetActivity::class.java)?.onClose()
    }
}
