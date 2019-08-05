package com.ringoid.origin.view.filters

import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.R

class FiltersFragment : BaseFragment<FiltersViewModel>() {

    companion object {
        internal const val TAG = "FiltersFragment_tag"

        fun newInstance(): FiltersFragment = FiltersFragment()
    }

    override fun getVmClass(): Class<FiltersViewModel> = FiltersViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_filters

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
}
