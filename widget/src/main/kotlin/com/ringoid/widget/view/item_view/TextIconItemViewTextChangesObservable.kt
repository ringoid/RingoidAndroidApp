package com.ringoid.widget.view.item_view

import android.text.Editable
import android.text.TextWatcher
import androidx.annotation.CheckResult
import com.jakewharton.rxbinding3.InitialValueObservable
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

/**
 * Text changes [Observable], that ignores hint value on [TextIconItemView].
 *
 * @see https://github.com/JakeWharton/RxBinding/blob/master/rxbinding/src/main/java/com/jakewharton/rxbinding3/widget/TextViewTextChangesObservable.kt
 */
@CheckResult
fun TextIconItemView.textChanges(): InitialValueObservable<CharSequence> =
    TextIconItemViewTextChangesObservable(this)

private class TextIconItemViewTextChangesObservable(private val view: TextIconItemView)
    : InitialValueObservable<CharSequence>() {

    override fun subscribeListener(observer: Observer<in CharSequence>) {
        val listener = Listener(view, observer)
        observer.onSubscribe(listener)
        view.addTextChangedListener(listener)
    }

    override val initialValue get() = view.getText()

    private class Listener(
            private val view: TextIconItemView,
            private val observer: Observer<in CharSequence>)
        : MainThreadDisposable(), TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (!isDisposed && view.hasText()) {
                observer.onNext(s)
            }
        }

        override fun afterTextChanged(s: Editable) {
        }

        override fun onDispose() {
            view.removeTextChangedListener(this)
        }
    }
}
