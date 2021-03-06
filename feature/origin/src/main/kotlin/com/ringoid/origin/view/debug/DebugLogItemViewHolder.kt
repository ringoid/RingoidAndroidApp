package com.ringoid.origin.view.debug

import android.view.View
import androidx.core.content.ContextCompat
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.debug.DebugLogLevel
import com.ringoid.origin.WidgetR_color
import kotlinx.android.synthetic.main.rv_item_debug_log.view.*

abstract class OriginDebugLogItemViewHolder(view: View) : BaseViewHolder<DebugLogItemVO>(view)

class DebugLogItemViewHolder(view: View) : OriginDebugLogItemViewHolder(view) {

    @Suppress("SetTextI18n")
    override fun bind(model: DebugLogItemVO) {
        with(itemView.tv_debug_log_text) {
            val textColorResId = when(model.log.level) {
                DebugLogLevel.LIFECYCLE -> WidgetR_color.primary
                DebugLogLevel.BUS -> WidgetR_color.sky
                DebugLogLevel.VERBOSE -> WidgetR_color.secondary_text
                DebugLogLevel.DEBUG -> WidgetR_color.primary_text
                DebugLogLevel.DEBUG2 -> WidgetR_color.green
                DebugLogLevel.INFO -> WidgetR_color.grass
                DebugLogLevel.WARNING -> WidgetR_color.warning
                DebugLogLevel.ERROR -> WidgetR_color.red_love
            }
            text = model.log.log()
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
