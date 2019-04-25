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
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.debug.ICloudDebug
import com.ringoid.domain.manager.IConnectionManager
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.utility.manager.KeyboardManager
import com.ringoid.utility.manager.KeyboardStatus
import com.ringoid.utility.manager.LocaleManager
import com.ringoid.utility.toast
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import dagger.android.AndroidInjection
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

abstract class BaseActivity<T : BaseViewModel> : AppCompatActivity(), IBaseActivity {

    companion object {
        private const val BUNDLE_KEY_CURRENT_RESULT_CODE = "bundle_key_current_result_code"
    }

    protected val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }
    protected val app by lazy { application as IBaseRingoidApplication }

    protected lateinit var vm: T
    private val keyboardManager by lazy { KeyboardManager(this) }
    @Inject protected lateinit var vmFactory: DaggerViewModelFactory<T>
    @Inject protected lateinit var connectionManager: IConnectionManager
    @Inject protected lateinit var spm: ISharedPrefsManager
    @Inject protected lateinit var cloudDebug: ICloudDebug

    private var isOnFreshStart = true
    protected var isStopped = false
        private set
    var isDestroying = false
        private set
    protected var isViewModelInitialized = false
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
        DebugLogUtil.lifecycle(this, "onViewStateChange: $newState")
        // override in subclasses
    }

    // ------------------------------------------
    override fun keyboard(): Observable<KeyboardStatus> = keyboardManager.status()

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    protected open fun onBeforeCreate() {
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.d("onBeforeCreate")
        DebugLogUtil.lifecycle(this, "onBeforeCreate")
        app.localeManager.setLocale(this)
        LocaleManager.resetActivityTitle(this)
        spm.getThemeResId().takeIf { it != 0 }?.let { setTheme(it) }
        // override in subclasses
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.d("onCreate($savedInstanceState)")
        DebugLogUtil.lifecycle(this, "onCreate")
        isDestroying = false
        AndroidInjection.inject(this)
        onBeforeCreate()
        super.onCreate(savedInstanceState)
        getLayoutId()?.let { setContentView(it) }
        vm = viewModel(klass = getVmClass(), factory = vmFactory) {
            subscribeOnBusEvents()
            observe(viewState, ::onViewStateChange)
        }
        savedInstanceState?.let { setResultExposed(it.getInt(BUNDLE_KEY_CURRENT_RESULT_CODE)) }
        isViewModelInitialized = true
        isOnFreshStart = savedInstanceState == null
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.d("onNewIntent(${intent.action} [${intent.dataString}]: ${intent.extras})")
        DebugLogUtil.lifecycle(this, "onNewIntent")
    }

    override fun onStart() {
        super.onStart()
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.d("onStart")
        DebugLogUtil.lifecycle(this, "onStart")
        isStopped = false
        if (isOnFreshStart) {
            vm.onFreshStart()
            isOnFreshStart = false
        }
        vm.onStart()
    }

    override fun onResume() {
        super.onResume()
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.d("onResume")
        DebugLogUtil.lifecycle(this, "onResume")
        vm.onResume()
    }

    override fun onPause() {
        super.onPause()
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.d("onPause")
        DebugLogUtil.lifecycle(this, "onPause")
        vm.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(BUNDLE_KEY_CURRENT_RESULT_CODE, currentResult)
        super.onSaveInstanceState(outState)
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.d("onSaveInstanceState")
        DebugLogUtil.lifecycle(this, "onSaveInstanceState")
    }

    override fun onStop() {
        super.onStop()
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.d("onStop")
        DebugLogUtil.lifecycle(this, "onStop")
        isStopped = true
        vm.onStop()
    }

    override fun onDestroy() {
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.d("onDestroy")
        DebugLogUtil.lifecycle(this, "onDestroy")
        isDestroying = true
        super.onDestroy()
        vm.unsubscribeFromBusEvents()
        vm.onDestroy()
    }

    // --------------------------------------------------------------------------------------------
    override fun recreate() {
        if (BuildConfig.IS_STAGING) {
            DebugLogUtil.w("Recreating screen: ${javaClass.simpleName}")
            toast("Recreate ${javaClass.simpleName}")
        }
        super.recreate()
    }

    fun setResultExposed(resultCode: Int, data: Intent? = null) {
        currentResult = resultCode
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("ResC=$resultCode, data=$data [${data?.extras}]")
        setResult(resultCode, data)
    }
}
