package com.ringoid.origin.view.debug

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.widget.R

@DebugOnly
class DebugView : ConstraintLayout {

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr) {
        init(context, attributes, defStyleAttr)
    }

    @LayoutRes private fun getLayoutId(): Int = R.layout.widget_debug

    private fun init(context: Context, attributes: AttributeSet?, defStyleAttr: Int) {
        setBackgroundColor(Color.WHITE)
        minimumHeight = resources.getDimensionPixelSize(R.dimen.widget_debug_height)
        LayoutInflater.from(context).inflate(getLayoutId(), this, true)
    }
}
