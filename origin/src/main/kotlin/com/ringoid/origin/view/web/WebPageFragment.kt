package com.ringoid.origin.view.web

import com.ringoid.base.view.BaseFragment

class WebPageFragment : BaseFragment<WebPageViewModel>() {

    override fun getVmClass(): Class<WebPageViewModel> = WebPageViewModel::class.java

    override fun getLayoutId(): Int = 0
}
