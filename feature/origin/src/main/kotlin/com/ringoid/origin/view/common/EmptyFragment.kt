package com.ringoid.origin.view.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.origin.R
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.communicator

class EmptyFragment : Fragment() {

    companion object {
        data class Input(@LayoutRes val layoutResId: Int = R.layout.fragment_empty_text,
                         @StringRes val emptyTitleResId: Int = 0,
                         @StringRes val emptyTextResId: Int = 0,
                         @ColorInt val labelTextColor: Int = 0,
                         val emptyLabelText: String? = null,
                         val isLabelClickable: Boolean = false)

        const val TAG = "EmptyFragment_tag"

        private const val BUNDLE_KEY_LAYOUT_RES_ID = "bundle_key_layout_res_id"
        private const val BUNDLE_KEY_LABEL_TEXT_COLOR = "bundle_key_label_text_color"
        private const val BUNDLE_KEY_EMPTY_TITLE_RES_ID = "bundle_key_empty_title_res_id"
        private const val BUNDLE_KEY_EMPTY_TEXT_RES_ID = "bundle_key_empty_text_res_id"
        private const val BUNDLE_KEY_EMPTY_LABEL_TEXT = "bundle_key_empty_label_text"
        private const val BUNDLE_KEY_FLAG_LABEL_CLICKABLE = "bundle_key_flag_label_clickable"

        fun newInstance(): EmptyFragment = newInstance(Input())

        fun newInstance(input: Input?): EmptyFragment =
            input?.let {
                EmptyFragment().apply {
                    arguments = Bundle().apply {
                        putInt(BUNDLE_KEY_LAYOUT_RES_ID, input.layoutResId)
                        putInt(BUNDLE_KEY_LABEL_TEXT_COLOR, input.labelTextColor)
                        putInt(BUNDLE_KEY_EMPTY_TITLE_RES_ID, input.emptyTitleResId)
                        putInt(BUNDLE_KEY_EMPTY_TEXT_RES_ID, input.emptyTextResId)
                        putString(BUNDLE_KEY_EMPTY_LABEL_TEXT, input.emptyLabelText)
                        putBoolean(BUNDLE_KEY_FLAG_LABEL_CLICKABLE, input.isLabelClickable)
                    }
                }
            } ?: newInstance()
    }

    @LayoutRes private var layoutResId: Int = 0
    @ColorInt private var labelTextColor: Int = 0
    @StringRes private var emptyTitleResId: Int = 0
    @StringRes private var emptyTextResId: Int = 0
    private var emptyLabelText: String? = null
    private var isLabelClickable: Boolean = false

    /* Lifecycle */
    // --------------------------------------––-----––-––-––––--–----––----------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            layoutResId = it.getInt(BUNDLE_KEY_LAYOUT_RES_ID)
            labelTextColor = it.getInt(BUNDLE_KEY_LABEL_TEXT_COLOR)
            emptyTitleResId = it.getInt(BUNDLE_KEY_EMPTY_TITLE_RES_ID)
            emptyTextResId = it.getInt(BUNDLE_KEY_EMPTY_TEXT_RES_ID)
            emptyLabelText = it.getString(BUNDLE_KEY_EMPTY_LABEL_TEXT)
            isLabelClickable = it.getBoolean(BUNDLE_KEY_FLAG_LABEL_CLICKABLE, false)
        } ?: throw IllegalArgumentException("EmptyFragment requires non-null input arguments")
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(layoutResId, container, false)
            .apply {
                labelTextColor
                    .takeIf { it != 0 }
                    ?.let { findViewById<TextView>(R.id.tv_empty)?.setTextColor(it) }
                emptyTitleResId
                    .takeIf { it != 0 }
                    ?.let { findViewById<TextView>(R.id.tv_empty_title)?.setText(it) }
                emptyTextResId
                    .takeIf { it != 0 }
                    ?.let {
                        findViewById<TextView>(R.id.tv_empty)?.let { tv ->
                            tv.setText(it)
                            if (isLabelClickable) {
                                tv.clicks().compose(clickDebounce()).subscribe {
                                    communicator(IEmptyScreenCallback::class.java)?.onEmptyLabelClick()
                                }
                            }
                        }
                    }
                emptyLabelText
                    .takeIf { !it.isNullOrBlank() }
                    ?.let {
                        findViewById<TextView>(R.id.tv_empty)?.let { tv ->
                            tv.text = it
                            if (isLabelClickable) {
                                tv.clicks().compose(clickDebounce()).subscribe {
                                    communicator(IEmptyScreenCallback::class.java)?.onEmptyLabelClick()
                                }
                            }
                        }
                    }
            }
    }
}
