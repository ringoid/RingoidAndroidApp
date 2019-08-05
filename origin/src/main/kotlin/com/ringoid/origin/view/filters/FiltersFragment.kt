package com.ringoid.origin.view.filters

import android.os.Bundle
import android.view.View
import com.innovattic.rangeseekbar.RangeSeekBar
import com.ringoid.base.view.BaseFragment
import com.ringoid.domain.DomainUtil
import com.ringoid.origin.AppRes
import com.ringoid.origin.R
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import kotlinx.android.synthetic.main.filters_content.*

class FiltersFragment : BaseFragment<FiltersViewModel>() {

    companion object {
        const val TAG = "FiltersFragment_tag"

        fun newInstance(): FiltersFragment = FiltersFragment()
    }

    override fun getVmClass(): Class<FiltersViewModel> = FiltersViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_filters

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with (seekbar_age) {
            minRange = 4
            max = DomainUtil.FILTER_MAX_AGE - DomainUtil.FILTER_MIN_AGE
            seekBarChangeListener = object : RangeSeekBar.SeekBarChangeListener {
                override fun onStartedSeeking() {}
                override fun onStoppedSeeking() {}
                override fun onValueChanged(minThumbValue: Int, maxThumbValue: Int) {
                    displayMinMaxAgeRange(minThumbValue, maxThumbValue)
                }
            }
            displayMinMaxAgeRange(0, max)  // initialize counters
        }

        with (seekbar_distance) {
            min = DomainUtil.FILTER_MIN_DISTANCE / 1000f
            max = DomainUtil.FILTER_MAX_DISTANCE / 1000f
            onSeekChangeListener = object : OnSeekChangeListener {
                override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {}
                override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {}
                override fun onSeeking(seekParams: SeekParams) {
                    displayDistance(seekParams.progress)
                }
            }
            displayDistance(min.toInt())
        }
    }

    // --------------------------------------------------------------------------------------------
    private fun displayMinMaxAgeRange(minThumbValue: Int, maxThumbValue: Int) {
        val valueStr = String.format(AppRes.FILTER_AGE, minThumbValue + DomainUtil.FILTER_MIN_AGE, maxThumbValue + DomainUtil.FILTER_MIN_AGE)
        tv_age.text = if (maxThumbValue >= seekbar_age.max) "$valueStr+" else valueStr
    }

    private fun displayDistance(thumbValue: Int) {
        val valueStr = String.format(AppRes.FILTER_DISTANCE_KM, thumbValue)
        tv_distance.text = if (thumbValue >= seekbar_distance.max) "$valueStr+" else valueStr
    }
}
