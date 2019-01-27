package com.ringoid.base.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.ringoid.base.observe
import com.ringoid.base.viewModel
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.base.viewmodel.DaggerViewModelFactory
import com.ringoid.domain.repository.ISharedPrefsManager
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import javax.inject.Inject

abstract class BaseFragment<T : BaseViewModel> : Fragment() {

    protected val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    protected lateinit var vm: T
    @Inject protected lateinit var vmFactory: DaggerViewModelFactory<T>
    @Inject protected lateinit var spm: ISharedPrefsManager

    var isActivityCreated = false
        private set
    var isOnSaveInstanceState = false
        private set

    protected abstract fun getVmClass(): Class<T>  // cannot infer type of T in runtime due to Type Erasure

    @LayoutRes protected abstract fun getLayoutId(): Int

    // --------------------------------------------------------------------------------------------
    protected open fun onViewStateChange(newState: ViewState) {
        Timber.v("View State transition to: ${newState.javaClass.simpleName}")
        // override in subclasses
    }

    // ------------------------------------------
    open fun onTabReselect() {
        // override in subclasses
    }

    open fun onTabTransaction(payload: String?) {
        Timber.v("onTabTransaction, payload: $payload")
        // override in subclasses
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(getLayoutId(), container, false)
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
        isActivityCreated = true
        vm = viewModel(klass = getVmClass(), factory = vmFactory) {
            // tie observer to view's lifecycle rather than Fragment's one
            viewLifecycleOwner.apply {
                subscribeOnBusEvents()
                observe(viewState, this@BaseFragment::onViewStateChange)
                observe(navigation) { it.call(this@BaseFragment) }
            }
        }
    }

    override fun onResume() {
        isOnSaveInstanceState = false
        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        isOnSaveInstanceState = true
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.unsubscribeFromBusEvents()
    }
}
