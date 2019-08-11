package com.ringoid.origin.feed.view.widget

import android.view.View
import com.github.techisfun.android.topsheet.TopSheetBehavior
import kotlinx.android.synthetic.main.dialog_filters.view.*

class FiltersPopupWidget(private val rootView: View, private val onShowCallback: () -> Unit) {

    internal fun hide() {
        show(isVisible = false)
    }

    internal fun show() {
        show(isVisible = true)
    }

    private fun show(isVisible: Boolean) {
        rootView.ll_top_sheet?.let {
            val behavior = TopSheetBehavior.from(rootView.ll_top_sheet)
            when (behavior.state) {
                TopSheetBehavior.STATE_HIDDEN -> if (!isVisible) return
                TopSheetBehavior.STATE_EXPANDED -> if (isVisible) return
            }

            behavior.state =
                if (isVisible) {
                    onShowCallback.invoke()
                    TopSheetBehavior.STATE_EXPANDED
                } else TopSheetBehavior.STATE_HIDDEN
        }  // ignore if view hierarchy hasn't been initialized yet
    }
}
