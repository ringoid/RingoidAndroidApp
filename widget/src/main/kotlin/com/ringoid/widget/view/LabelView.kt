package com.ringoid.widget.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ringoid.utility.changeTypeface
import com.ringoid.widget.R
import kotlinx.android.synthetic.main.widget_label_view_layout.view.*

class LabelView : LinearLayout {

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr) {
        init(context, attributes, defStyleAttr)
    }

    private fun init(context: Context, attributes: AttributeSet?, defStyleAttr: Int) {
        orientation = HORIZONTAL

        LayoutInflater.from(context).inflate(R.layout.widget_label_view_layout, this, true)

        context.obtainStyledAttributes(attributes, R.styleable.LabelView, defStyleAttr, 0)
            .apply {
                setIcon(resId = getResourceId(R.styleable.LabelView_label_icon, 0))

                val wrapContent = getBoolean(R.styleable.LabelView_label_wrap_content, false)
                if (!wrapContent) {
                    minimumHeight = context.resources.getDimensionPixelSize(R.dimen.std_icon_36)
                }

                getResourceId(R.styleable.LabelView_label_icon_size, 0)
                    .takeIf { it != 0 }?.let { setIconSize(resId = it) }

                getResourceId(R.styleable.LabelView_label_min_height, R.dimen.std_icon_24)
                    .takeIf { it != 0 }?.let { minimumHeight = resources.getDimensionPixelSize(it) }

                getResourceId(R.styleable.LabelView_label_text_size, 0)
                    .takeIf { it != 0 }?.let { tv_text.changeTypeface(textSize = resources.getDimensionPixelSize(it)) }

                getInt(R.styleable.LabelView_label_text_gravity, Gravity.CENTER_VERTICAL or Gravity.START)
                    .let { tv_text.gravity = it }

                getResourceId(R.styleable.LabelView_label_text, 0)
                    .takeIf { it != 0 }?.let { setText(resId = it) }
                    ?: run { setText(text = getString(R.styleable.LabelView_label_text)) }

                recycle()
            }
    }

    /* API */
    // --------------------------------------------------------------------------------------------
    fun setIcon(@DrawableRes resId: Int) {
        resId.takeIf { it != 0 }?.let { iv_icon.setImageResource(it) }
    }

    fun setIconSize(@DimenRes resId: Int) {
        resId.takeIf { it != 0 }
            ?.let {
                val size = resources.getDimensionPixelSize(resId)
                iv_icon.layoutParams = LayoutParams(size, size)
                    .apply { gravity = Gravity.CENTER_VERTICAL or Gravity.START }
            }
    }

    fun setText(@StringRes resId: Int) {
        tv_text.setText(resId)
    }

    fun setText(text: String?) {
        tv_text.text = text ?: ""
    }
}