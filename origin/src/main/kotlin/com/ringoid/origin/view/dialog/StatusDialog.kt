package com.ringoid.origin.view.dialog

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.ringoid.base.view.SimpleBaseDialogFragment
import com.ringoid.origin.AppRes
import com.ringoid.origin.R
import com.ringoid.utility.readFromUrl
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_status.*
import timber.log.Timber

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

    @LayoutRes
    override fun getLayoutId(): Int = R.layout.dialog_status

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_dialog_title.setText(arguments?.getInt(BUNDLE_KEY_TITLE_RES_ID) ?: R.string.error_common)

        AppRes.WEB_URL_ERROR_STATUS.readFromUrl()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(scopeProvider)
            .subscribe({ tv_dialog_status.text = it }, Timber::e)
    }
}
