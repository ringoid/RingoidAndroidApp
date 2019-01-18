package com.ringoid.origin.feed.view.lmm

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.BaseFragment
import com.ringoid.base.view.ViewState
import com.ringoid.origin.feed.R
import com.ringoid.utility.changeTypeface
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
        vm.onRefresh()
    }

    override fun onTabReselect() {
        super.onTabReselect()
        vp_pages?.let { selectPage(it.currentItem xor 1) }
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
        vm.clearScreen(mode = ViewState.CLEAR.MODE_NEED_REFRESH)
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vp_pages.apply {
            adapter = lmmPagesAdapter
            offscreenPageLimit = 2
        }
        btn_tab_likes.clicks().compose(clickDebounce()).subscribe { selectPage(0) }
        btn_tab_matches.clicks().compose(clickDebounce()).subscribe { selectPage(1) }
        selectPage(position = 0)
    }

    private fun selectPage(position: Int) {
        when (position) {
            0 -> {
                btn_tab_likes?.changeTypeface(style = Typeface.BOLD, isSelected = true)
                btn_tab_matches?.changeTypeface()
            }
            1 -> {
                btn_tab_likes?.changeTypeface()
                btn_tab_matches?.changeTypeface(style = Typeface.BOLD, isSelected = true)
            }
        }
        vp_pages?.currentItem = position
    }
}
