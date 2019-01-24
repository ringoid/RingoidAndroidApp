package com.ringoid.base.view

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.utility.view.StateBottomSheetDialog
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider

abstract class BaseDialogFragment<T : BaseViewModel> : DialogFragment() {

    protected val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    protected var asBottomSheet: Boolean = false
        private set

    protected abstract fun getVmClass(): Class<T>  // cannot infer type of T in runtime due to Type Erasure

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resolveAnnotations()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        if (asBottomSheet) {
            StateBottomSheetDialog(context!!, theme)
                .apply { setState(BottomSheetBehavior.STATE_EXPANDED) }
        } else super.onCreateDialog(savedInstanceState)

    // ------------------------------------------
    private fun resolveAnnotations() {
        asBottomSheet =
            javaClass
                .takeIf { it.isAnnotationPresent(BottomSheet::class.java) }
                ?.let { it.getAnnotation(BottomSheet::class.java) }
                ?.value ?: false
    }
}
