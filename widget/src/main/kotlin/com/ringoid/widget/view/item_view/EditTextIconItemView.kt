package com.ringoid.widget.view.item_view

import android.content.Context
import android.util.AttributeSet
import com.ringoid.widget.R
import kotlinx.android.synthetic.main.widget_edit_text_icon_item_view_layout.view.*

class EditTextIconItemView : IconItemView {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : super(context, attributes, defStyleAttr)

    override fun getLayoutId(): Int = R.layout.widget_edit_text_icon_item_view_layout

    /* API */
    // --------------------------------------------------------------------------------------------
    fun setInputText(text: String?) {
        et_input.setText(text)
    }
}
