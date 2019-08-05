package com.ringoid.origin.view.filters

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.innovattic.rangeseekbar.RangeSeekBar
import com.ringoid.base.view.BaseFragment
import com.ringoid.domain.DomainUtil
import com.ringoid.origin.AppRes
import com.ringoid.origin.R
import kotlinx.android.synthetic.main.filters_content.*
import kotlinx.android.synthetic.main.fragment_filters.*

class FiltersFragment : BaseFragment<FiltersViewModel>() {

    companion object {
        internal const val TAG = "FiltersFragment_tag"

        fun newInstance(): FiltersFragment = FiltersFragment()
    }

    override fun getVmClass(): Class<FiltersViewModel> = FiltersViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_filters

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (toolbar as Toolbar).apply {
            setNavigationOnClickListener { activity?.onBackPressed() }
            setTitle(R.string.settings_filters)
        }

        with (seekbar_age) {
            minRange = 4
            max = DomainUtil.FILTER_MAX_AGE - DomainUtil.FILTER_MIN_AGE
            seekBarChangeListener = object : RangeSeekBar.SeekBarChangeListener {
                override fun onStartedSeeking() {}
                override fun onStoppedSeeking() {}
                override fun onValueChanged(minThumbValue: Int, maxThumbValue: Int) {
                    setMinMaxAgeRange(minThumbValue, maxThumbValue)
                }
            }
            setMinMaxAgeRange(0, seekbar_age.max)  // initialize counters
        }
    }

    // --------------------------------------------------------------------------------------------
    private fun setMinMaxAgeRange(minThumbValue: Int, maxThumbValue: Int) {
        val valueStr = String.format(AppRes.FILTER_AGE, minThumbValue + DomainUtil.FILTER_MIN_AGE, maxThumbValue + DomainUtil.FILTER_MIN_AGE)
        tv_age.text = if (maxThumbValue >= seekbar_age.max) "$valueStr+" else valueStr
    }
}
