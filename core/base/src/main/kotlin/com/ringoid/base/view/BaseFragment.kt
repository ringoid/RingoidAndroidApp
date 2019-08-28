package com.ringoid.base.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.ringoid.base.IBaseRingoidApplication
import com.ringoid.base.navigation.AppScreen
import com.ringoid.base.navigation.NavigationRegistry
import com.ringoid.base.observe
import com.ringoid.base.viewModel
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.base.viewmodel.DaggerViewModelFactory
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.debug.ICloudDebug
import com.ringoid.domain.manager.IConnectionManager
import com.ringoid.domain.manager.IRuntimeConfig
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.scope.LocalScopeProvider
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import dagger.android.support.AndroidSupportInjection
import leakcanary.AppWatcher
import timber.log.Timber
import javax.inject.Inject

abstract class BaseFragment<T : BaseViewModel> : Fragment() {

    protected val localScopeProvider by lazy { LocalScopeProvider() }
    protected val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }
    protected val app by lazy { activity?.application as? IBaseRingoidApplication }

    protected lateinit var vm: T
    @Inject protected lateinit var vmFactory: DaggerViewModelFactory<T>
    @Inject protected lateinit var connectionManager: IConnectionManager
    @Inject protected lateinit var spm: ISharedPrefsManager
    @Inject protected lateinit var cloudDebug: ICloudDebug
    @Inject protected lateinit var config: IRuntimeConfig

    var isActivityCreated = false
        private set
    private var isOnFreshStart = true
    var isOnSaveInstanceState = false
        private set
    protected var isViewModelInitialized = false
        private set
    private var lastTabTransactionPayload: String? = null

    protected abstract fun getVmClass(): Class<T>  // cannot infer type of T in runtime due to Type Erasure

    @LayoutRes protected abstract fun getLayoutId(): Int

    protected open fun appScreen(): AppScreen = AppScreen.UNKNOWN

    fun setLastTabTransactionPayload(payload: String?) {
        lastTabTransactionPayload = payload
    }

    // --------------------------------------------------------------------------------------------
    protected open fun onViewStateChange(newState: ViewState) {
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.d("View State transition to: $newState")
        DebugLogUtil.lifecycle(this, "onViewStateChange: $newState")
        // override in subclasses
    }

    // ------------------------------------------
    /**
     * Called immediately before switching to another [Fragment], so that the current [Fragment] has
     * the last chance to detect it's being left.
     */
    open fun onBeforeTabSelect() {
        DebugLogUtil.lifecycle(this, "onBeforeTabSelect")
        if (userVisibleHint) userVisibleHint = false
        if (isViewModelInitialized) {
            vm.onBeforeTabSelect()
        }
        if (isAdded) {
            childFragmentManager.fragments.forEach {
                it.takeIf { it.isAdded }
                    ?.takeIf { it is BaseFragment<*> }
                    ?.let { it as BaseFragment<*> }
                    ?.onBeforeTabSelect()
            }
        }
        // override in subclasses
    }

    open fun onTabReselect(payload: String?) {
        DebugLogUtil.lifecycle(this, "onTabReselect")
        // override in subclasses
    }

    /**
     * Called upon switching on new [Fragment] by tab.
     */
    open fun onTabTransaction(payload: String?) {
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onTabTransaction, payload: $payload")
        DebugLogUtil.lifecycle(this, "onTabTransaction")
        lastTabTransactionPayload = payload
        if (!userVisibleHint) userVisibleHint = true
        // override in subclasses
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        val changed = userVisibleHint != isVisibleToUser
        super.setUserVisibleHint(isVisibleToUser)
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("setUserVisibleHint: $isVisibleToUser")
        DebugLogUtil.lifecycle(this, "setUserVisibleHint: $isVisibleToUser")
        if (isViewModelInitialized && changed) {
            vm.setUserVisibleHint(isVisibleToUser)
        }
    }

    protected fun doPostponedTabTransaction() {
        onTabTransaction(payload = lastTabTransactionPayload)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onAttach")
        DebugLogUtil.lifecycle(this, "onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onCreate")
        DebugLogUtil.lifecycle(this, "onCreate")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onCreateView")
        DebugLogUtil.lifecycle(this, "onCreateView")
        return inflater.inflate(getLayoutId(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onViewCreated")
        DebugLogUtil.lifecycle(this, "onViewCreated")
    }

    /**
     * Follow the current view hierarchy lifecycle rather than [Fragment]'s lifecycle in order to
     * unsubscribe observers of [LiveData], which has subscribed in [Fragment.onCreateView] safely
     * in [Fragment.onDestroyView]. This avoids double-subscription here in [Fragment.onActivityCreated]
     * for the [Fragment]s that detach and then reattach, because in that case lifespan of [Fragment]
     * is longer than lifespan of it's view hierarchy. So the observer is added to [Fragment.getViewLifecycleOwner].
     *
     * @see https://medium.com/@BladeCoder/architecture-components-pitfalls-part-1-9300dd969808
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onActivityCreated($savedInstanceState)")
        DebugLogUtil.lifecycle(this, "onActivityCreated")
        isActivityCreated = true
        vm = viewModel(klass = getVmClass(), factory = vmFactory) {
            setUserVisibleHintInternal(userVisibleHint)  // actualize visible hint on viewModel
            // tie observer to view's lifecycle rather than Fragment's one
            with(viewLifecycleOwner) {
                subscribeOnBusEvents()
                observe(viewState(), this@BaseFragment::onViewStateChange)
            }
        }
        isViewModelInitialized = true
        isOnFreshStart = savedInstanceState == null
        vm.onCreate(savedInstanceState)  // for Fragments
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onActivityResult(rc=$requestCode,result=$resultCode,data=$data)")
        DebugLogUtil.lifecycle(this, "onActivityResult")
        vm.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onStart")
        DebugLogUtil.lifecycle(this, "onStart")
        if (isOnFreshStart) {
            vm.onFreshStart()
            isOnFreshStart = false
        }
        NavigationRegistry.recordCurrentScreen(screen = appScreen())
        vm.onStart()
    }

    override fun onResume() {
        isOnSaveInstanceState = false
        super.onResume()
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onResume")
        DebugLogUtil.lifecycle(this, "onResume")
        vm.onResume()
    }

    override fun onPause() {
        super.onPause()
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onPause")
        DebugLogUtil.lifecycle(this, "onPause")
        vm.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        isOnSaveInstanceState = true
        super.onSaveInstanceState(outState)
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onSaveInstanceState")
        DebugLogUtil.lifecycle(this, "onSaveInstanceState")
        vm.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onStop")
        DebugLogUtil.lifecycle(this, "onStop")
        vm.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onDestroyView")
        DebugLogUtil.lifecycle(this, "onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.unsubscribeFromBusEvents()
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onDestroy")
        DebugLogUtil.lifecycle(this, "onDestroy")
        AppWatcher.objectWatcher.watch(this)
    }

    override fun onDetach() {
        super.onDetach()
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onDetach")
        DebugLogUtil.lifecycle(this, "onDetach")
    }
}
