package com.ringoid.origin.view.dialog

import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.ringoid.base.view.BaseActivity
import com.ringoid.origin.R

object Dialogs {

    fun getTextDialog(activity: BaseActivity<*>, @StringRes titleResId: Int, description: String): AlertDialog =
            AlertDialog.Builder(activity)
                .setTitle(titleResId)
                .setMessage(description)
                .setPositiveButton(R.string.button_close, null)
                .create()

    fun showTextDialog(activity: BaseActivity<*>, @StringRes titleResId: Int, description: String) {
        activity.takeIf { !it.isAfterOnSaveInstanceState }?.let {
            getTextDialog(it, titleResId, description).show()
        }
    }
}
