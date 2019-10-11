package com.ringoid.origin.view.debug

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.ringoid.base.debug.DebugVisibilityHintItem
import com.ringoid.base.debug.DebugVisibilityLogUtil
import com.ringoid.base.view.VisibleHint
import com.ringoid.debug.DebugLogUtil
import com.ringoid.domain.BuildConfig
import com.ringoid.origin.R
import com.ringoid.origin.WidgetR_dimen
import com.ringoid.origin.WidgetR_drawable
import com.ringoid.utility.DebugOnly
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.scope
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.widget_debug_visibility.view.*
import timber.log.Timber

@DebugOnly
class DebugVisibilityView : LinearLayout {

    private val prevStates = mutableMapOf<String, VisibleHint>()

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr) {
        init(context)
    }

    @LayoutRes
    private fun getLayoutId(): Int = R.layout.widget_debug_visibility

    private fun init(context: Context) {
        val size = context.resources.getDimensionPixelSize(WidgetR_dimen.std_margin_8)
        background = ContextCompat.getDrawable(context, WidgetR_drawable.rect_debug_round)
        orientation = VERTICAL
        setPadding(size, size, size, size)
        LayoutInflater.from(context).inflate(getLayoutId(), this, true)
    }

    @Suppress("SetTextI18n")
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // debug info is only available on debug
        if (BuildConfig.DEBUG) {
            DebugVisibilityLogUtil.loggerSource()
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(AutoDispose.autoDisposable(scope()))
                .subscribe({
                    when (it.tag) {
                        "ExploreFeedFragment" -> tv_vis_explore.text = getLogText(it)
                        "ExploreFeedFiltersFragment" -> tv_vis_explore_filters.text = getLogText(it)
                        "LikesFeedFragment" -> tv_vis_likes.text = getLogText(it)
                        "LikesFeedFiltersFragment" -> tv_vis_likes_filters.text = getLogText(it)
                        "MessagesFeedFragment" -> tv_vis_messages.text = getLogText(it)
                        "MessagesFeedFiltersFragment" -> tv_vis_messages_filters.text = getLogText(it)
                        "UserProfileFragment" -> tv_vis_profile.text = getLogText(it)
                    }
                    prevStates[it.tag] = it.hint
                }, DebugLogUtil::e)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Timber.v("DebugVisibilityView has been detached from Window")
    }

    // --------------------------------------------------------------------------------------------
    private fun getLogText(it: DebugVisibilityHintItem): String =
        "${it.hint}  ${getPrevStateByTag(it)}".trim()

    private fun getPrevStateByTag(hint: DebugVisibilityHintItem): String =
        prevStates[hint.tag]
            ?.takeIf { prevHint -> prevHint != hint.hint }
            ?.let { "($it)" }
            ?: ""
}
