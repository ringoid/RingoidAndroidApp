package com.ringoid.origin.feed.view.widget

import android.view.View
import com.github.techisfun.android.topsheet.TopSheetBehavior
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.touches
import com.ringoid.debug.DebugLogUtil
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.collection.EvictingQueue
import kotlinx.android.synthetic.main.dialog_filters.view.*

class FiltersPopupWidget(private val rootView: View, private val onShowCallback: () -> Unit) {

    companion object {
        private const val SLIDE_NUMBER = 4
    }

    interface OnSlideUpListener {
        fun onSlideUp(isSlidingUp: Boolean)
    }

    private val stateHistory = EvictingQueue<Int>(20)
    private var slideHistory = EvictingQueue<Float>(SLIDE_NUMBER)

    private var isSliding: Boolean = false
    private var slideCounter: Int = 0
    private var slideListener: OnSlideUpListener? = null
    private var slideOneShot: Boolean = true

    init {
        with (rootView.ll_top_sheet) {
            setOnTouchListener { _, _ -> true }
            // allow [TopSheetBehavior.STATE_HIDDEN]
            TopSheetBehavior.from(this).isHideable = true
            dim_overlay.touches().compose(clickDebounce()).subscribe { hide() }
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
    internal fun setOnClickListener_applyFilters(l: ((widget: FiltersPopupWidget) -> Unit)?) {
        rootView.btn_apply_filters.clicks().compose(clickDebounce()).subscribe { l?.invoke(this) }
    }

    @Suppress("AutoDispose", "CheckResult")
    internal fun setOnClickListener_showAll(l: ((widget: FiltersPopupWidget) -> Unit)?) {
        rootView.btn_show_all.clicks().compose(clickDebounce()).subscribe { l?.invoke(this) }
    }

    internal fun setOnSlideUpListener(l: OnSlideUpListener?) {
        slideListener = l
    }

    internal fun setOnStateChangedListener(l: ((newState: Int) -> Unit)?) {
        TopSheetBehavior.from(rootView.ll_top_sheet)
            .setTopSheetCallback(object: TopSheetBehavior.TopSheetCallback() {
                override fun onStateChanged(popupView: View, newState: Int) {
                    DebugLogUtil.d("Filters popup state: $newState")
                    stateHistory.add(newState)
                    isSliding = newState == TopSheetBehavior.STATE_SETTLING
                    slideOneShot = isSliding
                    popupView.dim_overlay.alpha = when (newState) {
                        TopSheetBehavior.STATE_HIDDEN -> 0.0f
                        TopSheetBehavior.STATE_EXPANDED -> 1.0f
                        else -> popupView.dim_overlay.alpha  // no change
                    }
                    l?.invoke(newState)
                }

                /**
                 * [slideOffset] changes from 0 to 1 while expanding, and from 1 to 0 while collapsing.
                 */
                override fun onSlide(popupView: View, slideOffset: Float) {
                    fun hasCollectedSlideHistory(): Boolean =
                        slideHistory.size >= SLIDE_NUMBER && slideCounter >= SLIDE_NUMBER

                    if (isSliding) {
                        slideHistory.add(slideOffset)
                        ++slideCounter
                        if (slideOneShot && hasCollectedSlideHistory()) {
                            slideOneShot = false
                            val delta = slideHistory.peekFirst() - slideHistory.peekLast()
                            slideListener?.onSlideUp(delta >= 0)
                        }
                    } else {
                        slideCounter = 0
                    }

                    popupView.dim_overlay.alpha = slideOffset
                }
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
                } else {
                    TopSheetBehavior.STATE_HIDDEN
                }
        }  // ignore if view hierarchy hasn't been initialized yet
    }
}
