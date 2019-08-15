package com.ringoid.base.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ringoid.base.manager.permission.PermissionManager
import com.ringoid.base.observe
import com.ringoid.base.viewModel
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.base.viewmodel.DaggerViewModelFactory
import com.ringoid.domain.debug.ICloudDebug
import com.ringoid.domain.manager.IConnectionManager
import com.ringoid.domain.manager.IRuntimeConfig
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.utility.view.StateBottomSheetDialog
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import dagger.android.support.AndroidSupportInjection
import leakcanary.AppWatcher
import timber.log.Timber
import javax.inject.Inject

abstract class BaseDialogFragment<T : BaseViewModel> : DialogFragment() {

    protected val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    protected lateinit var vm: T
    @Inject protected lateinit var vmFactory: DaggerViewModelFactory<T>
    @Inject protected lateinit var connectionManager: IConnectionManager
    @Inject protected lateinit var permissionManager: PermissionManager
    @Inject protected lateinit var spm: ISharedPrefsManager
    @Inject protected lateinit var cloudDebug: ICloudDebug
    @Inject protected lateinit var config: IRuntimeConfig

    protected var asBottomSheet: Boolean = false
        private set
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

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onCreate")
        resolveAnnotations()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onCreateDialog")
        return if (asBottomSheet) {
            StateBottomSheetDialog(context!!, theme)
                .apply { setState(BottomSheetBehavior.STATE_EXPANDED) }
        } else super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onCreateView")
        dialog?.apply {
            setCanceledOnTouchOutside(true)
            window?.requestFeature(Window.FEATURE_NO_TITLE)
        }
        return inflater.inflate(getLayoutId(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onViewCreated")
    }

    /**
     * @see [BaseFragment.onActivityCreated] description.
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onActivityCreated")
        isActivityCreated = true
        vm = viewModel(klass = getVmClass(), factory = vmFactory) {
            // tie observer to view's lifecycle rather than Fragment's one
            with(viewLifecycleOwner) {
                subscribeOnBusEvents()
                observe(viewState, this@BaseDialogFragment::onViewStateChange)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onStart")
        vm.onStart()
    }

    override fun onResume() {
        isOnSaveInstanceState = false
        super.onResume()
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onResume")
        vm.onResume()
    }

    override fun onPause() {
        super.onPause()
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onPause")
        vm.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        isOnSaveInstanceState = true
        super.onSaveInstanceState(outState)
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onSaveInstanceState")
        vm.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onStop")
        vm.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onDestroy")
        vm.unsubscribeFromBusEvents()
        AppWatcher.objectWatcher.watch(this)
    }

    override fun onDetach() {
        super.onDetach()
        Timber.tag("${javaClass.simpleName}[${hashCode()}]")
        Timber.v("onDetach")
    }

    // ------------------------------------------
    private fun resolveAnnotations() {
        asBottomSheet =
            javaClass
                .takeIf { it.isAnnotationPresent(BottomSheet::class.java) }
                ?.let { it.getAnnotation(BottomSheet::class.java) }
                ?.value ?: false
    }
}
