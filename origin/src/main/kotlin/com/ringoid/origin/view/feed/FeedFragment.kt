package com.ringoid.origin.view.feed

import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.R

abstract class FeedFragment<T : FeedViewModel> : BaseFragment<T>() {

    override fun getLayoutId(): Int = R.layout.fragment_feed
}
