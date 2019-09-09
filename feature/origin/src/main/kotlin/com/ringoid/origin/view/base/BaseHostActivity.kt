package com.ringoid.origin.view.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.ringoid.origin.R
import com.ringoid.origin.view.base.theme.ThemedSimpleBaseActivity

abstract class BaseHostActivity : ThemedSimpleBaseActivity() {

    override fun getLayoutId(): Int? = R.layout.activity_container

    protected abstract fun getFragmentTag(): String
    protected abstract fun instantiateFragment(): Fragment

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState ?: run {
            val fragment = instantiateFragment()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fl_container, fragment, getFragmentTag())
                .commitNow()
        }
    }
}
