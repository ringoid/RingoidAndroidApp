package com.ringoid.widget.view

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.EditText

class KeyEditText : EditText {

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr)

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean =
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> true
            else -> super.onKeyPreIme(keyCode, event)
        }
}
