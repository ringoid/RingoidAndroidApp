package com.ringoid.widget.view

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.getSelectableItemBgBorderless
import com.ringoid.widget.R
import kotlinx.android.synthetic.main.widget_extend_image_button.view.*

class ExtendImageButton : FrameLayout {

    private val detector = GestureDetector(context, HorizontalSwipeGestureRecognizer())
    private var flingListener: ((direction: Direction) -> Unit)? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : super(context, attributes, defStyleAttr) {
        init(context, attributes, defStyleAttr)
    }

    // --------------------------------------------------------------------------------------------
    private fun init(context: Context, attributes: AttributeSet?, defStyleAttr: Int) {
        background = ContextCompat.getDrawable(context, R.drawable.rect_debug_area)  //context.getSelectableItemBgBorderless()
        foreground = context.getSelectableItemBgBorderless()
        isClickable = true
        isFocusable = true

        LayoutInflater.from(context).inflate(R.layout.widget_extend_image_button, this, true)

        context.obtainStyledAttributes(attributes, R.styleable.ExtendImageButton, defStyleAttr, 0)
            .apply {
                setImageSize(resId = getResourceId(R.styleable.ExtendImageButton_xbtnInnerSize, 0))
                setImageBgResource(resId = getResourceId(R.styleable.ExtendImageButton_xbtnBg, 0))
                setImageSrcResource(resId = getResourceId(R.styleable.ExtendImageButton_xbtnSrc, 0))
                recycle()
            }

        setOnTouchListener { _, event -> detector.onTouchEvent(event) }
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

    fun setOnFlingListener(l: ((direction: Direction) -> Unit)?) {
        flingListener = l
    }

    // --------------------------------------------------------------------------------------------
    inner class HorizontalSwipeGestureRecognizer : SwipeGestureRecognizer() {

        override fun onSwipe(direction: Direction): Boolean {
            flingListener?.invoke(direction)
            return super.onSwipe(direction)
        }
    }
}
