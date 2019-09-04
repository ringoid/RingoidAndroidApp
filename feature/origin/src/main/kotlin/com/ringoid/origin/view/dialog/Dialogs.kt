package com.ringoid.origin.view.dialog

import android.app.Activity
import android.content.DialogInterface
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.ringoid.base.isActivityDestroyed
import com.ringoid.base.view.BaseActivity
import com.ringoid.origin.R
import com.ringoid.utility.randomLong
import com.ringoid.utility.showKeyboard
import kotlinx.android.synthetic.main.dialog_edit_text.view.*

object Dialogs {

    private val registry = mutableSetOf<Long>()

    private fun hashOf(activity: Activity?, @StringRes titleResId: Int, description: String?,
                       @StringRes positiveBtnLabelResId: Int,
                       @StringRes negativeBtnLabelResId: Int): Long =
        hashOf(activity, titleResId.takeIf { it != 0 }?.let { activity?.resources?.getString(it) },
               description, positiveBtnLabelResId, negativeBtnLabelResId)

    private fun hashOf(activity: Activity?, title: String?, description: String?,
                       @StringRes positiveBtnLabelResId: Int,
                       @StringRes negativeBtnLabelResId: Int): Long {
        var hash = 7L
        hash = 31 * hash + (activity?.hashCode() ?: 0)
        hash = 31 * hash + (title?.hashCode() ?: 0)
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
    private fun getTextDialog(
            activity: Activity?, @StringRes titleResId: Int = 0, @StringRes descriptionResId: Int,
            @StringRes positiveBtnLabelResId: Int = R.string.button_close,
            @StringRes negativeBtnLabelResId: Int = 0,
            positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
            negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
            isCancellable: Boolean = true): HashAlertDialog =
        getTextDialog(activity, titleResId, descriptionResId.takeIf { it != 0 }?.let { activity?.resources?.getString(it) },
            positiveBtnLabelResId, negativeBtnLabelResId, positiveListener, negativeListener, isCancellable)

    private fun getTextDialog(
            activity: Activity?, @StringRes titleResId: Int = 0, description: String? = null,
            @StringRes positiveBtnLabelResId: Int = R.string.button_close,
            @StringRes negativeBtnLabelResId: Int = 0,
            positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
            negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
            isCancellable: Boolean = true): HashAlertDialog =
        getTextDialog(activity, titleResId.takeIf { it != 0 }?.let { activity?.resources?.getString(it) }, description,
            positiveBtnLabelResId, negativeBtnLabelResId, positiveListener, negativeListener, isCancellable)

    fun getTextDialog(
            activity: Activity?, title: String? = null, description: String? = null,
            @StringRes positiveBtnLabelResId: Int = R.string.button_close,
            @StringRes negativeBtnLabelResId: Int = 0,
            positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
            negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
            isCancellable: Boolean = true): HashAlertDialog =
        activity?.let {
            val hash = hashOf(activity, title, description, positiveBtnLabelResId, negativeBtnLabelResId)
            val builder = AlertDialog.Builder(activity).setCancelable(isCancellable)
            title?.let { title -> builder.also { it.setTitle(title) } }
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
            negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
            isCancellable: Boolean = true) {
        showTextDialog(activity, titleResId, descriptionResId.takeIf { it != 0}?.let { activity?.resources?.getString(it) },
            positiveBtnLabelResId, negativeBtnLabelResId, positiveListener, negativeListener, isCancellable)
    }

    fun showTextDialog(
            activity: BaseActivity<*>?, @StringRes titleResId: Int = 0, description: String? = null,
            @StringRes positiveBtnLabelResId: Int = R.string.button_close,
            @StringRes negativeBtnLabelResId: Int = 0,
            positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
            negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
            isCancellable: Boolean = true) =
        showTextDialog(activity, titleResId.takeIf { it != 0 }?.let { activity?.resources?.getString(it) }, description,
            positiveBtnLabelResId, negativeBtnLabelResId, positiveListener, negativeListener, isCancellable)

    fun showTextDialog(
            activity: BaseActivity<*>?, title: String? = null, description: String? = null,
            @StringRes positiveBtnLabelResId: Int = R.string.button_close,
            @StringRes negativeBtnLabelResId: Int = 0,
            positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
            negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
            isCancellable: Boolean = true) =
        activity?.takeIf { !it.isActivityDestroyed() }
                ?.let { activity ->
                    val dialog = getTextDialog(activity, title, description,
                        positiveBtnLabelResId, negativeBtnLabelResId,
                        positiveListener, negativeListener, isCancellable)
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
            negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
            isCancellable: Boolean = true) =
        showTextDialog(activity, titleResId, descriptionResId.takeIf { it != 0 }?.let { activity?.resources?.getString(it) },
            positiveBtnLabelResId, negativeBtnLabelResId, positiveListener, negativeListener, isCancellable)

    fun showTextDialog(
            activity: Activity?, @StringRes titleResId: Int = 0, description: String? = null,
            @StringRes positiveBtnLabelResId: Int = R.string.button_close,
            @StringRes negativeBtnLabelResId: Int = 0,
            positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
            negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
            isCancellable: Boolean = true) =
        showTextDialog(activity, titleResId.takeIf { it != 0 }?.let { activity?.resources?.getString(it) }, description,
            positiveBtnLabelResId, negativeBtnLabelResId, positiveListener, negativeListener, isCancellable)

    fun showTextDialog(
            activity: Activity?, title: String? = null, description: String? = null,
            @StringRes positiveBtnLabelResId: Int = R.string.button_close,
            @StringRes negativeBtnLabelResId: Int = 0,
            positiveListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
            negativeListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
            isCancellable: Boolean = true) =
        activity?.takeIf { it is BaseActivity<*> }
                ?.let {
                    showTextDialog(it as BaseActivity<*>, title, description,
                        positiveBtnLabelResId, negativeBtnLabelResId,
                        positiveListener, negativeListener, isCancellable)
                }
                ?: run {
                    val dialog = getTextDialog(
                        activity, title, description,
                        positiveBtnLabelResId, negativeBtnLabelResId,
                        positiveListener, negativeListener, isCancellable)
                    registry.takeIf { !it.contains(dialog.hash) }
                            ?.add(dialog.hash)
                            ?.also { dialog.dialog.show() }
                    dialog
                }

    // -------------------------------–––––––--------––---––---------------------------------------
    private fun getEditTextDialog(
        activity: Activity?, @StringRes titleResId: Int = 0, @StringRes hintRestId: Int = 0,
        @StringRes positiveBtnLabelResId: Int = R.string.button_close,
        @StringRes negativeBtnLabelResId: Int = 0,
        positiveListener: ((dialog: DialogInterface, which: Int, inputText: String?) -> Unit)? = null,
        negativeListener: ((dialog: DialogInterface, which: Int, inputText: String?) -> Unit)? = null,
        cancelListener: ((dialog: DialogInterface, inputText: String?) -> Unit)? = null,
        initText: String? = null, inputType: Int = InputType.TYPE_CLASS_TEXT, maxLength: Int = Int.MAX_VALUE,
        imeOptions: Int = EditorInfo.IME_NULL, imeActionListener: ((dialog: DialogInterface?) -> Unit)? = null)
            :HashAlertDialog {
        fun getInputText(view: View?): String? = view?.et_dialog_entry?.text?.toString()

        return activity?.let { activity ->
            var xdialog: DialogInterface? = null  // lateinit reference to dialog being built
            val hash = hashOf(activity, titleResId, hintRestId.takeIf { it != 0}?.let { activity.resources.getString(it) }, positiveBtnLabelResId, negativeBtnLabelResId)
            val view = LayoutInflater.from(activity).inflate(R.layout.dialog_edit_text, null)
                .apply {
                    with (et_dialog_entry) {
                        initText?.let { setText(it); setSelection(it.length) }
                        this.inputType = inputType
                        this.filters = arrayOf(InputFilter.LengthFilter(maxLength))
                        // IME options
                        if (imeOptions != EditorInfo.IME_NULL) {
                            this.imeOptions = imeOptions
                            setOnEditorActionListener { et, actionId, _ /* event */ ->
                                if (actionId == et.imeOptions) {
                                    imeActionListener?.invoke(xdialog)
                                        ?: run { positiveListener?.invoke(xdialog!!, 0, text.toString()) }
                                    true
                                } else false
                            }
                        }
                    }
                }
            val builder = AlertDialog.Builder(activity).setView(view)
            titleResId.takeIf { it != 0 }?.let { resId -> builder.also { it.setTitle(resId) } }
            positiveBtnLabelResId.takeIf { it != 0 }?.let { resId -> builder.also { it.setPositiveButton(resId) { dialog, which -> positiveListener?.invoke(dialog, which, getInputText(view)) } } }
            negativeBtnLabelResId.takeIf { it != 0 }?.let { resId -> builder.also { it.setNegativeButton(resId) { dialog, which -> negativeListener?.invoke(dialog, which, getInputText(view)) } } }
            xdialog = builder.create().apply { setOnCancelListener { dialog -> cancelListener?.invoke(dialog, getInputText(view)) } }
            HashAlertDialog(dialog = xdialog, hash = hash)
        } ?: throw NullPointerException("Unable to show dialog: Activity is null")
    }

    fun showEditTextDialog(
            activity: BaseActivity<*>?, @StringRes titleResId: Int = 0, @StringRes hintRestId: Int = 0,
            @StringRes positiveBtnLabelResId: Int = R.string.button_close,
            @StringRes negativeBtnLabelResId: Int = 0,
            positiveListener: ((dialog: DialogInterface, which: Int, inputText: String?) -> Unit)? = null,
            negativeListener: ((dialog: DialogInterface, which: Int, inputText: String?) -> Unit)? = null,
            cancelListener: ((dialog: DialogInterface, inputText: String?) -> Unit)? = null,
            initText: String? = null, inputType: Int = InputType.TYPE_CLASS_TEXT, maxLength: Int = Int.MAX_VALUE,
            imeOptions: Int = EditorInfo.IME_NULL, imeActionListener: ((dialog: DialogInterface?) -> Unit)? = null) =
        activity?.takeIf { !it.isActivityDestroyed() }
                ?.let { activity ->
                    val dialog = getEditTextDialog(activity, titleResId, hintRestId,
                        positiveBtnLabelResId, negativeBtnLabelResId,
                        positiveListener, negativeListener, cancelListener,
                        initText, inputType, maxLength,
                        imeOptions, imeActionListener)
                    registry.takeIf { !it.contains(dialog.hash) }
                            ?.add(dialog.hash)
                            ?.let { dialog.dialog }
                            ?.also { it.show() }
                            ?.also { it.window?.showKeyboard() }
                    dialog
                }

    fun showEditTextDialog(
            activity: Activity?, @StringRes titleResId: Int = 0, @StringRes hintRestId: Int = 0,
            @StringRes positiveBtnLabelResId: Int = R.string.button_close,
            @StringRes negativeBtnLabelResId: Int = 0,
            positiveListener: ((dialog: DialogInterface, which: Int, inputText: String?) -> Unit)? = null,
            negativeListener: ((dialog: DialogInterface, which: Int, inputText: String?) -> Unit)? = null,
            cancelListener: ((dialog: DialogInterface, inputText: String?) -> Unit)? = null,
            initText: String? = null, inputType: Int = InputType.TYPE_CLASS_TEXT, maxLength: Int = Int.MAX_VALUE,
            imeOptions: Int = EditorInfo.IME_NULL, imeActionListener: ((dialog: DialogInterface?) -> Unit)? = null) =
        activity?.takeIf { it is BaseActivity<*> }
            ?.let {
                showEditTextDialog(it as BaseActivity<*>, titleResId, hintRestId,
                    positiveBtnLabelResId, negativeBtnLabelResId,
                    positiveListener, negativeListener, cancelListener,
                    initText, inputType, maxLength,
                    imeOptions, imeActionListener)
            }
            ?: run {
                val dialog = getEditTextDialog(
                    activity, titleResId, hintRestId,
                    positiveBtnLabelResId, negativeBtnLabelResId,
                    positiveListener, negativeListener, cancelListener,
                    initText, inputType, maxLength,
                    imeOptions, imeActionListener)
                registry.takeIf { !it.contains(dialog.hash) }
                        ?.add(dialog.hash)
                        ?.let { dialog.dialog }
                        ?.also { it.show() }
                        ?.also { it.window?.showKeyboard() }
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

    private fun getSupportDialog(activity: FragmentActivity?, @StringRes descriptionResId: Int) =
        activity
            ?.takeIf { !it.isActivityDestroyed() }
            ?.let {
                val hash = randomLong().also { registry.add(it) }
                SupportDialog.newInstance(titleResId = R.string.error_common, descriptionResId = descriptionResId)
                    .apply { dialog?.setOnDismissListener { registry.remove(hash) } }
            }

    fun errorDialog(activity: FragmentActivity?, e: Throwable? = null) {
        getErrorDialog(activity, e)?.also { it.show(activity!!.supportFragmentManager, StatusDialog.TAG) }
    }

    fun errorDialog(fragment: Fragment, e: Throwable? = null) {
        getErrorDialog(fragment.activity, e)?.also { it.show(fragment.childFragmentManager, StatusDialog.TAG) }
    }

    fun supportDialog(activity: FragmentActivity?, @StringRes descriptionResId: Int) {
        getSupportDialog(activity, descriptionResId)?.also { it.show(activity!!.supportFragmentManager, SupportDialog.TAG) }
    }

    fun supportDialog(fragment: Fragment, @StringRes descriptionResId: Int) {
        getSupportDialog(fragment.activity, descriptionResId)?.also { it.show(fragment.childFragmentManager, SupportDialog.TAG) }
    }
}
