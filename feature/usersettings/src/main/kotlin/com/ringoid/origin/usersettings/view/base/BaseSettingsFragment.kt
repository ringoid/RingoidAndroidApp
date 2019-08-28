package com.ringoid.origin.usersettings.view.base

import android.os.Bundle
import com.ringoid.base.observeOneShot
import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.usersettings.OriginR_string
import com.ringoid.origin.view.dialog.BigEditTextDialog
import com.ringoid.origin.view.dialog.Dialogs

abstract class BaseSettingsFragment<VM : BaseSettingsViewModel> : BaseFragment<VM>(), BigEditTextDialog.IBigEditTextDialogDone {

    private var dialog: Dialogs.HashAlertDialog? = null

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with (viewLifecycleOwner) {
            observeOneShot(vm.suggestImprovementsOneShot()) { showSuggestImprovementsDoneDialog() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialog?.dialog?.dismiss()
        dialog = null
    }

    // --------------------------------------------------------------------------------------------
    override fun onCancel(text: String, tag: String?, fromBtn: Boolean) {
        spm.setBigEditText(text)
    }

    override fun onDone(text: String, tag: String?) {
        vm.suggestImprovements(text, tag)
    }

    protected fun openSuggestImprovementsDialog(tag: String) {
        BigEditTextDialog.newInstance(titleResId = OriginR_string.suggest_improvements_title,
            descriptionResId = OriginR_string.suggest_improvements_description,
            btnPositiveResId = OriginR_string.suggest_improvements_positive_button,
            input = spm.getBigEditText(), tag = tag)
            .show(childFragmentManager, BigEditTextDialog.TAG)
    }

    private fun showSuggestImprovementsDoneDialog() {
        dialog = Dialogs.showTextDialog(activity, titleResId = 0, descriptionResId = OriginR_string.suggest_improvements_dialog_done_title)
    }
}
