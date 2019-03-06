package com.ringoid.origin.view.debug

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.copyToClipboard
import com.ringoid.utility.toast
import com.ringoid.origin.R
import com.uber.autodispose.AutoDispose.autoDisposable
import com.uber.autodispose.android.scope
import kotlinx.android.synthetic.main.widget_debug.view.*
import timber.log.Timber

@DebugOnly
class DebugView : ConstraintLayout {

    private val debugLogItemAdapter = DebugLogItemAdapter { post { rv_debug_items.scrollToPosition(it) } }

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr) {
        init(context)
    }

    @LayoutRes private fun getLayoutId(): Int = R.layout.widget_debug

    @Suppress("CheckResult")
    private fun init(context: Context) {
        setBackgroundColor(Color.WHITE)
        minimumHeight = resources.getDimensionPixelSize(R.dimen.widget_debug_height)
        LayoutInflater.from(context).inflate(getLayoutId(), this, true)

        rv_debug_items.apply {
            adapter = debugLogItemAdapter
            layoutManager = LinearLayoutManager(context).also { it.stackFromEnd = true }
        }

        ibtn_clear_debug.clicks().compose(clickDebounce()).subscribe { debugLogItemAdapter.clear() }
        ibtn_share_debug.clicks().compose(clickDebounce()).subscribe {
            context.copyToClipboard(key = DomainUtil.CLIPBOARD_KEY_DEBUG, value = debugLogItemAdapter.getContentText())
            context.toast(R.string.common_clipboard)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        DebugLogUtil.logger
            .`as`(autoDisposable(scope()))
            .subscribe({ debugLogItemAdapter.append(DebugLogItemVO.from(it)) }, Timber::e)
    }
}
