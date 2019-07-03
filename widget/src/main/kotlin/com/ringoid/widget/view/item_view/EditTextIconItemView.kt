package com.ringoid.widget.view.item_view

import android.content.Context
import android.util.AttributeSet
import com.jakewharton.rxbinding3.InitialValueObservable
import com.jakewharton.rxbinding3.widget.textChanges
import com.ringoid.widget.R
import kotlinx.android.synthetic.main.widget_edit_text_icon_item_view_layout.view.*

class EditTextIconItemView : TextIconItemView {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : super(context, attributes, defStyleAttr)

    override fun getLayoutId(): Int = R.layout.widget_edit_text_icon_item_view_layout

    /* API */
    // --------------------------------------------------------------------------------------------
    override fun getText(): String = et_input.text.toString()

    override fun setInputText(text: String?): Boolean {
        with (et_input) {
            setText(text)
            setSelection(text?.length ?: 0)
        }
        return true
    }
}

fun EditTextIconItemView.textChanges(): InitialValueObservable<CharSequence> = et_input.textChanges()
