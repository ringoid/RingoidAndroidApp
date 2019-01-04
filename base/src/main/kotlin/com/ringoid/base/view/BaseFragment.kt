package com.ringoid.base.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.ringoid.base.observe
import com.ringoid.base.viewModel
import com.ringoid.base.viewmodel.BaseViewModel
import timber.log.Timber
import javax.inject.Inject

abstract class BaseFragment<T : BaseViewModel> : Fragment() {

    protected lateinit var vm: T
    @Inject protected lateinit var vmFactory: ViewModelProvider.Factory

    protected abstract fun getVmClass(): Class<T>  // cannot infer type of T in runtime due to Type Erasure

    @LayoutRes protected abstract fun getLayoutId(): Int

    // --------------------------------------------------------------------------------------------
    protected open fun onViewStateChange(newState: ViewState) {
        Timber.v("View State transition to: $newState")
        // override in subclasses
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
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
        vm = viewModel(klass = getVmClass(), factory = vmFactory) {
            // tie observer to view's lifecycle rather than Fragment's one
            viewLifecycleOwner.observe(viewState) { onViewStateChange(it) }
        }
    }
}
