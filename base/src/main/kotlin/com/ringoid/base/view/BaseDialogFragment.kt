package com.ringoid.base.view

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ringoid.utility.view.StateBottomSheetDialog

abstract class BaseDialogFragment : DialogFragment() {

    protected var asBottomSheet: Boolean = false
        private set

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
