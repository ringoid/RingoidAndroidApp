package com.ringoid.origin.rateus.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.ringoid.origin.rateus.R
import kotlinx.android.synthetic.main.widget_rating_line.view.*

open class RatingLineView : LinearLayout {

    protected val stars = mutableListOf<ImageView>()

    protected var fullStar: Drawable? = null
    protected var halfStar: Drawable? = null
    protected var noneStar: Drawable? = null

    protected var ratingValue: Float = 0.0F

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        init(context)
    }

    // ------------------------------------------
    private fun init(context: Context) {
        orientation = HORIZONTAL
        LayoutInflater.from(context).inflate(R.layout.widget_rating_line, this, true).apply {
            stars.add(iv_rating_1)
            stars.add(iv_rating_2)
            stars.add(iv_rating_3)
            stars.add(iv_rating_4)
            stars.add(iv_rating_5)
        }
        initResources()
    }

    /* API */
    // --------------------------------------------------------------------------------------------
    fun getRating(): Float = ratingValue

    /**
     * Set rating visually. If [rating] is less than zero - this widget is hidden.
     * Zero value is allowed.
     */
    fun setRating(rating: Float) {
        if (rating < 0) {
            noRating()
            return
        }

        ratingValue = rating

        var quotient = rating.toInt()
        val residual = rating - quotient

        for (i in 0 until quotient) {
            stars[i].background = fullStar
        }
        if (residual >= 0.5) {
            stars[quotient].background = halfStar
            ++quotient
        }
        for (i in quotient until stars.size) {
            stars[i].background = noneStar
        }
    }

    /**
     * Hides widget.
     */
    open fun noRating() {
        ratingValue = 0.0F
        for (star in stars) star.visibility = View.GONE
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private fun initResources() {
        if (noneStar != null && halfStar != null && fullStar != null) return

        fullStar = ContextCompat.getDrawable(context, R.drawable.ic_rating_star_full_36dp)
        halfStar = ContextCompat.getDrawable(context, R.drawable.ic_rating_star_half_36dp)
        noneStar = ContextCompat.getDrawable(context, R.drawable.ic_rating_star_empty_36dp)
    }
}
