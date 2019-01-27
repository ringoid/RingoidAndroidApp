package com.ringoid.widget.view.item_view

import android.content.Context
import android.util.AttributeSet
import com.ringoid.widget.R
import kotlinx.android.synthetic.main.widget_switch_icon_item_view_layout.view.*

class SwitchIconItemView : IconItemView {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : super(context, attributes, defStyleAttr) {
        val sidePadding = resources.getDimensionPixelSize(R.dimen.std_margin_8)
        setPadding(paddingLeft, paddingTop, sidePadding, paddingBottom)
    }

    override fun getLayoutId(): Int = R.layout.widget_switch_icon_item_view_layout

    private var listener: ((isChecked: Boolean) -> Unit)? = null

    /* API */
    // --------------------------------------------------------------------------------------------
    fun isChecked(): Boolean = switcher.isChecked

    fun setChecked(isChecked: Boolean) {
        setOnCheckedChangeListener(null)
        switcher.isChecked = isChecked
        setOnCheckedChangeListener(listener)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        fun call(l: OnClickListener?) {
            switcher.isChecked = !switcher.isChecked
            l?.onClick(switcher)
        }

        if (l == null) {
            setOnCheckedChangeListener(null)
            super.setOnClickListener(null)
        } else {
            setOnCheckedChangeListener { l.onClick(switcher) }
            super.setOnClickListener { call(l) }
        }
    }

    private fun setOnCheckedChangeListener(l: ((isChecked: Boolean) -> Unit)?) {
        listener = l
        switcher.setOnCheckedChangeListener { _, isChecked -> l?.invoke(isChecked) }
    }
}
