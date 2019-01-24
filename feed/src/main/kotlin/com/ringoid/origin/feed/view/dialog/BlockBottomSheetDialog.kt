package com.ringoid.origin.feed.view.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.BottomSheet
import com.ringoid.base.view.SimpleBaseDialogFragment
import com.ringoid.origin.feed.R
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.communicator
import kotlinx.android.synthetic.main.dialog_bottom_sheet_block.*

@BottomSheet(true)
class BlockBottomSheetDialog : SimpleBaseDialogFragment() {

    companion object {
        const val TAG = "BlockBottomSheetDialog_tag"

        fun newInstance(): BlockBottomSheetDialog = BlockBottomSheetDialog()
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)
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
            dismiss()
            communicator(IBlockBottomSheetActivity::class.java)?.onReportSheetOpen()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        close()
    }

    // ------------------------------------------
    private fun close() {
        communicator(IBlockBottomSheetActivity::class.java)?.onClose()
    }
}
