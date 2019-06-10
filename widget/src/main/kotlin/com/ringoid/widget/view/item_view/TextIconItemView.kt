package com.ringoid.widget.view.item_view

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StringRes
import com.jakewharton.rxbinding3.InitialValueObservable
import com.jakewharton.rxbinding3.widget.textChanges
import com.ringoid.widget.R
import kotlinx.android.synthetic.main.widget_text_icon_item_view_layout.view.*

open class TextIconItemView : IconItemView {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : super(context, attributes, defStyleAttr) {
        isClickable = true
    }

    override fun getLayoutId(): Int = R.layout.widget_text_icon_item_view_layout

    /* API */
    // --------------------------------------------------------------------------------------------
    open fun getText(): String = tv_input.text.toString()

    open fun setInputText(text: String?) {
        tv_input.text = text ?: ""
    }

    fun setSuffix(@StringRes resId: Int) {
        tv_suffix.setText(resId)
    }
}

fun TextIconItemView.textChanges(): InitialValueObservable<CharSequence> = tv_input.textChanges()
