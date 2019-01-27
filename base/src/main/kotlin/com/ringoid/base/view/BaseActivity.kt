package com.ringoid.base.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import com.ringoid.base.observe
import com.ringoid.base.viewModel
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.base.viewmodel.DaggerViewModelFactory
import com.ringoid.domain.DomainUtil.BAD_RESOURCE
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

abstract class BaseActivity<T : BaseViewModel> : AppCompatActivity() {

    protected val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    protected lateinit var vm: T
    @Inject protected lateinit var vmFactory: DaggerViewModelFactory<T>

    var isDestroying = false
        private set
    protected var currentResult: Int = Activity.RESULT_CANCELED
        private set
    private var pendingTheme: Int = BAD_RESOURCE

    protected abstract fun getVmClass(): Class<T>  // cannot infer type of T in runtime due to Type Erasure

    @LayoutRes protected open fun getLayoutId(): Int? = null  // null means no layout

    // --------------------------------------------------------------------------------------------
    protected fun applyTheme(@StyleRes themeResId: Int) {
        pendingTheme = themeResId
        recreate()
    }

    // ------------------------------------------
    protected open fun onViewStateChange(newState: ViewState) {
        Timber.v("View State transition to: $newState")
        // override in subclasses
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        isDestroying = false
        AndroidInjection.inject(this)
        pendingTheme = pendingTheme.takeIf { it != BAD_RESOURCE }
            ?.let { setTheme(pendingTheme) ; BAD_RESOURCE } ?: BAD_RESOURCE
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
    protected fun setResultExposed(resultCode: Int, data: Intent? = null) {
        currentResult = resultCode
        Timber.v("ResC=$resultCode, data=$data [${data?.extras}]")
        setResult(resultCode, data)
    }
}
