package com.ringoid.origin.view.dialog

import android.app.Activity
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.ringoid.base.view.BaseActivity
import com.ringoid.origin.R

object Dialogs {

    fun getTextDialog(activity: Activity?, @StringRes titleResId: Int, description: String): AlertDialog =
        activity?.let {
            AlertDialog.Builder(activity)
                .setTitle(titleResId)
                .setMessage(description)
                .setPositiveButton(R.string.button_close, null)
                .create()
        } ?: throw NullPointerException("Unable to show dialog: Activity is null")

    fun showTextDialog(activity: BaseActivity<*>?, @StringRes titleResId: Int, description: String) {
        activity?.takeIf { !it.isAfterOnSaveInstanceState }
                ?.let { getTextDialog(it, titleResId, description).show() }
    }

    fun showTextDialog(activity: Activity?, @StringRes titleResId: Int, description: String) =
        activity?.takeIf { it is BaseActivity<*> }
                ?.let { showTextDialog(it as BaseActivity<*>, titleResId, description) }
                ?: getTextDialog(activity, titleResId, description).show()
}
