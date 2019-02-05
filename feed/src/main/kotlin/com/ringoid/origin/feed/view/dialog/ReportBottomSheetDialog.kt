package com.ringoid.origin.feed.view.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.BottomSheet
import com.ringoid.base.view.SimpleBaseDialogFragment
import com.ringoid.origin.feed.R
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.communicator
import kotlinx.android.synthetic.main.dialog_bottom_sheet_report.*

@BottomSheet(true)
class ReportBottomSheetDialog : SimpleBaseDialogFragment() {

    companion object {
        const val TAG = "ReportBottomSheetDialog_tag"

        private const val BUNDLE_KEY_EXCLUDED_REASONS = "bundle_key_excluded_reasons"

        fun newInstance(excludedReasons: String? = null): ReportBottomSheetDialog =
            ReportBottomSheetDialog().apply {
                arguments = excludedReasons
                    ?.takeIf { !it.isNullOrBlank() }
                    ?.let {
                        val reasons = it.split(',').map { it.toInt() }
                        Bundle().apply { putIntArray(BUNDLE_KEY_EXCLUDED_REASONS, reasons.toIntArray()) }
                    }
            }
    }

    @LayoutRes
    override fun getLayoutId(): Int = R.layout.dialog_bottom_sheet_report

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val excludedReasons = arguments?.getIntArray(BUNDLE_KEY_EXCLUDED_REASONS)

        btn_report_0.apply {
            clicks().compose(clickDebounce()).subscribe {
                communicator(IBlockBottomSheetActivity::class.java)?.onReport(reason = 10)
                close()
            }
        }.changeVisibility(isVisible = excludedReasons?.contains(10) != true)
        btn_report_1.apply {
            clicks().compose(clickDebounce()).subscribe {
                communicator(IBlockBottomSheetActivity::class.java)?.onReport(reason = 20)
                close()
            }
        }.changeVisibility(isVisible = excludedReasons?.contains(20) != true)
        btn_report_2.apply {
            clicks().compose(clickDebounce()).subscribe {
                communicator(IBlockBottomSheetActivity::class.java)?.onReport(reason = 30)
                close()
            }
        }.changeVisibility(isVisible = excludedReasons?.contains(30) != true)
        btn_report_3.apply {
            clicks().compose(clickDebounce()).subscribe {
                communicator(IBlockBottomSheetActivity::class.java)?.onReport(reason = 40)
                close()
            }
        }.changeVisibility(isVisible = excludedReasons?.contains(40) != true)
        btn_report_4.apply {
            clicks().compose(clickDebounce()).subscribe {
                communicator(IBlockBottomSheetActivity::class.java)?.onReport(reason = 50)
                close()
            }
        }.changeVisibility(isVisible = excludedReasons?.contains(50) != true)
        btn_report_5.apply {
            clicks().compose(clickDebounce()).subscribe {
                communicator(IBlockBottomSheetActivity::class.java)?.onReport(reason = 60)
                close()
            }
        }.changeVisibility(isVisible = excludedReasons?.contains(60) != true)
        btn_report_6.apply {
            clicks().compose(clickDebounce()).subscribe {
                communicator(IBlockBottomSheetActivity::class.java)?.onReport(reason = 70)
                close()
            }
        }.changeVisibility(isVisible = excludedReasons?.contains(70) != true)
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
