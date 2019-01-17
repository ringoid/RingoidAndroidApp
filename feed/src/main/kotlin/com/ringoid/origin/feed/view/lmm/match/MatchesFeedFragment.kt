package com.ringoid.origin.feed.view.lmm.match

import android.os.Bundle
import androidx.lifecycle.Observer
import com.ringoid.origin.feed.view.lmm.ILmmFragment
import com.ringoid.origin.feed.view.lmm.like.BaseLikesFeedFragment
import com.ringoid.utility.communicator

class MatchesFeedFragment : BaseLikesFeedFragment<MatchesFeedViewModel>() {

    companion object {
        fun newInstance(): MatchesFeedFragment = MatchesFeedFragment()
    }

    override fun getVmClass(): Class<MatchesFeedViewModel> = MatchesFeedViewModel::class.java

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        communicator(ILmmFragment::class.java)
            ?.getViewModel()
            ?.feedMatches?.observe(viewLifecycleOwner,
                Observer { feedAdapter.submitList(it.map { it.profile() }) })
    }
}
