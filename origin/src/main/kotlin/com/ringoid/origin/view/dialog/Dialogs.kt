package com.ringoid.origin.view.dialog

import android.app.Activity
import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.ringoid.base.view.BaseActivity
import com.ringoid.origin.R

object Dialogs {

    // --------------------------------------------------------------------------------------------
    fun getTextDialog(activity: Activity?, @StringRes titleResId: Int, @StringRes descriptionResId: Int,
                      @StringRes positiveBtnLabelResId: Int = R.string.button_close,
                      @StringRes negativeBtnLabelResId: Int = 0,
                      positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
                      negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null): AlertDialog =
        getTextDialog(activity, titleResId, activity?.resources?.getString(descriptionResId),
            positiveBtnLabelResId, negativeBtnLabelResId, positiveListener, negativeListener)

    fun getTextDialog(activity: Activity?, @StringRes titleResId: Int, description: String? = null,
                      @StringRes positiveBtnLabelResId: Int = R.string.button_close,
                      @StringRes negativeBtnLabelResId: Int = 0,
                      positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
                      negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null): AlertDialog =
        activity?.let {
            val builder = AlertDialog.Builder(activity)
                .setTitle(titleResId)
                .setPositiveButton(positiveBtnLabelResId, positiveListener)

            if (!description.isNullOrBlank()) {
                builder.also { it.setMessage(description) }
            }
            if (negativeBtnLabelResId != 0) {
                builder.also { it.setNegativeButton(negativeBtnLabelResId, negativeListener) }
            }

            builder.create()
        } ?: throw NullPointerException("Unable to show dialog: Activity is null")

    fun showTextDialog(activity: BaseActivity<*>?, @StringRes titleResId: Int, @StringRes descriptionResId: Int,
                       @StringRes positiveBtnLabelResId: Int = R.string.button_close,
                       @StringRes negativeBtnLabelResId: Int = 0,
                       positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
                       negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null) {
        showTextDialog(activity, titleResId, activity?.resources?.getString(descriptionResId),
            positiveBtnLabelResId, negativeBtnLabelResId, positiveListener, negativeListener)
    }

    fun showTextDialog(activity: BaseActivity<*>?, @StringRes titleResId: Int, description: String? = null,
                       @StringRes positiveBtnLabelResId: Int = R.string.button_close,
                       @StringRes negativeBtnLabelResId: Int = 0,
                       positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
                       negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null) {
        activity?.takeIf { !it.isAfterOnSaveInstanceState }
                ?.let {
                    getTextDialog(it, titleResId, description,
                        positiveBtnLabelResId, negativeBtnLabelResId,
                        positiveListener, negativeListener).show()
                }
    }

    fun showTextDialog(activity: Activity?, @StringRes titleResId: Int, @StringRes descriptionResId: Int,
                       @StringRes positiveBtnLabelResId: Int = R.string.button_close,
                       @StringRes negativeBtnLabelResId: Int = 0,
                       positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
                       negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null) =
        showTextDialog(activity, titleResId, activity?.resources?.getString(descriptionResId),
            positiveBtnLabelResId, negativeBtnLabelResId, positiveListener, negativeListener)

    fun showTextDialog(activity: Activity?, @StringRes titleResId: Int, description: String? = null,
                       @StringRes positiveBtnLabelResId: Int = R.string.button_close,
                       @StringRes negativeBtnLabelResId: Int = 0,
                       positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
                       negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null) =
        activity?.takeIf { it is BaseActivity<*> }
                ?.let {
                    showTextDialog(it as BaseActivity<*>, titleResId, description,
                        positiveBtnLabelResId, negativeBtnLabelResId,
                        positiveListener, negativeListener)
                }
                ?: getTextDialog(activity, titleResId, description,
                    positiveBtnLabelResId, negativeBtnLabelResId,
                    positiveListener, negativeListener).show()
}
