package com.ringoid.origin.view.filters

import androidx.fragment.app.Fragment
import com.ringoid.origin.view.base.BaseHostActivity

class FiltersActivity : BaseHostActivity() {

    override fun getFragmentTag(): String = FiltersFragment.TAG
    override fun instantiateFragment(): Fragment = FiltersFragment.newInstance()
}
