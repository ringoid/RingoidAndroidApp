package com.ringoid.origin.view.dialog

import android.app.Activity
import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.ringoid.base.isActivityDestroyed
import com.ringoid.base.view.BaseActivity
import com.ringoid.origin.R
import com.ringoid.utility.randomLong

object Dialogs {

    private val registry = mutableSetOf<Long>()

    private fun hashOf(activity: Activity?, @StringRes titleResId: Int, description: String?,
                       @StringRes positiveBtnLabelResId: Int,
                       @StringRes negativeBtnLabelResId: Int): Long {
        var hash = 7L
        hash = 31 * hash + (activity?.hashCode() ?: 0)
        hash = 31 * hash + titleResId
        hash = 31 * hash + (description?.hashCode() ?: 0)
        hash = 31 * hash + positiveBtnLabelResId
        hash = 31 * hash + negativeBtnLabelResId
        return hash
    }

    private fun wrapListener(hash: Long, l: ((dialog: DialogInterface, which: Int) -> Unit)?)
        : ((dialog: DialogInterface, which: Int) -> Unit)? =
            { dialog, which ->
                registry.remove(hash)
                l?.invoke(dialog, which)
                dialog.dismiss()
            }

    data class HashAlertDialog(val dialog: AlertDialog, val hash: Long) {

        init {
            dialog.setOnDismissListener { registry.remove(hash) }
        }
    }

    // --------------------------------------------------------------------------------------------
    fun getTextDialog(
            activity: Activity?, @StringRes titleResId: Int = 0, @StringRes descriptionResId: Int,
            @StringRes positiveBtnLabelResId: Int = R.string.button_close,
            @StringRes negativeBtnLabelResId: Int = 0,
            positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
            negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null): HashAlertDialog =
        getTextDialog(activity, titleResId, activity?.resources?.getString(descriptionResId),
            positiveBtnLabelResId, negativeBtnLabelResId, positiveListener, negativeListener)

    fun getTextDialog(
            activity: Activity?, @StringRes titleResId: Int = 0, description: String? = null,
            @StringRes positiveBtnLabelResId: Int = R.string.button_close,
            @StringRes negativeBtnLabelResId: Int = 0,
            positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
            negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null): HashAlertDialog =
        activity?.let {
            val hash = hashOf(activity, titleResId, description, positiveBtnLabelResId, negativeBtnLabelResId)
            val builder = AlertDialog.Builder(activity)
            titleResId.takeIf { it != 0 }?.let { resId -> builder.also { it.setTitle(resId) } }
            description.takeIf { !it.isNullOrBlank() }?.let { str -> builder.also { it.setMessage(str) } }
            positiveBtnLabelResId.takeIf { it != 0 }?.let { resId -> builder.also { it.setPositiveButton(resId, positiveListener) } }
            negativeBtnLabelResId.takeIf { it != 0 }?.let { resId -> builder.also { it.setNegativeButton(resId, negativeListener) } }
            HashAlertDialog(dialog = builder.create(), hash = hash)
        } ?: throw NullPointerException("Unable to show dialog: Activity is null")

    fun showTextDialog(
            activity: BaseActivity<*>?, @StringRes titleResId: Int = 0, @StringRes descriptionResId: Int = 0,
            @StringRes positiveBtnLabelResId: Int = R.string.button_close,
            @StringRes negativeBtnLabelResId: Int = 0,
            positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
            negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null) {
        showTextDialog(activity, titleResId, descriptionResId.takeIf { it != 0}?.let { activity?.resources?.getString(it) },
            positiveBtnLabelResId, negativeBtnLabelResId, positiveListener, negativeListener)
    }

    fun showTextDialog(
            activity: BaseActivity<*>?, @StringRes titleResId: Int = 0, description: String? = null,
            @StringRes positiveBtnLabelResId: Int = R.string.button_close,
            @StringRes negativeBtnLabelResId: Int = 0,
            positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
            negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null) =
        activity?.takeIf { !it.isActivityDestroyed() }
                ?.let {
                    val dialog = getTextDialog(it, titleResId, description,
                        positiveBtnLabelResId, negativeBtnLabelResId,
                        positiveListener, negativeListener)
                    registry.takeIf { !it.contains(dialog.hash) }
                                ?.add(dialog.hash)
                                ?.also { dialog.dialog.show() }
                    dialog
                }

    fun showTextDialog(
            activity: Activity?, @StringRes titleResId: Int = 0, @StringRes descriptionResId: Int = 0,
            @StringRes positiveBtnLabelResId: Int = R.string.button_close,
            @StringRes negativeBtnLabelResId: Int = 0,
            positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
            negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null) =
        showTextDialog(activity, titleResId, descriptionResId.takeIf { it != 0 }?.let { activity?.resources?.getString(it) },
            positiveBtnLabelResId, negativeBtnLabelResId, positiveListener, negativeListener)

    fun showTextDialog(
            activity: Activity?, @StringRes titleResId: Int = 0, description: String? = null,
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
                ?: run {
                    val dialog = getTextDialog(
                        activity, titleResId, description,
                        positiveBtnLabelResId, negativeBtnLabelResId,
                        positiveListener, negativeListener)
                    registry.takeIf { !it.contains(dialog.hash) }
                                ?.add(dialog.hash)
                                ?.also { dialog.dialog.show() }
                    dialog
                }

    // --------------------------------------------------------------------------------------------
    fun getSingleChoiceDialog(activity: Activity?, items: Array<String>, l: ((dialog: DialogInterface, which: Int) -> Unit)? = null): AlertDialog? =
        activity?.let { AlertDialog.Builder(it).setItems(items, l).create() }

    fun showSingleChoiceDialog(activity: Activity?, items: Array<String>, l: ((dialog: DialogInterface, which: Int) -> Unit)? = null) =
        activity?.takeIf { !it.isActivityDestroyed() }?.let { getSingleChoiceDialog(activity, items, l)?.show() }

    // --------------------------------------------------------------------------------------------
    private fun getErrorDialog(activity: FragmentActivity?, e: Throwable? = null) =
        activity
            ?.takeIf { !it.isActivityDestroyed() }
            ?.let {
                val hash = randomLong().also { registry.add(it) }
                StatusDialog.newInstance(titleResId = R.string.error_common)
                    .apply { dialog?.setOnDismissListener { registry.remove(hash) } }
            }

    fun errorDialog(activity: FragmentActivity?, e: Throwable? = null) {
        getErrorDialog(activity, e)?.also { it.show(activity!!.supportFragmentManager, StatusDialog.TAG) }
    }

    fun errorDialog(fragment: Fragment, e: Throwable? = null) {
        getErrorDialog(fragment.activity, e)?.also { it.show(fragment.childFragmentManager, StatusDialog.TAG) }
    }
}
