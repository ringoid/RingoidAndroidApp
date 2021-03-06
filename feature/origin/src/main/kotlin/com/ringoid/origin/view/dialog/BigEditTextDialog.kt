package com.ringoid.origin.view.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import com.ringoid.base.view.SimpleBaseDialogFragment
import com.ringoid.origin.R
import com.ringoid.utility.*
import kotlinx.android.synthetic.main.dialog_big_edit_text.*

class BigEditTextDialog : SimpleBaseDialogFragment() {

    interface IBigEditTextDialogDone : ICommunicator {
        fun onCancel(text: String, tag: String?, fromBtn: Boolean)
        fun onDone(text: String, tag: String?)
    }

    companion object {
        const val TAG = "BigEditTextDialog_tag"

        private const val BUNDLE_KEY_BUTTON_NEGATIVE_RES_ID = "bundle_key_button_negative_res_id"
        private const val BUNDLE_KEY_BUTTON_POSITIVE_RES_ID = "bundle_key_button_positive_res_id"
        private const val BUNDLE_KEY_DESCRIPTION_RES_ID = "bundle_key_description_res_id"
        private const val BUNDLE_KEY_FLAG_CLOSE_ON_ENTER = "bundle_key_flag_close_on_enter"
        private const val BUNDLE_KEY_SUBTITLE_RES_ID = "bundle_key_subtitle_res_id"
        private const val BUNDLE_KEY_TITLE_RES_ID = "bundle_key_title_res_id"
        private const val BUNDLE_KEY_INPUT = "bundle_key_input"
        private const val BUNDLE_KEY_TAG = "bundle_key_tag"

        fun newInstance(
                @StringRes titleResId: Int,
                @StringRes subtitleResId: Int = 0,
                @StringRes descriptionResId: Int = 0,
                @StringRes btnPositiveResId: Int = R.string.button_ok,
                @StringRes btnNegativeResId: Int = R.string.button_cancel,
                closeOnEnter: Boolean = false, input: String = "", tag: String? = null)
                : BigEditTextDialog =
            BigEditTextDialog().apply {
                arguments = Bundle().apply {
                    putInt(BUNDLE_KEY_TITLE_RES_ID, titleResId)
                    putInt(BUNDLE_KEY_SUBTITLE_RES_ID, subtitleResId)
                    putInt(BUNDLE_KEY_DESCRIPTION_RES_ID, descriptionResId)
                    putBoolean(BUNDLE_KEY_FLAG_CLOSE_ON_ENTER, closeOnEnter)
                    putInt(BUNDLE_KEY_BUTTON_POSITIVE_RES_ID, btnPositiveResId)
                    putInt(BUNDLE_KEY_BUTTON_NEGATIVE_RES_ID, btnNegativeResId)
                    putString(BUNDLE_KEY_INPUT, input)
                    putString(BUNDLE_KEY_TAG, tag)
                }
            }
    }

    @LayoutRes
    override fun getLayoutId(): Int = R.layout.dialog_big_edit_text

    private var dialogTag: String? = null
    private var fromBtn: Boolean = false

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = super.onCreateView(inflater, container, savedInstanceState)
        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return rootView
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialogTag = arguments?.getString(BUNDLE_KEY_TAG)
        fromBtn = false

        btn_cancel.clicks().compose(clickDebounce()).subscribe { fromBtn = true; cancel() }
        btn_done.clicks().compose(clickDebounce()).subscribe {
            communicator(IBigEditTextDialogDone::class.java)?.onDone(text = getText(), tag = dialogTag)
            dismiss()
        }

        with (et_dialog_entry) {
            imeOptions = EditorInfo.IME_ACTION_DONE
            setOnEditorActionListener { _, actionId, event ->
                when (actionId) {
                    EditorInfo.IME_NULL -> {
                        if (event.action == KeyEvent.ACTION_DOWN) {
                            btn_done?.performClick()
                            true
                        } else false
                    }
                    EditorInfo.IME_ACTION_DONE -> { btn_done?.performClick(); true }
                    else -> false
                }
            }
            arguments?.let { args ->
                args.getBoolean(BUNDLE_KEY_FLAG_CLOSE_ON_ENTER)
                    .takeIf { it }
                    ?.let {
                        textChanges().compose(inputDebounce()).subscribe {
                            if (it.endsWith('\n')) btn_done?.performClick()
                        }
                    }
            }
        }

        arguments?.let { args ->
            tv_dialog_title.setText(args.getInt(BUNDLE_KEY_TITLE_RES_ID))

            args.getInt(BUNDLE_KEY_SUBTITLE_RES_ID)
                .takeIf { it != 0 }
                ?.let { tv_dialog_subtitle.setText(it) }
                ?: run { tv_dialog_subtitle.changeVisibility(isVisible = false) }

            args.getInt(BUNDLE_KEY_DESCRIPTION_RES_ID)
                .takeIf { it != 0 }
                ?.let { tv_dialog_description.setText(it) }
                ?: run { tv_dialog_description.changeVisibility(isVisible = false) }

            args.getInt(BUNDLE_KEY_BUTTON_POSITIVE_RES_ID).takeIf { it != 0 }?.let { btn_done.setText(it) }
            args.getInt(BUNDLE_KEY_BUTTON_NEGATIVE_RES_ID).takeIf { it != 0 }?.let { btn_cancel.setText(it) }

            args.getString(BUNDLE_KEY_INPUT)
                .takeIf { !it.isNullOrBlank() }
                ?.let {
                    with (et_dialog_entry) {
                        setText(it)
                        setSelection(it.length)
                    }
                }
        }
    }

    private fun cancel() {
        dialog?.cancel()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        communicator(IBigEditTextDialogDone::class.java)?.onCancel(text = getText(), tag = dialogTag, fromBtn = fromBtn)
    }

    // --------------------------------------------------------------------------------------------
    private fun getText(): String =
        et_dialog_entry.text.trim().replace("\\s+".toRegex(), " ")
}
