package com.ringoid.widget.view.item_view

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.LayoutRes
import com.ringoid.widget.R
import kotlinx.android.synthetic.main.widget_count_text_icon_item_view_layout.view.*

class CountTextIconItemView : TextIconItemView {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : super(context, attributes, defStyleAttr)

    @LayoutRes
    override fun getLayoutId(): Int = R.layout.widget_count_text_icon_item_view_layout

    /* API */
    // --------------------------------------------------------------------------------------------
    @Suppress("SetTextI18n")
    override fun setInputText(text: String?): Boolean {
        tv_chars_count.text = "${text?.length ?: 0}/$MAX_LENGTH"
        return super.setInputText(text)
    }
}
