package com.ringoid.base.view

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ringoid.base.observe
import com.ringoid.base.viewModel
import com.ringoid.base.viewmodel.ActivityDelegateVmFactory
import com.ringoid.base.viewmodel.BaseViewModel
import timber.log.Timber

abstract class BaseActivity<T : BaseViewModel> : AppCompatActivity() {

    protected lateinit var vm: T
    protected val vmFactory: ViewModelProvider.Factory by  ActivityDelegateVmFactory()

    protected abstract fun getVmClass(): Class<T>  // cannot infer type of T in runtime due to Type Erasure

    @LayoutRes protected open fun getLayoutId(): Int? = null  // null means no layout

    // --------------------------------------------------------------------------------------------
    protected open fun onViewStateChange(newState: ViewState) {
        Timber.v("View State transition to: $newState")
        // override in subclasses
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getLayoutId()?.let { setContentView(it) }
        vm = viewModel(klass = getVmClass(), factory = vmFactory) {
            observe(viewState) { onViewStateChange(it) }
        }
    }
}
