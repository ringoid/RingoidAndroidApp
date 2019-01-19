package com.ringoid.origin.view.dialog

import android.app.Activity
import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.ringoid.base.isActivityDestroyed
import com.ringoid.base.view.BaseActivity
import com.ringoid.origin.R

object Dialogs {

    // --------------------------------------------------------------------------------------------
    fun getTextDialog(activity: Activity?, @StringRes titleResId: Int = 0, @StringRes descriptionResId: Int,
                      @StringRes positiveBtnLabelResId: Int = R.string.button_close,
                      @StringRes negativeBtnLabelResId: Int = 0,
                      positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
                      negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null): AlertDialog =
        getTextDialog(activity, titleResId, activity?.resources?.getString(descriptionResId),
            positiveBtnLabelResId, negativeBtnLabelResId, positiveListener, negativeListener)

    fun getTextDialog(activity: Activity?, @StringRes titleResId: Int = 0, description: String? = null,
                      @StringRes positiveBtnLabelResId: Int = R.string.button_close,
                      @StringRes negativeBtnLabelResId: Int = 0,
                      positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
                      negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null): AlertDialog =
        activity?.let {
            val builder = AlertDialog.Builder(activity)
            titleResId.takeIf { it != 0 }?.let { resId -> builder.also { it.setTitle(resId) } }
            description.takeIf { !it.isNullOrBlank() }?.let { str -> builder.also { it.setMessage(str) } }
            positiveBtnLabelResId.takeIf { it != 0 }?.let { resId -> builder.also { it.setPositiveButton(resId, positiveListener) } }
            negativeBtnLabelResId.takeIf { it != 0 }?.let { resId -> builder.also { it.setNegativeButton(resId, negativeListener) } }
            builder.create()
        } ?: throw NullPointerException("Unable to show dialog: Activity is null")

    fun showTextDialog(activity: BaseActivity<*>?, @StringRes titleResId: Int = 0, @StringRes descriptionResId: Int = 0,
                       @StringRes positiveBtnLabelResId: Int = R.string.button_close,
                       @StringRes negativeBtnLabelResId: Int = 0,
                       positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
                       negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null) {
        showTextDialog(activity, titleResId, descriptionResId.takeIf { it != 0}?.let { activity?.resources?.getString(it) },
            positiveBtnLabelResId, negativeBtnLabelResId, positiveListener, negativeListener)
    }

    fun showTextDialog(activity: BaseActivity<*>?, @StringRes titleResId: Int = 0, description: String? = null,
                       @StringRes positiveBtnLabelResId: Int = R.string.button_close,
                       @StringRes negativeBtnLabelResId: Int = 0,
                       positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
                       negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null) {
        activity?.takeIf { !it.isActivityDestroyed() }
                ?.let {
                    getTextDialog(it, titleResId, description,
                        positiveBtnLabelResId, negativeBtnLabelResId,
                        positiveListener, negativeListener).show()
                }
    }

    fun showTextDialog(activity: Activity?, @StringRes titleResId: Int = 0, @StringRes descriptionResId: Int = 0,
                       @StringRes positiveBtnLabelResId: Int = R.string.button_close,
                       @StringRes negativeBtnLabelResId: Int = 0,
                       positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
                       negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null) =
        showTextDialog(activity, titleResId, descriptionResId.takeIf { it != 0 }?.let { activity?.resources?.getString(it) },
            positiveBtnLabelResId, negativeBtnLabelResId, positiveListener, negativeListener)

    fun showTextDialog(activity: Activity?, @StringRes titleResId: Int = 0, description: String? = null,
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

    // --------------------------------------------------------------------------------------------
    fun getSingleChoiceDialog(activity: Activity?, items: Array<String>, l: ((dialog: DialogInterface, which: Int) -> Unit)? = null): AlertDialog? =
        activity?.let { AlertDialog.Builder(it).setItems(items, l).create() }

    fun showSingleChoiceDialog(activity: Activity?, items: Array<String>, l: ((dialog: DialogInterface, which: Int) -> Unit)? = null) =
        activity?.takeIf { !it.isActivityDestroyed() }?.let { getSingleChoiceDialog(activity, items, l)?.show() }
}
