package com.ringoid.widget.view.item_view

import android.content.Context
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import androidx.annotation.StringRes
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.getAttributeColor
import com.ringoid.widget.R
import kotlinx.android.synthetic.main.widget_text_icon_item_view_layout.view.*

open class TextIconItemView : IconItemView {

    companion object {
        const val MAX_LENGTH = 30
    }

    protected var hint: String = ""
        private set
    protected var inputText: String? = null
        private set

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : super(context, attributes, defStyleAttr) {
        isClickable = true
        with (context.obtainStyledAttributes(attributes, R.styleable.TextIconItemView, defStyleAttr, R.style.TextIconItemView)) {
            val wrapContent = getBoolean(R.styleable.TextIconItemView_text_icon_item_wrap_content, true)
            if (!wrapContent) {
                space.changeVisibility(isVisible = false)
                tv_suffix.changeVisibility(isVisible = false)
                tv_input.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            }

            getResourceId(R.styleable.TextIconItemView_text_icon_item_text_field_height, 0)
                .takeIf { it != 0 }
                ?.let { resources.getDimensionPixelSize(it) }
                ?.let {
                    val lp = (tv_input.layoutParams as LayoutParams).apply { height = it }
                    tv_input.layoutParams = lp
                }

            getDimension(R.styleable.TextIconItemView_text_icon_item_side_padding, resources.getDimension(R.dimen.std_margin_12))
                .let { tv_input.setPadding(it.toInt(), paddingTop, it.toInt(), paddingBottom) }

            getInt(R.styleable.TextIconItemView_text_icon_item_text_max_length, MAX_LENGTH + 7)
                .takeIf { it > 0 }?.let { tv_input.filters = arrayOf(InputFilter.LengthFilter(it)) }

            getInt(R.styleable.TextIconItemView_text_icon_item_text_gravity, Gravity.CENTER_VERTICAL or Gravity.START)
                .let { tv_input.gravity = it }

            getResourceId(R.styleable.TextIconItemView_text_icon_item_text_hint, 0)
                .takeIf { it != 0 }?.let { setTextHint(resId = it) }
                ?: run { setTextHint(hint = getString(R.styleable.TextIconItemView_text_icon_item_text_hint)) }

            getResourceId(R.styleable.TextIconItemView_text_icon_item_text, 0)
                .takeIf { it != 0 }?.let { setInputText(resId = it) }
                ?: run { setInputText(text = getString(R.styleable.TextIconItemView_text_icon_item_text)) }

            recycle()
        }
    }

    override fun getLayoutId(): Int = R.layout.widget_text_icon_item_view_layout

    /* API */
    // --------------------------------------------------------------------------------------------
    open fun getText(): String = inputText ?: ""

    private fun setInputText(@StringRes resId: Int): Boolean {
        if (resId == 0) {
            return false
        }
        return setInputText(text = resources.getString(resId))
    }

    open fun setInputText(text: String?): Boolean {
        val changed = didTextChanged(text)  // check before assign inputText
        inputText = text

        if (text.isNullOrBlank()) {
            tv_input.text = null  // set empty text and notify text change watchers
            disableTextChangeWatchers()  // disable text watcher while setting hint
            tv_input.text = hint  // can be empty
            enableTextChangeWatchers()  // enable text watcher back
            if (hint.isNotBlank()) {
                tv_input.setTextColor(context.getAttributeColor(R.attr.refTextColorSecondary))
            }
        } else {
            tv_input.text = text
            tv_input.setTextColor(context.getAttributeColor(R.attr.refTextColorPrimary))
        }
        return changed
    }

    fun setSuffix(@StringRes resId: Int) {
        tv_suffix.setText(resId)
    }

    fun setTextHint(@StringRes resId: Int) {
        if (resId == 0) {
            return
        }
        setTextHint(hint = resources.getString(resId))
    }

    fun setTextHint(hint: String?) {
        this.hint = hint ?: ""
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    protected fun hasHint(): Boolean = hint.isNotBlank()
    protected fun hasText(): Boolean = !inputText.isNullOrBlank()
    protected fun didTextChanged(text: String?): Boolean =
        if (inputText.isNullOrBlank() && text.isNullOrBlank()) false
        else inputText != text

    internal fun assignText(text: String?): CharSequence? =
        text?.takeIf { it.isNotBlank() }
            ?.let {
                if (!hasText()) {
                    setInputTextInternal(it)
                } else {
                    inputText = text
                }
                text
            }
            ?: run {  // text is null or blank - use hint, if any
                setInputTextInternal(null)
                null
            }

    protected open fun setInputTextInternal(text: String?): Boolean {
        disableTextChangeWatchers()

        val changed = didTextChanged(text)  // check before assign inputText
        inputText = text

        if (text.isNullOrBlank()) {
            tv_input.text = hint  // can be empty
            if (hint.isNotBlank()) {
                tv_input.setTextColor(context.getAttributeColor(R.attr.refTextColorSecondary))
            }
        } else {
            tv_input.text = text
            tv_input.setTextColor(context.getAttributeColor(R.attr.refTextColorPrimary))
        }

        enableTextChangeWatchers()
        return changed
    }

    internal fun addTextChangedListener(listener: TextWatcher) {
        textChangeWatchers.add(listener)
        tv_input.addTextChangedListener(listener)
    }

    internal fun removeTextChangedListener(listener: TextWatcher) {
        textChangeWatchers.remove(listener)  // removes by ref compare
        tv_input.removeTextChangedListener(listener)
    }

    protected fun disableTextChangeWatchers() {
        textChangeWatchers.forEach { tv_input.removeTextChangedListener(it) }
    }

    protected fun enableTextChangeWatchers() {
        textChangeWatchers.forEach { tv_input.addTextChangedListener(it) }
    }

    private val textChangeWatchers = mutableListOf<TextWatcher>()
}
