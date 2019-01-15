package com.ringoid.origin.feed

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.feed.adapter.FeedAdapter
import kotlinx.android.synthetic.main.fragment_feed.*

abstract class FeedFragment<T : FeedViewModel> : BaseFragment<T>() {

    protected val feedAdapter: FeedAdapter = FeedAdapter()  // TODO: pass common pool

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
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
//            setRecycledViewPool(viewPool)  // TODO: use pool for feeds
//            OverScrollDecoratorHelper.setUpOverScroll(this, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
        }
    }
}
