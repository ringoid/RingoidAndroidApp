package com.ringoid.widget.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.TypedArrayUtils.getResourceId
import com.ringoid.utility.getSelectableItemBg
import com.ringoid.widget.R
import kotlinx.android.synthetic.main.widget_icon_item_view_layout.view.*

class IconItemView : LinearLayout {

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr) {
        init(context, attributes, defStyleAttr)
    }

    private fun init(context: Context, attributes: AttributeSet?, defStyleAttr: Int) {
        val sidePadding = resources.getDimensionPixelSize(R.dimen.std_text_20)
        background = context.getSelectableItemBg()
        orientation = LinearLayout.VERTICAL
        setPadding(sidePadding, paddingTop, sidePadding, paddingBottom)
        LayoutInflater.from(context).inflate(R.layout.widget_icon_item_view_layout, this, true)
        context.obtainStyledAttributes(attributes, R.styleable.IconItemView, defStyleAttr, R.style.IconItemView)
            .apply {
                setLabelColorRes(colorResId = getResourceId(R.styleable.IconItemView_icon_item_color, R.color.secondary_text))
                setIcon(resId = getResourceId(R.styleable.IconItemView_icon_item_icon, 0))
                getResourceId(R.styleable.IconItemView_icon_item_text, 0)
                    .takeIf { it != 0 }?.let { setText(resId = it) }
                    ?: run { setText(text = getString(R.styleable.IconItemView_icon_item_text)) }
                recycle()
            }
    }

    /* API */
    // --------------------------------------------------------------------------------------------
    fun setIcon(@DrawableRes resId: Int) {
        iv_icon.setImageResource(resId)
    }

    fun setLabelColorRes(@ColorRes colorResId: Int) {
        val color = ContextCompat.getColor(context, colorResId)
        iv_icon.setColorFilter(color)
        tv_text.setTextColor(color)
    }

    fun setText(@StringRes resId: Int) {
        tv_text.setText(resId)
    }

    fun setText(text: String?) {
        tv_text.text = text ?: ""
    }
}
