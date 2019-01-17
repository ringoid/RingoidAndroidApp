package com.ringoid.origin.feed.view.lmm.like

import android.os.Bundle
import androidx.lifecycle.Observer
import com.ringoid.origin.feed.view.lmm.ILmmFragment
import com.ringoid.utility.communicator

class LikesFeedFragment : BaseLikesFeedFragment<LikesFeedViewModel>() {

    companion object {
        fun newInstance(): LikesFeedFragment = LikesFeedFragment()
    }

    override fun getVmClass(): Class<LikesFeedViewModel> = LikesFeedViewModel::class.java

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        communicator(ILmmFragment::class.java)
            ?.getViewModel()
            ?.feedLikes?.observe(viewLifecycleOwner,
                Observer { feedAdapter.submitList(it.map { it.profile() }) })
    }
}
