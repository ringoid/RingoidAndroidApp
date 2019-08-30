package com.ringoid.widget.view.item_view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.jakewharton.rxbinding3.InitialValueObservable
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.showKeyboard
import com.ringoid.widget.R
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import kotlinx.android.synthetic.main.widget_edit_text_icon_item_view_layout.view.*

class EditTextIconItemView : TextIconItemView {

    internal var onFocusInterceptListener: OnFocusChangeListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : super(context, attributes, defStyleAttr) {
        with (context.obtainStyledAttributes(attributes, R.styleable.EditTextIconItemView, defStyleAttr, R.style.TextIconItemView)) {
            getInt(R.styleable.EditTextIconItemView_android_inputType, EditorInfo.TYPE_NULL)
                .takeIf { it != EditorInfo.TYPE_NULL }
                ?.let { tv_input.inputType = it }
            ll_count_container.changeVisibility(isVisible = getBoolean(R.styleable.EditTextIconItemView_edit_text_icon_item_with_counter, false))
            tv_input.setSelectAllOnFocus(getBoolean(R.styleable.EditTextIconItemView_edit_text_icon_item_select_all_on_focus, false))
            recycle()
        }

        tv_input.setOnFocusChangeListener { etView, hasFocus ->
            if (isEmpty() && hasHint()) {
                (etView as? TextView)?.let { et ->
                    disableTextChangeWatchers()
                    et.text = if (hasFocus) {
                        null  // replace hint with empty silently
                    } else {
                        hint  // show hint back if any
                    }
                    enableTextChangeWatchers()
                }
            }
            onFocusInterceptListener?.onFocusChange(etView, hasFocus)
        }

        setOnClickListener {
            with (tv_input) {
                requestFocusFromTouch()
                showKeyboard()
            }
            onFocusInterceptListener?.onFocusChange(tv_input, true)
        }
    }

    @LayoutRes
    override fun getLayoutId(): Int = R.layout.widget_edit_text_icon_item_view_layout

    /* API */
    // --------------------------------------------------------------------------------------------
    override fun getText(): String = tv_input.text.toString()

    override fun setInputText(text: String?): Boolean =
        super.setInputText(text).also { sideEffectOnSetInputText(text) }

    @Suppress("SetTextI18n")
    internal fun setCharsCount(count: Int) {
        tv_chars_count?.text = "$count/$MAX_LENGTH"
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private fun sideEffectOnSetInputText(text: String?) {
        with (tv_input) {
            setCharsCount(text?.length ?: 0)
            setSelection(text?.length ?: 0)
        }
    }

    override fun setInputTextInternal(text: String?): Boolean =
        super.setInputTextInternal(text).also { sideEffectOnSetInputText(text) }
}

// ------------------------------------------------------------------------------------------------
fun EditTextIconItemView.focuses(): InitialValueObservable<Boolean> =
    EditTextIconItemViewFocusChangeObservable(this)

private class EditTextIconItemViewFocusChangeObservable(private val etView: EditTextIconItemView)
    : InitialValueObservable<Boolean>() {

    override val initialValue: Boolean
        get() = etView.tv_input.hasFocus()

    override fun subscribeListener(observer: Observer<in Boolean>) {
        val listener = Listener(etView, observer)
        observer.onSubscribe(listener)
        etView.onFocusInterceptListener = listener
    }

    private class Listener(
            private val etView: EditTextIconItemView,
            private val observer: Observer<in Boolean>)
        : MainThreadDisposable(), View.OnFocusChangeListener {

        override fun onFocusChange(v: View, hasFocus: Boolean) {
            if (!isDisposed) {
                observer.onNext(hasFocus)
            }
        }

        override fun onDispose() {
            etView.onFocusInterceptListener = null
        }
    }
}
