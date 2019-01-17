package com.ringoid.origin.feed.view.lmm

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.feed.R
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.fragment_lmm.*

class LmmFragment : BaseFragment<LmmViewModel>(), ILmmFragment {

    companion object {
        fun newInstance(): LmmFragment = LmmFragment()
    }

    private lateinit var lmmPagesAdapter: LmmPagerAdapter

    override fun getVmClass(): Class<LmmViewModel> = LmmViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_lmm

    override fun getViewModel(): LmmViewModel = vm

    // --------------------------------------------------------------------------------------------
    override fun onRefresh() {
        vm.getFeed()
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lmmPagesAdapter = LmmPagerAdapter(childFragmentManager)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /**
         * Parent Fragment calls [Fragment.onActivityCreated] before any of child Fragment,
         * so it is safe to access [vm] of this parent Fragment from [Fragment.onActivityCreated]
         * of it's child Fragments.
         */
        vm.getFeed()
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vp_pages.apply {
            adapter = lmmPagesAdapter
            offscreenPageLimit = 2
        }
        btn_tab_likes.clicks().compose(clickDebounce()).subscribe { vp_pages.currentItem = 0 }
        btn_tab_matches.clicks().compose(clickDebounce()).subscribe { vp_pages.currentItem = 1 }
    }
}
