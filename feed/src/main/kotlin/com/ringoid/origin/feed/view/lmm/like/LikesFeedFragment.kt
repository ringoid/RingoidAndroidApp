package com.ringoid.origin.feed.view.lmm.like

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.ringoid.base.observe
import com.ringoid.origin.feed.adapter.lmm.like.LikeFeedAdapter
import com.ringoid.origin.feed.view.lmm.ILmmFragment
import com.ringoid.utility.communicator
import kotlinx.android.synthetic.main.fragment_feed.*

class LikesFeedFragment : BaseLikesFeedFragment<LikesFeedViewModel>() {

    companion object {
        fun newInstance(): LikesFeedFragment = LikesFeedFragment()
    }

    override fun getVmClass(): Class<LikesFeedViewModel> = LikesFeedViewModel::class.java

    override fun instantiateFeedAdapter(): LikeFeedAdapter = LikeFeedAdapter()

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        communicator(ILmmFragment::class.java)
            ?.getViewModel()
            ?.apply {
                viewLifecycleOwner.observe(viewState, this@LikesFeedFragment::onViewStateChange)
                feedLikes.observe(viewLifecycleOwner, Observer { feedAdapter.submitList(it.map { it.profile() }) })
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipe_refresh_layout.apply {
            setOnRefreshListener { communicator(ILmmFragment::class.java)?.onRefresh() }
        }
    }
}
