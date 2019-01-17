package com.ringoid.origin.feed.view.lmm

import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.feed.R
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.fragment_lmm.*

class LmmFragment : BaseFragment<LmmViewModel>() {

    companion object {
        fun newInstance(): LmmFragment = LmmFragment()
    }

    private val lmmPagesAdapter = LmmPagerAdapter(childFragmentManager)

    override fun getVmClass(): Class<LmmViewModel> = LmmViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_lmm

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vp_pages.adapter = lmmPagesAdapter
        btn_tab_likes.clicks().compose(clickDebounce()).subscribe { vp_pages.currentItem = 0 }
        btn_tab_matches.clicks().compose(clickDebounce()).subscribe { vp_pages.currentItem = 1 }
    }
}
