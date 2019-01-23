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
import kotlinx.android.synthetic.main.dialog_bottom_sheet_block.*

@BottomSheet(true)
class BlockBottomSheetDialog : BaseDialogFragment() {

    companion object {
        const val TAG = "BlockBottomSheetDialog_tag"

        fun newInstance(): BlockBottomSheetDialog = BlockBottomSheetDialog()
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        dialog.apply {
            setCanceledOnTouchOutside(true)
            setOnCancelListener { close() }
        }
        return inflater.inflate(R.layout.dialog_bottom_sheet_block, container, false)
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_block.clicks().compose(clickDebounce()).subscribe {
            communicator(IBlockBottomSheetActivity::class.java)?.onBlock()
            close()
        }
        btn_report.clicks().compose(clickDebounce()).subscribe {
            communicator(IBlockBottomSheetActivity::class.java)?.onReportSheetOpen()
            dismiss()
        }
    }

    // ------------------------------------------
    private fun close() {
        communicator(IBlockBottomSheetActivity::class.java)?.onClose()
    }
}
