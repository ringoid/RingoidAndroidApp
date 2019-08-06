package com.ringoid.origin.view.filters

import android.os.Bundle
import android.view.View
import com.innovattic.rangeseekbar.RangeSeekBar
import com.ringoid.base.observe
import com.ringoid.base.view.BaseFragment
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.essence.feed.FilterEssence
import com.ringoid.origin.AppRes
import com.ringoid.origin.R
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import kotlinx.android.synthetic.main.filters_content.*

class FiltersFragment : BaseFragment<FiltersViewModel>() {

    companion object {
        const val TAG = "FiltersFragment_tag"
        const val STEP_AGE = 1
        const val STEP_DISTANCE = 1

        fun newInstance(): FiltersFragment = FiltersFragment()
    }

    override fun getVmClass(): Class<FiltersViewModel> = FiltersViewModel::class.java

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
            minRange = 4
            max = DomainUtil.FILTER_MAX_AGE - DomainUtil.FILTER_MIN_AGE
            seekBarChangeListener = object : RangeSeekBar.SeekBarChangeListener {
                override fun onStartedSeeking() {}
                override fun onStoppedSeeking() {}
                override fun onValueChanged(minThumbValue: Int, maxThumbValue: Int) {
                    vm.setMinMaxAge(minAge = minThumbValue * STEP_AGE, maxAge = maxThumbValue * STEP_AGE)
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
                    vm.setDistance(distance = seekParams.progress * STEP_DISTANCE)
                    displayDistance(seekParams.progress)
                }
            }
            displayDistance(min.toInt())
        }
    }

    // --------------------------------------------------------------------------------------------
    private fun displayFilters(filters: FilterEssence) {
        val minAgeThumbValue = filters.minAge / STEP_AGE
        val maxAgeThumbValue = filters.maxAge / STEP_AGE
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
        val valueStr = String.format(AppRes.FILTER_AGE, minThumbValue + DomainUtil.FILTER_MIN_AGE, maxThumbValue + DomainUtil.FILTER_MIN_AGE)
        tv_age.text = if (maxThumbValue >= seekbar_age.max) "$valueStr+" else valueStr
    }

    private fun displayDistance(thumbValue: Int) {
        val valueStr = String.format(AppRes.FILTER_DISTANCE_KM, thumbValue)
        tv_distance.text = if (thumbValue >= seekbar_distance.max) "$valueStr+" else valueStr
    }
}
