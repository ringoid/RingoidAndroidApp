package com.ringoid.origin.view.filters

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.R
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
    }
}
