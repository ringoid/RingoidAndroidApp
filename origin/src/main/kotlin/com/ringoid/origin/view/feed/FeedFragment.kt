package com.ringoid.origin.view.feed

import com.ringoid.base.view.BaseFragment

abstract class FeedFragment<T : FeedViewModel> : BaseFragment<T>() {

    override fun getLayoutId(): Int = 0
}
