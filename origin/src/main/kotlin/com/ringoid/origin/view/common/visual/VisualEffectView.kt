package com.ringoid.origin.view.common.visual

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.scope
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

class VisualEffectView : FrameLayout {

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        // no-op
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        VisualEffectManager.effect
            .observeOn(AndroidSchedulers.mainThread())
            .`as`(AutoDispose.autoDisposable(scope()))
            .subscribe({
                Timber.v("VisualEffect: $it")
                // TODO: play animation
            }, Timber::e)
    }
}
