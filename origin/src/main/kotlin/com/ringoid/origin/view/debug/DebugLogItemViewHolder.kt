package com.ringoid.origin.view.debug

import android.view.View
import androidx.core.content.ContextCompat
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.debug.DebugLogLevel
import com.ringoid.origin.WidgetR_color
import com.ringoid.utility.time
import kotlinx.android.synthetic.main.rv_item_debug_log.view.*
import java.util.*

abstract class OriginDebugLogItemViewHolder(view: View) : BaseViewHolder<DebugLogItemVO>(view)

class DebugLogItemViewHolder(view: View) : OriginDebugLogItemViewHolder(view) {

    @Suppress("SetTextI18n")
    override fun bind(model: DebugLogItemVO) {
        with(itemView.tv_debug_log_text) {
            val textColorResId = when(model.log.level) {
                DebugLogLevel.VERBOSE -> WidgetR_color.secondary_text
                DebugLogLevel.DEBUG -> WidgetR_color.primary_text
                DebugLogLevel.INFO -> WidgetR_color.grass
                DebugLogLevel.WARNING -> WidgetR_color.warning
                DebugLogLevel.ERROR -> WidgetR_color.red_love
            }
            text = "${Date(model.log.ts).time()}: ${model.log.log}"
            setTextColor(ContextCompat.getColor(context, textColorResId))
        }
    }
}

class HeaderDebugLogItemViewHolder(view: View) : OriginDebugLogItemViewHolder(view) {

    override fun bind(model: DebugLogItemVO) {
        // no-op
    }
}

class FooterDebugLogItemViewHolder(view: View) : OriginDebugLogItemViewHolder(view) {

    override fun bind(model: DebugLogItemVO) {
        // no-op
    }
}
