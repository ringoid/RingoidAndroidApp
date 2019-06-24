package com.ringoid.origin.view.dialog

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.SimpleBaseDialogFragment
import com.ringoid.origin.R
import com.ringoid.utility.ICommunicator
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.communicator
import kotlinx.android.synthetic.main.dialog_big_edit_text.*

class BigEditTextDialog : SimpleBaseDialogFragment() {

    interface IBigEditTextDialogDone : ICommunicator {
        fun onDone(text: String, tag: String?)
    }

    companion object {
        const val TAG = "BigEditTextDialog_tag"

        private const val BUNDLE_KEY_BUTTON_NEGATIVE_RES_ID = "bundle_key_button_negative_res_id"
        private const val BUNDLE_KEY_BUTTON_POSITIVE_RES_ID = "bundle_key_button_positive_res_id"
        private const val BUNDLE_KEY_DESCRIPTION_RES_ID = "bundle_key_description_res_id"
        private const val BUNDLE_KEY_SUBTITLE_RES_ID = "bundle_key_subtitle_res_id"
        private const val BUNDLE_KEY_TITLE_RES_ID = "bundle_key_title_res_id"
        private const val BUNDLE_KEY_TAG = "bundle_key_tag"

        fun newInstance(@StringRes titleResId: Int,
                        @StringRes subtitleResId: Int = 0,
                        @StringRes descriptionResId: Int = 0,
                        @StringRes btnPositiveResId: Int = R.string.button_ok,
                        @StringRes btnNegativeResId: Int = R.string.button_cancel,
                        tag: String? = null)
                : BigEditTextDialog =
            BigEditTextDialog().apply {
                arguments = Bundle().apply {
                    putInt(BUNDLE_KEY_TITLE_RES_ID, titleResId)
                    putInt(BUNDLE_KEY_SUBTITLE_RES_ID, subtitleResId)
                    putInt(BUNDLE_KEY_DESCRIPTION_RES_ID, descriptionResId)
                    putInt(BUNDLE_KEY_BUTTON_POSITIVE_RES_ID, btnPositiveResId)
                    putInt(BUNDLE_KEY_BUTTON_NEGATIVE_RES_ID, btnNegativeResId)
                    putString(BUNDLE_KEY_TAG, tag)
                }
            }
    }

    @LayoutRes
    override fun getLayoutId(): Int = R.layout.dialog_big_edit_text

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btn_cancel.clicks().compose(clickDebounce()).subscribe { dismiss() }
        btn_done.clicks().compose(clickDebounce()).subscribe {
            val tag = arguments?.getString(BUNDLE_KEY_TAG)
            communicator(IBigEditTextDialogDone::class.java)?.onDone(text = et_dialog_entry.text.trim().toString(), tag = tag)
            dismiss()
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
        }
    }
}
