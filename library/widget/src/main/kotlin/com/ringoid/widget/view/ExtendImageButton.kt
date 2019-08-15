package com.ringoid.widget.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.touches
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.getSelectableItemBgBorderless
import com.ringoid.utility.model.Coordinates
import com.ringoid.utility.model.CoordinatesF
import com.ringoid.widget.R
import kotlinx.android.synthetic.main.widget_extend_image_button.view.*

open class ExtendImageButton : FrameLayout {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : super(context, attributes, defStyleAttr) {
        init(context, attributes, defStyleAttr)
    }

    @LayoutRes protected open fun getLayoutId(): Int = R.layout.widget_extend_image_button

    // --------------------------------------------------------------------------------------------
    private fun init(context: Context, attributes: AttributeSet?, defStyleAttr: Int) {
//        background = context.getSelectableItemBgBorderless()  // ContextCompat.getDrawable(context, R.drawable.rect_debug_area)
//        foreground = context.getSelectableItemBgBorderless()

        LayoutInflater.from(context).inflate(getLayoutId(), this, true)

        context.obtainStyledAttributes(attributes, R.styleable.ExtendImageButton, defStyleAttr, 0)
            .apply {
                setImageSize(resId = getResourceId(R.styleable.ExtendImageButton_xbtnInnerSize, 0))
                setImageBgResource(resId = getResourceId(R.styleable.ExtendImageButton_xbtnBg, 0))
                setImageSrcResource(resId = getResourceId(R.styleable.ExtendImageButton_xbtnSrc, 0))
                recycle()
            }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        return false
    }

    /* API */
    // --------------------------------------------------------------------------------------------
    private fun setImageSize(@DimenRes resId: Int) {
        resId.takeIf { it != 0 }?.let {
            val size = resources.getDimensionPixelSize(resId)
            ibtn.layoutParams = ibtn.layoutParams.apply { width = size ; height = size }
        }
    }

    private fun setImageBgResource(@DrawableRes resId: Int) {
        resId.takeIf { it != 0 }?.let { ibtn.setBackgroundResource(it) }
    }

    fun setImageSrcResource(@DrawableRes resId: Int) {
        resId.takeIf { it != 0 }?.let { ibtn.setImageResource(it) }
    }

    @Suppress("CheckResult")
    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
        ibtn.clicks().compose(clickDebounce()).subscribe { l?.onClick(ibtn) }
    }

    override fun setOnTouchListener(l: OnTouchListener?) {
        super.setOnTouchListener(l)
        touchImpl(l)
    }

    @Suppress("CheckResult")
    protected open fun touchImpl(l: OnTouchListener?) {
        ibtn.touches().compose(clickDebounce()).subscribe { l?.onTouch(ibtn, it) }
    }

    fun getClickLocation(): Coordinates {
        val xy = IntArray(2)
        ibtn.getLocationOnScreen(xy)
        return Coordinates(x = xy[0] + ibtn.width / 2, y = xy[1] + ibtn.height / 2)
    }

    fun getClickLocationF(): CoordinatesF {
        val xy = IntArray(2)
        ibtn.getLocationOnScreen(xy)
        return CoordinatesF(x = xy[0] + ibtn.width * 0.5f, y = xy[1] + ibtn.height * 0.5f)
    }
}
