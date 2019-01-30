package com.ringoid.origin.view.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.ringoid.origin.R

class EmptyFragment : Fragment() {

    companion object {
        data class Input(@LayoutRes val layoutResId: Int = R.layout.fragment_empty_text,
                         @StringRes val emptyTitleResId: Int = 0, @StringRes val emptyTextResId: Int)

        const val TAG = "EmptyFragment_tag"

        private const val BUNDLE_KEY_LAYOUT_RES_ID = "bundle_key_layout_res_id"
        private const val BUNDLE_KEY_EMPTY_TITLE_RES_ID = "bundle_key_empty_title_res_id"
        private const val BUNDLE_KEY_EMPTY_TEXT_RES_ID = "bundle_key_empty_text_res_id"

        fun newInstance(input: Input): EmptyFragment =
            EmptyFragment().apply {
                arguments = Bundle().apply {
                    putInt(BUNDLE_KEY_LAYOUT_RES_ID, input.layoutResId)
                    putInt(BUNDLE_KEY_EMPTY_TITLE_RES_ID, input.emptyTitleResId)
                    putInt(BUNDLE_KEY_EMPTY_TEXT_RES_ID, input.emptyTextResId)
                }
            }
    }

    private var layoutResId: Int = 0
    private var emptyTitleResId: Int = 0
    private var emptyTextResId: Int = 0

    /* Lifecycle */
    // --------------------------------------––-----––-––-––––--–----––----------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            layoutResId = it.getInt(BUNDLE_KEY_LAYOUT_RES_ID)
            emptyTitleResId = it.getInt(BUNDLE_KEY_EMPTY_TITLE_RES_ID)
            emptyTextResId = it.getInt(BUNDLE_KEY_EMPTY_TEXT_RES_ID)
        } ?: throw IllegalArgumentException("EmptyFragment requires non-null input arguments")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(layoutResId, container, false)
            .apply {
                emptyTitleResId
                    .takeIf { it != 0 }
                    ?.let { findViewById<TextView>(R.id.tv_empty_title)?.setText(it) }
                findViewById<TextView>(R.id.tv_empty)?.setText(emptyTextResId)
            }
    }
}
