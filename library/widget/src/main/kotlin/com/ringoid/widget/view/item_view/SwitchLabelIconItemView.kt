package com.ringoid.widget.view.item_view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import androidx.annotation.StringRes
import com.ringoid.utility.changeVisibility
import com.ringoid.widget.R
import kotlinx.android.synthetic.main.widget_switch_label_icon_item_view_layout.view.*

class SwitchLabelIconItemView : SwitchIconItemView {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : super(context, attributes, defStyleAttr) {
        with (context.obtainStyledAttributes(attributes, R.styleable.IconItemView, defStyleAttr, R.style.IconItemView)) {
            getResourceId(R.styleable.IconItemView_icon_item_text_label, 0)
                .takeIf { it != 0 }?.let { setLabel(resId = it) }
                ?: run { setLabel(text = getString(R.styleable.IconItemView_icon_item_text_label)) }
            recycle()
        }
    }

    override fun getLayoutId(): Int = R.layout.widget_switch_label_icon_item_view_layout

    /* API */
    // --------------------------------------------------------------------------------------------
    fun getLabel(): CharSequence = tv_label.text

    fun setLabel(@StringRes resId: Int = 0) {
        with (tv_label) {
            setText(resId)
            changeVisibility(isVisible = resId != 0)
        }
    }

    fun setLabel(text: String?) {
        with (tv_label) {
            this.text = text ?: ""
            changeVisibility(isVisible = !text.isNullOrBlank())
        }
    }

    fun showLabel(isVisible: Boolean) {
        tv_label.changeVisibility(isVisible)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.gravity = if (isVisible) Gravity.TOP or Gravity.END
                     else Gravity.CENTER_VERTICAL or Gravity.END
        switcher.layoutParams = lp
    }
}
