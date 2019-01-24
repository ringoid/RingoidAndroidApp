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
import com.ringoid.base.observe
import com.ringoid.base.viewModel
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.base.viewmodel.DaggerViewModelFactory
import com.ringoid.utility.view.StateBottomSheetDialog
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import javax.inject.Inject

abstract class BaseDialogFragment<T : BaseViewModel> : DialogFragment() {

    protected val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    protected lateinit var vm: T
    @Inject protected lateinit var vmFactory: DaggerViewModelFactory<T>

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resolveAnnotations()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        if (asBottomSheet) {
            StateBottomSheetDialog(context!!, theme)
                .apply { setState(BottomSheetBehavior.STATE_EXPANDED) }
        } else super.onCreateDialog(savedInstanceState)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return inflater.inflate(getLayoutId(), container, false)
    }

    /**
     * @see [BaseFragment.onActivityCreated] description.
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        isActivityCreated = true
        vm = viewModel(klass = getVmClass(), factory = vmFactory) {
            // tie observer to view's lifecycle rather than Fragment's one
            viewLifecycleOwner.apply {
                subscribeOnBusEvents()
                observe(viewState, this@BaseDialogFragment::onViewStateChange)
                observe(navigation) { it.call(this@BaseDialogFragment) }
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

    // ------------------------------------------
    private fun resolveAnnotations() {
        asBottomSheet =
            javaClass
                .takeIf { it.isAnnotationPresent(BottomSheet::class.java) }
                ?.let { it.getAnnotation(BottomSheet::class.java) }
                ?.value ?: false
    }
}
