package com.ringoid.origin.feed.view.widget

import android.view.View
import com.github.techisfun.android.topsheet.TopSheetBehavior
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.dialog_filters.view.*

class FiltersPopupWidget(private val rootView: View, private val onShowCallback: () -> Unit) {

    init {
        with (rootView.ll_top_sheet) {
            setOnTouchListener { _, _ -> true }
            // allow [TopSheetBehavior.STATE_HIDDEN]
            TopSheetBehavior.from(this).isHideable = true
        }
    }

    internal fun hide() {
        show(isVisible = false)
    }

    internal fun show() {
        show(isVisible = true)
    }

    internal fun hideShowAllButton() {
        rootView.btn_show_all.changeVisibility(isVisible = false)
    }

    internal fun setCountOfFilteredFeedItems(text: String) {
        rootView.btn_apply_filters.text = text
    }

    internal fun setTotalNotFilteredFeedItems(text: String) {
        rootView.btn_show_all.text = text
    }

    @Suppress("AutoDispose", "CheckResult")
    internal fun setOnClickListener_applyFilters(l: (() -> Unit)?) {
        rootView.btn_apply_filters.clicks().compose(clickDebounce()).subscribe { l?.invoke() }
    }

    @Suppress("AutoDispose", "CheckResult")
    internal fun setOnClickListener_showAll(l: (() -> Unit)?) {
        rootView.btn_show_all.clicks().compose(clickDebounce()).subscribe { l?.invoke() }
    }

    internal fun setOnStateChangedListener(l: ((newState: Int) -> Unit)?) {
        TopSheetBehavior.from(rootView.ll_top_sheet)
            .setTopSheetCallback(object: TopSheetBehavior.TopSheetCallback() {
                override fun onStateChanged(popupView: View, newState: Int) {
                    l?.invoke(newState)
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
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
