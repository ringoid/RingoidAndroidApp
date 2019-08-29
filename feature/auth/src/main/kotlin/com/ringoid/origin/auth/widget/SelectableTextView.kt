package com.ringoid.origin.auth.widget

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.TextView

class SelectableTextView : TextView {

    companion object {
        private const val BUNDLE_KEY_FLAG_IS_SELECTED = "bundle_key_flag_is_selected"
        private const val BUNDLE_KEY_SUPER_STATE = "superState"
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : super(context, attributes, defStyleAttr) {
        isClickable = true
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val xstate = state.let { it as? Bundle }
            ?.let {
                isSelected = it.getBoolean(BUNDLE_KEY_FLAG_IS_SELECTED, false)
                it.getParcelable(BUNDLE_KEY_SUPER_STATE) as Parcelable
            } ?: state
        super.onRestoreInstanceState(xstate)
    }

    override fun onSaveInstanceState(): Parcelable? =
        Bundle().apply {
            putParcelable(BUNDLE_KEY_SUPER_STATE, super.onSaveInstanceState())
            putBoolean(BUNDLE_KEY_FLAG_IS_SELECTED, isSelected)
        }
}
