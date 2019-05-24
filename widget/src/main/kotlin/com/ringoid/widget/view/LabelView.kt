package com.ringoid.widget.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ringoid.utility.changeVisibility
import com.ringoid.widget.R
import kotlinx.android.synthetic.main.widget_label_view_layout.view.*

class LabelView : LinearLayout {

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr) {
        init(context, attributes, defStyleAttr)
    }

    private fun init(context: Context, attributes: AttributeSet?, defStyleAttr: Int) {
        minimumHeight = context.resources.getDimensionPixelSize(R.dimen.std_icon_36)
        orientation = LinearLayout.HORIZONTAL

        LayoutInflater.from(context).inflate(R.layout.widget_label_view_layout, this, true)

        context.obtainStyledAttributes(attributes, R.styleable.LabelView, defStyleAttr, 0)
            .apply {
                setIcon(resId = getResourceId(R.styleable.LabelView_label_icon, 0))
                getResourceId(R.styleable.LabelView_label_text, 0)
                    .takeIf { it != 0 }?.let { setText(resId = it) }
                    ?: run { setText(text = getString(R.styleable.LabelView_label_text)) }
                recycle()
            }
    }

    /* API */
    // --------------------------------------------------------------------------------------------
    fun setIcon(@DrawableRes resId: Int) {
        resId.takeIf { it != 0 }
            ?.let { iv_icon.setImageResource(it) }
            ?: run { iv_icon.changeVisibility(isVisible = false) }
    }

    fun setText(@StringRes resId: Int) {
        tv_text.setText(resId)
    }

    fun setText(text: String?) {
        tv_text.text = text ?: ""
    }
}