package com.ringoid.widget.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.getAttributeColor
import com.ringoid.widget.R
import kotlinx.android.synthetic.main.widget_alert_popup_layout.view.*

class AlertPopup : ConstraintLayout {

    private var listener: (() -> Unit)? = null

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr) {
        init(context, attributes, defStyleAttr)
    }

    @Suppress("CheckResult")
    private fun init(context: Context, attributes: AttributeSet?, defStyleAttr: Int) {
        LayoutInflater.from(context).inflate(R.layout.widget_alert_popup_layout, this, true)

        with (context.obtainStyledAttributes(attributes, R.styleable.AlertPopup, defStyleAttr, R.style.AlertPopup)) {
            val defaultBg = context.getAttributeColor(R.attr.refColorBg)
            val defaultTextPrimaryColor = context.getAttributeColor(R.attr.refTextColorPrimary)

            setBackgroundResource(getResourceId(R.styleable.AlertPopup_alert_bg, defaultBg))
            setActionTextColorRes(colorResId = getResourceId(R.styleable.AlertPopup_alert_action_text_color, defaultTextPrimaryColor))
            setDescriptionTextColorRes(colorResId = getResourceId(R.styleable.AlertPopup_alert_description_text_color, defaultTextPrimaryColor))
            getResourceId(R.styleable.AlertPopup_alert_action_text, 0)
                .takeIf { it != 0 }?.let { setActionText(resId = it) }
                ?: run { setActionText(text = getString(R.styleable.AlertPopup_alert_action_text)) }
            getResourceId(R.styleable.AlertPopup_alert_description_text, 0)
                .takeIf { it != 0 }?.let { setDescriptionText(resId = it) }
                ?: run { setDescriptionText(text = getString(R.styleable.AlertPopup_alert_description_text)) }
            setIcon(resId = getResourceId(R.styleable.AlertPopup_alert_icon, 0))
            setHideIcon(resId = getResourceId(R.styleable.AlertPopup_alert_icon_hide, 0))

            recycle()
        }

        setOnClickListener { listener?.invoke() }
        btn_action.clicks().compose(clickDebounce()).subscribe { listener?.invoke() }
    }

    /* API */
    // -------------------------------------------------------------------------------------------
    fun setOnActionClickListener(l: (() -> Unit)?) {
        listener = l
    }

    @Suppress("CheckResult")
    fun setOnHideClickListener(l: (() -> Unit)?) {
        ibtn_hide_icon.clicks().compose(clickDebounce()).subscribe { l?.invoke() }
    }

    // --------------------------------------------------------------------------------------------
    private fun setActionText(@StringRes resId: Int) {
        btn_action.setText(resId)
    }

    private fun setActionText(text: String?) {
        btn_action.text = text ?: ""
    }

    private fun setActionTextColorRes(@ColorRes colorResId: Int) {
        btn_action.setTextColor(ContextCompat.getColor(context, colorResId))
    }

    private fun setDescriptionText(@StringRes resId: Int) {
        tv_description.setText(resId)
    }

    private fun setDescriptionText(text: String?) {
        tv_description.text = text ?: ""
    }

    private fun setDescriptionTextColorRes(@ColorRes colorResId: Int) {
        tv_description.setTextColor(ContextCompat.getColor(context, colorResId))
    }

    private fun setIcon(@DrawableRes resId: Int) {
        iv_icon.setImageResource(resId)
    }

    private fun setHideIcon(@DrawableRes resId: Int) {
        ibtn_hide_icon.setImageResource(resId)
    }
}
