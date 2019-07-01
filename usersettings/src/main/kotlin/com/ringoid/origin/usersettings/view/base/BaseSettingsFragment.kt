package com.ringoid.origin.usersettings.view.base

import com.ringoid.base.view.BaseFragment
import com.ringoid.base.view.ViewState
import com.ringoid.origin.usersettings.OriginR_string
import com.ringoid.origin.view.dialog.BigEditTextDialog
import com.ringoid.origin.view.dialog.Dialogs

abstract class BaseSettingsFragment<VM : BaseSettingsViewModel> : BaseFragment<VM>(), BigEditTextDialog.IBigEditTextDialogDone {

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.DONE -> {
                when (newState.residual) {
                    is SUGGEST_IMPROVEMENTS -> showSuggestImprovementsDoneDialog()
                }
            }
        }
    }

    // --------------------------------------------------------------------------------------------
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
        Dialogs.showTextDialog(activity, titleResId = 0, descriptionResId = OriginR_string.suggest_improvements_dialog_done_title)
    }
}
