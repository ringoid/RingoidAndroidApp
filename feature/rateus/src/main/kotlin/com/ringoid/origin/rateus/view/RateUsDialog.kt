package com.ringoid.origin.rateus.view

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.observeOneShot
import com.ringoid.base.view.BaseDialogFragment
import com.ringoid.base.view.ViewState
import com.ringoid.origin.navigation.ExternalNavigator
import com.ringoid.origin.rateus.OriginR_string
import com.ringoid.origin.rateus.R
import com.ringoid.utility.*
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

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        fun showLoading(isVisible: Boolean) {
            pb_loading.changeVisibility(isVisible)
            btn_rate.changeVisibility(!isVisible)
        }

        super.onViewStateChange(newState)
        when (newState) {
            ViewState.IDLE -> showLoading(isVisible = false)
            ViewState.LOADING -> showLoading(isVisible = true)
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = super.onCreateView(inflater, container, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeOneShot(vm.closeDialogOneShot()) { close() }
        observeOneShot(vm.openGooglePlayOneShot()) {
            context?.let { ExternalNavigator.openGooglePlay(it) }
            close()
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

        showNoRating()
    }

    override fun onCancel(dialog: DialogInterface) {
        // cancelling on touch outside
        onCancelRate()
    }

    // --------------------------------------------------------------------------------------------
    private fun close() {
        vm.onCloseDialog()
        et_dialog_entry?.hideKeyboard()
        dismiss()
        activity?.finish()
    }

    // ------------------------------------------
    private fun onCancelRate() {
        vm.cancelRate()
        close()
    }

    private fun onSendRating() {
        vm.sendRating(rating = rating_line.getRating().toInt(),
                      feedBackText = et_dialog_entry.text.toString(),
                      tag = "CloseChat")
    }

    // ------------------------------------------
    private fun showBadRating() {
        with (btn_rate) {
            isEnabled = true
            setText(OriginR_string.button_send)
        }
        et_dialog_entry.changeVisibility(isVisible = true)
        tv_dialog_description.changeVisibility(isVisible = true)
        et_dialog_entry.delay(300L) { showKeyboard() }  // show keyboard
    }

    private fun showGoodRating() {
        with (btn_rate) {
            isEnabled = true
            setText(OriginR_string.button_rate)
        }
        et_dialog_entry.changeVisibility(isVisible = false)
        tv_dialog_description.changeVisibility(isVisible = false)
        et_dialog_entry.delay(300L) { hideKeyboard() }
    }

    private fun showNoRating() {
        with (btn_rate) {
            isEnabled = false
            setText(OriginR_string.button_rate)
        }
        et_dialog_entry.changeVisibility(isVisible = false)
        tv_dialog_description.changeVisibility(isVisible = false)
        et_dialog_entry.delay(300L) { hideKeyboard() }
    }
}
