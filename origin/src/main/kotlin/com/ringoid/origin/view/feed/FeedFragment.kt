package com.ringoid.origin.view.feed

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.R
import com.ringoid.origin.view.adapter.FeedAdapter
import kotlinx.android.synthetic.main.fragment_feed.*

abstract class FeedFragment<T : FeedViewModel> : BaseFragment<T>() {

    protected val feedAdapter: FeedAdapter = FeedAdapter()

    override fun getLayoutId(): Int = R.layout.fragment_feed

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.feed.observe(viewLifecycleOwner, Observer { feedAdapter.submit(it) })
        vm.getFeed()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipe_refresh_layout.apply {
//            setColorSchemeResources(*resources.getIntArray(R.array.swipe_refresh_colors))
            setOnRefreshListener { vm.getFeed() }
        }
        rv_items.apply {
            adapter = feedAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }
}
