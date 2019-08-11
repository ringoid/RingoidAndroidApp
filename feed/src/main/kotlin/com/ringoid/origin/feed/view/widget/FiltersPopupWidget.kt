package com.ringoid.origin.feed.view.widget

import android.view.View
import com.github.techisfun.android.topsheet.TopSheetBehavior
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.collection.EvictingQueue
import kotlinx.android.synthetic.main.dialog_filters.view.*
import timber.log.Timber

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
                    l?.invoke(newState)
                }

                override fun onSlide(popupView: View, slideOffset: Float) {
                    if (isSliding) {
                        slideHistory.add(slideOffset)
                        ++slideCounter
                        if (slideHistory.size >= SLIDE_NUMBER && slideCounter >= SLIDE_NUMBER) {
                            val delta = slideHistory.peekFirst() - slideHistory.peekLast()
                            slideListener?.onSlideUp(delta >= 0)
                        }
                    } else {
                        slideCounter = 0
                    }
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
                } else TopSheetBehavior.STATE_HIDDEN
        }  // ignore if view hierarchy hasn't been initialized yet
    }
}
