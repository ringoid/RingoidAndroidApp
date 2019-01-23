package com.ringoid.base.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.ringoid.base.observe
import com.ringoid.base.viewModel
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.base.viewmodel.DaggerViewModelFactory
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

abstract class BaseActivity<T : BaseViewModel> : AppCompatActivity() {

    protected lateinit var vm: T
    @Inject protected lateinit var vmFactory: DaggerViewModelFactory<T>

    var isDestroying = false
        private set
    protected var currentResult: Int = Activity.RESULT_CANCELED
        private set

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
        isDestroying = false
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        getLayoutId()?.let { setContentView(it) }
        vm = viewModel(klass = getVmClass(), factory = vmFactory) {
            subscribeOnBusEvents()
            observe(viewState, ::onViewStateChange)
            observe(navigation) { it.call(this@BaseActivity) }
        }
    }

    override fun onDestroy() {
        isDestroying = true
        super.onDestroy()
        vm.unsubscribeFromBusEvents()
    }

    // --------------------------------------------------------------------------------------------
    protected fun setResultExposed(resultCode: Int, data: Intent?) {
        currentResult = resultCode
        setResult(resultCode, data)
    }
}
