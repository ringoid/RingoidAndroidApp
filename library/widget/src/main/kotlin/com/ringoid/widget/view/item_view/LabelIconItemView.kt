package com.ringoid.widget.view.item_view

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.ringoid.widget.R
import kotlinx.android.synthetic.main.widget_label_icon_item_view_layout.view.*

open class LabelIconItemView : IconItemView {

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr) {
        with (context.obtainStyledAttributes(attributes, R.styleable.IconItemView, defStyleAttr, R.style.IconItemView)) {
            getResourceId(R.styleable.IconItemView_icon_item_text_label, 0)
                .takeIf { it != 0 }?.let { setLabel(resId = it) }
                ?: run { setLabel(text = getString(R.styleable.IconItemView_icon_item_text_label)) }
            recycle()
        }
    }

    @LayoutRes
    override fun getLayoutId(): Int = R.layout.widget_label_icon_item_view_layout

    /* API */
    // --------------------------------------------------------------------------------------------
    fun getLabel(): CharSequence = tv_label.text

    fun setLabel(@StringRes resId: Int) {
        tv_label.setText(resId)
    }

    fun setLabel(text: String?) {
        tv_label.text = text ?: ""
    }
}
