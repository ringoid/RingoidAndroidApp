package com.ringoid.origin.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.StringRes
import com.ringoid.base.view.SimpleBaseDialogFragment
import com.ringoid.origin.AppRes
import com.ringoid.origin.R
import kotlinx.android.synthetic.main.dialog_status.*

class StatusDialog : SimpleBaseDialogFragment() {

    companion object {
        const val TAG = "StatusDialog_tag"

        private const val BUNDLE_KEY_TITLE_RES_ID = "bundle_key_title_res_id"

        fun newInstance(@StringRes titleResId: Int): StatusDialog =
            StatusDialog().apply {
                arguments = Bundle().apply {
                    putInt(BUNDLE_KEY_TITLE_RES_ID, titleResId)
                }
            }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return inflater.inflate(R.layout.dialog_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_dialog_title.setText(arguments?.getInt(BUNDLE_KEY_TITLE_RES_ID) ?: R.string.error_common)
        wv_status.loadUrl(AppRes.WEB_URL_ERROR_STATUS)
    }
}
