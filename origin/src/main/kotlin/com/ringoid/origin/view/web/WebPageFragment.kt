package com.ringoid.origin.view.web

import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.R

class WebPageFragment : BaseFragment<WebPageViewModel>() {

    override fun getVmClass(): Class<WebPageViewModel> = WebPageViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_web
}
