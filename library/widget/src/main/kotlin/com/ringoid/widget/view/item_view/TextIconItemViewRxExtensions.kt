package com.ringoid.widget.view.item_view

import com.jakewharton.rxbinding3.InitialValueObservable

fun TextIconItemView.textChanges(): InitialValueObservable<CharSequence> =
    TextIconItemViewTextChangesObservable(this)

fun EditTextIconItemView.textChanges(): InitialValueObservable<CharSequence> =
    TextIconItemViewTextChangesObservable(this).doOnNext { setCharsCount(it?.length ?: 0) }
