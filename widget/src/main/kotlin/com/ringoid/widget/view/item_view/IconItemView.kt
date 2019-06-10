package com.ringoid.widget.view.item_view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.getSelectableItemBg
import com.ringoid.widget.R
import kotlinx.android.synthetic.main.widget_icon_item_view_layout.view.*

open class IconItemView : LinearLayout {

    private var hideIcon: Boolean = true

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr) {
        init(context, attributes, defStyleAttr)
    }

    @LayoutRes
    protected open fun getLayoutId(): Int = R.layout.widget_icon_item_view_layout

    private fun init(context: Context, attributes: AttributeSet?, defStyleAttr: Int) {
        val sidePadding = resources.getDimensionPixelSize(R.dimen.std_margin_20)
        background = context.getSelectableItemBg()
        isClickable = true
        isFocusable = true
        orientation = HORIZONTAL
        setPadding(sidePadding, paddingTop, sidePadding, paddingBottom)

        LayoutInflater.from(context).inflate(getLayoutId(), this, true)

        context.obtainStyledAttributes(attributes, R.styleable.IconItemView, defStyleAttr, R.style.IconItemView)
            .apply {
                hideIcon = getBoolean(R.styleable.IconItemView_icon_item_hide_icon, true)
                setIconColorRes(colorResId = getResourceId(R.styleable.IconItemView_icon_item_icon_color, R.color.icons))
                setTextColorRes(colorResId = getResourceId(R.styleable.IconItemView_icon_item_text_color, R.color.secondary_text))
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
        resId.takeIf { it != 0 }
            ?.let { iv_icon.setImageResource(it) }
            ?: run { iv_icon.changeVisibility(isVisible = false, soft = !hideIcon) }
    }

    fun setIconColorRes(@ColorRes colorResId: Int) {
        val color = ContextCompat.getColor(context, colorResId)
        iv_icon.setColorFilter(color)
    }

    fun setTextColorRes(@ColorRes colorResId: Int) {
        val color = ContextCompat.getColor(context, colorResId)
        tv_text.setTextColor(color)
    }

    fun setText(@StringRes resId: Int) {
        tv_text.setText(resId)
    }

    fun setText(text: String?) {
        tv_text.text = text ?: ""
    }
}
