package com.ringoid.origin.feed.view.widget

import androidx.fragment.app.Fragment
import com.github.techisfun.android.topsheet.TopSheetBehavior
import com.ringoid.origin.view.filters.BaseFiltersFragment
import kotlinx.android.synthetic.main.dialog_filters.view.*

class FiltersPopupWidget(private val fragment: Fragment) {

    internal fun hide() {
        show(isVisible = false)
    }

    internal fun show() {
        show(isVisible = true)
    }

    private fun show(isVisible: Boolean) {
        fragment.view?.ll_top_sheet?.let {
            val behavior = TopSheetBehavior.from(fragment.view!!.ll_top_sheet)
            when (behavior.state) {
                TopSheetBehavior.STATE_HIDDEN -> if (!isVisible) return
                TopSheetBehavior.STATE_EXPANDED -> if (isVisible) return
            }

            behavior.state =
                if (isVisible) {
                    fragment.childFragmentManager.findFragmentByTag(BaseFiltersFragment.TAG)
                        ?.let { it as? BaseFiltersFragment<*> }
                        ?.requestFiltersForUpdate()

                    TopSheetBehavior.STATE_EXPANDED
                } else TopSheetBehavior.STATE_HIDDEN
        }  // ignore if view hierarchy hasn't been initialized yet
    }
}
