package com.ringoid.origin.view.filters

import android.os.Bundle
import android.view.View
import com.innovattic.rangeseekbar.RangeSeekBar
import com.ringoid.base.observe
import com.ringoid.base.view.BaseFragment
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.feed.Filters
import com.ringoid.origin.AppRes
import com.ringoid.origin.R
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import kotlinx.android.synthetic.main.filters_content.*

abstract class BaseFiltersFragment<VM : BaseFiltersViewModel> : BaseFragment<VM>() {

    companion object {
        const val TAG = "FiltersFragment_tag"

        const val MIN_AGE_RANGE = 4
        const val STEP_AGE = 1
        const val STEP_DISTANCE = 1000
    }

    override fun getLayoutId(): Int = R.layout.fragment_filters

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(viewLifecycleOwner) {
            observe(vm.filters, ::displayFilters)
        }
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with (seekbar_age) {
            minRange = MIN_AGE_RANGE / STEP_AGE
            max = (DomainUtil.FILTER_MAX_AGE - DomainUtil.FILTER_MIN_AGE) / STEP_AGE
            seekBarChangeListener = object : RangeSeekBar.SeekBarChangeListener {
                override fun onStartedSeeking() {}
                override fun onStoppedSeeking() {}
                override fun onValueChanged(minThumbValue: Int, maxThumbValue: Int) {
                    vm.setMinMaxAge(minAge = minThumbValue * STEP_AGE + DomainUtil.FILTER_MIN_AGE,
                                    maxAge = maxThumbValue * STEP_AGE + DomainUtil.FILTER_MIN_AGE)
                    displayMinMaxAgeRange(minThumbValue, maxThumbValue)
                }
            }
        }

        with (seekbar_distance) {
            min = DomainUtil.FILTER_MIN_DISTANCE / STEP_DISTANCE.toFloat()
            max = DomainUtil.FILTER_MAX_DISTANCE / STEP_DISTANCE.toFloat()
            onSeekChangeListener = object : OnSeekChangeListener {
                override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {}
                override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {}
                override fun onSeeking(seekParams: SeekParams) {
                    vm.setDistance(distance = seekParams.progress * STEP_DISTANCE)
                    displayDistance(seekParams.progress)
                }
            }
        }
    }

    // --------------------------------------------------------------------------------------------
    fun requestFiltersForUpdate() {
        vm.requestFiltersForUpdate()
    }

    private fun displayFilters(filters: Filters) {
        val minAgeThumbValue = (filters.minAge - DomainUtil.FILTER_MIN_AGE) / STEP_AGE
        val maxAgeThumbValue = (filters.maxAge - DomainUtil.FILTER_MIN_AGE) / STEP_AGE
        val maxDistance = filters.maxDistance / STEP_DISTANCE

        seekbar_age?.let {
            it.setMinThumbValue(minAgeThumbValue)
            it.setMaxThumbValue(maxAgeThumbValue)
        }
        seekbar_distance?.setProgress(maxDistance.toFloat())

        displayMinMaxAgeRange(minAgeThumbValue, maxAgeThumbValue)
        displayDistance(maxDistance)
    }

    private fun displayMinMaxAgeRange(minThumbValue: Int, maxThumbValue: Int) {
        val minAgeValue = minThumbValue * STEP_AGE
        val maxAgeValue = maxThumbValue * STEP_AGE
        val valueStr = String.format(AppRes.FILTER_AGE, minAgeValue + DomainUtil.FILTER_MIN_AGE, maxAgeValue + DomainUtil.FILTER_MIN_AGE)
        tv_age.text = if (maxThumbValue >= seekbar_age.max) "$valueStr+" else valueStr
    }

    private fun displayDistance(thumbValue: Int) {
        val distanceValue = thumbValue * STEP_DISTANCE
        val valueStr = String.format(AppRes.FILTER_DISTANCE_KM, distanceValue / 1000)  // display in kilometers
        tv_distance.text = if (thumbValue >= seekbar_distance.max) "$valueStr+" else valueStr
    }
}
