package com.ringoid.origin.view.main

import com.ringoid.base.view.BaseActivity
import com.ringoid.origin.R

class MainActivity : BaseActivity<MainViewModel>() {

    override fun getVmClass() = MainViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_main
}
