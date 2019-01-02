package com.ringoid.base.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ringoid.base.observe
import com.ringoid.base.viewModel
import com.ringoid.base.viewmodel.BaseViewModel
import timber.log.Timber
import javax.inject.Inject

/**
 * @see https://medium.com/@BladeCoder/architecture-components-pitfalls-part-1-9300dd969808
 */
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(getLayoutId(), container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm = viewModel(klass = getVmClass(), factory = vmFactory) {
            observe(viewState) { onViewStateChange(it) }
        }
    }
}
