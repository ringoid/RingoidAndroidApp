package com.ringoid.origin.view.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.origin.R
import com.ringoid.origin.WidgetR_color
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.communicator

class EmptyFragment : Fragment() {

    companion object {
        data class Input(@LayoutRes val layoutResId: Int = R.layout.fragment_empty_text,
                         @StringRes val emptyTitleResId: Int = 0, @StringRes val emptyTextResId: Int = 0,
                         @ColorRes val labelTextColorResId: Int = WidgetR_color.secondary_text,
                         val isLabelClickable: Boolean = false)

        const val TAG = "EmptyFragment_tag"

        private const val BUNDLE_KEY_LAYOUT_RES_ID = "bundle_key_layout_res_id"
        private const val BUNDLE_KEY_LABEL_TEXT_COLOR_RES_ID = "bundle_key_label_text_color_res_id"
        private const val BUNDLE_KEY_EMPTY_TITLE_RES_ID = "bundle_key_empty_title_res_id"
        private const val BUNDLE_KEY_EMPTY_TEXT_RES_ID = "bundle_key_empty_text_res_id"
        private const val BUNDLE_KEY_FLAG_LABEL_CLICKABLE = "bundle_key_flag_label_clickable"

        fun newInstance(): EmptyFragment = newInstance(Input())

        fun newInstance(input: Input?): EmptyFragment =
            input?.let {
                EmptyFragment().apply {
                    arguments = Bundle().apply {
                        putInt(BUNDLE_KEY_LAYOUT_RES_ID, input.layoutResId)
                        putInt(BUNDLE_KEY_LABEL_TEXT_COLOR_RES_ID, input.labelTextColorResId)
                        putInt(BUNDLE_KEY_EMPTY_TITLE_RES_ID, input.emptyTitleResId)
                        putInt(BUNDLE_KEY_EMPTY_TEXT_RES_ID, input.emptyTextResId)
                        putBoolean(BUNDLE_KEY_FLAG_LABEL_CLICKABLE, input.isLabelClickable)
                    }
                }
            } ?: newInstance()
    }

    @LayoutRes private var layoutResId: Int = 0
    @ColorRes private var labelTextColorResId: Int = 0
    @StringRes private var emptyTitleResId: Int = 0
    @StringRes private var emptyTextResId: Int = 0
    private var isLabelClickable: Boolean = false

    /* Lifecycle */
    // --------------------------------------––-----––-––-––––--–----––----------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            layoutResId = it.getInt(BUNDLE_KEY_LAYOUT_RES_ID)
            labelTextColorResId = it.getInt(BUNDLE_KEY_LABEL_TEXT_COLOR_RES_ID)
            emptyTitleResId = it.getInt(BUNDLE_KEY_EMPTY_TITLE_RES_ID)
            emptyTextResId = it.getInt(BUNDLE_KEY_EMPTY_TEXT_RES_ID)
            isLabelClickable = it.getBoolean(BUNDLE_KEY_FLAG_LABEL_CLICKABLE, false)
        } ?: throw IllegalArgumentException("EmptyFragment requires non-null input arguments")
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(layoutResId, container, false)
            .apply {
                labelTextColorResId
                    .takeIf { it != 0 }
                    ?.let { ContextCompat.getColor(context, it) }
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
            }
    }
}
