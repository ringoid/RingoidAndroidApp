package com.ringoid.base.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.ringoid.base.IBaseRingoidApplication
import com.ringoid.base.observe
import com.ringoid.base.viewModel
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.base.viewmodel.DaggerViewModelFactory
import com.ringoid.domain.manager.IConnectionManager
import com.ringoid.domain.repository.ISharedPrefsManager
import com.ringoid.utility.manager.KeyboardManager
import com.ringoid.utility.manager.KeyboardStatus
import com.ringoid.utility.manager.LocaleManager
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import dagger.android.AndroidInjection
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

abstract class BaseActivity<T : BaseViewModel> : AppCompatActivity(), IBaseActivity {

    protected val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }
    protected val app by lazy { application as IBaseRingoidApplication }

    protected lateinit var vm: T
    private val keyboardManager by lazy { KeyboardManager(this) }
    @Inject protected lateinit var vmFactory: DaggerViewModelFactory<T>
    @Inject protected lateinit var connectionManager: IConnectionManager
    @Inject protected lateinit var spm: ISharedPrefsManager

    var isDestroying = false
        private set
    protected var currentResult: Int = Activity.RESULT_CANCELED
        private set

    // ------------------------------------------
    protected abstract fun getVmClass(): Class<T>  // cannot infer type of T in runtime due to Type Erasure

    @LayoutRes protected open fun getLayoutId(): Int? = null  // null means no layout

    // --------------------------------------------------------------------------------------------
    protected open fun onViewStateChange(newState: ViewState) {
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.d("View State transition to: $newState")
        // override in subclasses
    }

    // ------------------------------------------
    override fun keyboard(): Observable<KeyboardStatus> = keyboardManager.status()

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    protected open fun onBeforeCreate() {
        app.localeManager.setLocale(this)
        LocaleManager.resetActivityTitle(this)
        spm.getThemeResId().takeIf { it != 0 }?.let { setTheme(it) }
        // override in subclasses
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.d("onCreate($savedInstanceState)")
        isDestroying = false
        AndroidInjection.inject(this)
        onBeforeCreate()
        super.onCreate(savedInstanceState)
        getLayoutId()?.let { setContentView(it) }
        vm = viewModel(klass = getVmClass(), factory = vmFactory) {
            subscribeOnBusEvents()
            observe(viewState, ::onViewStateChange)
        }
        savedInstanceState ?: run { vm.onFreshCreate() }
    }

    override fun onDestroy() {
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.d("onDestroy")
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
