package com.ringoid.origin.view.debug

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.ContextUtil
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.debug.DebugLogLevel
import com.ringoid.debug.DebugLogUtil
import com.ringoid.debug.barrier.SimpleBarrierLogUtil
import com.ringoid.debug.model.EmptyDebugLogItem
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.DomainUtil
import com.ringoid.origin.R
import com.ringoid.origin.WidgetR_drawable
import com.ringoid.utility.*
import com.uber.autodispose.AutoDispose.autoDisposable
import com.uber.autodispose.android.scope
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.widget_debug.view.*
import timber.log.Timber
import java.util.*

@DebugOnly
class DebugView : ConstraintLayout {

    private val debugLogItemAdapter = DebugLogItemAdapter { postDelayed({ rv_debug_items.linearLayoutManager()?.scrollToPositionWithOffset(it - 1, 0) }, 100L) }
    private var bgToggle = false
        set(value) {
            field = value
            background = if (value) BG_TRANS else BG_SOLID
        }
    private var lifecycleToggle = BuildConfig.IS_STAGING  // lifecycle logs turned ON on Staging by default
        set(value) {
            field = value
            DebugLogUtil.w("Lifecycle logs has been turned ${if (value) "ON" else "OFF"}")
        }
    private var onCloseListener: ((view: View) -> Unit)? = null
    private var sizeToggle = false

    companion object {
        private var MIN_HEIGHT = -1
        private var MAX_LP: ViewGroup.LayoutParams? = null
        private var MIN_LP: ViewGroup.LayoutParams? = null

        private var BG_SOLID: Drawable? = null
        private var BG_TRANS: Drawable? = null
    }

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr) {
        init(context, attributes, defStyleAttr)
    }

    @LayoutRes private fun getLayoutId(): Int = R.layout.widget_debug

    @Suppress("CheckResult")
    private fun init(context: Context, attributes: AttributeSet?, defStyleAttr: Int) {
        if (BG_SOLID == null) BG_SOLID = ContextCompat.getDrawable(context, WidgetR_drawable.rect_white)
        if (BG_TRANS == null) BG_TRANS = ContextCompat.getDrawable(context, WidgetR_drawable.rect_white_70_opaque)
        if (MIN_HEIGHT == -1) MIN_HEIGHT = resources.getDimensionPixelSize(R.dimen.widget_debug_height)
        if (MAX_LP == null) MAX_LP = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        if (MIN_LP == null) MIN_LP = LayoutParams(LayoutParams.MATCH_PARENT, MIN_HEIGHT)
            .apply { bottomToBottom = ConstraintSet.PARENT_ID }

        background = BG_SOLID
        LayoutInflater.from(context).inflate(getLayoutId(), this, true)

        with (context.obtainStyledAttributes(attributes, R.styleable.DebugView, defStyleAttr, 0)) {
            getResourceId(R.styleable.DebugView_debug_view_padding_bottom, 0)
                .takeIf { it != 0 }
                ?.let { resId -> context.resources.getDimensionPixelSize(resId) }
                ?.let { paddingBottom ->
                    with (rv_debug_items) {
                        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
                    }
                }
        }

        with (rv_debug_items) {
            adapter = debugLogItemAdapter
            layoutManager = LinearLayoutManager(context).also { it.stackFromEnd = true }
        }

        ibtn_barrier_log.clicks().compose(clickDebounce()).subscribe { barrierLogs() }
        ibtn_bg_flip_debug.clicks().compose(clickDebounce()).subscribe { bgToggle = !bgToggle }
        ibtn_clear_debug.clicks().compose(clickDebounce()).subscribe { clear() }
        ibtn_close_debug.clicks().compose(clickDebounce()).subscribe {
            onCloseListener?.invoke(this@DebugView) ?: run { Bus.post(BusEvent.CloseDebugView) }
        }
        ibtn_error_debug.clicks().compose(clickDebounce()).subscribe { }
        ibtn_lifecycle_debug.clicks().compose(clickDebounce()).subscribe { lifecycleToggle = !lifecycleToggle }
        ibtn_resize_debug.clicks().compose(clickDebounce()).subscribe {
            if (sizeToggle) minimize() else maximize()
        }
        ibtn_separator_debug.clicks().compose(clickDebounce()).subscribe { line() }
        ibtn_share_debug.clicks().compose(clickDebounce()).subscribe {
            context.copyToClipboard(key = DomainUtil.CLIPBOARD_KEY_DEBUG, value = DebugLogUtil.extractLog())
            context.toast(R.string.common_clipboard)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        DebugLogUtil.loggerSource()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { DebugLogUtil.d("${Date().date()} :: ${BuildConfig.VERSION_NAME}\n\n${ContextUtil.deviceInfo()}\n\n") }
            .doOnDispose { Timber.v("Disposed DebugView") }
            .`as`(autoDisposable(scope()))
            .subscribe({
                if (it == EmptyDebugLogItem) {
                    clear()
                } else {
//                    Timber.v("DebugView item: ${it.log}")
                    if (it.level == DebugLogLevel.LIFECYCLE && !lifecycleToggle) {
                        // ignore LIFECYCLE logs if they are turned off
                    } else {
                        debugLogItemAdapter.append(DebugLogItemVO.from(it))
                    }
                }
            }, DebugLogUtil::e)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Timber.v("DebugView has been detached from Window")
    }

    fun setOnCloseListener(l: ((view: View) -> Unit)?) {
        onCloseListener = l
    }

    // --------------------------------------------------------------------------------------------
    fun barrierLogs() {
        clear()
        line()
        line()
        DebugLogUtil.i("BEGIN barrier logs")
        SimpleBarrierLogUtil.printDebugLog()
        DebugLogUtil.i("END barrier logs")
        line()
        line()
    }

    private fun clear() {
        debugLogItemAdapter.clear()
    }

    private fun line() {
        DebugLogUtil.w("------------------------------------------------------------------------------------\n")
    }

    private fun maximize() {
        sizeToggle = true
        layoutParams = MAX_LP
    }

    private fun minimize() {
        sizeToggle = false
        layoutParams = MIN_LP
    }
}
