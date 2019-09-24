package com.ringoid.origin.rateus.view

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.observeOneShot
import com.ringoid.base.view.BaseDialogFragment
import com.ringoid.origin.navigation.ExternalNavigator
import com.ringoid.origin.rateus.OriginR_string
import com.ringoid.origin.rateus.R
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.hideKeyboard
import com.ringoid.utility.showKeyboard
import kotlinx.android.synthetic.main.dialog_rate_us.*

class RateUsDialog : BaseDialogFragment<RateUsViewModel>() {

    companion object {
        const val TAG = "RateUsDialog_tag"

        internal const val RATING_THRESHOLD = 4

        fun newInstance(): RateUsDialog = RateUsDialog()
    }

    override fun getVmClass(): Class<RateUsViewModel> = RateUsViewModel::class.java

    @LayoutRes
    override fun getLayoutId(): Int = R.layout.dialog_rate_us

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeOneShot(vm.openGooglePlayOneShot()) {
            context?.let { ExternalNavigator.openGooglePlay(it) }
        }
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_cancel.clicks().compose(clickDebounce()).subscribe { onCancelRate() }
        btn_rate.clicks().compose(clickDebounce()).subscribe { onSendRating() }
        rating_line.setRatingSelectListener { rating ->
            when {
                rating <= 0 -> showNoRating()
                rating < RATING_THRESHOLD -> showBadRating()
                rating >= RATING_THRESHOLD -> showGoodRating()
            }
        }
    }

    // --------------------------------------------------------------------------------------------
    private fun onCancelRate() {
        vm.cancelRate()
        dismiss()
    }

    private fun onSendRating() {
        vm.sendRating(rating = rating_line.getRating().toInt(),
                      feedBackText = et_dialog_entry.text.toString(),
                      tag = "CloseChat")
        dismiss()
    }

    // ------------------------------------------
    private fun showBadRating() {
        with (btn_rate) {
            isEnabled = true
            setText(OriginR_string.button_send)
        }
        et_dialog_entry.showKeyboard()  // show keyboard
        et_dialog_entry.changeVisibility(isVisible = true)
        tv_dialog_description.changeVisibility(isVisible = true)
    }

    private fun showGoodRating() {
        with (btn_rate) {
            isEnabled = true
            setText(OriginR_string.button_rate)
        }
        et_dialog_entry.hideKeyboard()
        et_dialog_entry.changeVisibility(isVisible = false)
        tv_dialog_description.changeVisibility(isVisible = false)
    }

    private fun showNoRating() {
        with (btn_rate) {
            isEnabled = false
            setText(OriginR_string.button_rate)
        }
        et_dialog_entry.hideKeyboard()
        et_dialog_entry.changeVisibility(isVisible = false)
        tv_dialog_description.changeVisibility(isVisible = false)
    }
}
