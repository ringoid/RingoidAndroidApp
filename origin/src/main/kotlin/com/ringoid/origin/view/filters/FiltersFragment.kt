package com.ringoid.origin.view.filters

class FiltersFragment : BaseFiltersFragment<FiltersViewModel>() {

    companion object {
        fun newInstance(): FiltersFragment = FiltersFragment()
    }

    override fun getVmClass(): Class<FiltersViewModel> = FiltersViewModel::class.java
}
