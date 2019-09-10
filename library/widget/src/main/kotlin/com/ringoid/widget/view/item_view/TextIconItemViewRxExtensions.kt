package com.ringoid.widget.view.item_view

import com.jakewharton.rxbinding3.InitialValueObservable

fun TextIconItemView._textChanges(): InitialValueObservable<CharSequence> =
    TextIconItemViewTextChangesObservable(this)

fun EditTextIconItemView._textChanges(): InitialValueObservable<CharSequence> =
    TextIconItemViewTextChangesObservable(this).doOnNext { setCharsCount(it?.length ?: 0) }
