package com.ringoid.widget.view

import android.content.Context
import android.util.AttributeSet
import com.ringoid.widget.R
import com.ringoid.widget.view.item_view.LabelIconItemView

class SubLabelIconItemView : LabelIconItemView {

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr)

    override fun getLayoutId(): Int = R.layout.widget_sublabel_icon_item_view_layout
}
