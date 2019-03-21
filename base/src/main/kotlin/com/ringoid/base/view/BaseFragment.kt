package com.ringoid.base.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.ringoid.base.IBaseRingoidApplication
import com.ringoid.base.observe
import com.ringoid.base.viewModel
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.base.viewmodel.DaggerViewModelFactory
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.debug.ICloudDebug
import com.ringoid.domain.manager.IConnectionManager
import com.ringoid.domain.repository.ISharedPrefsManager
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import javax.inject.Inject

abstract class BaseFragment<T : BaseViewModel> : Fragment() {

    protected val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }
    protected val app by lazy { activity?.application as? IBaseRingoidApplication }

    protected lateinit var vm: T
    @Inject protected lateinit var vmFactory: DaggerViewModelFactory<T>
    @Inject protected lateinit var connectionManager: IConnectionManager
    @Inject protected lateinit var spm: ISharedPrefsManager
    @Inject protected lateinit var cloudDebug: ICloudDebug

    var isActivityCreated = false
        private set
    private var isOnFreshStart = true
    var isOnSaveInstanceState = false
        private set
    private var isViewModelInitialized = false

    protected abstract fun getVmClass(): Class<T>  // cannot infer type of T in runtime due to Type Erasure

    @LayoutRes protected abstract fun getLayoutId(): Int

    // --------------------------------------------------------------------------------------------
    protected open fun onViewStateChange(newState: ViewState) {
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("View State transition to: $newState")
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
        // override in subclasses
    }

    open fun onTabReselect() {
        DebugLogUtil.lifecycle(this, "onTabReselect")
        // override in subclasses
    }

    open fun onTabTransaction(payload: String?) {
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onTabTransaction, payload: $payload")
        DebugLogUtil.lifecycle(this, "onTabTransaction")
        // override in subclasses
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("setUserVisibleHint: $isVisibleToUser")
        DebugLogUtil.lifecycle(this, "setUserVisibleHint: $isVisibleToUser")
        if (isViewModelInitialized) {
            vm.setUserVisibleHint(isVisibleToUser)
        }
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
        Timber.v("onActivityCreated")
        DebugLogUtil.lifecycle(this, "onActivityCreated")
        isActivityCreated = true
        vm = viewModel(klass = getVmClass(), factory = vmFactory) {
            // tie observer to view's lifecycle rather than Fragment's one
            with(viewLifecycleOwner) {
                subscribeOnBusEvents()
                observe(viewState, this@BaseFragment::onViewStateChange)
            }
        }
        isViewModelInitialized = true
        isOnFreshStart = savedInstanceState == null
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
    }

    override fun onDetach() {
        super.onDetach()
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onDetach")
        DebugLogUtil.lifecycle(this, "onDetach")
    }
}
