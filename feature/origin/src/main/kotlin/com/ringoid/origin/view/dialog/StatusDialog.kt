package com.ringoid.origin.view.dialog

import android.os.Bundle
import androidx.annotation.StringRes
import com.ringoid.debug.DebugLogUtil
import com.ringoid.origin.R
import com.ringoid.utility.readFromUrl
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_status.*

class StatusDialog : SupportDialog() {

    companion object {
        const val TAG = "StatusDialog_tag"

        fun newInstance(@StringRes titleResId: Int): StatusDialog =
            StatusDialog().apply {
                arguments = Bundle().apply {
                    putInt(BUNDLE_KEY_TITLE_RES_ID, titleResId)
                }
            }
    }

    override fun showDescription() {
        resources.getString(R.string.web_url_error_status).readFromUrl()
                 .subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread())
                 .autoDisposable(scopeProvider)
                 .subscribe({ tv_dialog_status.text = it }, DebugLogUtil::e)
    }
}
