package com.ringoid.base.view

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.ringoid.base.viewmodel.ActivityDelegateVmFactory
import com.ringoid.base.viewmodel.BaseViewModel

abstract class BaseActivity<T : BaseViewModel> : AppCompatActivity() {

    protected lateinit var vm: T
    protected val vmFactory: ViewModelProvider.Factory by  ActivityDelegateVmFactory()

    protected abstract fun getVmClass(): Class<T>  // cannot infer type of T in runtime due to Type Erasure

    @LayoutRes protected open fun getLayoutId(): Int? = null  // null means no layout

    private fun obtainViewModel(): T =
        ViewModelProviders.of(this, vmFactory)[getVmClass()]

    // --------------------------------------------------------------------------------------------
    protected open fun onViewStateChange(newState: ViewState) {
        // override in subclasses
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getLayoutId()?.let { setContentView(it) }
        vm = obtainViewModel()
        vm.viewState.observe(this, Observer { onViewStateChange(it) })
    }
}
