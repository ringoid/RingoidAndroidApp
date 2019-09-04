package com.ringoid.origin.view.debug

import android.content.Context
import android.os.Build
import android.os.Debug
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.ringoid.domain.BuildConfig
import com.ringoid.origin.R
import com.ringoid.origin.WidgetR_dimen
import com.ringoid.origin.WidgetR_drawable
import com.ringoid.origin.model.DebugInfoItem
import com.ringoid.utility.targetVersion
import com.uber.autodispose.AutoDispose.autoDisposable
import com.uber.autodispose.android.scope
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

class DebugInfoView : LinearLayout {

    private lateinit var tvConnInfo: TextView
    private lateinit var tvMemInfo:  TextView
    private lateinit var tvThreadInfo: TextView

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr) {
        init(context)
    }

    @LayoutRes
    private fun getLayoutId(): Int = R.layout.widget_debug_info

    private fun init(context: Context) {
        val size = context.resources.getDimensionPixelSize(WidgetR_dimen.std_margin_8)
        background = ContextCompat.getDrawable(context, WidgetR_drawable.rect_debug_round)
        orientation = VERTICAL
        setPadding(size, size, size, size)
        LayoutInflater.from(context).inflate(getLayoutId(), this, true)
            .also {
                tvConnInfo = it.findViewById(R.id.tv_debug_conninfo)
                tvMemInfo = it.findViewById(R.id.tv_debug_meminfo)
                tvThreadInfo = it.findViewById(R.id.tv_debug_threadinfo)
            }
    }

    @Suppress("SetTextI18n")
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // debug info is only available on staging
        if (BuildConfig.IS_STAGING && targetVersion(Build.VERSION_CODES.M)) {
            Observable.interval(20L, 4L, TimeUnit.SECONDS)
                .map {
                    val meminfo = Debug.MemoryInfo()
                    Debug.getMemoryInfo(meminfo)
                    DebugInfoItem(meminfo = meminfo)
                    // TODO: add conn and thread info
                }
                .doOnDispose { Timber.v("Disposed DebugInfoView") }
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(autoDisposable(scope()))
                .subscribe({
                    // TODO: show conn info
                    tvMemInfo.text = "Java: ${it.meminfo.java} kB\n" +
                                     "Natv: ${it.meminfo.native} kB\n" +
                                     "Code: ${it.meminfo.code} kB"

                    tvThreadInfo.text = "Threads: ${it.threadNumber}\n" +
                                        "Blocked: ${it.threadBlocked}\n" +
                                        "Running: ${it.threadRunning}\n" +
                                        "Waiting: ${it.threadWaiting}"
                }, Timber::e)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Timber.v("DebugInfoView has been detached from Window")
    }
}
